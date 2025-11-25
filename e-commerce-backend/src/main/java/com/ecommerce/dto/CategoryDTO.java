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
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    private String slug;
    private String description;
    private Long parentId;
    private String imageUrl;
    private boolean isActive;
    private Integer sortOrder;
    private String metaTitle;
    private String metaDescription;
    private Date createdAt;
    private Date updatedAt;
    private Integer productCount; // Number of products in this category

    // Custom constructor for basic category info
    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
