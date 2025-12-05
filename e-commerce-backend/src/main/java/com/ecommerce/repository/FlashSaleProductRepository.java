package com.ecommerce.repository;

import com.ecommerce.entity.FlashSaleProduct;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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
}