package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class CreateOrderRequest {
    @Valid
    @NotNull(message = "Customer info is required")
    private CustomerInfo customerInfo;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // COD, CARD, BANK_TRANSFER, E_WALLET

    @NotBlank(message = "Shipping method is required")
    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT

    @Valid
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;

    private BigDecimal shippingFee;

    // Inner class for customer info
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        @NotBlank(message = "Full name is required")
        private String fullName;

        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Address is required")
        private String address;

        private String city;
        private String district;
        private String ward;
        private String notes;
    }

    // Inner class for order item
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Price is required")
        private BigDecimal price;
    }
}