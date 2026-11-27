package com.industriaplus.backend.history;

import static com.industriaplus.backend.UnitFixtures.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequest;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestRepository;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.Urgency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

/** Registro de intervenções e peças no histórico, isolado com Mockito. */
class HistoryServiceTest {
    private HistoryRepository history;
    private PartRepository parts;
    private MaintenanceRequestRepository requests;
    private AccessService access;
    private HistoryService service;

    @BeforeEach
    void setUp() {
        history = mock(HistoryRepository.class);
        parts = mock(PartRepository.class);
        requests = mock(MaintenanceRequestRepository.class);
        access = mock(AccessService.class);
        service = new HistoryService(history, parts, requests, access);
        when(access.actor()).thenReturn("ana@test.local");
        when(history.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void interventionShouldRecordServiceAuthorAndTrimmedParts() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request =
                request(1, Urgency.HIGH, MaintenanceRequestStatus.IN_PROGRESS, ana);
        when(requests.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        HistoryResponse response =
                service.intervene(
                        1L,
                        new InterventionRequest(
                                "  Rolamento substituído.  ",
                                List.of(new InterventionRequest.PartInput("  Rolamento ", 2))));

        assertEquals("INTERVENTION", response.eventType());
        assertEquals("ana@test.local", response.actor());
        assertEquals("Rolamento substituído.", response.description());
        assertEquals(7L, response.currentTechnicianId());
        assertEquals("Ana", response.currentTechnicianName());
        verify(access).requireAttendance(request);

        ArgumentCaptor<ReplacedPart> part = ArgumentCaptor.forClass(ReplacedPart.class);
        verify(parts).save(part.capture());
        assertEquals("Rolamento", part.getValue().getName());
        assertEquals(2, part.getValue().getQuantity());
    }

    @Test
    void interventionShouldBeBlockedOnClosedRequest() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.CLOSED, null);
        when(requests.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.intervene(1L, new InterventionRequest("Tentativa", List.of())));

        assertEquals("MAINTENANCE_REQUEST_CLOSED", error.getCode());
        verify(history, never()).saveAndFlush(any());
    }

    @Test
    void interventionShouldRequireDescriptionAfterTrimming() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, null);
        when(requests.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.intervene(1L, new InterventionRequest("  ab   ", null)));

        assertEquals("INVALID_DESCRIPTION", error.getCode());
        verify(history, never()).saveAndFlush(any());
    }

    @Test
    void interventionShouldRespectTechnicianOwnership() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, null);
        when(requests.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        doThrow(new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "negado"))
                .when(access)
                .requireAttendance(request);

        assertThrows(
                BusinessException.class,
                () -> service.intervene(1L, new InterventionRequest("Serviço", List.of())));

        verify(history, never()).saveAndFlush(any());
    }

    @Test
    void requestHistoryShouldReportMissingRequest() {
        when(requests.existsById(42L)).thenReturn(false);

        BusinessException error =
                assertThrows(BusinessException.class, () -> service.requestHistory(42L));

        assertEquals("MAINTENANCE_REQUEST_NOT_FOUND", error.getCode());
    }

    @Test
    void recordShouldCaptureActorAndPreviousTechnician() {
        AppUser bruno = technician(8, "Bruno");
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, bruno);

        service.record(request, "ASSIGNMENT", "Responsável alterado.", "OPEN", 7L, "Ana");

        ArgumentCaptor<HistoryEntry> entry = ArgumentCaptor.forClass(HistoryEntry.class);
        verify(history).save(entry.capture());
        assertEquals("ana@test.local", entry.getValue().getActor());
        assertEquals(7L, entry.getValue().getPreviousTechnicianId());
        assertEquals("Ana", entry.getValue().getPreviousTechnicianName());
        assertEquals(8L, entry.getValue().getCurrentTechnicianId());
        assertEquals("Bruno", entry.getValue().getCurrentTechnicianName());
    }
}
