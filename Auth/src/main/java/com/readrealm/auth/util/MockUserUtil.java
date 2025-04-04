package com.readrealm.auth.util;

import com.readrealm.auth.model.Role;
import com.readrealm.auth.model.SecurityPrincipal;

import java.util.Set;

import static com.readrealm.auth.model.Role.ADMIN;
import static com.readrealm.auth.model.Role.CUSTOMER;

public class MockUserUtil {

    public static SecurityPrincipal mockAdmin(){
        return mockUser(ADMIN);
    }

    public static SecurityPrincipal mockCustomer(){
        return mockUser(CUSTOMER);
    }

    private static SecurityPrincipal mockUser(Role... roles){
        return new SecurityPrincipal(
                "testUserId",
                "testUsername",
                Set.of(roles),
                "testEmail",
                "testFirstName",
                "testLastName"
        );
    }
}
