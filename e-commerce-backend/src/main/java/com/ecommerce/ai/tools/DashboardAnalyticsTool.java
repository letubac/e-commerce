package com.ecommerce.ai.tools;

import com.ecommerce.service.DashboardService;
import com.ecommerce.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * LangChain4j tool that wraps DashboardService and TaskService for the Analytics AI agent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class DashboardAnalyticsTool {

    private final DashboardService dashboardService;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @Tool("Get dashboard overview statistics including total orders, revenue, users, and products")
    public String getDashboardOverview() {
        try {
            Map<String, Object> data = dashboardService.getDashboardOverview();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getDashboardOverview error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch dashboard overview: %s\"}", e.getMessage());
        }
    }

    @Tool("Get sales statistics for a number of days")
    public String getSalesStats(int days) {
        try {
            Map<String, Object> data = dashboardService.getSalesStatistics(days);
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getSalesStats error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch sales statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get user registration statistics")
    public String getUserStats() {
        try {
            Map<String, Object> data = dashboardService.getUserStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getUserStats error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch user statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get product performance statistics")
    public String getProductStats() {
        try {
            Map<String, Object> data = dashboardService.getProductStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getProductStats error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch product statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get order statistics by status")
    public String getOrderStats() {
        try {
            Map<String, Object> data = dashboardService.getOrderStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getOrderStats error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch order statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get task management statistics")
    public String getTaskStats() {
        try {
            Map<String, Object> data = taskService.getTaskStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("DashboardAnalyticsTool - getTaskStats error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch task statistics: %s\"}", e.getMessage());
        }
    }
}
