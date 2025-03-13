package com.readrealm.auth.authorizer;


import com.readrealm.auth.model.SecurityPrincipal;
import com.readrealm.auth.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.readrealm.auth.model.Role.ADMIN;
import static com.readrealm.auth.model.Role.CUSTOMER;

@Component
public class Authorizer {

    private static final Logger log = LoggerFactory.getLogger(Authorizer.class);

    public boolean isAuthorizedUser(String userId){
        SecurityPrincipal securityPrincipal = SecurityUtil.getSecurityPrincipal();
        logSecurityPrincipal(securityPrincipal);
        return isAdmin() || securityPrincipal.userId().equals(userId) && securityPrincipal.roles().contains(CUSTOMER);
    }

    public boolean isAdmin(){
        SecurityPrincipal securityPrincipal = SecurityUtil.getSecurityPrincipal();
        logSecurityPrincipal(securityPrincipal);
        return securityPrincipal.roles().contains(ADMIN);
    }

    public boolean isCustomer(){
        SecurityPrincipal securityPrincipal = SecurityUtil.getSecurityPrincipal();
        logSecurityPrincipal(securityPrincipal);
        return securityPrincipal.roles().contains(CUSTOMER);
    }

    private static void logSecurityPrincipal(SecurityPrincipal securityPrincipal) {
        log.info("Security Principal: {}", securityPrincipal);
    }
}
