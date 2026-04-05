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
@Table(name = TableConstant.CART_ITEMS)
/**
 * author: LeTuBac
 */
public class CartItem {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.CART_ITEMS)
    @Column(name = "id")
    private Long id;

    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public BigDecimal getSubTotal() {
        if (quantity == null || price == null)
            return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}