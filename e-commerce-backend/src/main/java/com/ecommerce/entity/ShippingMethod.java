package com.ecommerce.entity;

/**
 * author: LeTuBac
 */
public enum ShippingMethod {
    STANDARD("Giao hàng tiêu chuẩn", 0.0),
    EXPRESS("Giao hàng nhanh", 50000.0),
    SAME_DAY("Giao hàng trong ngày", 100000.0);

    private final String displayName;
    private final Double fee;

    ShippingMethod(String displayName, Double fee) {
        this.displayName = displayName;
        this.fee = fee;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getFee() {
        return fee;
    }
}