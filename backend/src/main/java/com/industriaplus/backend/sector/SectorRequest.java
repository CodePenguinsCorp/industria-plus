package com.industriaplus.backend.sector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectorRequest(
    @NotBlank(message = "O nome do setor é obrigatório.")
    @Size(min = 2, max = 100, message = "O nome do setor deve ter entre 2 e 100 caracteres.")
    String name,

    @Size(max = 255, message = "A descrição do setor deve ter no máximo 255 caracteres.")
    String description
) {
    public SectorRequest {
        name = strip(name);
        description = stripToNull(description);
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    private static String stripToNull(String value) {
        String stripped = strip(value);
        return stripped == null || stripped.isEmpty() ? null : stripped;
    }
}
