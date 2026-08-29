package com.industriaplus.backend.maintenancerequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MaintenanceRequestCreateRequest(
    @NotBlank(message = "O título do chamado é obrigatório.")
    @Size(min = 3, max = 160, message = "O título deve ter entre 3 e 160 caracteres.")
    String title,

    @NotBlank(message = "A descrição do chamado é obrigatória.")
    @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres.")
    String description,

    @NotNull(message = "O equipamento do chamado é obrigatório.")
    @Positive(message = "O equipamento informado é inválido.")
    Long equipmentId,

    @NotNull(message = "O setor do chamado é obrigatório.")
    @Positive(message = "O setor informado é inválido.")
    Long sectorId,

    @NotNull(message = "O tipo de manutenção é obrigatório.")
    MaintenanceType type,

    @NotNull(message = "A urgência do chamado é obrigatória.")
    Urgency urgency
) {
    public MaintenanceRequestCreateRequest {
        title = title == null ? null : title.strip();
        description = description == null ? null : description.strip();
    }
}
