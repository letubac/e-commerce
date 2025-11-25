package com.ecommerce.entity;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    VNPAY("VNPay"),
    MOMO("MoMo"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    CREDIT_CARD("Thẻ tín dụng");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}