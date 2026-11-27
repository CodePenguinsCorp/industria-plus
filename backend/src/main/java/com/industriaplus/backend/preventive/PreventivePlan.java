package com.industriaplus.backend.preventive;

import com.industriaplus.backend.equipment.Equipment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "preventive_plans")
@Getter
@NoArgsConstructor
public class PreventivePlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false, unique = true) private Equipment equipment;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 2000) private String description;
    @Column(name = "interval_days", nullable = false) private int intervalDays;
    @Column(name = "next_due_date", nullable = false) private LocalDate nextDueDate;
    @Column(nullable = false) private boolean enabled;

    public PreventivePlan(Equipment equipment) { this.equipment = equipment; }

    public void configure(String title, String description, int intervalDays, LocalDate nextDueDate, boolean enabled) {
        this.title = title.strip();
        this.description = description.strip();
        this.intervalDays = intervalDays;
        this.nextDueDate = nextDueDate;
        this.enabled = enabled;
    }

    public void advanceAfter(LocalDate today) {
        long periods = ChronoUnit.DAYS.between(nextDueDate, today) / intervalDays + 1;
        nextDueDate = nextDueDate.plusDays(periods * intervalDays);
    }
}
