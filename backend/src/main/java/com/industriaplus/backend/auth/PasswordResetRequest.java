package com.industriaplus.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Senha provisória definida pelo administrador; o usuário precisa trocá-la ao entrar. */
public record PasswordResetRequest(
        @NotBlank(message = "Informe a senha provisória.")
                @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres.")
                String password) {}
