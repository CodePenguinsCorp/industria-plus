package com.industriaplus.backend.auth;

import static com.industriaplus.backend.UnitFixtures.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequest;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.Urgency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Permissões por perfil aplicadas no backend, isoladas com Mockito. */
class AccessServiceTest {
    private UserRepository users;
    private AccessService access;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        access = new AccessService(users);
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void technicianShouldAttendOnlyOwnRequests() {
        AppUser ana = loggedIn(technician(7, "Ana"));
        AppUser bruno = technician(8, "Bruno");

        assertDoesNotThrow(() -> access.requireAttendance(request(ana)));
        assertForbidden(() -> access.requireAttendance(request(bruno)));
        assertForbidden(() -> access.requireAttendance(request(null)));
        assertEquals(ana.getEmail(), access.actor());
    }

    @Test
    void managersAndAdministratorsShouldAttendAnyRequest() {
        AppUser bruno = technician(8, "Bruno");

        loggedIn(manager(2));
        assertDoesNotThrow(() -> access.requireAttendance(request(bruno)));
        assertDoesNotThrow(() -> access.requireAttendance(request(null)));
        assertDoesNotThrow(access::requireManager);

        loggedIn(user(1, "Admin", Role.ADMIN));
        assertDoesNotThrow(() -> access.requireAttendance(request(bruno)));
        assertDoesNotThrow(access::requireAdmin);
    }

    @Test
    void technicianShouldNotManage() {
        loggedIn(technician(7, "Ana"));

        assertForbidden(access::requireManager);
        assertForbidden(access::requireAdmin);
    }

    @Test
    void managerShouldNotPerformAdministratorActions() {
        loggedIn(manager(2));

        assertForbidden(access::requireAdmin);
    }

    @Test
    void shouldRejectMissingOrUnknownSession() {
        BusinessException missing = assertThrows(BusinessException.class, access::current);
        assertEquals(HttpStatus.UNAUTHORIZED, missing.getStatus());

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new TestingAuthenticationToken("fantasma@test.local", null, "ROLE_ADMIN"));
        when(users.findByEmailIgnoreCase("fantasma@test.local")).thenReturn(Optional.empty());
        BusinessException unknown = assertThrows(BusinessException.class, access::current);
        assertEquals(HttpStatus.UNAUTHORIZED, unknown.getStatus());
    }

    private AppUser loggedIn(AppUser user) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new TestingAuthenticationToken(
                                user.getEmail(), null, "ROLE_" + user.getRole().name()));
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        return user;
    }

    private static MaintenanceRequest request(AppUser technician) {
        return com.industriaplus.backend.UnitFixtures.request(
                1, Urgency.LOW, MaintenanceRequestStatus.OPEN, technician);
    }

    private static void assertForbidden(org.junit.jupiter.api.function.Executable action) {
        BusinessException error = assertThrows(BusinessException.class, action);
        assertEquals("ACCESS_DENIED", error.getCode());
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }
}
