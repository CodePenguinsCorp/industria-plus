package com.industriaplus.backend.maintenancerequest;

import static com.industriaplus.backend.UnitFixtures.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.equipment.Equipment;
import com.industriaplus.backend.equipment.EquipmentService;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.history.HistoryService;
import com.industriaplus.backend.sector.Sector;
import com.industriaplus.backend.sector.SectorService;
import com.industriaplus.backend.technician.TechnicianService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

/** Regras de negócio dos chamados (RN-001 a RN-005), isoladas com Mockito. */
class MaintenanceRequestServiceTest {

    private AccessService access;
    private HistoryService history;
    private MaintenanceRequestRepository repository;
    private EquipmentService equipments;
    private SectorService sectors;
    private TechnicianService technicians;
    private MaintenanceRequestService service;

    @BeforeEach
    void setUp() {
        access = mock(AccessService.class);
        history = mock(HistoryService.class);
        repository = mock(MaintenanceRequestRepository.class);
        equipments = mock(EquipmentService.class);
        sectors = mock(SectorService.class);
        technicians = mock(TechnicianService.class);
        service =
                new MaintenanceRequestService(
                        access, history, repository, equipments, sectors, technicians);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createShouldOpenUnassignedRequestAndRecordHistory() {
        Sector sector = sector(1);
        Equipment equipment = equipment(2, sector);
        when(equipments.getRequired(2L)).thenReturn(equipment);
        when(sectors.getRequired(1L)).thenReturn(sector);

        MaintenanceRequestResponse response =
                service.create(
                        new MaintenanceRequestCreateRequest(
                                "  Prensa sem pressão ",
                                " A prensa perdeu pressão no turno. ",
                                2L,
                                1L,
                                MaintenanceType.CORRECTIVE,
                                Urgency.HIGH));

        assertEquals(MaintenanceRequestStatus.OPEN, response.status());
        assertNull(response.technicianId());
        assertEquals("Prensa sem pressão", response.title());
        verify(access).requireManager();
        verify(history)
                .record(any(MaintenanceRequest.class), eq("CREATED"), anyString(), isNull(), isNull(), isNull());
    }

    @Test
    void createShouldRejectEquipmentFromAnotherSector() {
        Sector equipmentSector = sector(1);
        Sector otherSector = sector(9);
        when(equipments.getRequired(2L)).thenReturn(equipment(2, equipmentSector));
        when(sectors.getRequired(9L)).thenReturn(otherSector);

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new MaintenanceRequestCreateRequest(
                                                "Falha",
                                                "Descrição suficiente",
                                                2L,
                                                9L,
                                                MaintenanceType.PREVENTIVE,
                                                Urgency.LOW)));

