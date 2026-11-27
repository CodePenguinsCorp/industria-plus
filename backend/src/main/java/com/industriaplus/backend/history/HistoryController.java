package com.industriaplus.backend.history;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance-requests")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService service;

    @GetMapping("/{id}/history")
    public List<HistoryResponse> history(@PathVariable Long id) {
        return service.requestHistory(id);
    }

    @PostMapping("/{id}/interventions")
    @ResponseStatus(HttpStatus.CREATED)
    public HistoryResponse intervene(
            @PathVariable Long id, @Valid @RequestBody InterventionRequest input) {
        return service.intervene(id, input);
    }
}
