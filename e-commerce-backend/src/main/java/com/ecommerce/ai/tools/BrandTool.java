package com.ecommerce.ai.tools;

import com.ecommerce.dto.BrandDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.BrandService;
import com.ecommerce.service.ProductService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tool cho AI Agent: thông tin thương hiệu (brand) sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrandTool {

    private final BrandService brandService;
    private final ProductService productService;

    @Tool("Lấy danh sách các thương hiệu (brand) sản phẩm đang kinh doanh. "
            + "Dùng khi khách hỏi về thương hiệu, nhãn hiệu, hãng sản xuất.")
    public String getActiveBrands() {
        try {
            log.info("AI Tool - getActiveBrands");
            List<BrandDTO> brands = brandService.findActiveBrands();
            if (brands == null || brands.isEmpty()) {
                return "Hiện chưa có thương hiệu nào được cập nhật.";
            }
            StringBuilder sb = new StringBuilder("Các thương hiệu đang kinh doanh:\n");
            for (BrandDTO b : brands) {
                String count = b.getProductCount() != null ? " (" + b.getProductCount() + " sản phẩm)" : "";
                String desc = b.getDescription() != null && !b.getDescription().isBlank()
                        ? " - " + b.getDescription() : "";
                sb.append(String.format("- %s%s%s\n", b.getName(), count, desc));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getActiveBrands error: {}", e.getMessage());
            return "Không thể lấy danh sách thương hiệu lúc này.";
        }
    }

    @Tool("Tìm sản phẩm theo tên thương hiệu (brand). "
            + "Dùng khi khách hỏi sản phẩm của một hãng cụ thể, ví dụ: 'Samsung có gì', 'hàng Nike'.")
    public String getProductsByBrandName(String brandName) {
        try {
            log.info("AI Tool - getProductsByBrandName: {}", brandName);
            List<BrandDTO> brands = brandService.findActiveBrands();
            if (brands == null || brands.isEmpty()) {
                return "Không tìm thấy thương hiệu \"" + brandName + "\".";
            }

            // Find matching brand (case-insensitive)
            BrandDTO matched = brands.stream()
                    .filter(b -> b.getName() != null && b.getName().toLowerCase().contains(brandName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matched == null) {
                return "Không tìm thấy thương hiệu \"" + brandName + "\". "
                        + "Gọi getActiveBrands() để xem danh sách thương hiệu.";
            }

            List<ProductDTO> products = productService.getProductsByBrand(matched.getId());
            if (products == null || products.isEmpty()) {
                return "Thương hiệu \"" + matched.getName() + "\" hiện chưa có sản phẩm nào.";
            }

            StringBuilder sb = new StringBuilder(
                    String.format("Sản phẩm của %s (%d):\n", matched.getName(), products.size()));
            products.stream().limit(5).forEach(p -> sb.append(String.format(
                    "- %s | Giá: %s VNĐ | %s\n",
                    p.getName(),
                    p.getPrice() != null ? p.getPrice().toPlainString() : "N/A",
                    p.isActive() ? "Còn hàng" : "Hết hàng")));
            if (products.size() > 5) {
                sb.append(String.format("... và %d sản phẩm khác.\n", products.size() - 5));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getProductsByBrandName error: {}", e.getMessage());
            return "Không thể tìm sản phẩm theo thương hiệu lúc này.";
        }
    }
}
