package com.industriaplus.backend.preventive;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.equipment.EquipmentService;
import com.industriaplus.backend.history.HistoryEntry;
import com.industriaplus.backend.history.HistoryRepository;
import com.industriaplus.backend.maintenancerequest.*;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PreventivePlanService {
    private final PreventivePlanRepository plans;
    private final EquipmentService equipment;
    private final MaintenanceRequestRepository requests;
    private final HistoryRepository history;
    private final AccessService access;

    public PlanResponse save(Long equipmentId, PlanInput input) {
        access.requireManager();
        var item = equipment.getRequired(equipmentId);
        var plan = plans.findByEquipmentIdForUpdate(equipmentId).orElseGet(() -> new PreventivePlan(item));
        plan.configure(input.title(), input.description(), input.intervalDays(), input.nextDueDate(), input.enabled());
        return response(plans.saveAndFlush(plan));
    }

    @Transactional(readOnly = true)
    public PlanResponse get(Long equipmentId) {
        equipment.getRequired(equipmentId);
        return plans.findByEquipmentId(equipmentId).map(this::response).orElse(null);
    }

    public void generateDue(Long id, LocalDate today) {
        var plan = plans.findByIdForUpdate(id).orElse(null);
        if (plan == null || !plan.isEnabled() || plan.getNextDueDate().isAfter(today)) return;
        LocalDate dueDate = plan.getNextDueDate();
        if (!requests.existsByPreventivePlanIdAndScheduledFor(id, dueDate)) {
            var request = new MaintenanceRequest(plan.getTitle(), plan.getDescription(), plan.getEquipment(),
                plan.getEquipment().getSector(), MaintenanceType.PREVENTIVE, Urgency.MEDIUM);
            request.schedule(id, dueDate);
            requests.saveAndFlush(request);
            history.save(new HistoryEntry(request, "CREATED", "Agendamento automático",
                "Preventiva programada para " + dueDate + ".", null, null, null));
        }
        plan.advanceAfter(today);
        plans.saveAndFlush(plan);
    }

    private PlanResponse response(PreventivePlan p) {
        return new PlanResponse(p.getId(), p.getTitle(), p.getDescription(), p.getIntervalDays(), p.getNextDueDate(), p.isEnabled());
    }

    public record PlanInput(@NotBlank @Size(min = 3, max = 160) String title,
        @NotBlank @Size(min = 10, max = 2000) String description,
        @NotNull @Min(1) @Max(3650) Integer intervalDays, @NotNull LocalDate nextDueDate, @NotNull Boolean enabled) {
        public PlanInput {
            title = title == null ? null : title.strip();
            description = description == null ? null : description.strip();
        }
    }
    public record PlanResponse(Long id, String title, String description, int intervalDays, LocalDate nextDueDate, boolean enabled) {}
}
