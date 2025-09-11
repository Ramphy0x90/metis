package com.r16a.metis.identity.models;

import lombok.Getter;

@Getter
public enum UserRole {
    GLOBAL_ADMIN("Global Admin"),
    ADMIN("Admin"),
    EMPLOYEE("Employee"),
    USER("User");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getSpringSecurityRole() {
        return "ROLE_" + this.name();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
