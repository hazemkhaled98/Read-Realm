package com.readrealm.auth.util;

import com.readrealm.auth.model.Role;
import com.readrealm.auth.model.SecurityPrincipal;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

import static com.readrealm.auth.model.Role.ADMIN;
import static com.readrealm.auth.model.Role.CUSTOMER;


public class MockAuthorizationUtil {

    private MockAuthorizationUtil() {}

    public static void mockAdminAuthorization() {
        SecurityPrincipal mockAdmin = mockAdmin();
        mockAuthorization(mockAdmin);
    }

    public static void mockCustomerAuthorization() {
        SecurityPrincipal mockCustomer = mockCustomer();
        mockAuthorization(mockCustomer);
    }

    public static Jwt mockAdminJWT() {
        return createMockJWT(ADMIN.getValue());
    }

    public static Jwt mockCustomerJWT() {
        return createMockJWT(CUSTOMER.getValue());
    }

    private static void mockAuthorization(SecurityPrincipal mockPrincipal) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(authentication.getDetails()).thenReturn(mockPrincipal);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private static SecurityPrincipal mockAdmin() {
        return createMockUser(ADMIN);
    }

    private static SecurityPrincipal mockCustomer() {
        return createMockUser(CUSTOMER);
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
