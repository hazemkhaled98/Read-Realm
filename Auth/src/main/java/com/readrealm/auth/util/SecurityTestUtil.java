package com.readrealm.auth.util;

import com.readrealm.auth.model.Role;
import com.readrealm.auth.model.SecurityPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

import static com.readrealm.auth.model.Role.ADMIN;
import static com.readrealm.auth.model.Role.CUSTOMER;

public class SecurityTestUtil {

    public static SecurityPrincipal mockAdmin() {
        return createMockUser(ADMIN);
    }

    public static SecurityPrincipal mockCustomer() {
        return createMockUser(CUSTOMER);
    }

    public static Jwt mockAdminJWT() {
        return createMockJWT(ADMIN.getValue());
    }

    public static Jwt mockCustomerJWT() {
        return createMockJWT(CUSTOMER.getValue());
    }

    private static SecurityPrincipal createMockUser(Role... roles) {
        return new SecurityPrincipal(
                "testUserId",
                "testUsername",
                Set.of(roles),
                "testEmail",
                "testFirstName",
                "testLastName"
        );
    }

    private static Jwt createMockJWT(String role) {

        return Jwt.withTokenValue("testJwt")
                .header("alg", "HS256")
                .header("typ", "JWT")
                .subject("testUserId")
                .claim("preferred_username", "testUsername")
                .claim("roles", formatRole(role))
                .claim("email", "testEmail")
                .claim("given_name", "testFirstName")
                .claim("family_name", "testLastName")
                .build();
    }


    private static String formatRole(String role){
        return new StringBuilder()
                .append("[")
                .append(role)
                .append("]")
                .toString();
    }
}
