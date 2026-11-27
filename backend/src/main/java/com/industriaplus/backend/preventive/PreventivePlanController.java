package com.industriaplus.backend.preventive;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/equipments/{equipmentId}/preventive-plan")
@RequiredArgsConstructor
public class PreventivePlanController {
    private final PreventivePlanService service;

    @GetMapping
    public ResponseEntity<PreventivePlanService.PlanResponse> get(@PathVariable Long equipmentId) {
        var plan = service.get(equipmentId);
        return plan == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(plan);
    }

    @PutMapping
    public PreventivePlanService.PlanResponse save(@PathVariable Long equipmentId,
            @Valid @RequestBody PreventivePlanService.PlanInput input) {
        var plan = service.save(equipmentId, input);
        service.generateDue(plan.id(), LocalDate.now(ZoneId.of("America/Sao_Paulo")));
        return service.get(equipmentId);
    }
}
