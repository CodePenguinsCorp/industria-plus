package com.industriaplus.backend.auth;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String specialty,
        Long managerId,
        String managerName,
        boolean mustChangePassword,
        LocalDateTime createdAt) {
    public static UserResponse from(AppUser user) {
        AppUser manager = user.getManager();
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getSpecialty(),
                manager == null ? null : manager.getId(),
                manager == null ? null : manager.getName(),
                user.isMustChangePassword(),
                user.getCreatedAt());
    }
}
