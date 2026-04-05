package com.ecommerce.entity;

/**
 * author: LeTuBac
 */
public class OrderStatus {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PROCESSING = "PROCESSING";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURNED = "RETURNED";

    private OrderStatus() {
        // Utility class
    }
}