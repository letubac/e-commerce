package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.ProductImageDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;

@Component
/**
 * author: LeTuBac
 */
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setShortDescription(product.getShortDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setLowStockThreshold(product.getLowStockThreshold());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setCategoryId(product.getCategoryId());
        dto.setBrandId(product.getBrandId());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());
        dto.setStatus(product.getStatus());
        dto.setMetaTitle(product.getMetaTitle());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setSlug(product.getSlug());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setShortDescription(dto.getShortDescription());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setCostPrice(dto.getCostPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setLowStockThreshold(dto.getLowStockThreshold());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());
        product.setStatus(dto.getStatus());
        product.setMetaTitle(dto.getMetaTitle());
        product.setMetaDescription(dto.getMetaDescription());
        product.setSlug(dto.getSlug());
        product.setCreatedAt(dto.getCreatedAt());
        product.setUpdatedAt(dto.getUpdatedAt());

        return product;
    }
    
    public ProductImage toEntityProductImage(ProductImageDTO dto) {
        if (dto == null) {
            return null;
        }
        ProductImage product = new ProductImage();
        product.setId(dto.getId());
        product.setProductId(dto.getProductId());
        product.setImageUrl(dto.getImageUrl());
        product.setAltText(dto.getAltText());
        product.setPrimary(dto.isPrimary());
        product.setSortOrder(dto.getSortOrder());
        product.setCreatedAt(dto.getCreatedAt());
        
        return product;
    }
    
    public ProductImageDTO toDtoProductImage(ProductImage dto) {
        if (dto == null) {
            return null;
        }
        ProductImageDTO product = new ProductImageDTO();
        product.setId(dto.getId());
        product.setProductId(dto.getProductId());
        product.setImageUrl(dto.getImageUrl());
        product.setAltText(dto.getAltText());
        product.setPrimary(dto.isPrimary());
        product.setSortOrder(dto.getSortOrder());
        product.setCreatedAt(dto.getCreatedAt());
        
        return product;
    }
}