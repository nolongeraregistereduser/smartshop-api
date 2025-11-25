package com.smartshop.api.enums;

public enum PaymentStatus {
    EN_ATTENTE("En attente"),
    ENCAISSE("Encaissé"),
    REJETE("Rejeté");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

