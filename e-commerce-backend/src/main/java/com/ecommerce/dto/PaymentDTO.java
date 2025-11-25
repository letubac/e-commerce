package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private String method; // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, VNPAY, MOMO, CASH_ON_DELIVERY, STRIPE
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    private String transactionId;
    private String gatewayResponse;
    private String failureReason;
    private BigDecimal refundAmount;
    private Date refundedAt;
    private Date paidAt;
    private Date createdAt;
    private Date updatedAt;

    // Business methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isRefunded() {
        return "REFUNDED".equals(status);
    }

    public BigDecimal getNetAmount() {
        if (refundAmount == null)
            return amount;
        return amount.subtract(refundAmount);
    }
}
