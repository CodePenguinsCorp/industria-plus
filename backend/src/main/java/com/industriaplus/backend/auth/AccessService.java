package com.industriaplus.backend.auth;

import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final UserRepository users;

    public AppUser current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Entre no sistema para continuar.");
        return users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(
                        () ->
                                new BusinessException(
                                        HttpStatus.UNAUTHORIZED,
                                        "UNAUTHENTICATED",
                                        "Sessão inválida. Entre novamente."));
    }

    public void requireManager() {
        if (current().getRole() == Role.TECHNICIAN) forbidden();
    }

    public void requireAdmin() {
        if (current().getRole() != Role.ADMIN) forbidden();
    }

    /** Técnicos só atendem os chamados atribuídos a eles; gestores e administradores, todos. */
    public void requireAttendance(MaintenanceRequest request) {
        AppUser user = current();
        if (user.getRole() == Role.TECHNICIAN
                && (request.getTechnician() == null
                        || !Objects.equals(user.getId(), request.getTechnician().getId())))
            forbidden();
    }

    public String actor() {
        return current().getEmail();
    }

    private void forbidden() {
        throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Você não tem permissão para realizar esta operação.");
    }
}
