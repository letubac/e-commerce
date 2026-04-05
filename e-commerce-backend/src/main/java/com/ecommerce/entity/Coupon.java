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
@Table(name = TableConstant.COUPONS)
/**
 * author: LeTuBac
 */
public class Coupon {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.COUPONS)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_type")
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount; // Max discount for percentage coupons

    @Column(name = "usage_limit")
    private Integer usageLimit; // Total usage limit

    @Column(name = "used_count")
    private Integer usedCount;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isValid() {
        if (!isActive)
            return false;

        Date now = new Date();
        if (now.before(startDate) || now.after(endDate))
            return false;

        if (usageLimit != null && usedCount >= usageLimit)
            return false;

        return true;
    }

    public boolean canBeUsedForOrder(BigDecimal orderAmount) {
        if (!isValid())
            return false;
        return minOrderAmount == null || orderAmount.compareTo(minOrderAmount) >= 0;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!canBeUsedForOrder(orderAmount))
            return BigDecimal.ZERO;

        if ("PERCENTAGE".equals(discountType)) {
            BigDecimal discount = orderAmount.multiply(discountValue).divide(new BigDecimal("100"));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                return maxDiscountAmount;
            }
            return discount;
        } else if ("FIXED_AMOUNT".equals(discountType)) {
            return discountValue;
        } else if ("FREE_SHIPPING".equals(discountType)) {
            return BigDecimal.ZERO; // Shipping cost discount handled separately
        }

        return BigDecimal.ZERO;
    }
}
