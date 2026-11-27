package com.industriaplus.backend;

import com.industriaplus.backend.preventive.PreventivePlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PreventiveConcurrencyIntegrationTest extends ApiIntegrationTestSupport {
    @Autowired private PreventivePlanService service;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void shouldGenerateOnlyOneOccurrenceWhenTwoWorkersRunAtOnce() throws Exception {
        long equipment = createEquipment(createSector("Concorrência preventiva"), "CONC-PLAN");
        var response = mockMvc.perform(put("/api/equipments/{id}/preventive-plan", equipment).contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("title", "Inspeção semanal",
                    "description", "Verificar componentes de segurança.", "intervalDays", 7,
                    "nextDueDate", LocalDate.now().plusDays(7).toString(), "enabled", true))))
            .andExpect(status().isOk()).andReturn();
        long id = body(response).get("id").asLong();
        jdbc.update("update preventive_plans set next_due_date=? where id=?", LocalDate.now().minusDays(1), id);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> generate = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout");
                service.generateDue(id, LocalDate.now());
                return null;
            };
            Future<Void> first = workers.submit(generate);
            Future<Void> second = workers.submit(generate);
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        } finally { workers.shutdownNow(); }
        assertEquals(1, jdbc.queryForObject("select count(*) from maintenance_requests where preventive_plan_id=?", Integer.class, id));
        assertEquals(1, jdbc.queryForObject("select count(*) from maintenance_history where actor='Agendamento automático'", Integer.class));
    }
}
