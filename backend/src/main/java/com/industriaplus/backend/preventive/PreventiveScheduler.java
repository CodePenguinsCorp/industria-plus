package com.industriaplus.backend.preventive;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.preventive-scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class PreventiveScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(PreventiveScheduler.class);
    private final PreventivePlanRepository plans;
    private final PreventivePlanService service;

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void generate() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        for (Long id : plans.findDueIds(today)) {
            try { service.generateDue(id, today); }
            catch (RuntimeException error) { LOG.error("Falha ao gerar preventiva do plano {}", id, error); }
        }
    }
}
