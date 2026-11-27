package com.industriaplus.backend.auth;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Usuário do sistema. Técnicos são usuários com {@link Role#TECHNICIAN}; não existe mais uma
 * tabela própria para técnicos. Um técnico pode estar vinculado a um gestor ({@link
 * Role#MANAGER}).
 */
@Entity
@Table(name = "app_users")
@Getter
@NoArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 120)
    private String specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private AppUser manager;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AppUser(
            String name,
            String email,
            String passwordHash,
            Role role,
            String specialty,
            AppUser manager,
            boolean mustChangePassword) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.specialty = specialty;
        this.manager = manager;
        this.mustChangePassword = mustChangePassword;
    }

    public void changePassword(String passwordHash, boolean mustChangePassword) {
        this.passwordHash = passwordHash;
        this.mustChangePassword = mustChangePassword;
    }

    public void assignManager(AppUser manager) {
        this.manager = manager;
    }

    public boolean isTechnician() {
        return role == Role.TECHNICIAN;
    }
}
