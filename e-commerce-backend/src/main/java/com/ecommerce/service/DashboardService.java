package com.ecommerce.service;

import java.util.Map;

import com.ecommerce.exception.DetailException;

/**
 * Service interface for dashboard data and statistics
 */
public interface DashboardService {

    /**
     * Get dashboard overview with real-time data
     */
    Map<String, Object> getDashboardOverview() throws DetailException;

    /**
     * Get sales statistics for specified number of days
     */
    Map<String, Object> getSalesStatistics(int days) throws DetailException;

    /**
     * Get user statistics including growth and activity
     */
    Map<String, Object> getUserStatistics() throws DetailException;

    /**
     * Get product statistics including inventory and categories
     */
    Map<String, Object> getProductStatistics() throws DetailException;

    /**
     * Get order statistics including status breakdown
     */
    Map<String, Object> getOrderStatistics() throws DetailException;

    /**
     * Get recent activities with specified limit
     */
    Map<String, Object> getRecentActivities(int limit) throws DetailException;

    /**
     * Get system health status
     */
    Map<String, Object> getSystemHealth() throws DetailException;
}