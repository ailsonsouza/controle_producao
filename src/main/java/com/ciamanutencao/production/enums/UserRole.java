package com.ciamanutencao.production.enums;

public enum UserRole {
    ADMIN (1),
    TECHNICIAN (2), // O "Técnico" da manutenção
    REQUESTER (3);  // O "Requerente" que abre a OS

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    public static UserRole valueOf(int code){
        for (UserRole value : UserRole.values()){
            if(value.getCode() == code){
                return value;
            }
        } throw new IllegalArgumentException("Invalid userRole code");
    }




}
