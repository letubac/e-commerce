package com.ecommerce.ai.tools;

import com.ecommerce.service.CouponService;
import com.ecommerce.service.FlashSaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * LangChain4j tool for the Marketing AI Agent.
 * Provides flash sale management insights and coupon analytics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class MarketingTool {

    private final FlashSaleService flashSaleService;
    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    @Tool("Get all currently active flash sales with their schedules and discount information")
    public String getActiveFlashSales() {
        try {
            var sales = flashSaleService.getActiveFlashSales();
            return objectMapper.writeValueAsString(Map.of(
                    "activeCount", sales.size(),
                    "sales", sales));
        } catch (Exception e) {
            log.error("MarketingTool - getActiveFlashSales error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch active flash sales: %s\"}", e.getMessage());
        }
    }

    @Tool("Get upcoming scheduled flash sales to plan future promotions")
    public String getUpcomingFlashSales() {
        try {
            var sales = flashSaleService.getUpcomingFlashSales();
            return objectMapper.writeValueAsString(Map.of(
                    "upcomingCount", sales.size(),
                    "sales", sales));
        } catch (Exception e) {
            log.error("MarketingTool - getUpcomingFlashSales error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch upcoming flash sales: %s\"}", e.getMessage());
        }
    }

    @Tool("Get all active coupon codes with their discount types, values, and minimum order requirements")
    public String getActiveCoupons() {
        try {
            var coupons = couponService.getActiveCoupons();
            return objectMapper.writeValueAsString(Map.of(
                    "activeCouponsCount", coupons.size(),
                    "coupons", coupons));
        } catch (Exception e) {
            log.error("MarketingTool - getActiveCoupons error: {}", e.getMessage());
            return String.format("{\"error\": \"Unable to fetch active coupons: %s\"}", e.getMessage());
        }
    }

    @Tool("Get usage statistics for a specific coupon by its ID")
    public String getCouponStatistics(Long couponId) {
        try {
            Map<String, Object> stats = couponService.getCouponStatistics(couponId);
            return objectMapper.writeValueAsString(stats);
        } catch (Exception e) {
            log.error("MarketingTool - getCouponStatistics error for id {}: {}", couponId, e.getMessage());
            return String.format("{\"error\": \"Unable to fetch coupon statistics: %s\"}", e.getMessage());
        }
    }
}
