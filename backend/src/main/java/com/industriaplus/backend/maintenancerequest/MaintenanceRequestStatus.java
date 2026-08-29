package com.industriaplus.backend.maintenancerequest;

public enum MaintenanceRequestStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED;

    public boolean canTransitionTo(MaintenanceRequestStatus target) {
        return switch (this) {
            case OPEN -> target == IN_PROGRESS || target == CLOSED;
            case IN_PROGRESS -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
