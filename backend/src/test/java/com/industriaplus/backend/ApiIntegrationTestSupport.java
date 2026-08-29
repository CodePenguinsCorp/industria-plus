package com.industriaplus.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiIntegrationTestSupport {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("delete from maintenance_requests");
        jdbcTemplate.update("delete from equipments");
        jdbcTemplate.update("delete from technicians");
        jdbcTemplate.update("delete from sectors");
    }

    protected long createSector(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sectors")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", name,
                    "description", "Setor usado nos testes de integração"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        return body(result).get("id").asLong();
    }

    protected long createEquipment(long sectorId, String assetTag) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/equipments")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "assetTag", assetTag,
                    "name", "Prensa hidráulica",
                    "description", "Equipamento usado nos testes",
                    "sectorId", sectorId
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        return body(result).get("id").asLong();
    }

    protected long createTechnician(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/technicians")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "Técnico de manutenção",
                    "email", email,
                    "specialty", "Mecânica"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        return body(result).get("id").asLong();
    }

    protected long createMaintenanceRequest(
        long equipmentId,
        long sectorId,
        String urgency
    ) throws Exception {
        long sequence = SEQUENCE.incrementAndGet();
        MvcResult result = mockMvc.perform(post("/api/maintenance-requests")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", "Falha industrial " + sequence,
                    "description", "Descrição detalhada da falha industrial " + sequence,
                    "equipmentId", equipmentId,
                    "sectorId", sectorId,
                    "type", "CORRECTIVE",
                    "urgency", urgency
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        return body(result).get("id").asLong();
    }

    protected MvcResult assign(long requestId, long technicianId) throws Exception {
        return mockMvc.perform(patch(
                "/api/maintenance-requests/{id}/assignment",
                requestId
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("technicianId", technicianId))))
            .andReturn();
    }

    protected MvcResult updateStatus(long requestId, String statusValue) throws Exception {
        return mockMvc.perform(patch(
                "/api/maintenance-requests/{id}/status",
                requestId
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", statusValue))))
            .andReturn();
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
