package com.industriaplus.backend.equipment;

import java.time.LocalDateTime;

public record EquipmentResponse(
    Long id,
    String assetTag,
    String name,
    String description,
    Long sectorId,
    String sectorName,
    LocalDateTime createdAt
) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
            equipment.getId(),
            equipment.getAssetTag(),
            equipment.getName(),
            equipment.getDescription(),
            equipment.getSector().getId(),
            equipment.getSector().getName(),
            equipment.getCreatedAt()
        );
    }
}
