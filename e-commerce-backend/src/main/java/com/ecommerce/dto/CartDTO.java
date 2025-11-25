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
public class CartDTO {
    private Long id;
    private Long userId;
    private String sessionId; // For guest users
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalPrice;
    private Integer itemCount;
    private Date createdAt;
    private Date updatedAt;
}
