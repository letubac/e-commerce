package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandDTO {
    private Long id;

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String name;

    private String slug;
    private String description;
    private String logoUrl;
    private String imageUrl; // For compatibility with service
    private String websiteUrl;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Integer productCount; // Number of products in this brand

    // Custom constructor
    public BrandDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
}