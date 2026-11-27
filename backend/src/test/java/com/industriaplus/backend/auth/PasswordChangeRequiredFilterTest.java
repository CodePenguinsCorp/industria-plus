package com.industriaplus.backend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Bloqueio da API enquanto a senha do usuário for provisória. */
class PasswordChangeRequiredFilterTest {
    private UserRepository users;
    private PasswordChangeRequiredFilter filter;
    private FilterChain chain;
    private HttpServletResponse response;
    private ByteArrayOutputStream body;

    @BeforeEach
    void setUp() throws Exception {
        users = mock(UserRepository.class);
        filter = new PasswordChangeRequiredFilter(users, new ObjectMapper());
        chain = mock(FilterChain.class);
        response = mock(HttpServletResponse.class);
        body = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new CapturingStream(body));
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBlockApiUntilTemporaryPasswordIsChanged() throws Exception {
        login("ana@test.local", true);
        HttpServletRequest request = request("/api/equipments");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(403);
        verify(chain, never()).doFilter(request, response);
        String json = body.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"code\":\"PASSWORD_CHANGE_REQUIRED\""), json);
    }

    @Test
    void shouldAllowEndpointsNeededToChangePasswordOrLeave() throws Exception {
        login("ana@test.local", true);
        for (String path : new String[] {"/api/auth/password", "/api/auth/me", "/api/auth/logout"}) {
            HttpServletRequest request = request(path);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
        verify(response, never()).setStatus(403);
    }

    @Test
    void shouldLetUsersWithDefinitivePasswordThrough() throws Exception {
        login("gestor@test.local", false);
        HttpServletRequest request = request("/api/equipments");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreRequestsWithoutAuthentication() throws Exception {
        HttpServletRequest request = request("/api/equipments");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(users);
    }

    @Test
    void shouldResolvePathWithoutContextPath() {
        assertEquals("/auth/password", PasswordChangeRequiredFilter.path(request("/api/auth/password")));
        HttpServletRequest rootContext = mock(HttpServletRequest.class);
        when(rootContext.getRequestURI()).thenReturn("/auth/me");
        when(rootContext.getContextPath()).thenReturn("");
        assertEquals("/auth/me", PasswordChangeRequiredFilter.path(rootContext));
    }

    private void login(String email, boolean mustChangePassword) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(email, null, "ROLE_TECHNICIAN"));
        when(users.findMustChangePasswordByEmail(email)).thenReturn(Optional.of(mustChangePassword));
    }

    private static HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("/api");
        return request;
    }

    private static final class CapturingStream extends ServletOutputStream {
        private final ByteArrayOutputStream target;

        CapturingStream(ByteArrayOutputStream target) {
            this.target = target;
        }

        @Override
        public void write(int value) {
            target.write(value);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {}
    }
}
