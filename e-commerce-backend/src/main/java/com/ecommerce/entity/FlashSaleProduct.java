package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.FLASH_SALE_PRODUCTS)
/**
 * author: LeTuBac
 */
public class FlashSaleProduct {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.FLASH_SALE_PRODUCTS)
    @Column(name = "id")
    private Long id;

    @Column(name = "flash_sale_id")
    private Long flashSaleId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "flash_price")
    private BigDecimal flashPrice;

    @Column(name = "stock_limit")
    private Integer stockLimit;

    @Column(name = "stock_sold")
    private Integer stockSold;

    @Column(name = "max_per_customer")
    private Integer maxPerCustomer;

    @Column(name = "sort_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private Date createdAt;

    // Business methods
    public BigDecimal getDiscountAmount() {
        return originalPrice.subtract(flashPrice);
    }

    public BigDecimal getDiscountPercentage() {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return getDiscountAmount()
                .divide(originalPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public Integer getRemainingStock() {
        return stockLimit - (stockSold != null ? stockSold : 0);
    }

    public boolean isSoldOut() {
        return getRemainingStock() <= 0;
    }

    public boolean canPurchase(int requestedQuantity) {
        if (!isActive || isSoldOut())
            return false;
        if (maxPerCustomer != null && requestedQuantity > maxPerCustomer)
            return false;
        return requestedQuantity <= getRemainingStock();
    }
}
