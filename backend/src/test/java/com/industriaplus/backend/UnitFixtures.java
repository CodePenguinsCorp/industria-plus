package com.industriaplus.backend;

import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.auth.Role;
import com.industriaplus.backend.equipment.Equipment;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequest;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.MaintenanceType;
import com.industriaplus.backend.maintenancerequest.Urgency;
import com.industriaplus.backend.sector.Sector;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/** Objetos de domínio para testes unitários, sem banco de dados nem contexto Spring. */
public final class UnitFixtures {
    private UnitFixtures() {}

    public static AppUser user(long id, String name, Role role) {
        AppUser user =
                new AppUser(
                        name,
                        name.toLowerCase().replace(' ', '.') + "@test.local",
                        "hash-" + id,
                        role,
                        null,
                        null,
                        false);
        return set(user, "id", id);
    }

    public static AppUser manager(long id) {
        return user(id, "Gestor " + id, Role.MANAGER);
    }

    public static AppUser technician(long id, String name) {
        return user(id, name, Role.TECHNICIAN);
    }

    public static Sector sector(long id) {
        return set(new Sector("Setor " + id, null), "id", id);
    }

    public static Equipment equipment(long id, Sector sector) {
        return set(new Equipment("EQ-" + id, "Equipamento " + id, null, sector), "id", id);
    }

    public static MaintenanceRequest request(
            long id, Urgency urgency, MaintenanceRequestStatus status, AppUser technician) {
        Sector sector = sector(1);
        MaintenanceRequest request =
                new MaintenanceRequest(
                        "Chamado " + id,
                        "Descrição do chamado " + id,
                        equipment(1, sector),
                        sector,
                        MaintenanceType.CORRECTIVE,
                        urgency);
        request.assignTo(technician);
        request.transitionTo(status);
        set(request, "id", id);
        return set(request, "createdAt", LocalDateTime.of(2026, 10, 1, 8, 0).plusMinutes(id));
    }

    public static <T> T set(T target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
            return target;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // procura na superclasse
            }
        }
        throw new NoSuchFieldException(name);
    }
}
