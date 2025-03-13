package com.readrealm.auth.util;


import com.readrealm.auth.model.SecurityPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtil {

    private SecurityUtil() {
    }

    public static SecurityPrincipal getSecurityPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getDetails() instanceof SecurityPrincipal)){
            throw new AuthenticationCredentialsNotFoundException("No valid authentication found in the context");
        }
        return (SecurityPrincipal) authentication.getDetails();
    }


    public static String getCurrentUserId(){
        return getSecurityPrincipal().userId();
    }
}
