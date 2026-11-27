package com.industriaplus.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionRevocationIntegrationTest extends ApiIntegrationTestSupport {
    private static final String EMAIL = "session@test.local";
    private static final String NEXT_PASSWORD = "NovaSenhaDefinitiva123!";

    @Autowired JdbcTemplate jdbc;
    @Autowired WebApplicationContext context;

    private MockMvc secured() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private long createActiveTechnician() throws Exception {
        long id = createTechnician(EMAIL);
        jdbc.update("update app_users set must_change_password=false where id=?", id);
        return id;
    }

    private MockHttpSession login(MockMvc api, String password) throws Exception {
        return (MockHttpSession) api.perform(post("/api/auth/login").contextPath("/api")
                .with(csrf()).param("email", EMAIL).param("password", password))
            .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private void changePassword(MockMvc api, MockHttpSession session, String current) throws Exception {
        api.perform(post("/api/auth/password").contextPath("/api").session(session)
                .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", current, "newPassword", NEXT_PASSWORD))))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRevokeDeletedTechnicianSession() throws Exception {
        long technician = createActiveTechnician();
        MockMvc api = secured();
        MockHttpSession session = login(api, TEMPORARY_PASSWORD);
        mockMvc.perform(delete("/api/technicians/{id}", technician).contextPath("/api"))
            .andExpect(status().isNoContent());

        api.perform(get("/api/equipments").contextPath("/api").session(session))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        assertTrue(session.isInvalid());
    }

    @Test
    void shouldNotReuseSessionWhenEmailIsRegisteredAgain() throws Exception {
        long technician = createActiveTechnician();
        MockMvc api = secured();
        MockHttpSession session = login(api, TEMPORARY_PASSWORD);
        mockMvc.perform(delete("/api/technicians/{id}", technician).contextPath("/api"))
            .andExpect(status().isNoContent());
        long replacement = createActiveTechnician();
        assertNotEquals(technician, replacement);

        api.perform(get("/api/auth/me").contextPath("/api").session(session))
            .andExpect(status().isUnauthorized());
        assertTrue(session.isInvalid());
        api.perform(get("/api/auth/me").contextPath("/api")
                .session(login(api, TEMPORARY_PASSWORD)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(replacement));
    }

    @Test
    void shouldRevokeOldSessionsAtResetAndKeepThemRevokedAfterFirstAccess() throws Exception {
        long technician = createActiveTechnician();
        MockMvc api = secured();
        MockHttpSession activeSession = login(api, TEMPORARY_PASSWORD);
        MockHttpSession dormantSession = login(api, TEMPORARY_PASSWORD);
        String resetPassword = "OutraProvisoria123!";
        mockMvc.perform(post("/api/users/{id}/password-reset", technician).contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("password", resetPassword))))
            .andExpect(status().isOk());

        api.perform(get("/api/auth/me").contextPath("/api").session(activeSession))
            .andExpect(status().isUnauthorized());
        assertTrue(activeSession.isInvalid());
        MockHttpSession newSession = login(api, resetPassword);
        api.perform(get("/api/equipments").contextPath("/api").session(newSession))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        changePassword(api, newSession, resetPassword);

        api.perform(get("/api/equipments").contextPath("/api").session(newSession))
            .andExpect(status().isOk());
        api.perform(get("/api/equipments").contextPath("/api").session(dormantSession))
            .andExpect(status().isUnauthorized());
        assertTrue(dormantSession.isInvalid());
    }

    @Test
    void shouldKeepOnlyCurrentSessionAfterChangingOwnPassword() throws Exception {
        createActiveTechnician();
        MockMvc api = secured();
        MockHttpSession currentSession = login(api, TEMPORARY_PASSWORD);
        MockHttpSession otherSession = login(api, TEMPORARY_PASSWORD);
        String previousSessionId = currentSession.getId();

        changePassword(api, currentSession, TEMPORARY_PASSWORD);

        assertNotEquals(previousSessionId, currentSession.getId());
        api.perform(get("/api/auth/me").contextPath("/api").session(currentSession))
            .andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(false));
        api.perform(get("/api/auth/me").contextPath("/api").session(otherSession))
            .andExpect(status().isUnauthorized());
        assertTrue(otherSession.isInvalid());
    }

    @Test
    void shouldKeepSessionsWhenPasswordChangeFails() throws Exception {
        createActiveTechnician();
        MockMvc api = secured();
        MockHttpSession currentSession = login(api, TEMPORARY_PASSWORD);
        MockHttpSession otherSession = login(api, TEMPORARY_PASSWORD);
        api.perform(post("/api/auth/password").contextPath("/api").session(currentSession)
                .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("currentPassword", "SenhaErrada123!", "newPassword", NEXT_PASSWORD))))
            .andExpect(status().isBadRequest());

        for (MockHttpSession session : new MockHttpSession[] {currentSession, otherSession}) {
            api.perform(get("/api/auth/me").contextPath("/api").session(session))
                .andExpect(status().isOk());
        }
    }

    @Test
    void shouldKeepSessionWhenTechnicianDeletionFails() throws Exception {
        long technician = createActiveTechnician();
        long sector = createSector("Sessões");
        long equipment = createEquipment(sector, "SESSION-01");
        long request = createMaintenanceRequest(equipment, sector, "LOW");
        assertEquals(200, assign(request, technician).getResponse().getStatus());
        MockMvc api = secured();
        MockHttpSession session = login(api, TEMPORARY_PASSWORD);

        mockMvc.perform(delete("/api/technicians/{id}", technician).contextPath("/api"))
            .andExpect(status().isConflict());
        api.perform(get("/api/equipments").contextPath("/api").session(session))
            .andExpect(status().isOk());
    }
}
