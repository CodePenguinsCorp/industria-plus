package com.industriaplus.backend.auth;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    @EntityGraph(attributePaths = "manager")
    Optional<AppUser> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "manager")
    List<AppUser> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "manager")
    List<AppUser> findByRoleOrderByNameAsc(Role role);

    Optional<AppUser> findByIdAndRole(Long id, Role role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AppUser u where u.id = :id and u.role = :role")
    Optional<AppUser> findByIdAndRoleForUpdate(@Param("id") Long id, @Param("role") Role role);

    @Query(
            "select u.mustChangePassword from AppUser u where lower(u.email) = lower(:email)")
    Optional<Boolean> findMustChangePasswordByEmail(@Param("email") String email);
}
