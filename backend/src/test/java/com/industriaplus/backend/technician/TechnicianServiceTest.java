package com.industriaplus.backend.technician;

import static com.industriaplus.backend.UnitFixtures.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.auth.Role;
import com.industriaplus.backend.auth.UserRepository;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestRepository;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.Urgency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

/** Técnicos como usuários: carga, vínculo com gestor e exclusão, isolados com Mockito. */
class TechnicianServiceTest {
    private UserRepository users;
    private MaintenanceRequestRepository requests;
    private AccessService access;
    private TechnicianService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        requests = mock(MaintenanceRequestRepository.class);
        access = mock(AccessService.class);
        service = new TechnicianService(users, requests, access);
    }

    @Test
    void listShouldReturnOnlyTechniciansWithWorkloadAndManager() {
        AppUser manager = manager(2);
        AppUser ana = technician(7, "Ana");
        ana.assignManager(manager);
        AppUser bruno = technician(8, "Bruno");
        when(users.findByRoleOrderByNameAsc(Role.TECHNICIAN)).thenReturn(List.of(ana, bruno));
        when(requests.findTechnicianWorkloads(Urgency.HIGH, MaintenanceRequestStatus.CLOSED))
                .thenReturn(List.of(new TechnicianWorkload(7L, 2L)));

        List<TechnicianResponse> team = service.list();

        assertEquals(2, team.size());
        assertEquals(2L, team.get(0).highUrgencyOpenRequests());
        assertEquals(2L, team.get(0).managerId());
        assertEquals("Gestor 2", team.get(0).managerName());
        assertEquals(0L, team.get(1).highUrgencyOpenRequests());
        assertNull(team.get(1).managerId());
    }

    @Test
    void managersShouldListOnlyManagerProfile() {
        when(users.findByRoleOrderByNameAsc(Role.MANAGER)).thenReturn(List.of(manager(2)));

        List<ManagerOption> managers = service.managers();

        assertEquals(List.of(new ManagerOption(2L, "Gestor 2", "gestor.2@test.local")), managers);
    }

    @Test
    void updateManagerShouldLinkTechnicianToManager() {
        AppUser ana = technician(7, "Ana");
        when(users.findByIdAndRoleForUpdate(7L, Role.TECHNICIAN)).thenReturn(Optional.of(ana));
        when(users.findByIdAndRole(2L, Role.MANAGER)).thenReturn(Optional.of(manager(2)));
        when(requests.countActiveByTechnicianAndUrgency(
                        7L, Urgency.HIGH, MaintenanceRequestStatus.CLOSED))
                .thenReturn(1L);

        TechnicianResponse response = service.updateManager(7L, new TechnicianManagerRequest(2L));

        assertEquals(2L, response.managerId());
        assertEquals(1L, response.highUrgencyOpenRequests());
        verify(access).requireAdmin();
        verify(users).saveAndFlush(ana);
    }

    @Test
    void updateManagerShouldRejectUserWithoutManagerProfile() {
        AppUser ana = technician(7, "Ana");
        when(users.findByIdAndRoleForUpdate(7L, Role.TECHNICIAN)).thenReturn(Optional.of(ana));
        when(users.findByIdAndRole(8L, Role.MANAGER)).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateManager(7L, new TechnicianManagerRequest(8L)));

        assertEquals("INVALID_MANAGER_LINK", error.getCode());
        assertNull(ana.getManager());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void updateManagerShouldBeExclusiveToAdministrators() {
        doThrow(new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "negado"))
                .when(access)
                .requireAdmin();

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.updateManager(7L, new TechnicianManagerRequest(2L)));

        assertEquals("ACCESS_DENIED", error.getCode());
        verifyNoMoreInteractions(users);
    }

    @Test
    void getRequiredForUpdateShouldOnlyAcceptTechnicianProfile() {
        when(users.findByIdAndRoleForUpdate(2L, Role.TECHNICIAN)).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(BusinessException.class, () -> service.getRequiredForUpdate(2L));

        assertEquals("TECHNICIAN_NOT_FOUND", error.getCode());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void deleteShouldRejectTechnicianWithRequests() {
        AppUser ana = technician(7, "Ana");
        when(users.findByIdAndRole(7L, Role.TECHNICIAN)).thenReturn(Optional.of(ana));
        when(requests.existsByTechnicianId(7L)).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class, () -> service.delete(7L));

        assertEquals("TECHNICIAN_IN_USE", error.getCode());
        verify(users, never()).delete(any());
    }

    @Test
    void deleteShouldRemoveTechnicianWithoutRequests() {
        AppUser ana = technician(7, "Ana");
        when(users.findByIdAndRole(7L, Role.TECHNICIAN)).thenReturn(Optional.of(ana));

        service.delete(7L);

        verify(access).requireAdmin();
        verify(users).delete(ana);
        verify(users).flush();
    }
}
