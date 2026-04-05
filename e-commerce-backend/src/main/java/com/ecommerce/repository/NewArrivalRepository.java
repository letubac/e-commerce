package com.ecommerce.repository;

import com.ecommerce.entity.NewArrival;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * author: LeTuBac
 */
public interface NewArrivalRepository extends DbRepository<NewArrival, Long> {

    // Maps to: newArrivalRepository_findActive.sql
    List<NewArrival> findActive();

    // Maps to: newArrivalRepository_findByProductId.sql
    NewArrival findByProductId(@Param("productId") Long productId);

    // Maps to: newArrivalRepository_findOrderedByDisplayOrder.sql
    List<NewArrival> findOrderedByDisplayOrder();

    // Maps to: newArrivalRepository_updateDisplayOrder.sql
    @Modifying
    int updateDisplayOrder(@Param("id") Long id, @Param("displayOrder") Integer displayOrder);
}