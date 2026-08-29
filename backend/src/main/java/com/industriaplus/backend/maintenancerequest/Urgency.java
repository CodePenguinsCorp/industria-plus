package com.industriaplus.backend.maintenancerequest;

public enum Urgency {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int priority;

    Urgency(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
