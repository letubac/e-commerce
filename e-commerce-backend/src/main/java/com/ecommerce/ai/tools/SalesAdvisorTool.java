package com.ecommerce.ai.tools;

import com.ecommerce.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * LangChain4j tool for the Sales Advisor AI Agent.
 * Provides sales trend analysis, product performance, and order analytics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesAdvisorTool {

    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @Tool("Get sales statistics for a given number of days to analyze revenue trends and performance")
    public String getSalesStatistics(int days) {
        try {
            Map<String, Object> data = dashboardService.getSalesStatistics(days);
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("SalesAdvisorTool - getSalesStatistics error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch sales statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get top selling products, inventory value, and product performance metrics")
    public String getProductPerformance() {
        try {
            Map<String, Object> data = dashboardService.getProductStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("SalesAdvisorTool - getProductPerformance error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch product performance: %s\"}", e.getMessage());
        }
    }

    @Tool("Get order statistics by status to analyze completion, cancellation, and pending rates")
    public String getOrderAnalysis() {
        try {
            Map<String, Object> data = dashboardService.getOrderStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("SalesAdvisorTool - getOrderAnalysis error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch order analysis: %s\"}", e.getMessage());
        }
    }

    @Tool("Get overall business dashboard overview including total revenue, orders, and users")
    public String getDashboardOverview() {
        try {
            Map<String, Object> data = dashboardService.getDashboardOverview();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("SalesAdvisorTool - getDashboardOverview error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch dashboard overview: %s\"}", e.getMessage());
        }
    }
}
