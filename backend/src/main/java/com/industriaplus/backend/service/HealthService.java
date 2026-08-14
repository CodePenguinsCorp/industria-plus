package com.industriaplus.backend.service;

import com.industriaplus.backend.health.HealthResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class HealthService {

    public HealthResponse getStatus() {
        return new HealthResponse(
            "industria-plus-backend",
            "UP",
            OffsetDateTime.now(),
            List.of("Angular 21", "Spring Boot 3", "MySQL 8.4")
        );
    }
}
