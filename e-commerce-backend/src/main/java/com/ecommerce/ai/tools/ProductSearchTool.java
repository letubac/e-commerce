package com.ecommerce.ai.tools;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool cho AI Agent: tìm kiếm thông tin sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class ProductSearchTool {

    private final ProductService productService;

    @Tool("Tìm kiếm sản phẩm theo từ khóa. Dùng khi khách hỏi về sản phẩm, giá cả, tồn kho.")
    public String searchProducts(String keyword) {
        try {
            log.info("AI Tool - searchProducts: {}", keyword);
            List<ProductDTO> products = productService.searchProducts(keyword);
            if (products == null || products.isEmpty()) {
                return "Không tìm thấy sản phẩm nào phù hợp với từ khóa: " + keyword;
            }
            // Return top 5 results
            return products.stream()
                    .limit(5)
                    .map(p -> String.format("- %s | Giá: %s VNĐ | Tồn kho: %s | Trạng thái: %s",
                            p.getName(),
                            p.getPrice() != null ? p.getPrice().toPlainString() : "N/A",
                            p.getStockQuantity() != null ? p.getStockQuantity() : "N/A",
                            p.isActive() ? "Còn hàng" : "Hết hàng"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("AI Tool - searchProducts error: {}", e.getMessage());
            return "Không thể tìm kiếm sản phẩm lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool("Lấy danh sách sản phẩm nổi bật / được đề xuất.")
    public String getFeaturedProducts() {
        try {
            log.info("AI Tool - getFeaturedProducts");
            List<ProductDTO> products = productService.getFeaturedProducts();
            if (products == null || products.isEmpty()) {
                return "Hiện chưa có sản phẩm nổi bật.";
            }
            return "Sản phẩm nổi bật:\n" + products.stream()
                    .limit(5)
                    .map(p -> String.format("- %s | Giá: %s VNĐ",
                            p.getName(),
                            p.getPrice() != null ? p.getPrice().toPlainString() : "N/A"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("AI Tool - getFeaturedProducts error: {}", e.getMessage());
            return "Không thể lấy danh sách sản phẩm nổi bật lúc này.";
        }
    }
}
