package com.industriaplus.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.csrf.*;

import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return email ->
                users.findByEmailIgnoreCase(email.trim())
                        .map(SessionUser::new)
                        .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, ObjectMapper json, UserRepository users)
            throws Exception {
        var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        http.csrf(
                        c ->
                                c.csrfTokenRepository(csrf)
                                        .csrfTokenRequestHandler(
                                                new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/auth/csrf",
                                                "/auth/login",
                                                "/health",
                                                "/actuator/health",
                                                "/error")
                                        .permitAll()
                                        .requestMatchers("/users", "/users/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/technicians/managers")
                                        .hasAnyRole("ADMIN", "MANAGER")
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/sectors",
                                                "/equipments",
                                                "/equipments/**",
                                                "/technicians",
                                                "/maintenance-requests",
                                                "/maintenance-requests/**")
                                        .authenticated()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/sectors",
                                                "/equipments",
                                                "/maintenance-requests")
                                        .hasAnyRole("ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.PATCH, "/technicians/*/manager")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/equipments/*/reliability", "/equipments/*/preventive-plan")
                                        .hasAnyRole("ADMIN", "MANAGER")
                                        .requestMatchers(HttpMethod.DELETE, "/technicians/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(
                                                HttpMethod.DELETE, "/sectors/**", "/equipments/**")
                                        .hasAnyRole("ADMIN", "MANAGER")
                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/maintenance-requests/*/assignment")
                                        .hasAnyRole("ADMIN", "MANAGER")
                                        .requestMatchers(
                                                HttpMethod.PATCH, "/maintenance-requests/*/status")
                                        .authenticated()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/maintenance-requests/*/interventions")
                                        .authenticated()
                                        .requestMatchers("/auth/me", "/auth/logout")
                                        .authenticated()
                                        .requestMatchers(HttpMethod.POST, "/auth/password")
                                        .authenticated()
                                        .anyRequest()
                                        .denyAll())
                .formLogin(
                        f ->
                                f.loginProcessingUrl("/auth/login")
                                        .usernameParameter("email")
                                        .successHandler(
                                                (req, res, auth) -> {
                                                    res.setContentType("application/json");
                                                    json.writeValue(
                                                            res.getOutputStream(),
                                                            UserResponse.from(
                                                                    users.findByEmailIgnoreCase(
                                                                                    auth.getName())
                                                                            .orElseThrow()));
                                                })
                                        .failureHandler(
                                                (req, res, error) ->
                                                        writeError(
                                                                res,
                                                                json,
                                                                401,
                                                                "INVALID_CREDENTIALS",
                                                                "E-mail ou senha inválidos.")))
                .logout(
                        l ->
                                l.logoutUrl("/auth/logout")
                                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                                        .logoutSuccessHandler(
                                                (req, res, auth) -> res.setStatus(204)))
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                                (req, res, error) ->
                                                        writeError(
                                                                res,
                                                                json,
                                                                401,
                                                                "UNAUTHENTICATED",
                                                                "Entre no sistema para continuar."))
                                        .accessDeniedHandler(
                                                (req, res, error) ->
                                                        writeError(
                                                                res,
                                                                json,
                                                                403,
                                                                "ACCESS_DENIED",
                                                                "Você não tem permissão ou sua"
                                                                    + " sessão expirou. Atualize a"
                                                                    + " página.")))
                .requestCache(c -> c.disable())
                .addFilterAfter(
                        new PasswordChangeRequiredFilter(users, json), AuthorizationFilter.class);
        return http.build();
    }

    private void writeError(
            HttpServletResponse res, ObjectMapper json, int status, String code, String message)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        json.writeValue(
                res.getOutputStream(), Map.of("status", status, "code", code, "message", message));
    }
}
