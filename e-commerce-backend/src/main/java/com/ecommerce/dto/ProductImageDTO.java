package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class ProductImageDTO {
    private Long id;
    private Long productId;
    private String imageUrl;
    private String altText;
    private boolean isPrimary;
    private Integer sortOrder;
    private Date createdAt;

    // Custom constructor
    public ProductImageDTO(Long productId, String imageUrl, boolean isPrimary) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
    }
}
