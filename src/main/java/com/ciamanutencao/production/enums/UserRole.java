package com.ciamanutencao.production.enums;

public enum UserRole {
    ROLE_ADMIN(1),
    ROLE_CHIEF(2),
    ROLE_USER(3);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole valueOf(int code) {
        for (UserRole value : UserRole.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid userRole code");
    }

}
