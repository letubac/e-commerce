package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Cart;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface CartRepository extends DbRepository<Cart, Long> {

    // Maps to: cartRepository_findByUserId.sql
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    // Maps to: cartRepository_findByUserIdWithItems.sql
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    // Maps to: cartRepository_existsByUserId.sql
    boolean existsByUserId(@Param("userId") Long userId);

    // Maps to: cartRepository_deleteByUserId.sql
    @Modifying
    @Transactional
    void deleteByUserId(@Param("userId") Long userId);

	Optional<Cart> findById(@Param("id") Long id);
}