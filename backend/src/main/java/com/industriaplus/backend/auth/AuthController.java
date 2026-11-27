package com.industriaplus.backend.auth;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.csrf.CsrfToken;
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
    public UserResponse changePassword(@Valid @RequestBody PasswordChangeRequest input) {
        return users.changeOwnPassword(access.actor(), input);
    }
}
