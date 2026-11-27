package com.industriaplus.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

class SecurityAndHistoryIntegrationTest extends ApiIntegrationTestSupport {
    @Autowired JdbcTemplate jdbc;
    @Autowired WebApplicationContext webContext;

    private MockMvc secured() {
        return MockMvcBuilders.webAppContextSetup(webContext).apply(springSecurity()).build();
    }

    @Test
    void shouldRequireLoginAndValidatePasswordAndSession() throws Exception {
        MockMvc api = secured();
        api.perform(get("/api/equipments").contextPath("/api"))
                .andExpect(status().isUnauthorized());
        api.perform(
                        post("/api/auth/login")
                                .contextPath("/api")
                                .with(csrf())
                                .param("email", "admin@test.local")
                                .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
        var result =
                api.perform(
                                post("/api/auth/login")
                                        .contextPath("/api")
                                        .with(csrf())
                                        .param("email", "admin@test.local")
                                        .param("password", "SenhaDeTeste123!"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.role").value("ADMIN"))
                        .andExpect(jsonPath("$.mustChangePassword").value(false))
                        .andExpect(jsonPath("$.passwordHash").doesNotExist())
                        .andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        api.perform(get("/api/auth/me").contextPath("/api").session(session))
                .andExpect(status().isOk());
        api.perform(
                        post("/api/sectors")
                                .contextPath("/api")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of("name", "Sem CSRF"))))
                .andExpect(status().isForbidden());
        api.perform(post("/api/auth/logout").contextPath("/api").session(session).with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
    }

    @Test
    void shouldEnforceRolesAndRequireManagerLink() throws Exception {
        var api = secured();
        api.perform(
                        post("/api/users")
                                .contextPath("/api")
                                .with(user("gestor@test.local").roles("MANAGER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isForbidden());
        api.perform(
                        post("/api/sectors")
                                .contextPath("/api")
                                .with(user("tech@test.local").roles("TECHNICIAN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        post("/api/users")
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        json(
                                                Map.of(
                                                        "name",
                                                        "Técnico",
                                                        "email",
                                                        "tech@test.local",
                                                        "password",
                                                        "SenhaDoTecnico123!",
                                                        "role",
                                                        "TECHNICIAN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MANAGER_LINK"));
        long technician = createTechnician("tecnico.base@test.local");
        api.perform(
                        patch("/api/technicians/{id}/manager", technician)
                                .contextPath("/api")
                                .with(user("gestor@test.local").roles("MANAGER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"managerId\":1}"))
                .andExpect(status().isForbidden());
        api.perform(
                        delete("/api/technicians/{id}", technician)
                                .contextPath("/api")
                                .with(user("gestor@test.local").roles("MANAGER"))
                                .with(csrf()))
                .andExpect(status().isForbidden());
        api.perform(
                        get("/api/technicians/managers")
                                .contextPath("/api")
                                .with(user("tech@test.local").roles("TECHNICIAN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        post("/api/technicians")
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Sem cadastro direto\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRecordAssignmentStatusServiceAndPartsWithoutChangingOldRequests() throws Exception {
        long sector = createSector("Produção"),
                equipment = createEquipment(sector, "MAQ-01"),
                tech = createTechnician("tech@test.local");
        long request = createMaintenanceRequest(equipment, sector, "HIGH");
        assertEquals(200, assign(request, tech).getResponse().getStatus());
        assertEquals(200, updateStatus(request, "IN_PROGRESS").getResponse().getStatus());
        mockMvc.perform(
                        post("/api/maintenance-requests/{id}/interventions", request)
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"description\":\"Substituído o rolamento e testada a"
                                            + " máquina.\",\"parts\":[{\"name\":\"Rolamento\",\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parts[0].quantity").value(2));
        mockMvc.perform(get("/api/equipments/{id}", equipment).contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requests.length()").value(1))
                .andExpect(jsonPath("$.history.length()").value(4))
                .andExpect(jsonPath("$.history[0].actor").value("admin@test.local"));
        assertEquals(200, updateStatus(request, "CLOSED").getResponse().getStatus());
        mockMvc.perform(
                        post("/api/maintenance-requests/{id}/interventions", request)
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"description\":\"Tentativa depois do"
                                            + " encerramento\",\"parts\":[]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLimitTechnicianToOwnRequestsAndKeepHistoryAtomic() throws Exception {
        long sector = createSector("Montagem"),
                equipment = createEquipment(sector, "MAQ-02"),
                tech = createTechnician("ana@test.local"),
                other = createTechnician("bia@test.local");
        jdbc.update("update app_users set must_change_password = false where id = ?", tech);
        long own = createMaintenanceRequest(equipment, sector, "LOW"),
                foreign = createMaintenanceRequest(equipment, sector, "LOW");
        assign(own, tech);
        assign(foreign, other);
        var api = secured();
        api.perform(
                        patch("/api/maintenance-requests/{id}/status", foreign)
                                .contextPath("/api")
                                .with(authenticatedUser("ana@test.local"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isForbidden());
        api.perform(
                        post("/api/maintenance-requests/{id}/interventions", foreign)
                                .contextPath("/api")
                                .with(authenticatedUser("ana@test.local"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"description\":\"Serviço não"
                                            + " autorizado\",\"parts\":[]}"))
                .andExpect(status().isForbidden());
        api.perform(
                        post("/api/maintenance-requests/{id}/interventions", own)
                                .contextPath("/api")
                                .with(authenticatedUser("ana@test.local"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"description\":\"Serviço"
                                            + " autorizado\",\"parts\":[{\"name\":\"Filtro\",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from maintenance_history where event_type='INTERVENTION'",
                        Integer.class));
        api.perform(
                        post("/api/maintenance-requests/{id}/interventions", own)
                                .contextPath("/api")
                                .with(authenticatedUser("ana@test.local"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"description\":\"Limpeza e teste\",\"parts\":[]}"))
                .andExpect(status().isCreated());
        api.perform(
                        patch("/api/maintenance-requests/{id}/status", own)
                                .contextPath("/api")
                                .with(authenticatedUser("ana@test.local"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateTechnicianLoginWithTemporaryPasswordAndForceChange() throws Exception {
        long manager = createManager("gestor.login@test.local");
        var created =
                mockMvc.perform(
                                post("/api/users")
                                        .contextPath("/api")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                json(
                                                        Map.of(
                                                                "name",
                                                                "Técnico",
                                                                "email",
                                                                "login@test.local",
                                                                "password",
                                                                "SenhaDoTecnico123!",
                                                                "role",
                                                                "TECHNICIAN",
                                                                "managerId",
                                                                manager))))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.password").doesNotExist())
                        .andExpect(jsonPath("$.managerId").value(manager))
                        .andExpect(jsonPath("$.mustChangePassword").value(true))
                        .andReturn();
        long tech = body(created).get("id").asLong();
        String hash =
                jdbc.queryForObject(
                        "select password_hash from app_users where email='login@test.local'",
                        String.class);
        assertNotEquals("SenhaDoTecnico123!", hash);
        assertTrue(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .matches("SenhaDoTecnico123!", hash));

        MockMvc api = secured();
        var login =
                api.perform(
                                post("/api/auth/login")
                                        .contextPath("/api")
                                        .with(csrf())
                                        .param("email", "login@test.local")
                                        .param("password", "SenhaDoTecnico123!"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(tech))
                        .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                        .andExpect(jsonPath("$.mustChangePassword").value(true))
                        .andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);

        api.perform(get("/api/equipments").contextPath("/api").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        api.perform(get("/api/auth/me").contextPath("/api").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true));
        api.perform(changePassword(session, "SenhaErrada123!", "NovaSenhaDefinitiva1!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));
        api.perform(changePassword(session, "SenhaDoTecnico123!", "SenhaDoTecnico123!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_REUSED"));
        api.perform(changePassword(session, "SenhaDoTecnico123!", "curta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        api.perform(changePassword(session, "SenhaDoTecnico123!", "NovaSenhaDefinitiva1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(false));
        api.perform(get("/api/equipments").contextPath("/api").session(session))
                .andExpect(status().isOk());

        api.perform(
                        post("/api/auth/login")
                                .contextPath("/api")
                                .with(csrf())
                                .param("email", "login@test.local")
                                .param("password", "SenhaDoTecnico123!"))
                .andExpect(status().isUnauthorized());
        api.perform(
                        post("/api/auth/login")
                                .contextPath("/api")
                                .with(csrf())
                                .param("email", "login@test.local")
                                .param("password", "NovaSenhaDefinitiva1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(false));
    }

    @Test
    void shouldLetAdminDefineTemporaryPasswordAgain() throws Exception {
        long tech = createTechnician("reset@test.local");
        jdbc.update("update app_users set must_change_password = false where id = ?", tech);

        mockMvc.perform(
                        post("/api/users/{id}/password-reset", tech)
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"password\":\"OutraProvisoria123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true));
        secured()
                .perform(
                        post("/api/users/{id}/password-reset", tech)
                                .contextPath("/api")
                                .with(user("gestor@test.local").roles("MANAGER"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"password\":\"OutraProvisoria123!\"}"))
                .andExpect(status().isForbidden());

        var login =
                secured()
                        .perform(
                                post("/api/auth/login")
                                        .contextPath("/api")
                                        .with(csrf())
                                        .param("email", "reset@test.local")
                                        .param("password", "OutraProvisoria123!"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.mustChangePassword").value(true))
                        .andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        secured()
                .perform(get("/api/maintenance-requests").contextPath("/api").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
    }

    @Test
    void shouldLetAdminChangeTechnicianManagerAndManagerOnlyView() throws Exception {
        long firstManager = createManager("gestor.a@test.local");
        long secondManager = createManager("gestor.b@test.local");
        long tech = createTechnician("vinculo@test.local", firstManager);
        jdbc.update("update app_users set must_change_password = false where role = 'MANAGER'");

        mockMvc.perform(
                        patch("/api/technicians/{id}/manager", tech)
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of("managerId", secondManager))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId").value(secondManager));
        mockMvc.perform(
                        patch("/api/technicians/{id}/manager", tech)
                                .contextPath("/api")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(Map.of("managerId", tech))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MANAGER_LINK"));

        var api = secured();
        api.perform(
                        get("/api/technicians")
                                .contextPath("/api")
                                .with(authenticatedUser("gestor.a@test.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].managerId").value(secondManager));
        api.perform(
                        get("/api/technicians/managers")
                                .contextPath("/api")
                                .with(authenticatedUser("gestor.a@test.local")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/users").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    private org.springframework.test.web.servlet.RequestBuilder changePassword(
            MockHttpSession session, String current, String next) throws Exception {
        return post("/api/auth/password")
                .contextPath("/api")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", current, "newPassword", next)));
    }
}
