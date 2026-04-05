package com.ecommerce.ai.tools;

import com.ecommerce.service.DashboardService;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * LangChain4j tool for the Inventory AI Agent.
 * Provides stock monitoring, low-stock alerts, and inventory statistics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class InventoryTool {

    private final ProductService productService;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @Tool("Get list of low-stock products that may need restocking soon")
    public String getLowStockProducts() {
        try {
            var products = productService.getLowStockProducts();
            var result = products.stream()
                    .map(p -> Map.of(
                            "id", p.getId() != null ? p.getId() : 0L,
                            "name", p.getName() != null ? p.getName() : "",
                            "sku", p.getSku() != null ? p.getSku() : "",
                            "stock", p.getStockQuantity() != null ? p.getStockQuantity() : 0,
                            "price", p.getPrice() != null ? p.getPrice() : 0))
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of(
                    "lowStockCount", result.size(),
                    "products", result));
        } catch (Exception e) {
            log.error("InventoryTool - getLowStockProducts error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch low stock products: %s\"}", e.getMessage());
        }
    }

    @Tool("Get overall product inventory statistics including stock levels, out-of-stock items, and inventory value")
    public String getInventoryStatistics() {
        try {
            Map<String, Object> data = dashboardService.getProductStatistics();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("InventoryTool - getInventoryStatistics error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch inventory statistics: %s\"}", e.getMessage());
        }
    }

    @Tool("Get a quick stock summary: total active products, low-stock count, and top low-stock items")
    public String getStockSummary() {
        try {
            long totalActive = productService.getActiveProductCount();
            var lowStock = productService.getLowStockProducts();
            var topLow = lowStock.stream().limit(10)
                    .map(p -> Map.of(
                            "name", p.getName() != null ? p.getName() : "",
                            "stock", p.getStockQuantity() != null ? p.getStockQuantity() : 0))
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(Map.of(
                    "totalActiveProducts", totalActive,
                    "lowStockCount", lowStock.size(),
                    "topLowStockItems", topLow));
        } catch (Exception e) {
            log.error("InventoryTool - getStockSummary error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch stock summary: %s\"}", e.getMessage());
        }
    }
}
