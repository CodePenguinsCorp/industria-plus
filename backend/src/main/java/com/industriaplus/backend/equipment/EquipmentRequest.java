package com.industriaplus.backend.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipmentRequest(
    @NotBlank(message = "O patrimônio do equipamento é obrigatório.")
    @Size(min = 2, max = 50, message = "O patrimônio deve ter entre 2 e 50 caracteres.")
    String assetTag,

    @NotBlank(message = "O nome do equipamento é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome do equipamento deve ter entre 2 e 120 caracteres.")
    String name,

    @Size(max = 500, message = "A descrição do equipamento deve ter no máximo 500 caracteres.")
    String description,

    @NotNull(message = "O setor do equipamento é obrigatório.")
    @Positive(message = "O setor informado é inválido.")
    Long sectorId
) {
    public EquipmentRequest {
        assetTag = strip(assetTag);
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
