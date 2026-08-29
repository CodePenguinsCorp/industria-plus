package com.industriaplus.backend.maintenancerequest;

import com.industriaplus.backend.equipment.Equipment;
import com.industriaplus.backend.sector.Sector;
import com.industriaplus.backend.technician.Technician;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@Getter
@NoArgsConstructor
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private Technician technician;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceRequestStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MaintenanceRequest(
        String title,
        String description,
        Equipment equipment,
        Sector sector,
        MaintenanceType type,
        Urgency urgency
    ) {
        this.title = title;
        this.description = description;
        this.equipment = equipment;
        this.sector = sector;
        this.type = type;
        this.urgency = urgency;
        this.status = MaintenanceRequestStatus.OPEN;
    }

    public void assignTo(Technician technician) {
        this.technician = technician;
    }

    public void transitionTo(MaintenanceRequestStatus status) {
        this.status = status;
    }
}
