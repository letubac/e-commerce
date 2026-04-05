package com.ecommerce.ai.tools;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Tool cho AI Agent: xem giỏ hàng của khách hàng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class CartTool {

    private final CartService cartService;

    @Tool("Xem giỏ hàng của khách hàng theo userId. "
            + "Dùng khi khách hỏi về giỏ hàng, số lượng sản phẩm trong giỏ, tổng tiền giỏ hàng.")
    public String getCartSummary(Long userId) {
        try {
            log.info("AI Tool - getCartSummary: userId={}", userId);
            CartDTO cart = cartService.getCartByUserId(userId);
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return "Giỏ hàng của bạn hiện đang trống.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Giỏ hàng (%d sản phẩm):\n", cart.getItemCount() != null ? cart.getItemCount() : cart.getItems().size()));
            for (CartItemDTO item : cart.getItems()) {
                sb.append(String.format("- %s | SL: %d | Đơn giá: %s VNĐ\n",
                        item.getProductName() != null ? item.getProductName() : "Sản phẩm #" + item.getProductId(),
                        item.getQuantity() != null ? item.getQuantity() : 0,
                        item.getPrice() != null ? item.getPrice().toPlainString() : "N/A"));
            }
            if (cart.getTotalPrice() != null) {
                sb.append(String.format("Tổng tiền: %s VNĐ", cart.getTotalPrice().toPlainString()));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getCartSummary error: {}", e.getMessage());
            return "Không thể xem giỏ hàng lúc này. Vui lòng thử lại sau.";
        }
    }
}
