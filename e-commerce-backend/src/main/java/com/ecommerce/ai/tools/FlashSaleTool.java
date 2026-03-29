package com.ecommerce.ai.tools;

import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import com.ecommerce.service.FlashSaleService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tool cho AI Agent: thông tin chương trình Flash Sale.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashSaleTool {

    private final FlashSaleService flashSaleService;

    @Tool("Lấy thông tin chương trình Flash Sale đang diễn ra hiện tại. "
            + "Dùng khi khách hỏi về khuyến mãi, flash sale, giảm giá đặc biệt, ưu đãi hôm nay.")
    public String getCurrentFlashSale() {
        try {
            log.info("AI Tool - getCurrentFlashSale");
            FlashSaleDTO flashSale = flashSaleService.getCurrentActiveFlashSale();
            if (flashSale == null) {
                // Check upcoming
                List<FlashSaleDTO> upcoming = flashSaleService.getUpcomingFlashSales();
                if (upcoming != null && !upcoming.isEmpty()) {
                    FlashSaleDTO next = upcoming.get(0);
                    return String.format("Hiện không có Flash Sale nào đang diễn ra. "
                            + "Chương trình sắp tới: \"%s\" bắt đầu lúc %s.",
                            next.getName(),
                            next.getStartTime() != null ? next.getStartTime().toString() : "chưa xác định");
                }
                return "Hiện không có chương trình Flash Sale nào đang diễn ra hoặc sắp diễn ra.";
            }

            long remainingMins = flashSale.getRemainingTimeInMinutes();
            String timeLeft = remainingMins >= 60
                    ? String.format("%d giờ %d phút", TimeUnit.MINUTES.toHours(remainingMins), remainingMins % 60)
                    : remainingMins + " phút";

            return String.format(
                    "🔥 Flash Sale đang diễn ra: \"%s\"\n"
                    + "- Kết thúc sau: %s\n"
                    + "- Số sản phẩm: %d\n"
                    + "Hỏi tôi để xem danh sách sản phẩm khuyến mãi!",
                    flashSale.getName(),
                    timeLeft,
                    flashSale.getTotalProducts());
        } catch (Exception e) {
            log.error("AI Tool - getCurrentFlashSale error: {}", e.getMessage());
            return "Không thể lấy thông tin Flash Sale lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool("Lấy danh sách sản phẩm trong chương trình Flash Sale hiện tại. "
            + "Dùng khi khách muốn xem sản phẩm giảm giá, mua hàng flash sale.")
    public String getFlashSaleProducts() {
        try {
            log.info("AI Tool - getFlashSaleProducts");
            List<FlashSaleProductDTO> products = flashSaleService.getCurrentFlashSaleProducts();
            if (products == null || products.isEmpty()) {
                return "Hiện không có sản phẩm Flash Sale nào đang được bán.";
            }
            StringBuilder sb = new StringBuilder("🛍️ Sản phẩm Flash Sale:\n");
            for (FlashSaleProductDTO p : products) {
                String sold = p.isSoldOut() ? " (HẾT HÀNG)" : "";
                String discount = p.getDiscountPercentage() != null
                        ? String.format(" | Giảm %.0f%%", p.getDiscountPercentage().doubleValue()) : "";
                sb.append(String.format("- %s: %s VNĐ → %s VNĐ%s%s\n",
                        p.getProductName(),
                        p.getOriginalPrice() != null ? p.getOriginalPrice().toPlainString() : "N/A",
                        p.getFlashPrice() != null ? p.getFlashPrice().toPlainString() : "N/A",
                        discount,
                        sold));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getFlashSaleProducts error: {}", e.getMessage());
            return "Không thể lấy danh sách sản phẩm Flash Sale lúc này.";
        }
    }
}
