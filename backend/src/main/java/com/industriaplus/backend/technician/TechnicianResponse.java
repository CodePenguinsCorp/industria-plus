package com.industriaplus.backend.technician;

import java.time.LocalDateTime;

public record TechnicianResponse(
    Long id,
    String name,
    String email,
    String specialty,
    long highUrgencyOpenRequests,
    LocalDateTime createdAt
) {
    public static TechnicianResponse from(
        Technician technician,
        long highUrgencyOpenRequests
    ) {
        return new TechnicianResponse(
            technician.getId(),
            technician.getName(),
            technician.getEmail(),
            technician.getSpecialty(),
            highUrgencyOpenRequests,
            technician.getCreatedAt()
        );
    }
}
