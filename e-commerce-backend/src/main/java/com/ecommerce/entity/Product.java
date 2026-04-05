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
@Table(name = TableConstant.PRODUCTS)
/**
 * author: LeTuBac
 */
public class Product {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.PRODUCTS)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "sku")
    private String sku;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_featured")
    private boolean isFeatured;

    @Column(name = "status")
    private String status;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "slug")
    private String slug;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
    
    @Column(name = "warranty_months")
    private Integer warrantyMonths;
    
    @Column(name = "compare_at_price")
    private BigDecimal compareAtPrice;
    
    @Column(name = "min_order_quantity")
    private Integer minOrderQuantity;
    
    @Column(name = "max_order_quantity")
    private Integer maxOrderQuantity;
    
    @Column(name = "tags")
    private String tags;
    
    @Column(name = "is_digital")
    private boolean isDigital;
    
    @Column(name = "view_count")
    private Integer viewCount;
    
    @Column(name = "purchase_count")
    private Integer purchaseCount;
    
    @Column(name = "featured_until")
    private Date featuredUntil;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "category_name")
    private String categoryName;

    // Business methods
    public BigDecimal getEffectivePrice() {
        return salePrice != null ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(price) < 0;
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null && stockQuantity <= lowStockThreshold;
    }
}
