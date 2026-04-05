package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * author: LeTuBac
 */
public interface OrderItemRepository extends DbRepository<OrderItem, Long> {

    /**
     * Find order items by order ID
     * Maps to: orderItemRepository_findByOrderId.sql
     */
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find order item by ID
     * Maps to: orderItemRepository_findById.sql
     */
    Optional<OrderItem> findById(@Param("id") Long id);

    /**
     * Insert a single order item
     * Maps to: orderItemRepository_insertOrderItem.sql
     */
    @Modifying
    void insertOrderItem(@Param("orderItem") OrderItem orderItem);

    /**
     * Insert multiple order items (batch)
     * Maps to: orderItemRepository_insertBatch.sql
     */
    @Modifying
    void insertOrderItems(@Param("orderItems") List<OrderItem> orderItems);

    /**
     * Update an order item
     * Maps to: orderItemRepository_updateOrderItem.sql
     */
    @Modifying
    void updateOrderItem(@Param("orderItem") OrderItem orderItem);

    /**
     * Delete order item by ID
     * Maps to: orderItemRepository_deleteById.sql
     */
    @Modifying
    void deleteById(@Param("id") Long id);

    /**
     * Delete all order items by order ID
     * Maps to: orderItemRepository_deleteByOrderId.sql
     */
    @Modifying
    void deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * Count order items by order ID
     * Maps to: orderItemRepository_countByOrderId.sql
     */
    int countByOrderId(@Param("orderId") Long orderId);

    /**
     * Find order items by product ID
     * Maps to: orderItemRepository_findByProductId.sql
     */
    List<OrderItem> findByProductId(@Param("productId") Long productId);
}