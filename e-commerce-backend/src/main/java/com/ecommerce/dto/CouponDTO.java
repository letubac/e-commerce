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
public class CouponDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private Boolean isActive;
    private Date startDate;
    private Date endDate;
    private Long createdBy;
    private Date createdAt;
    private Date updatedAt;
}
