package com.industriaplus.backend.auth;

import static com.industriaplus.backend.UnitFixtures.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.industriaplus.backend.error.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/** Cadastro de usuários, vínculo técnico-gestor e senha provisória, isolados com Mockito. */
class UserServiceTest {
    private static final String PASSWORD = "SenhaProvisoria123!";

    private UserRepository users;
    private PasswordEncoder passwords;
    private UserService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        passwords = mock(PasswordEncoder.class);
        service = new UserService(users, passwords);
        when(passwords.encode(anyString())).thenAnswer(call -> "bcrypt:" + call.getArgument(0));
        when(users.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void shouldCreateTechnicianLinkedToManagerWithTemporaryPassword() {
        AppUser manager = manager(2);
        when(users.findByIdAndRole(2L, Role.MANAGER)).thenReturn(Optional.of(manager));

        UserResponse response =
                service.create(
                        new UserCreateRequest(
                                "  Ana Martins ",
                                "  ANA@Industria.COM ",
                                PASSWORD,
                                Role.TECHNICIAN,
                                2L,
                                "  Hidráulica  "));

        AppUser saved = savedUser();
        assertEquals("Ana Martins", saved.getName());
        assertEquals("ana@industria.com", saved.getEmail());
        assertEquals("bcrypt:" + PASSWORD, saved.getPasswordHash());
        assertEquals("Hidráulica", saved.getSpecialty());
        assertSame(manager, saved.getManager());
        assertTrue(saved.isMustChangePassword());
        assertTrue(response.mustChangePassword());
        assertEquals(2L, response.managerId());
        assertEquals("Gestor 2", response.managerName());
        verify(users).existsByEmailIgnoreCase("ana@industria.com");
    }

    @Test
    void shouldRequireManagerForTechnician() {
        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new UserCreateRequest(
                                                "Ana",
                                                "ana@test.local",
                                                PASSWORD,
                                                Role.TECHNICIAN,
                                                null,
                                                null)));

        assertEquals("INVALID_MANAGER_LINK", error.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectManagerLinkForNonTechnicianProfiles() {
        for (Role role : new Role[] {Role.ADMIN, Role.MANAGER}) {
            BusinessException error =
                    assertThrows(
                            BusinessException.class,
                            () ->
                                    service.create(
                                            new UserCreateRequest(
                                                    "Pessoa",
                                                    "pessoa@test.local",
                                                    PASSWORD,
                                                    role,
                                                    2L,
                                                    null)));
            assertEquals("INVALID_MANAGER_LINK", error.getCode());
        }
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectLinkToUserThatIsNotManager() {
        when(users.findByIdAndRole(5L, Role.MANAGER)).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new UserCreateRequest(
                                                "Ana",
                                                "ana@test.local",
                                                PASSWORD,
                                                Role.TECHNICIAN,
                                                5L,
                                                null)));

        assertEquals("INVALID_MANAGER_LINK", error.getCode());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCaseAndSpaces() {
        when(users.existsByEmailIgnoreCase("gestor@test.local")).thenReturn(true);

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new UserCreateRequest(
                                                "Gestor",
                                                " GESTOR@test.local ",
                                                PASSWORD,
                                                Role.MANAGER,
                                                null,
                                                null)));

        assertEquals("DUPLICATE_USER_EMAIL", error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void shouldRejectPasswordLongerThan72Bytes() {
        String multiByte = "é".repeat(40); // 40 caracteres, 80 bytes em UTF-8

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new UserCreateRequest(
                                                "Gestor",
                                                "gestor@test.local",
                                                multiByte,
                                                Role.MANAGER,
                                                null,
                                                null)));

