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
@Table(name = TableConstant.ORDERS)
/**
 * author: LeTuBac
 */
public class Order {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.ORDERS)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_email")
    private String guestEmail; // For guest checkout

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "tax")
    private BigDecimal tax;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED

    @Column(name = "payment_status")
    private String paymentStatus; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "shipped_at")
    private Date shippedAt;

    @Column(name = "delivered_at")
    private Date deliveredAt;

    @Column(name = "cancelled_at")
    private Date cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "billing_address_id")
    private Long billingAddressId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCompleted() {
        return "DELIVERED".equals(status);
    }

    public boolean isPaid() {
        return "COMPLETED".equals(paymentStatus);
    }

    public BigDecimal getFinalTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }
}
