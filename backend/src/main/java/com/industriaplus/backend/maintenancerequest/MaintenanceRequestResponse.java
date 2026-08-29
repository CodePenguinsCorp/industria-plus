package com.industriaplus.backend.maintenancerequest;

import java.time.LocalDateTime;

public record MaintenanceRequestResponse(
    Long id,
    String title,
    String description,
    Long equipmentId,
    String equipmentName,
    Long sectorId,
    String sectorName,
    MaintenanceType type,
    Urgency urgency,
    MaintenanceRequestStatus status,
    Long technicianId,
    String technicianName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MaintenanceRequestResponse from(MaintenanceRequest request) {
        return new MaintenanceRequestResponse(
            request.getId(),
            request.getTitle(),
            request.getDescription(),
            request.getEquipment().getId(),
            request.getEquipment().getName(),
            request.getSector().getId(),
            request.getSector().getName(),
            request.getType(),
            request.getUrgency(),
            request.getStatus(),
            request.getTechnician() == null ? null : request.getTechnician().getId(),
            request.getTechnician() == null ? null : request.getTechnician().getName(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