        assertEquals("INVALID_PASSWORD", error.getCode());
        verify(passwords, never()).encode(anyString());
    }

    @Test
    void shouldRejectNameWithOnlyOneVisibleCharacter() {
        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.create(
                                        new UserCreateRequest(
                                                " a ",
                                                "gestor@test.local",
                                                PASSWORD,
                                                Role.MANAGER,
                                                null,
                                                null)));

        assertEquals("INVALID_NAME", error.getCode());
    }

    @Test
    void shouldIgnoreSpecialtyForManagers() {
        service.create(
                new UserCreateRequest(
                        "Gestor", "gestor@test.local", PASSWORD, Role.MANAGER, null, "Elétrica"));

        assertNull(savedUser().getSpecialty());
        assertNull(savedUser().getManager());
    }

    @Test
    void initialAdminShouldNotNeedToChangeConfiguredPassword() {
        UserResponse response =
                service.createInitialAdmin("Administrador", "admin@test.local", PASSWORD);

        assertEquals(Role.ADMIN, response.role());
        assertFalse(response.mustChangePassword());
        assertFalse(savedUser().isMustChangePassword());
    }

    @Test
    void changeOwnPasswordShouldRequireCurrentPassword() {
        AppUser ana = temporaryUser();
        when(users.findByEmailIgnoreCase("ana@test.local")).thenReturn(Optional.of(ana));
        when(passwords.matches("errada", ana.getPasswordHash())).thenReturn(false);

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.changeOwnPassword(
                                        "ana@test.local",
                                        new PasswordChangeRequest("errada", "NovaSenhaSegura123!")));

        assertEquals("INVALID_CURRENT_PASSWORD", error.getCode());
        assertTrue(ana.isMustChangePassword());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void changeOwnPasswordShouldRejectReusingTemporaryPassword() {
        AppUser ana = temporaryUser();
        when(users.findByEmailIgnoreCase("ana@test.local")).thenReturn(Optional.of(ana));
        when(passwords.matches(PASSWORD, ana.getPasswordHash())).thenReturn(true);

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.changeOwnPassword(
                                        "ana@test.local", new PasswordChangeRequest(PASSWORD, PASSWORD)));

        assertEquals("PASSWORD_REUSED", error.getCode());
        assertTrue(ana.isMustChangePassword());
    }

    @Test
    void changeOwnPasswordShouldStoreNewHashAndReleaseAccess() {
        AppUser ana = temporaryUser();
        when(users.findByEmailIgnoreCase("ana@test.local")).thenReturn(Optional.of(ana));
        when(passwords.matches(PASSWORD, ana.getPasswordHash())).thenReturn(true);

        UserResponse response =
                service.changeOwnPassword(
                        "ana@test.local", new PasswordChangeRequest(PASSWORD, "NovaSenhaSegura123!"));

        assertFalse(response.mustChangePassword());
        assertFalse(ana.isMustChangePassword());
        assertEquals("bcrypt:NovaSenhaSegura123!", ana.getPasswordHash());
    }

    @Test
    void changeOwnPasswordShouldRejectUnknownSessionUser() {
        when(users.findByEmailIgnoreCase("fantasma@test.local")).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                service.changeOwnPassword(
                                        "fantasma@test.local",
                                        new PasswordChangeRequest(PASSWORD, "NovaSenhaSegura123!")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
    }

    @Test
    void resetPasswordShouldForceChangeOnNextLogin() {
        AppUser bruno = technician(4, "Bruno");
        when(users.findById(4L)).thenReturn(Optional.of(bruno));

        UserResponse response =
                service.resetPassword(4L, new PasswordResetRequest("OutraProvisoria123!"));

        assertTrue(response.mustChangePassword());
        assertEquals("bcrypt:OutraProvisoria123!", bruno.getPasswordHash());
    }

    @Test
    void resetPasswordShouldReportMissingUser() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        BusinessException error =
                assertThrows(
                        BusinessException.class,
                        () -> service.resetPassword(99L, new PasswordResetRequest(PASSWORD)));

        assertEquals("USER_NOT_FOUND", error.getCode());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    private AppUser temporaryUser() {
        AppUser ana =
                new AppUser(
                        "Ana",
                        "ana@test.local",
                        "bcrypt:" + PASSWORD,
                        Role.TECHNICIAN,
                        null,
                        manager(2),
                        true);
        return set(ana, "id", 7L);
    }

    private AppUser savedUser() {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(users, atLeastOnce()).saveAndFlush(captor.capture());
        return captor.getValue();
    }
}
