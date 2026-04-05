package com.ecommerce.ai.tools;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.service.OrderService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool cho AI Agent: tra cứu thông tin đơn hàng của khách hàng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class OrderLookupTool {

    private final OrderService orderService;

    @Tool("Tra cứu danh sách đơn hàng của khách hàng theo userId. "
            + "Dùng khi khách hỏi về đơn hàng của họ, trạng thái giao hàng, v.v.")
    public String getOrdersByUserId(Long userId) {
        try {
            log.info("AI Tool - getOrdersByUserId: {}", userId);
            List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
            if (orders == null || orders.isEmpty()) {
                return "Khách hàng này chưa có đơn hàng nào.";
            }
            return "Danh sách đơn hàng:\n" + orders.stream()
                    .limit(5)
                    .map(o -> String.format("- Mã ĐH: %s | Tổng: %s VNĐ | Trạng thái: %s | Ngày tạo: %s",
                            o.getOrderNumber(),
                            o.getTotal() != null ? o.getTotal().toPlainString() : "N/A",
                            translateStatus(o.getStatus()),
                            o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("AI Tool - getOrdersByUserId error: {}", e.getMessage());
            return "Không thể tra cứu đơn hàng lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool("Tra cứu chi tiết một đơn hàng theo mã đơn hàng (orderId). "
            + "Dùng khi khách cung cấp số đơn hàng cụ thể.")
    public String getOrderById(Long orderId) {
        try {
            log.info("AI Tool - getOrderById: {}", orderId);
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return "Không tìm thấy đơn hàng với ID: " + orderId;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Đơn hàng #%s\n", order.getOrderNumber()));
            sb.append(String.format("- Trạng thái: %s\n", translateStatus(order.getStatus())));
            sb.append(String.format("- Thanh toán: %s\n", translatePaymentStatus(order.getPaymentStatus())));
            sb.append(String.format("- Tổng tiền: %s VNĐ\n",
                    order.getTotal() != null ? order.getTotal().toPlainString() : "N/A"));
            if (order.getTrackingNumber() != null) {
                sb.append(String.format("- Mã vận đơn: %s\n", order.getTrackingNumber()));
            }
            if (order.getShippingMethod() != null) {
                sb.append(String.format("- Phương thức giao hàng: %s\n", order.getShippingMethod()));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getOrderById error: {}", e.getMessage());
            return "Không thể tra cứu đơn hàng lúc này. Vui lòng thử lại sau.";
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "N/A";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPED" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "CANCELLED" -> "Đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    private String translatePaymentStatus(String status) {
        if (status == null) return "N/A";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chưa thanh toán";
            case "COMPLETED" -> "Đã thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }
}
