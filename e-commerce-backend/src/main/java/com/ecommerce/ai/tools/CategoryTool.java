package com.ecommerce.ai.tools;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.service.CategoryService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tool cho AI Agent: xem danh mục sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class CategoryTool {

    private final CategoryService categoryService;

    @Tool("Lấy danh sách các danh mục sản phẩm. "
            + "Dùng khi khách hỏi về các loại sản phẩm, danh mục hàng hóa, phân loại sản phẩm.")
    public String getCategories() {
        try {
            log.info("AI Tool - getCategories");
            List<CategoryDTO> categories = categoryService.findActiveCategories();
            if (categories == null || categories.isEmpty()) {
                return "Hiện chưa có danh mục sản phẩm nào.";
            }
            StringBuilder sb = new StringBuilder("Danh mục sản phẩm:\n");
            for (CategoryDTO cat : categories) {
                String count = cat.getProductCount() != null ? " (" + cat.getProductCount() + " sản phẩm)" : "";
                sb.append(String.format("- %s%s\n", cat.getName(), count));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getCategories error: {}", e.getMessage());
            return "Không thể lấy danh mục sản phẩm lúc này.";
        }
    }
}
