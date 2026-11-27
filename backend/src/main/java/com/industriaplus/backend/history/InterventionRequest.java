package com.industriaplus.backend.history;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record InterventionRequest(
        @NotBlank @Size(min = 3, max = 2000) String description,
        @Size(max = 50) List<@NotNull @Valid PartInput> parts) {
    public record PartInput(
            @NotBlank @Size(max = 120) String name,
            @NotNull @Min(1) @Max(100000) Integer quantity) {}
}
