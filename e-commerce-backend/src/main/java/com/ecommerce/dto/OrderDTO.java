package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String guestEmail; // For guest checkout
    private List<OrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String currency;
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    private String paymentStatus; // PENDING, COMPLETED, FAILED, REFUNDED
    private String shippingMethod;
    private String trackingNumber;
    private String notes;
    private Date shippedAt;
    private Date deliveredAt;
    private Date cancelledAt;
    private String cancellationReason;
    private Long shippingAddressId;
    private Long billingAddressId;
    private Date createdAt;
    private Date updatedAt;
}
