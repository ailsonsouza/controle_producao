package com.ciamanutencao.production.enums;

public enum OrderStatus {
    OPEN("Aberta"),
    FINISHED("Finalizada"),
    CANCELED("Cancelada"),
    ON_HOLD("Suspensa");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
