package com.ecommerce.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")

public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
    	try {
            Long userId = getUserIdFromAuthentication(authentication);
            OrderDTO order = orderService.createOrder(userId, request);
            return ResponseEntity.ok(order);
    	} catch (Exception ex) {
    		throw ex;
    	}
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        OrderDTO order = orderService.getOrderByIdAndUserId(orderId, userId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/status/{status}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/admin/{orderId}/tracking")
    public ResponseEntity<OrderDTO> updateTrackingNumber(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        String trackingNumber = request.get("trackingNumber");
        OrderDTO order = orderService.updateTrackingNumber(orderId, trackingNumber);
        return ResponseEntity.ok(order);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId(); // getId() trả về Long userId
    }
}