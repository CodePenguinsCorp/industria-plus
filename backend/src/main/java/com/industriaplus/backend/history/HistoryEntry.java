package com.industriaplus.backend.history;

import com.industriaplus.backend.maintenancerequest.MaintenanceRequest;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_history")
@Getter
@NoArgsConstructor
public class HistoryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private MaintenanceRequest request;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(nullable = false, length = 254)
    private String actor;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "current_status", length = 20)
    private String currentStatus;

    @Column(name = "previous_technician_id")
    private Long previousTechnicianId;

    @Column(name = "previous_technician_name", length = 120)
    private String previousTechnicianName;

    @Column(name = "current_technician_id")
    private Long currentTechnicianId;

    @Column(name = "current_technician_name", length = 120)
    private String currentTechnicianName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public HistoryEntry(
            MaintenanceRequest request,
            String type,
            String actor,
            String description,
            String previousStatus,
            Long previousTechnicianId,
            String previousTechnicianName) {
        this.request = request;
        this.eventType = type;
        this.actor = actor;
        this.description = description;
        this.previousStatus = previousStatus;
        this.currentStatus = request.getStatus().name();
        this.previousTechnicianId = previousTechnicianId;
        this.previousTechnicianName = previousTechnicianName;
        this.currentTechnicianId =
                request.getTechnician() == null ? null : request.getTechnician().getId();
        this.currentTechnicianName =
                request.getTechnician() == null ? null : request.getTechnician().getName();
    }
}
