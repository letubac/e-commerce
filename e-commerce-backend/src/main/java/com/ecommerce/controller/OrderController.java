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
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    @PostMapping
    public ResponseEntity<BusinessApiResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            OrderDTO order = orderService.createOrder(userId, request);
            return ResponseEntity.ok(successHandler.handlerSuccess(order, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping
    public ResponseEntity<BusinessApiResponse> getUserOrders(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(orders, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<BusinessApiResponse> getOrder(
            @PathVariable(name = "orderId") Long orderId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            OrderDTO order = orderService.getOrderByIdAndUserId(orderId, userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(order, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<BusinessApiResponse> cancelOrder(
            @PathVariable(name = "orderId") Long orderId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            orderService.cancelOrder(orderId, userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<BusinessApiResponse> getAllOrders(Pageable pageable) {
        long start = System.currentTimeMillis();
        try {
            Page<OrderDTO> orders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(successHandler.handlerSuccess(orders, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<BusinessApiResponse> getOrderById(
            @PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(order, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/admin/status/{status}")
    public ResponseEntity<BusinessApiResponse> getOrdersByStatus(
            @PathVariable(name = "status") String status,
            Pageable pageable) {
        long start = System.currentTimeMillis();
        try {
            Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
            return ResponseEntity.ok(successHandler.handlerSuccess(orders, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<BusinessApiResponse> updateOrderStatus(
            @PathVariable(name = "orderId") Long orderId,
            @RequestParam(name = "status") String status) {
        long start = System.currentTimeMillis();
        try {
            OrderDTO order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(successHandler.handlerSuccess(order, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/admin/{orderId}/tracking")
    public ResponseEntity<BusinessApiResponse> updateTrackingNumber(
            @PathVariable(name = "orderId") Long orderId,
            @RequestBody Map<String, String> request) {
        long start = System.currentTimeMillis();
        try {
            String trackingNumber = request.get("trackingNumber");
            OrderDTO order = orderService.updateTrackingNumber(orderId, trackingNumber);
            return ResponseEntity.ok(successHandler.handlerSuccess(order, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId(); // getId() trả về Long userId
    }
}