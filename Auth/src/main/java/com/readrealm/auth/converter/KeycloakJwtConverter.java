package com.readrealm.auth.converter;

import com.readrealm.auth.model.SecurityPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        SecurityPrincipal principal = new SecurityPrincipal(jwt);

        Collection<GrantedAuthority> authorities = principal.roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getValue()))
                .collect(Collectors.toSet());

        AbstractAuthenticationToken token =  new JwtAuthenticationToken(jwt, authorities);
        token.setDetails(principal);
        return token;
    }

}