package com.industriaplus.backend.maintenancerequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaintenanceRequestAssignmentRequest(
    @NotNull(message = "O técnico é obrigatório.")
    @Positive(message = "O técnico informado é inválido.")
    Long technicianId
) {
}
