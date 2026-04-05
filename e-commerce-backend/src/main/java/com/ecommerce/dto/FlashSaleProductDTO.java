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
/**
 * author: LeTuBac
 */
public class FlashSaleProductDTO {
    private Long id;
    private Long flashSaleId;
    private String flashSaleName;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String productSku;
    private BigDecimal originalPrice;
    private BigDecimal flashPrice;
    private Integer stockLimit;
    private Integer stockSold;
    private Integer maxPerCustomer;
    private Integer displayOrder;
    private boolean isActive;
    private Date createdAt;

    // Additional fields for display
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private Integer remainingStock;
    private boolean soldOut;
    private boolean canPurchase;
    private String productDescription;
    private String brandName;
    private String categoryName;
}
