package com.smartshop.api.enums;

public enum CustomerTier {
    BASIC(0, 0.0),
    SILVER(500, 0.05),
    GOLD(800, 0.10),
    PLATINUM(1200, 0.15);

    private final double minimumOrderAmount;
    private final double discountRate;

    CustomerTier(double minimumOrderAmount, double discountRate) {
        this.minimumOrderAmount = minimumOrderAmount;
        this.discountRate = discountRate;
    }

    public double getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public boolean isEligibleForDiscount(double orderSubtotal) {
        return orderSubtotal >= minimumOrderAmount;
    }
}

