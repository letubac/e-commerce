package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface CartItemRepository extends DbRepository<CartItem, Long> {

    // Maps to: cartItemRepository_findById.sql
    CartItem findById(@Param("id") Long id);

    // Maps to: cartItemRepository_findByCartId.sql
    List<CartItem> findByCartId(@Param("cartId") Long cartId);

    // Maps to: cartItemRepository_findByCartIdAndProductId.sql
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // Maps to: cartItemRepository_deleteByCartId.sql
    @Modifying
    void deleteByCartId(@Param("cartId") Long cartId);

    // Maps to: cartItemRepository_deleteByCartIdAndProductId.sql
    @Modifying
    void deleteByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // Maps to: cartItemRepository_updateCartItem.sql
    @Modifying
    void updateCartItem(@Param("id") Long id, @Param("quantity") Integer quantity,
            @Param("updatedAt") java.util.Date updatedAt);

    // Maps to: cartItemRepository_existsByCartIdAndProductId.sql
    boolean existsByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // JPA standard method for finding by Cart and Product entities
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}