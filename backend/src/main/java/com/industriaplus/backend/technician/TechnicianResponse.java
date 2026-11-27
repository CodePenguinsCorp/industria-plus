package com.industriaplus.backend.technician;

import com.industriaplus.backend.auth.AppUser;

import java.time.LocalDateTime;

public record TechnicianResponse(
        Long id,
        String name,
        String email,
        String specialty,
        Long managerId,
        String managerName,
        long highUrgencyOpenRequests,
        LocalDateTime createdAt) {
    public static TechnicianResponse from(AppUser technician, long highUrgencyOpenRequests) {
        AppUser manager = technician.getManager();
        return new TechnicianResponse(
                technician.getId(),
                technician.getName(),
                technician.getEmail(),
                technician.getSpecialty(),
                manager == null ? null : manager.getId(),
                manager == null ? null : manager.getName(),
                highUrgencyOpenRequests,
                technician.getCreatedAt());
    }
}
