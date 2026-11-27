package com.industriaplus.backend.auth;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest input) {
        return service.create(input);
    }

    @PostMapping("/{id}/password-reset")
    public UserResponse resetPassword(
            @PathVariable Long id, @Valid @RequestBody PasswordResetRequest input) {
        return service.resetPassword(id, input);
    }
}
