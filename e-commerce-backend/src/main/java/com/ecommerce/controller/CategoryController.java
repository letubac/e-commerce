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
/**
 * author: LeTuBac
 */
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
    public ResponseEntity<BusinessApiResponse> getCategoryProducts(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "brandId", required = false) Long brandId,
            @RequestParam(name = "active", required = false, defaultValue = "true") Boolean active) {
        long start = System.currentTimeMillis();
        try {
            // Validate category exists
            categoryService.findById(categoryId);

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products = productService.findByCategoryId(
                    categoryId, pageRequest, minPrice, maxPrice, brandId, active);

            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo danh mục ID: {}", categoryId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all categories for admin (including inactive)
     */
    @GetMapping("/admin/categories")
    public ResponseEntity<BusinessApiResponse> getAllCategoriesAdmin() {
        long start = System.currentTimeMillis();
        try {
            List<CategoryDTO> categories = categoryService.findAll();
            return ResponseEntity.ok(successHandler.handlerSuccess(categories, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tất cả danh mục cho admin", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create new category (Admin only)
     */
    @PostMapping("/admin/categories")
    public ResponseEntity<BusinessApiResponse> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        long start = System.currentTimeMillis();
        try {
            CategoryDTO createdCategory = categoryService.save(categoryDTO);
            log.info("Đã tạo danh mục mới: {}", createdCategory.getName());
            return ResponseEntity.ok(successHandler.handlerSuccess(createdCategory, start));
        } catch (Exception e) {
            log.error("Lỗi khi tạo danh mục", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update category (Admin only)
     */
    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<BusinessApiResponse> updateCategory(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        long start = System.currentTimeMillis();
        try {
            categoryDTO.setId(id);
            CategoryDTO updatedCategory = categoryService.update(categoryDTO);
            log.info("Đã cập nhật danh mục ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedCategory, start));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật danh mục ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete category (Admin only)
     */
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<BusinessApiResponse> deleteCategory(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            categoryService.deleteById(id);
            log.info("Đã xóa danh mục ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh mục ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get category by ID
     */
    @GetMapping("/admin/categories/{id}")
    public ResponseEntity<BusinessApiResponse> getCategoryById(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            CategoryDTO category = categoryService.findById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(category, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh mục ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Toggle category active status (Admin only)
     */
    @PutMapping("/admin/categories/{id}/toggle-status")
    public ResponseEntity<BusinessApiResponse> toggleCategoryStatus(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            CategoryDTO updatedCategory = categoryService.toggleActiveStatus(id);
            String status = updatedCategory.isActive() ? "kích hoạt" : "vô hiệu hóa";
            log.info("Đã {} danh mục ID: {}", status, id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedCategory, start));
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái danh mục ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}