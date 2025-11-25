package com.ecommerce.service;

import java.util.Map;

/**
 * Service interface for dashboard data and statistics
 */
public interface DashboardService {

    /**
     * Get dashboard overview with real-time data
     */
    Map<String, Object> getDashboardOverview();

    /**
     * Get sales statistics for specified number of days
     */
    Map<String, Object> getSalesStatistics(int days);

    /**
     * Get user statistics including growth and activity
     */
    Map<String, Object> getUserStatistics();

    /**
     * Get product statistics including inventory and categories
     */
    Map<String, Object> getProductStatistics();

    /**
     * Get order statistics including status breakdown
     */
    Map<String, Object> getOrderStatistics();

    /**
     * Get recent activities with specified limit
     */
    Map<String, Object> getRecentActivities(int limit);

    /**
     * Get system health status
     */
    Map<String, Object> getSystemHealth();
}