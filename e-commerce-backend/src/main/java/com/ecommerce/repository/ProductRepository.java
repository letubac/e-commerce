package com.ecommerce.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
public interface ProductRepository extends DbRepository<Product, Long> {

	List<Product> findAllData();

	Optional<Product> findById(@Param("id") Long id);

	Optional<Product> findBySku(@Param("sku") String sku);

	Optional<Product> findBySlug(@Param("slug") String slug);

	List<Product> findActive();

	List<Product> findFeatured();

	List<Product> findByCategory(@Param("categoryId") Long categoryId);

	List<Product> findByBrand(@Param("brandId") Long brandId);

	List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

	List<Product> findLowStock(@Param("threshold") Integer threshold);

	List<Product> searchByName(@Param("name") String name);

	List<Product> findWithPagination(@Param("limit") Integer limit, @Param("offset") Integer offset);

	Boolean existsBySku(@Param("sku") String sku);

	Boolean existsBySlug(@Param("slug") String slug);

	Long countActive();

	Long countByCategory(@Param("categoryId") Long categoryId);

	Long countByBrand(@Param("brandId") Long brandId);

	// Additional methods for dashboard statistics
	// count() is already provided by JpaRepository

	Long countByActiveTrue();

	Long countLowStockProducts(@Param("threshold") Integer threshold);

	Long countByStockQuantity(@Param("stockQuantity") Integer stockQuantity);

	BigDecimal getAveragePrice();

	BigDecimal getTotalInventoryValue();

	Long countByCategory(@Param("category") String category);

	@Modifying
	@Transactional
	Integer insertProduct(@Param("name") String name, @Param("description") String description,
			@Param("shortDescription") String shortDescription, @Param("sku") String sku,
			@Param("price") BigDecimal price, @Param("salePrice") BigDecimal salePrice,
			@Param("costPrice") BigDecimal costPrice, @Param("stockQuantity") Integer stockQuantity,
			@Param("lowStockThreshold") Integer lowStockThreshold, @Param("weight") BigDecimal weight,
			@Param("dimensions") String dimensions, @Param("categoryId") Long categoryId,
			@Param("brandId") Long brandId, @Param("isActive") Boolean isActive,
			@Param("isFeatured") Boolean isFeatured, @Param("status") String status,
			@Param("metaTitle") String metaTitle, @Param("metaDescription") String metaDescription,
			@Param("slug") String slug, @Param("createdAt") Date createdAt, @Param("updatedAt") Date updatedAt);

	@Modifying
	@Transactional
	Integer updateProduct(@Param("id") Long id, @Param("name") String name, @Param("description") String description,
			@Param("price") BigDecimal price, @Param("salePrice") BigDecimal salePrice,
			@Param("stockQuantity") Integer stockQuantity, @Param("categoryId") Long categoryId,
			@Param("brandId") Long brandId, @Param("weight") BigDecimal weight, @Param("dimensions") String dimensions,
			@Param("isActive") Boolean isActive, @Param("isFeatured") Boolean isFeatured,
			@Param("status") String status, @Param("metaTitle") String metaTitle,
			@Param("metaDescription") String metaDescription, @Param("updatedAt") Date updatedAt);

	@Modifying
	@Transactional
	Integer updateStock(@Param("id") Long id, @Param("stockQuantity") Integer stockQuantity,
			@Param("updatedAt") Date updatedAt);

	@Modifying
	@Transactional
	Integer updatePrice(@Param("id") Long id, @Param("price") BigDecimal price, @Param("updatedAt") Date updatedAt);

	@Modifying
	@Transactional
	Integer updateSalePrice(@Param("id") Long id, @Param("salePrice") BigDecimal salePrice,
			@Param("updatedAt") Date updatedAt);

	@Modifying
	@Transactional
	Integer hardDelete(@Param("id") Long id);
}
