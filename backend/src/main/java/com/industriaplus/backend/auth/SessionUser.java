package com.industriaplus.backend.auth;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/** Identidade e credencial do momento do login, mesmo após o Spring apagar a senha do principal. */
public final class SessionUser extends User {
    private final Long userId;
    private final String credentialHash;
    private final Role role;

    public SessionUser(AppUser user) {
        super(user.getEmail(), user.getPasswordHash(),
            AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name()));
        userId = user.getId();
        credentialHash = user.getPasswordHash();
        role = user.getRole();
    }

    public boolean matches(AppUser user) {
        return userId.equals(user.getId())
            && credentialHash.equals(user.getPasswordHash())
            && role == user.getRole();
    }
}
