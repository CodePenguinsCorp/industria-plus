package com.industriaplus.backend.maintenancerequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MaintenanceRequestStatusTest {

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
        "OPEN, IN_PROGRESS, true",
        "OPEN, CLOSED, true",
        "OPEN, OPEN, false",
        "IN_PROGRESS, CLOSED, true",
        "IN_PROGRESS, OPEN, false",
        "IN_PROGRESS, IN_PROGRESS, false",
        "CLOSED, OPEN, false",
        "CLOSED, IN_PROGRESS, false",
        "CLOSED, CLOSED, false"
    })
    void shouldAllowOnlyForwardTransitions(
            MaintenanceRequestStatus from, MaintenanceRequestStatus to, boolean allowed) {
        assertEquals(allowed, from.canTransitionTo(to));
    }

    @ParameterizedTest
    @CsvSource({"HIGH, 3", "MEDIUM, 2", "LOW, 1"})
    void shouldPrioritizeHighThenMediumThenLow(Urgency urgency, int priority) {
        assertEquals(priority, urgency.priority());
    }
}
