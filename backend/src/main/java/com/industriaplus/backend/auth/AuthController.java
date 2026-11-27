package com.industriaplus.backend.auth;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AccessService access;
    private final UserService users;

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(access.current());
    }

    @PostMapping("/password")
    public UserResponse changePassword(@Valid @RequestBody PasswordChangeRequest input,
            HttpServletRequest request, HttpServletResponse response) {
        UserResponse result = users.changeOwnPassword(access.actor(), input);
        SessionUser principal = new SessionUser(access.current());
        principal.eraseCredentials();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        request.changeSessionId();
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        return result;
    }
}
