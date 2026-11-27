package com.industriaplus.backend.auth;

import com.industriaplus.backend.error.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository users;
    private final PasswordEncoder passwords;

    /**
     * Cadastro feito pelo administrador. A senha informada é provisória: o usuário precisa
     * trocá-la no primeiro acesso.
     */
    public UserResponse create(UserCreateRequest input) {
        return UserResponse.from(register(input, true));
    }

    /** Primeiro administrador criado pela configuração do ambiente; a senha já é definitiva. */
    public UserResponse createInitialAdmin(String name, String email, String password) {
        return UserResponse.from(
                register(new UserCreateRequest(name, email, password, Role.ADMIN, null, null), false));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAllByOrderByNameAsc().stream().map(UserResponse::from).toList();
    }

    /** Administrador define uma senha provisória; o usuário deverá trocá-la ao entrar. */
    public UserResponse resetPassword(Long id, PasswordResetRequest input) {
        AppUser user =
                users.findById(id)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                HttpStatus.NOT_FOUND,
                                                "USER_NOT_FOUND",
                                                "Usuário não encontrado."));
        requirePasswordBytes(input.password());
        user.changePassword(passwords.encode(input.password()), true);
        return UserResponse.from(users.saveAndFlush(user));
    }

    /** Troca da própria senha. Obrigatória enquanto a senha for provisória. */
    public UserResponse changeOwnPassword(String email, PasswordChangeRequest input) {
        AppUser user =
                users.findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                HttpStatus.UNAUTHORIZED,
                                                "UNAUTHENTICATED",
                                                "Sessão inválida. Entre novamente."));
        if (!passwords.matches(input.currentPassword(), user.getPasswordHash()))
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CURRENT_PASSWORD",
                    "A senha atual não confere.");
        requirePasswordBytes(input.newPassword());
        if (input.newPassword().equals(input.currentPassword()))
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_REUSED",
                    "A nova senha deve ser diferente da senha atual.");
        user.changePassword(passwords.encode(input.newPassword()), false);
        return UserResponse.from(users.saveAndFlush(user));
    }

    private AppUser register(UserCreateRequest input, boolean mustChangePassword) {
        if (input.name().trim().length() < 2) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_NAME",
                    "O nome precisa ter ao menos 2 caracteres.");
        }
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email))
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_USER_EMAIL",
                    "Já existe um usuário com esse e-mail.");
        requirePasswordBytes(input.password());
        boolean technician = input.role() == Role.TECHNICIAN;
        if (technician != (input.managerId() != null))
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_MANAGER_LINK",
                    "Vincule um gestor somente para o perfil Técnico.");
        AppUser manager = technician ? requireManager(input.managerId()) : null;
        return users.saveAndFlush(
                new AppUser(
                        input.name().trim(),
                        email,
                        passwords.encode(input.password()),
                        input.role(),
                        technician ? trimToNull(input.specialty()) : null,
                        manager,
                        mustChangePassword));
    }

    private AppUser requireManager(Long managerId) {
        return users.findByIdAndRole(managerId, Role.MANAGER)
                .orElseThrow(
                        () ->
                                new BusinessException(
                                        HttpStatus.BAD_REQUEST,
                                        "INVALID_MANAGER_LINK",
                                        "Selecione um gestor válido."));
    }

    private void requirePasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES)
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PASSWORD",
                    "A senha deve ter no máximo 72 bytes.");
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
