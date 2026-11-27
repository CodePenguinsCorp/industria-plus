package com.industriaplus.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EquipmentEvolutionIntegrationTest extends ApiIntegrationTestSupport {
    @Autowired private JdbcTemplate jdbc;

    @Test
    void shouldGenerateOneOverduePreventiveAndKeepCadenceWithoutDuplicating() throws Exception {
        long equipment = createEquipment(createSector("Preventivas"), "PLAN-01");
        Map<String, Object> plan = Map.of("title", "Lubrificação programada",
            "description", "Verificar e lubrificar o equipamento.", "intervalDays", 7,
            "nextDueDate", "2026-10-01", "enabled", true);
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(put("/api/equipments/{id}/preventive-plan", equipment).contextPath("/api")
                    .contentType(MediaType.APPLICATION_JSON).content(json(plan)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(true));
        }
        mockMvc.perform(get("/api/maintenance-requests").contextPath("/api"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].type").value("PREVENTIVE"))
            .andExpect(jsonPath("$[0].urgency").value("MEDIUM"))
            .andExpect(jsonPath("$[0].technicianId").doesNotExist());
        org.junit.jupiter.api.Assertions.assertEquals("Agendamento automático", jdbc.queryForObject(
            "select actor from maintenance_history where event_type='CREATED'", String.class));
        java.time.LocalDate next = jdbc.queryForObject("select next_due_date from preventive_plans",
            java.time.LocalDate.class);
        org.junit.jupiter.api.Assertions.assertTrue(next.isAfter(java.time.LocalDate.now()));
    }

    @Test
    void shouldNotGenerateFutureOrPausedPlansAndGenerateWhenResumed() throws Exception {
        long equipment = createEquipment(createSector("Pausa"), "PLAN-02");
        for (Map<String, Object> input : java.util.List.<Map<String, Object>>of(
            Map.of("title", "Inspeção", "description", "Inspeção programada da máquina.", "intervalDays", 30,
                "nextDueDate", java.time.LocalDate.now().plusDays(2).toString(), "enabled", true),
            Map.of("title", "Inspeção", "description", "Inspeção programada da máquina.", "intervalDays", 30,
                "nextDueDate", "2026-10-01", "enabled", false))) {
            mockMvc.perform(put("/api/equipments/{id}/preventive-plan", equipment).contextPath("/api")
                    .contentType(MediaType.APPLICATION_JSON).content(json(input)))
                .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/maintenance-requests").contextPath("/api"))
            .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(put("/api/equipments/{id}/preventive-plan", equipment).contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "title", "Inspeção", "description", "Inspeção programada da máquina.", "intervalDays", 30,
                    "nextDueDate", "2026-10-01", "enabled", true))))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/maintenance-requests").contextPath("/api"))
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldForbidTechnicianConfigurationButAllowReadingReliability() throws Exception {
        long equipment = createEquipment(createSector("Permissões"), "PERM-01");
        createTechnician("tech.evolution@test.local");
        jdbc.update("update app_users set must_change_password=false where email='tech.evolution@test.local'");
        var tech = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
            .user("tech.evolution@test.local").roles("TECHNICIAN");
        mockMvc.perform(get("/api/equipments/{id}/reliability", equipment).contextPath("/api").with(tech))
            .andExpect(status().isOk());
        for (String endpoint : java.util.List.of("reliability", "preventive-plan")) {
            mockMvc.perform(put("/api/equipments/{id}/" + endpoint, equipment).contextPath("/api")
                    .with(tech).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        }
    }

    @Test
    void shouldCalculateMtbfUsingRealHoursAndOnlyCorrectiveFailuresInThePeriod() throws Exception {
        long sector = createSector("MTBF");
        long equipment = createEquipment(sector, "MTBF-01");
        long first = createMaintenanceRequest(equipment, sector, "HIGH");
        long second = createMaintenanceRequest(equipment, sector, "LOW");
        long preventive = createMaintenanceRequest(equipment, sector, "MEDIUM");
        jdbc.update("update maintenance_requests set created_at='2026-10-15 10:00:00'");
        jdbc.update("update maintenance_requests set created_at='2026-10-01 00:00:00' where id=?", first);
        jdbc.update("update maintenance_requests set created_at='2026-10-31 23:59:59' where id=?", second);
        jdbc.update("update maintenance_requests set request_type='PREVENTIVE' where id=?", preventive);
        long outside = createMaintenanceRequest(equipment, sector, "LOW");
        jdbc.update("update maintenance_requests set created_at='2026-11-01 00:00:00' where id=?", outside);

        mockMvc.perform(put("/api/equipments/{id}/reliability", equipment).contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "startDate", "2026-10-01", "endDate", "2026-10-31", "operatingHours", 100))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.failureCount").value(2))
            .andExpect(jsonPath("$.mtbfHours").value(50));
        mockMvc.perform(get("/api/equipments/{id}/reliability", equipment).contextPath("/api"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.operatingHours").value(100))
            .andExpect(jsonPath("$.mtbfHours").value(50));
    }

    @Test
    void shouldDistinguishMissingMeasurementFromNoRecordedFailures() throws Exception {
        long equipment = createEquipment(createSector("Sem falhas"), "MTBF-02");
        mockMvc.perform(get("/api/equipments/{id}/reliability", equipment).contextPath("/api"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("INSUFFICIENT_DATA"));
        mockMvc.perform(put("/api/equipments/{id}/reliability", equipment).contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of(
                    "startDate", "2026-10-01", "endDate", "2026-10-01", "operatingHours", 8))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("NO_FAILURES"))
            .andExpect(jsonPath("$.mtbfHours").doesNotExist());
    }

    @Test
    void shouldRejectImpossibleHoursAndInvertedPeriod() throws Exception {
        long equipment = createEquipment(createSector("Validações"), "MTBF-03");
        for (Map<String, Object> input : java.util.List.<Map<String, Object>>of(
                Map.of("startDate", "2026-10-01", "endDate", "2026-10-01", "operatingHours", 25),
                Map.of("startDate", "2026-10-02", "endDate", "2026-10-01", "operatingHours", 8),
                Map.of("startDate", "2026-10-01", "endDate", "2026-10-01", "operatingHours", 0),
                Map.of("startDate", java.time.LocalDate.now().plusDays(1).toString(),
                    "endDate", java.time.LocalDate.now().plusDays(2).toString(), "operatingHours", 8))) {
            mockMvc.perform(put("/api/equipments/{id}/reliability", equipment).contextPath("/api")
                    .contentType(MediaType.APPLICATION_JSON).content(json(input)))
                .andExpect(status().isBadRequest());
        }
    }
}
