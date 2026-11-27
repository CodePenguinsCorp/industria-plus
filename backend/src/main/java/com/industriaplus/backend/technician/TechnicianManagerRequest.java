package com.industriaplus.backend.technician;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TechnicianManagerRequest(
        @NotNull(message = "O gestor é obrigatório.")
                @Positive(message = "O gestor informado é inválido.")
                Long managerId) {}
