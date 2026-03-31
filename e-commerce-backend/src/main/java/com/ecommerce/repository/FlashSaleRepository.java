package com.ecommerce.repository;

import com.ecommerce.entity.FlashSale;
import com.ecommerce.repository.base.DbRepository;
import vn.com.unit.springframework.data.mirage.repository.query.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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
}