package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * author: LeTuBac
 */
public interface OrderService {
    OrderDTO createOrder(Long userId, CreateOrderRequest request) throws DetailException;

    OrderDTO getOrderById(Long orderId) throws DetailException;

    OrderDTO getOrderByIdAndUserId(Long orderId, Long userId) throws DetailException;

    List<OrderDTO> getOrdersByUserId(Long userId) throws DetailException;

    Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) throws DetailException;

    Page<OrderDTO> getAllOrders(Pageable pageable) throws DetailException;

    Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) throws DetailException;

    OrderDTO updateOrderStatus(Long orderId, String status) throws DetailException;

    OrderDTO updateTrackingNumber(Long orderId, String trackingNumber) throws DetailException;

    void cancelOrder(Long orderId, Long userId) throws DetailException;

    String generateOrderNumber() throws DetailException;
}