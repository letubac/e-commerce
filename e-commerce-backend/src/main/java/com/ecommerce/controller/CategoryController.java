package com.ecommerce.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.constant.CategoryConstant;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.validation.Valid;

/**
 * REST controller for managing categories.
 * Provides endpoints for category CRUD operations and admin management.
 */
@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get all active categories for public use
     */
    @GetMapping("/categories")
    public ResponseEntity<BusinessApiResponse> getAllCategories() {
        long start = System.currentTimeMillis();
        try {
            List<CategoryDTO> categories = categoryService.findActiveCategories();
            return ResponseEntity.ok(successHandler.handlerSuccess(categories, start));
        } catch (Exception e) {
            log.error("Error fetching active categories", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get products by category with pagination and filtering
     */
    @GetMapping("/categories/{categoryId}/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getCategoryProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, defaultValue = "true") Boolean active) {

        try {
            // Validate category exists
            categoryService.findById(categoryId);

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products = productService.findByCategoryId(
                    categoryId, pageRequest, minPrice, maxPrice, brandId, active);

            return ResponseEntity.ok(ApiResponse.success(products,
                    "Lấy sản phẩm theo danh mục thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo danh mục ID: {}", categoryId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy sản phẩm theo danh mục"));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all categories for admin (including inactive)
     */
    @GetMapping("/admin/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategoriesAdmin() {
        try {
            List<CategoryDTO> categories = categoryService.findAll();
            return ResponseEntity.ok(ApiResponse.success(categories,
                    "Lấy danh sách tất cả danh mục thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tất cả danh mục cho admin", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy danh sách danh mục"));
        }
    }

    /**
     * Create new category (Admin only)
     */
    @PostMapping("/admin/categories")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO createdCategory = categoryService.save(categoryDTO);
            log.info("Đã tạo danh mục mới: {}", createdCategory.getName());
            return ResponseEntity.ok(ApiResponse.success(createdCategory,
                    "Tạo danh mục thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo danh mục: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo danh mục", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi tạo danh mục"));
        }
    }

    /**
     * Update category (Admin only)
     */
    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            categoryDTO.setId(id);
            CategoryDTO updatedCategory = categoryService.update(categoryDTO);
            log.info("Đã cập nhật danh mục ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCategory,
                    "Cập nhật danh mục thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi cập nhật danh mục ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy danh mục ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật danh mục ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi cập nhật danh mục"));
        }
    }

    /**
     * Delete category (Admin only)
     */
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteById(id);
            log.info("Đã xóa danh mục ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null, "Xóa danh mục thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy danh mục ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh mục ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi xóa danh mục"));
        }
    }

    /**
     * Get category by ID
     */
    @GetMapping("/admin/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.findById(id);
            return ResponseEntity.ok(ApiResponse.success(category, "Lấy thông tin danh mục thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy danh mục ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh mục ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thông tin danh mục"));
        }
    }

    /**
     * Toggle category active status (Admin only)
     */
    @PutMapping("/admin/categories/{id}/toggle-status")
    public ResponseEntity<ApiResponse<CategoryDTO>> toggleCategoryStatus(@PathVariable Long id) {
        try {
            CategoryDTO updatedCategory = categoryService.toggleActiveStatus(id);
            String status = updatedCategory.isActive() ? "kích hoạt" : "vô hiệu hóa";
            log.info("Đã {} danh mục ID: {}", status, id);
            return ResponseEntity.ok(ApiResponse.success(updatedCategory,
                    "Thay đổi trạng thái danh mục thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy danh mục ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái danh mục ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi thay đổi trạng thái danh mục"));
        }
    }
}