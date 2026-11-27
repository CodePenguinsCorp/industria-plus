package com.industriaplus.backend.technician;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Técnicos são cadastrados em Usuários; não há mais POST /technicians. */
@RestController
@RequestMapping("/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @GetMapping
    public List<TechnicianResponse> list() {
        return technicianService.list();
    }

    @GetMapping("/managers")
    public List<ManagerOption> managers() {
        return technicianService.managers();
    }

    @PatchMapping("/{id}/manager")
    public TechnicianResponse updateManager(
            @PathVariable Long id, @Valid @RequestBody TechnicianManagerRequest request) {
        return technicianService.updateManager(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        technicianService.delete(id);
    }
}
