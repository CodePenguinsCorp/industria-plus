package com.industriaplus.backend.technician;

public record TechnicianWorkload(
    Long technicianId,
    Long highUrgencyOpenRequests
) {
}
