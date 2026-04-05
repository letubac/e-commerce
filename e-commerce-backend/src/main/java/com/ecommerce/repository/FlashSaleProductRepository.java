package com.ecommerce.repository;

import com.ecommerce.entity.FlashSaleProduct;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
/**
 * author: LeTuBac
 */
public interface FlashSaleProductRepository extends DbRepository<FlashSaleProduct, Long> {

    // Maps to: flashSaleProductRepository_findByFlashSaleId.sql
    List<FlashSaleProduct> findByFlashSaleId(@Param("flashSaleId") Long flashSaleId);

    // Maps to: flashSaleProductRepository_findActiveByFlashSaleId.sql
    List<FlashSaleProduct> findActiveByFlashSaleId(@Param("flashSaleId") Long flashSaleId);

    // Maps to: flashSaleProductRepository_findByProductId.sql
    FlashSaleProduct findByProductId(@Param("productId") Long productId);

    // Maps to: flashSaleProductRepository_updateSoldQuantity.sql
    @Modifying
    int updateSoldQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    // Maps to: FlashSaleProductRepository_insertFlashSaleProduct.sql
    @Modifying
    Long insertFlashSaleProduct(
            @Param("flashSaleId") Long flashSaleId,
            @Param("productId") Long productId,
            @Param("originalPrice") BigDecimal originalPrice,
            @Param("flashPrice") BigDecimal flashPrice,
            @Param("stockLimit") Integer stockLimit,
            @Param("stockSold") Integer stockSold,
            @Param("maxPerCustomer") Integer maxPerCustomer,
            @Param("displayOrder") Integer displayOrder,
            @Param("isActive") boolean isActive,
            @Param("createdAt") Date createdAt);

    // Maps to: FlashSaleProductRepository_updateFlashSaleProduct.sql
    @Modifying
    void updateFlashSaleProduct(
            @Param("id") Long id,
            @Param("originalPrice") BigDecimal originalPrice,
            @Param("flashPrice") BigDecimal flashPrice,
            @Param("stockLimit") Integer stockLimit,
            @Param("maxPerCustomer") Integer maxPerCustomer,
            @Param("displayOrder") Integer displayOrder,
            @Param("isActive") boolean isActive);

    // Maps to: FlashSaleProductRepository_updateActiveStatus.sql
    @Modifying
    void updateActiveStatus(@Param("id") Long id, @Param("isActive") boolean isActive);

    // Maps to: FlashSaleProductRepository_findSoldOutByFlashSaleId.sql
    List<FlashSaleProduct> findSoldOutByFlashSaleId(@Param("flashSaleId") Long flashSaleId);
}