package com.industriaplus.backend;

import com.industriaplus.backend.auth.InitialAdmin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = {
    "INITIAL_ADMIN_EMAIL=bootstrap@test.local",
    "INITIAL_ADMIN_PASSWORD=SenhaInicialTeste123!"
})
class InitialAdminIntegrationTest extends ApiIntegrationTestSupport {
    @Autowired
    private InitialAdmin initialAdmin;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwords;

    @Test
    void shouldCreateFirstAdminWhenMigratedTechniciansAlreadyExist() {
        jdbc.update("delete from app_users");
        jdbc.update("""
            insert into app_users(name,email,password_hash,role,must_change_password)
            values ('Técnico migrado','migrado@test.local','MIGRATED-WITHOUT-PASSWORD','TECHNICIAN',true)
            """);

        initialAdmin.run(new DefaultApplicationArguments());
        initialAdmin.run(new DefaultApplicationArguments());

        assertEquals(1, jdbc.queryForObject(
            "select count(*) from app_users where role='ADMIN'", Integer.class));
        String passwordHash = jdbc.queryForObject(
            "select password_hash from app_users where email='bootstrap@test.local'", String.class);
        assertTrue(passwords.matches("SenhaInicialTeste123!", passwordHash));
        assertEquals("MIGRATED-WITHOUT-PASSWORD", jdbc.queryForObject(
            "select password_hash from app_users where email='migrado@test.local'", String.class));
    }

    @Test
    void shouldKeepExistingAdminWithoutCreatingAnotherAccount() {
        initialAdmin.run(new DefaultApplicationArguments());

        assertEquals(1, jdbc.queryForObject("select count(*) from app_users", Integer.class));
        assertEquals("admin@test.local", jdbc.queryForObject(
            "select email from app_users where role='ADMIN'", String.class));
    }
}
