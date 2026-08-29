package com.industriaplus.backend.maintenancerequest;

import jakarta.validation.constraints.NotNull;

public record MaintenanceRequestStatusRequest(
    @NotNull(message = "O status é obrigatório.")
    MaintenanceRequestStatus status
) {
}
