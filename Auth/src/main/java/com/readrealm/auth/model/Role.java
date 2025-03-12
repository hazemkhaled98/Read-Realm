package com.readrealm.auth.model;

public enum Role {

    ADMIN("ROLE_ADMIN"),
    CUSTOMER("ROLE_CUSTOMER");



    private final String value;


    Role(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }


    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

}
