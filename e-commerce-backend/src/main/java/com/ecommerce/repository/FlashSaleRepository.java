package com.ecommerce.repository;

import com.ecommerce.entity.FlashSale;
import com.ecommerce.repository.base.DbRepository;
import vn.com.unit.springframework.data.mirage.repository.query.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
/**
 * author: LeTuBac
 */
public interface FlashSaleRepository extends DbRepository<FlashSale, Long> {

    // Maps to: FlashSaleRepository_findAllFlashSales.sql
    List<FlashSale> findAllFlashSales();

    // Maps to: flashSaleRepository_findActive.sql
    List<FlashSale> findActive();

    // Maps to: flashSaleRepository_findCurrent.sql
    List<FlashSale> findCurrent();

    // Maps to: flashSaleRepository_findUpcoming.sql
    List<FlashSale> findUpcoming();

    // Maps to: flashSaleRepository_updateStatus.sql
    @Modifying
    void updateStatus(@Param("id") Long id, @Param("isActive") boolean isActive);

    // Maps to: FlashSaleRepository_insertFlashSale.sql
    @Modifying
    Long insertFlashSale(
            @Param("name") String name,
            @Param("description") String description,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("isActive") boolean isActive,
            @Param("bannerImageUrl") String bannerImageUrl,
            @Param("backgroundColor") String backgroundColor,
            @Param("createdAt") Date createdAt,
            @Param("updatedAt") Date updatedAt);

    // Maps to: FlashSaleRepository_updateFlashSale.sql
    @Modifying
    void updateFlashSale(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("isActive") boolean isActive,
            @Param("bannerImageUrl") String bannerImageUrl,
            @Param("backgroundColor") String backgroundColor,
            @Param("updatedAt") Date updatedAt);

    // Maps to: FlashSaleRepository_findOverlapping.sql
    List<FlashSale> findOverlapping(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("excludeId") Long excludeId);

    // Maps to: FlashSaleRepository_findExpiredActive.sql
    List<FlashSale> findExpiredActive();

    // Maps to: FlashSaleRepository_findToActivate.sql
    List<FlashSale> findToActivate();
}