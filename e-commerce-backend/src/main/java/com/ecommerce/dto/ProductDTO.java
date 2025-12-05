package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal salePrice;
    private BigDecimal costPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private Integer lowStockThreshold;
    private BigDecimal weight;
    private String dimensions;
    private Long categoryId;
    private Long brandId;
    private boolean isActive;
    private boolean isFeatured;
    private String status;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private Date createdAt;
    private Date updatedAt;
    
    private List<ProductImageDTO> productImages;

    // Custom constructor for basic product info
    public ProductDTO(String name, String sku, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

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
