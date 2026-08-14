package com.industriaplus.backend.controller;

import com.industriaplus.backend.health.HealthResponse;
import com.industriaplus.backend.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping
    public HealthResponse getStatus() {
        return healthService.getStatus();
    }
}
