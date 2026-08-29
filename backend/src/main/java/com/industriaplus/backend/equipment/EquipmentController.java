package com.industriaplus.backend.equipment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.create(request);
    }

    @GetMapping
    public List<EquipmentResponse> list() {
        return equipmentService.list();
    }
}
