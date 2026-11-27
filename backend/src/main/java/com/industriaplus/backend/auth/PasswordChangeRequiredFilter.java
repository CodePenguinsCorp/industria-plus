package com.industriaplus.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Enquanto a senha do usuário for provisória, bloqueia todas as rotas da API exceto as
 * necessárias para trocar a senha ou sair. Instanciado pela {@link SecurityConfig} (não é um
 * bean para não ser registrado duas vezes como filtro do servlet).
 */
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    static final String CODE = "PASSWORD_CHANGE_REQUIRED";
    static final Set<String> ALLOWED =
            Set.of(
                    "/auth/csrf",
                    "/auth/me",
                    "/auth/password",
                    "/auth/logout",
                    "/health",
                    "/actuator/health",
                    "/error");

    private final UserRepository users;
    private final ObjectMapper json;

    public PasswordChangeRequiredFilter(UserRepository users, ObjectMapper json) {
        this.users = users;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            AppUser user = users.findByEmailIgnoreCase(auth.getName()).orElse(null);
            if (user == null || !(auth.getPrincipal() instanceof SessionUser principal)
                    || !principal.matches(user)) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                json.writeValue(response.getOutputStream(), Map.of(
                    "status", 401, "code", "UNAUTHENTICATED",
                    "message", "Sessão inválida. Entre novamente."));
                return;
            }
            if (!ALLOWED.contains(path(request)) && user.isMustChangePassword()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                json.writeValue(
                        response.getOutputStream(),
                        Map.of(
                                "status",
                                403,
                                "code",
                                CODE,
                                "message",
                                "Defina uma nova senha para continuar usando o sistema."));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context != null && !context.isEmpty() && uri.startsWith(context)
                ? uri.substring(context.length())
                : uri;
    }
}
