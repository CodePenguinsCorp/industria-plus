package com.industriaplus.backend.technician;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TechnicianRequest(
    @NotBlank(message = "O nome do técnico é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome do técnico deve ter entre 2 e 120 caracteres.")
    String name,

    @NotBlank(message = "O e-mail do técnico é obrigatório.")
    @Email(message = "O e-mail do técnico deve ser válido.")
    @Size(max = 254, message = "O e-mail do técnico deve ter no máximo 254 caracteres.")
    String email,

    @Size(max = 120, message = "A especialidade deve ter no máximo 120 caracteres.")
    String specialty
) {
    public TechnicianRequest {
        name = strip(name);
        email = strip(email);
        specialty = stripToNull(specialty);
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    private static String stripToNull(String value) {
        String stripped = strip(value);
        return stripped == null || stripped.isEmpty() ? null : stripped;
    }
}
