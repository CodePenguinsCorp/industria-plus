package com.industriaplus.backend.sector;

import java.time.LocalDateTime;

public record SectorResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt
) {
    public static SectorResponse from(Sector sector) {
        return new SectorResponse(
            sector.getId(),
            sector.getName(),
            sector.getDescription(),
            sector.getCreatedAt()
        );
    }
}
