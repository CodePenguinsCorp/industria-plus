package com.industriaplus.backend.health;

import java.time.OffsetDateTime;
import java.util.List;

public record HealthResponse(
    String applicationName,
    String status,
    OffsetDateTime timestamp,
    List<String> stack
) {
}
