package com.industriaplus.backend.auth;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitialAdmin implements ApplicationRunner {
    private final UserRepository users;
    private final UserService service;
    private final JdbcTemplate jdbc;

    @Value("${INITIAL_ADMIN_EMAIL:admin@industria.local}")
    private String email;

    @Value("${INITIAL_ADMIN_PASSWORD:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (password.isBlank()) return;
        jdbc.queryForList("select id from bootstrap_registry order by id for update");
        if (users.count() > 0) return;
        if (password.length() < 12
                || password.length() > 72
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException(
                    "INITIAL_ADMIN_PASSWORD deve ter entre 12 caracteres e 72 bytes.");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("INITIAL_ADMIN_EMAIL inválido.");
        service.createInitialAdmin("Administrador", email, password);
    }
}
