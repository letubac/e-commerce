package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing products.
 * Provides endpoints for product CRUD operations and search functionality.
 */
@RestController
@RequestMapping("/api/v1")

public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Get products with filtering and pagination
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword, pageRequest);
            } else if (categoryId != null) {
                products = productService.getProductsByCategory(categoryId, pageRequest);
            } else if (brandId != null && brandId != 0) { // Handle null and invalid brandId
                products = productService.getProductsByBrand(brandId, pageRequest);
            } else {
                products = productService.getAllProducts(pageRequest);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách sản phẩm thành công", products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy danh sách sản phẩm"));
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse> getProduct(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductByIdOrThrow(id);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin sản phẩm thành công", product));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy sản phẩm ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy thông tin sản phẩm"));
        }
    }

    /**
     * Search products
     */
    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products = productService.searchProducts(keyword, pageRequest);

            return ResponseEntity.ok(new ApiResponse(true, "Tìm kiếm sản phẩm thành công", products));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm sản phẩm với từ khóa: {}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi tìm kiếm sản phẩm"));
        }
    }

    /**
     * Get search suggestions
     */
    @GetMapping("/products/search/suggestions")
    public ResponseEntity<ApiResponse> getSearchSuggestions(@RequestParam String keyword) {
        try {
            // Tạm thời return empty list, implement sau
            List<String> suggestions = List.of();
            return ResponseEntity.ok(new ApiResponse(true, "Lấy gợi ý tìm kiếm thành công", suggestions));
        } catch (Exception e) {
            log.error("Lỗi khi lấy gợi ý tìm kiếm với từ khóa: {}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy gợi ý tìm kiếm"));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all products for admin (including inactive)
     */
    @GetMapping("/admin/products")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllProductsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean active) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword, pageRequest);
            } else if (categoryId != null) {
                products = productService.getProductsByCategory(categoryId, pageRequest);
            } else if (brandId != null && brandId != 0) { // Handle null and invalid brandId
                products = productService.getProductsByBrand(brandId, pageRequest);
            } else {
                products = productService.getAllProducts(pageRequest);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách tất cả sản phẩm thành công", products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm cho admin", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi lấy danh sách sản phẩm"));
        }
    }

    /**
     * Create product (Admin only)
     */
    @PostMapping("/admin/products")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            log.info("Admin đã tạo sản phẩm mới: {}", createdProduct.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Tạo sản phẩm thành công", createdProduct));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo sản phẩm: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo sản phẩm", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi tạo sản phẩm"));
        }
    }

    /**
     * Update product (Admin only)
     */
    @PutMapping("/admin/products/{id}")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            log.info("Admin đã cập nhật sản phẩm ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật sản phẩm thành công", updatedProduct));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi cập nhật sản phẩm ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy sản phẩm ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi cập nhật sản phẩm"));
        }
    }

    /**
     * Delete product (Admin only)
     */
    @DeleteMapping("/admin/products/{id}")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            log.info("Admin đã xóa sản phẩm ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa sản phẩm thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy sản phẩm ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi xóa sản phẩm"));
        }
    }

    /**
     * Toggle product active status (Admin only)
     */
    @PutMapping("/admin/products/{id}/toggle-status")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> toggleProductStatus(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductByIdOrThrow(id);

            if (product.isActive()) {
                productService.deactivateProduct(id);
                product.setActive(false);
                log.info("Admin đã vô hiệu hóa sản phẩm ID: {}", id);
            } else {
                productService.activateProduct(id);
                product.setActive(true);
                log.info("Admin đã kích hoạt sản phẩm ID: {}", id);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Thay đổi trạng thái sản phẩm thành công", product));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy sản phẩm ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái sản phẩm ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi thay đổi trạng thái sản phẩm"));
        }
    }

    /**
     * Update product stock (Admin only)
     */
    @PutMapping("/admin/products/{id}/stock")
//    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        try {
            productService.updateStock(id, quantity);
            ProductDTO updatedProduct = productService.getProductByIdOrThrow(id);
            log.info("Admin đã cập nhật tồn kho sản phẩm ID {}: {}", id, quantity);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật tồn kho thành công", updatedProduct));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy sản phẩm ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tồn kho sản phẩm ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Lỗi hệ thống khi cập nhật tồn kho"));
        }
    }
}