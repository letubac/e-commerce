package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.PAYMENTS)
/**
 * author: LeTuBac
 */
public class Payment {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.PAYMENTS)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "method")
    private String method; // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, VNPAY, MOMO, CASH_ON_DELIVERY, STRIPE

    @Column(name = "status")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED

    @Column(name = "transaction_id")
    private String transactionId; // External payment processor transaction ID

    @Column(name = "gateway_response")
    private String gatewayResponse; // Raw response from payment gateway - stored as JSON string

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private Date refundedAt;

    @Column(name = "paid_at")
    private Date paidAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
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