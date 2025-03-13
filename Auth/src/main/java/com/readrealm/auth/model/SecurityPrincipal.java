package com.readrealm.auth.model;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;
import java.util.stream.Collectors;

public record SecurityPrincipal(
        String userId,
        String username,
        Set<Role> roles,
        String email,
        String firstName,
        String lastName
) {

    public SecurityPrincipal(Jwt jwt){
        this(jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                extractRoles(jwt),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));
    }

    private static Set<Role> extractRoles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream()
                .map(Role::fromValue)
                .collect(Collectors.toSet());
    }

}
