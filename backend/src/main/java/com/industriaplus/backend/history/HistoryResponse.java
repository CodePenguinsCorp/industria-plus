package com.industriaplus.backend.history;

import java.time.LocalDateTime;
import java.util.List;

public record HistoryResponse(
        Long id,
        Long requestId,
        String eventType,
        String actor,
        String description,
        String previousStatus,
        String currentStatus,
        Long previousTechnicianId,
        String previousTechnicianName,
        Long currentTechnicianId,
        String currentTechnicianName,
        LocalDateTime createdAt,
        List<PartResponse> parts) {
    public record PartResponse(Long id, String name, Integer quantity) {}

    public static HistoryResponse from(HistoryEntry e, List<ReplacedPart> parts) {
        return new HistoryResponse(
                e.getId(),
                e.getRequest().getId(),
                e.getEventType(),
                e.getActor(),
                e.getDescription(),
                e.getPreviousStatus(),
                e.getCurrentStatus(),
                e.getPreviousTechnicianId(),
                e.getPreviousTechnicianName(),
                e.getCurrentTechnicianId(),
                e.getCurrentTechnicianName(),
                e.getCreatedAt(),
                parts.stream()
                        .map(p -> new PartResponse(p.getId(), p.getName(), p.getQuantity()))
                        .toList());
    }
}
