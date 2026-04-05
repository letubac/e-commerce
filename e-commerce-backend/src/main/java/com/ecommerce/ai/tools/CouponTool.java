package com.ecommerce.ai.tools;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.service.CouponService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tool cho AI Agent: kiểm tra mã giảm giá (coupon).
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class CouponTool {

    private final CouponService couponService;

    @Tool("Lấy danh sách mã giảm giá đang hoạt động. "
            + "Dùng khi khách hỏi về khuyến mãi, mã giảm giá, discount.")
    public String getActiveCoupons() {
        try {
            log.info("AI Tool - getActiveCoupons");
            List<CouponDTO> coupons = couponService.getActiveCoupons();
            if (coupons == null || coupons.isEmpty()) {
                return "Hiện chưa có mã giảm giá nào đang hoạt động.";
            }
            StringBuilder sb = new StringBuilder("Mã giảm giá hiện có:\n");
            for (CouponDTO c : coupons) {
                String discount = "PERCENTAGE".equals(c.getDiscountType())
                        ? c.getDiscountValue() + "%" : c.getDiscountValue() + " VNĐ";
                String minOrder = c.getMinOrderAmount() != null
                        ? " | Đơn tối thiểu: " + c.getMinOrderAmount().toPlainString() + " VNĐ" : "";
                sb.append(String.format("- [%s] %s: Giảm %s%s\n",
                        c.getCode(), c.getName() != null ? c.getName() : "", discount, minOrder));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getActiveCoupons error: {}", e.getMessage());
            return "Không thể lấy danh sách mã giảm giá lúc này.";
        }
    }

    @Tool("Kiểm tra thông tin và tính hợp lệ của một mã giảm giá cụ thể. "
            + "Dùng khi khách cung cấp mã giảm giá và muốn biết mức giảm giá.")
    public String validateCoupon(String couponCode, Double orderAmount) {
        try {
            log.info("AI Tool - validateCoupon: code={}, amount={}", couponCode, orderAmount);
            CouponDTO coupon = couponService.getCouponByCode(couponCode);
            if (coupon == null) {
                return "Mã giảm giá \"" + couponCode + "\" không tồn tại.";
            }
            if (!Boolean.TRUE.equals(coupon.getIsActive())) {
                return "Mã giảm giá \"" + couponCode + "\" hiện đã hết hạn hoặc không hoạt động.";
            }
            boolean valid = couponService.validateCoupon(couponCode, orderAmount);
            if (!valid) {
                String minOrder = coupon.getMinOrderAmount() != null
                        ? " (Đơn hàng tối thiểu: " + coupon.getMinOrderAmount().toPlainString() + " VNĐ)" : "";
                return "Mã giảm giá \"" + couponCode + "\" không hợp lệ với đơn hàng này." + minOrder;
            }
            Double discount = couponService.calculateDiscount(couponCode, orderAmount);
            return String.format("Mã giảm giá \"%s\" hợp lệ! Bạn được giảm %.0f VNĐ.", couponCode, discount);
        } catch (Exception e) {
            log.error("AI Tool - validateCoupon error: {}", e.getMessage());
            return "Không thể kiểm tra mã giảm giá lúc này. Vui lòng thử lại sau.";
        }
    }
}
