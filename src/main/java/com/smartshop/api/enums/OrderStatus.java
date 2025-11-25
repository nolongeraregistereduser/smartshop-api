package com.smartshop.api.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELED,
    REJECTED;

    public boolean isFinal() {
        return this == CONFIRMED || this == CANCELED || this == REJECTED;
    }

    public boolean canBeCanceled() {
        return this == PENDING;
    }

    public boolean canBeConfirmed() {
        return this == PENDING;
    }
}