        assertEquals("EQUIPMENT_SECTOR_MISMATCH", error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createShouldStopWhenUserIsNotManager() {
        doThrow(new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "negado"))
                .when(access)
                .requireManager();

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new MaintenanceRequestCreateRequest(
                                                "Falha",
                                                "Descrição suficiente",
                                                2L,
                                                1L,
                                                MaintenanceType.PREVENTIVE,
                                                Urgency.LOW)));

        assertEquals("ACCESS_DENIED", error.getCode());
        verifyNoMoreInteractions(equipments, sectors, repository);
    }

    @Test
    void assignShouldAcceptSecondHighUrgencyRequest() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request = request(1, Urgency.HIGH, MaintenanceRequestStatus.OPEN, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(technicians.getRequiredForUpdate(7L)).thenReturn(ana);
        when(repository.countActiveByTechnicianAndUrgency(
                        7L, Urgency.HIGH, MaintenanceRequestStatus.CLOSED))
                .thenReturn(1L);

        MaintenanceRequestResponse response =
                service.assign(1L, new MaintenanceRequestAssignmentRequest(7L));

        assertEquals(7L, response.technicianId());
        assertEquals("Ana", response.technicianName());
        verify(history).record(request, "ASSIGNMENT", "Responsável alterado.", "OPEN", null, null);
    }

    @Test
    void assignShouldRejectThirdHighUrgencyRequest() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request = request(1, Urgency.HIGH, MaintenanceRequestStatus.OPEN, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(technicians.getRequiredForUpdate(7L)).thenReturn(ana);
        when(repository.countActiveByTechnicianAndUrgency(
                        7L, Urgency.HIGH, MaintenanceRequestStatus.CLOSED))
                .thenReturn(2L);

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.assign(1L, new MaintenanceRequestAssignmentRequest(7L)));

        assertEquals("HIGH_URGENCY_LIMIT", error.getCode());
        assertNull(request.getTechnician());
        verify(repository, never()).saveAndFlush(any());
        verifyNoMoreInteractions(history);
    }

    @Test
    void assignShouldIgnoreLimitForNonHighUrgency() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request =
                request(1, Urgency.MEDIUM, MaintenanceRequestStatus.OPEN, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(technicians.getRequiredForUpdate(7L)).thenReturn(ana);

        service.assign(1L, new MaintenanceRequestAssignmentRequest(7L));

        assertSame(ana, request.getTechnician());
        verify(repository, never()).countActiveByTechnicianAndUrgency(anyLong(), any(), any());
    }

    @Test
    void assignShouldBeIdempotentForCurrentTechnician() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request = request(1, Urgency.HIGH, MaintenanceRequestStatus.OPEN, ana);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(technicians.getRequiredForUpdate(7L)).thenReturn(ana);

        MaintenanceRequestResponse response =
                service.assign(1L, new MaintenanceRequestAssignmentRequest(7L));

        assertEquals(7L, response.technicianId());
        verify(repository, never()).countActiveByTechnicianAndUrgency(anyLong(), any(), any());
        verify(repository, never()).saveAndFlush(any());
        verifyNoMoreInteractions(history);
    }

    @Test
    void reassignShouldRecordPreviousTechnician() {
        AppUser ana = technician(7, "Ana");
        AppUser bruno = technician(8, "Bruno");
        MaintenanceRequest request =
                request(1, Urgency.LOW, MaintenanceRequestStatus.IN_PROGRESS, ana);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(technicians.getRequiredForUpdate(8L)).thenReturn(bruno);

        service.assign(1L, new MaintenanceRequestAssignmentRequest(8L));

        assertSame(bruno, request.getTechnician());
        verify(history)
                .record(request, "ASSIGNMENT", "Responsável alterado.", "IN_PROGRESS", 7L, "Ana");
    }

    @Test
    void assignShouldRejectClosedRequest() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.CLOSED, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.assign(1L, new MaintenanceRequestAssignmentRequest(7L)));

        assertEquals("MAINTENANCE_REQUEST_CLOSED", error.getCode());
        verifyNoMoreInteractions(technicians);
    }

    @Test
    void assignShouldReportMissingRequest() {
        when(repository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.assign(99L, new MaintenanceRequestAssignmentRequest(7L)));

        assertEquals("MAINTENANCE_REQUEST_NOT_FOUND", error.getCode());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void updateStatusShouldCheckAttendanceAndRecordHistory() {
        AppUser ana = technician(7, "Ana");
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, ana);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        MaintenanceRequestResponse response =
                service.updateStatus(
                        1L, new MaintenanceRequestStatusRequest(MaintenanceRequestStatus.IN_PROGRESS));

        assertEquals(MaintenanceRequestStatus.IN_PROGRESS, response.status());
        verify(access).requireAttendance(request);
        verify(history).record(request, "STATUS", "Status alterado.", "OPEN", 7L, "Ana");
    }

    @Test
    void updateStatusShouldRejectInvalidTransition() {
        MaintenanceRequest request =
                request(1, Urgency.LOW, MaintenanceRequestStatus.IN_PROGRESS, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.updateStatus(
                                        1L,
                                        new MaintenanceRequestStatusRequest(
                                                MaintenanceRequestStatus.OPEN)));

        assertEquals("INVALID_STATUS_TRANSITION", error.getCode());
        assertEquals(MaintenanceRequestStatus.IN_PROGRESS, request.getStatus());
    }

    @Test
    void updateStatusShouldBlockClosedRequest() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.CLOSED, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.updateStatus(
                                        1L,
                                        new MaintenanceRequestStatusRequest(
                                                MaintenanceRequestStatus.CLOSED)));

        assertEquals("MAINTENANCE_REQUEST_CLOSED", error.getCode());
        verifyNoMoreInteractions(history);
    }

    @Test
    void updateStatusShouldStopWhenTechnicianDoesNotOwnRequest() {
        MaintenanceRequest request = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, null);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        doThrow(new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "negado"))
                .when(access)
                .requireAttendance(request);

        assertThrows(
                BusinessException.class,
                () ->
                        service.updateStatus(
                                1L,
                                new MaintenanceRequestStatusRequest(
                                        MaintenanceRequestStatus.CLOSED)));

        assertEquals(MaintenanceRequestStatus.OPEN, request.getStatus());
    }

    @Test
    void listShouldOrderByUrgencyThenNewestThenHighestId() {
        MaintenanceRequest low = request(1, Urgency.LOW, MaintenanceRequestStatus.OPEN, null);
        MaintenanceRequest oldHigh = request(2, Urgency.HIGH, MaintenanceRequestStatus.OPEN, null);
        MaintenanceRequest medium = request(3, Urgency.MEDIUM, MaintenanceRequestStatus.OPEN, null);
        MaintenanceRequest newHigh = request(4, Urgency.HIGH, MaintenanceRequestStatus.OPEN, null);
        MaintenanceRequest tieHigh = request(5, Urgency.HIGH, MaintenanceRequestStatus.OPEN, null);
        set(tieHigh, "createdAt", newHigh.getCreatedAt());
        when(repository.findFiltered(null, null))
                .thenReturn(List.of(low, oldHigh, medium, newHigh, tieHigh));

        List<Long> ids =
                service.list(null, null).stream().map(MaintenanceRequestResponse::id).toList();

        assertEquals(List.of(5L, 4L, 2L, 3L, 1L), ids);
    }
}
