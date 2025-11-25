package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for admin dashboard.
 * Provides endpoints for dashboard overview and statistics.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard overview (Admin only)
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview() {
        try {
            Map<String, Object> overview = dashboardService.getDashboardOverview();
            return ResponseEntity.ok(ApiResponse.success(overview, "Lấy tổng quan dashboard thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng quan dashboard", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy tổng quan dashboard"));
        }
    }

    /**
     * Get sales statistics (Admin only)
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesStatistics(
            @RequestParam(defaultValue = "7") int days) {

        try {
            Map<String, Object> salesStats = dashboardService.getSalesStatistics(days);
            return ResponseEntity.ok(ApiResponse.success(salesStats, "Lấy thống kê bán hàng thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê bán hàng", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê bán hàng"));
        }
    }

    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        try {
            Map<String, Object> userStats = dashboardService.getUserStatistics();
            return ResponseEntity.ok(ApiResponse.success(userStats, "Lấy thống kê người dùng thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê người dùng", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê người dùng"));
        }
    }

    /**
     * Get product statistics (Admin only)
     */
    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStatistics() {
        try {
            Map<String, Object> productStats = dashboardService.getProductStatistics();
            return ResponseEntity.ok(ApiResponse.success(productStats, "Lấy thống kê sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê sản phẩm", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê sản phẩm"));
        }
    }

    /**
     * Get order statistics (Admin only)
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatistics() {
        try {
            Map<String, Object> orderStats = dashboardService.getOrderStatistics();
            return ResponseEntity.ok(ApiResponse.success(orderStats, "Lấy thống kê đơn hàng thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê đơn hàng", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê đơn hàng"));
        }
    }

    /**
     * Get recent activities (Admin only)
     */
    @GetMapping("/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "20") int limit) {

        try {
            Map<String, Object> activities = dashboardService.getRecentActivities(limit);
            return ResponseEntity.ok(ApiResponse.success(activities, "Lấy hoạt động gần đây thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy hoạt động gần đây", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy hoạt động gần đây"));
        }
    }

    /**
     * Get system health status (Admin only)
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        try {
            Map<String, Object> health = dashboardService.getSystemHealth();
            return ResponseEntity.ok(ApiResponse.success(health, "Lấy trạng thái hệ thống thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy trạng thái hệ thống", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy trạng thái hệ thống"));
        }
    }
}