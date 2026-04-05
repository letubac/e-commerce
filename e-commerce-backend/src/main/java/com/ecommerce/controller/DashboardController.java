package com.ecommerce.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.DashboardService;
import com.ecommerce.webapp.BusinessApiResponse;

/**
 * REST controller for admin dashboard.
 * Provides endpoints for dashboard overview and statistics.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
/**
 * author: LeTuBac
 */
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get dashboard overview (Admin only)
     */
    @GetMapping("/overview")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getDashboardOverview() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> overview = dashboardService.getDashboardOverview();
            return ResponseEntity.ok(successHandler.handlerSuccess(overview, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng quan dashboard", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get sales statistics (Admin only)
     */
    @GetMapping("/sales")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getSalesStatistics(
            @RequestParam(name = "days", defaultValue = "7") int days) {

        long start = System.currentTimeMillis();
        try {
            Map<String, Object> salesStats = dashboardService.getSalesStatistics(days);
            return ResponseEntity.ok(successHandler.handlerSuccess(salesStats, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê bán hàng", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getUserStatistics() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> userStats = dashboardService.getUserStatistics();
            return ResponseEntity.ok(successHandler.handlerSuccess(userStats, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê người dùng", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get product statistics (Admin only)
     */
    @GetMapping("/products")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getProductStatistics() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> productStats = dashboardService.getProductStatistics();
            return ResponseEntity.ok(successHandler.handlerSuccess(productStats, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê sản phẩm", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get order statistics (Admin only)
     */
    @GetMapping("/orders")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getOrderStatistics() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> orderStats = dashboardService.getOrderStatistics();
            return ResponseEntity.ok(successHandler.handlerSuccess(orderStats, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê đơn hàng", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get recent activities (Admin only)
     */
    @GetMapping("/activities")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getRecentActivities(
            @RequestParam(name = "limit", defaultValue = "20") int limit) {

        long start = System.currentTimeMillis();
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities(limit);
            return ResponseEntity.ok(successHandler.handlerSuccess(activities, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy hoạt động gần đây", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get system health status (Admin only)
     */
    @GetMapping("/health")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getSystemHealth() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> health = dashboardService.getSystemHealth();
            return ResponseEntity.ok(successHandler.handlerSuccess(health, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy trạng thái hệ thống", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}