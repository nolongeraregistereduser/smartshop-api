package com.smartshop.api.enums;

public enum PaymentMethod {
    ESPECES("Espèces", 20000.0, true),
    CHEQUE("Chèque", null, false),
    VIREMENT("Virement", null, false);

    private final String displayName;
    private final Double maxAmount;
    private final boolean isImmediate;

    PaymentMethod(String displayName, Double maxAmount, boolean isImmediate) {
        this.displayName = displayName;
        this.maxAmount = maxAmount;
        this.isImmediate = isImmediate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public boolean hasMaxAmount() {
        return maxAmount != null;
    }

    public boolean isImmediate() {
        return isImmediate;
    }

    public boolean isValidAmount(double amount) {
        if (hasMaxAmount()) {
            return amount > 0 && amount <= maxAmount;
        }
        return amount > 0;
    }
}

