package com.industriaplus.backend.auth;

import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres.")
                String password,
        @NotNull Role role,
        @Positive(message = "O gestor informado é inválido.") Long managerId,
        @Size(max = 120, message = "A especialidade deve ter no máximo 120 caracteres.")
                String specialty) {
    public UserCreateRequest {
        email = email == null ? null : email.strip();
    }
}
