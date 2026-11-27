package com.industriaplus.backend.equipment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipments/{id}/reliability")
@RequiredArgsConstructor
public class EquipmentReliabilityController {
    private final EquipmentReliabilityService service;

    @GetMapping
    public EquipmentReliabilityService.ReliabilityResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping
    public EquipmentReliabilityService.ReliabilityResponse save(@PathVariable Long id,
            @Valid @RequestBody EquipmentReliabilityService.ReliabilityInput input) {
        return service.save(id, input);
    }
}
