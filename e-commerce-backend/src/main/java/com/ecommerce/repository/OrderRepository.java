package com.ecommerce.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Order;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface OrderRepository extends DbRepository<Order, Long> {
	
		Long countAll();

        // Maps to: orderRepository_findById.sql
        Order findById(@Param("id") Long id);

        // Maps to: orderRepository_findByUserId.sql
        List<Order> findByUserId(@Param("userId") Long userId);

        // Maps to: orderRepository_findByUserIdPaged.sql
        Page<Order> findByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

        // Maps to: orderRepository_findByStatus.sql
        Page<Order> findByStatus(@Param("status") String status, Pageable pageable);

        // Maps to: orderRepository_findByOrderNumber.sql
        Optional<Order> findByOrderNumber(@Param("orderNumber") String orderNumber);

        // Maps to: orderRepository_findByIdWithItems.sql
        Optional<Order> findByIdWithItems(@Param("id") Long id);

        // Maps to: orderRepository_findByIdAndUserId.sql
        Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

        // Maps to: orderRepository_findByUserIdAndStatus.sql
        List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

        // Maps to: orderRepository_findByCreatedAtBetween.sql
        List<Order> findByCreatedAtBetween(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // Maps to: orderRepository_countByStatus.sql
        Long countByStatus(@Param("status") String status);

        // Maps to: orderRepository_getTotalRevenueByDateRange.sql
        Double getTotalRevenueByDateRange(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // Maps to: orderRepository_countByStatusString.sql
        Long countByStatusString(@Param("status") String status);

        // Maps to: orderRepository_getTotalRevenue.sql
        BigDecimal getTotalRevenue();

        // Maps to: orderRepository_countByCreatedAtAfter.sql
        Long countByCreatedAtAfter(@Param("date") Date date);

        // Maps to: orderRepository_getTotalRevenueAfter.sql
        BigDecimal getTotalRevenueAfter(@Param("date") Date date);

        // Maps to: orderRepository_getAverageOrderValue.sql
        BigDecimal getAverageOrderValue();

        /**
         * Insert a new order
         * Maps to: orderRepository_insertOrder.sql
         */
        @Modifying
        void insertOrder(@Param("order") Order order);

        /**
         * Update an existing order
         * Maps to: orderRepository_updateOrder.sql
         */
        @Modifying
        void updateOrder(@Param("order") Order order);

        /**
         * Find all orders with pagination
         * Maps to: orderRepository_findAllPaged.sql
         */
        Page<Order> findAllPaged(Pageable pageable);

        /**
         * Find orders by status with pagination (String version for MirageSQL)
         * Maps to: orderRepository_findByStatusPaged.sql
         */
        Page<Order> findByStatusPaged(@Param("status") String status, Pageable pageable);

        /**
         * Check if user exists (used in OrderService)
         * Maps to: userRepository_existsById.sql
         */
        boolean existsByUserId(@Param("userId") Long userId);
}
