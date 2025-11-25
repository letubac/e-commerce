package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(Long userId, CreateOrderRequest request);

    OrderDTO getOrderById(Long orderId);

    OrderDTO getOrderByIdAndUserId(Long orderId, Long userId);

    List<OrderDTO> getOrdersByUserId(Long userId);

    Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderDTO> getAllOrders(Pageable pageable);

    Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable);

    OrderDTO updateOrderStatus(Long orderId, String status);

    OrderDTO updateTrackingNumber(Long orderId, String trackingNumber);

    void cancelOrder(Long orderId, Long userId);

    String generateOrderNumber();
}