package com.industriaplus.backend.technician;

import com.industriaplus.backend.auth.AppUser;

/** Gestor disponível para vínculo com técnicos. */
public record ManagerOption(Long id, String name, String email) {
    public static ManagerOption from(AppUser manager) {
        return new ManagerOption(manager.getId(), manager.getName(), manager.getEmail());
    }
}
