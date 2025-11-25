package com.ecommerce.entity;

public class PaymentStatus {
    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String REFUNDED = "REFUNDED";
    public static final String PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";

    private PaymentStatus() {
        // Utility class
    }
}