package com.readrealm.catalog.util;

import com.readrealm.auth.model.SecurityPrincipal;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.readrealm.auth.util.SecurityTestUtil.mockAdmin;
import static com.readrealm.auth.util.SecurityTestUtil.mockCustomer;


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

    private static void mockAuthorization(SecurityPrincipal mockPrincipal) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(authentication.getDetails()).thenReturn(mockPrincipal);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
