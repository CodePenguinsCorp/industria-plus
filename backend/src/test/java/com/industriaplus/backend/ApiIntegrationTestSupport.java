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
    protected static final String TEMPORARY_PASSWORD = "SenhaProvisoria123!";

    private Long baseManagerId;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.web.context.WebApplicationContext webContext;

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetails;

    protected org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedUser(String email) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
            .user(userDetails.loadUserByUsername(email));
    }

    @BeforeEach
    void cleanDatabase() {
        baseManagerId = null;
        jdbcTemplate.update("delete from replaced_parts");
        jdbcTemplate.update("delete from maintenance_history");
        jdbcTemplate.update("delete from maintenance_requests");
        jdbcTemplate.update("update app_users set manager_id = null");
        jdbcTemplate.update("delete from app_users");
        jdbcTemplate.update("delete from equipments");
        jdbcTemplate.update("delete from sectors");
        jdbcTemplate.update("insert into app_users(name,email,password_hash,role) values(?,?,?,?)", "Administrador de teste", "admin@test.local", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("SenhaDeTeste123!"), "ADMIN");
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webContext)
            .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/")
                .with(authenticatedUser("admin@test.local"))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
            .build();
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

    /** Técnicos são usuários com perfil TECHNICIAN, vinculados a um gestor. */
    protected long createTechnician(String email) throws Exception {
        if (baseManagerId == null) {
            baseManagerId = createManager("gestor.base@test.local");
        }
        return createTechnician(email, baseManagerId);
    }

    protected long createTechnician(String email, long managerId) throws Exception {
        return createUser(Map.of(
            "name", "Técnico de manutenção",
            "email", email,
            "password", TEMPORARY_PASSWORD,
            "role", "TECHNICIAN",
            "managerId", managerId,
            "specialty", "Mecânica"
        ));
    }

    protected long createManager(String email) throws Exception {
        return createUser(Map.of(
            "name", "Gestor de manutenção",
            "email", email,
            "password", TEMPORARY_PASSWORD,
            "role", "MANAGER"
        ));
    }

    private long createUser(Map<String, Object> payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payload)))
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
