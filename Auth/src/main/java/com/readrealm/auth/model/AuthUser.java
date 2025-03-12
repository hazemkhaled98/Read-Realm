package com.readrealm.auth.model;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public record AuthUser(
        String userId,
        String username,
        List<Role> roles,
        String email,
        String firstName,
        String lastName
) {

    public AuthUser(Jwt jwt){
        this(jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsStringList("roles").stream().map(Role::fromValue).toList(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));
    }

}
