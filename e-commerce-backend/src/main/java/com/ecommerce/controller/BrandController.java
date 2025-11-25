package com.ecommerce.controller;

import com.ecommerce.dto.BrandDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.BrandService;
import com.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing brands.
 * Provides endpoints for brand CRUD operations and admin management.
 */
@RestController
@RequestMapping("/api/v1")
public class BrandController {

    private static final Logger log = LoggerFactory.getLogger(BrandController.class);

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    /**
     * Get all active brands for public use
     */
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getAllBrands() {
        try {
            List<BrandDTO> brands = brandService.findActiveBrands();
            return ResponseEntity.ok(ApiResponse.success(brands, "Lấy danh sách thương hiệu thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thương hiệu", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy danh sách thương hiệu"));
        }
    }

    /**
     * Get products by brand with pagination and filtering
     */
    @GetMapping("/brands/{brandId}/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getBrandProducts(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "true") Boolean active) {

        try {
            // Validate brand exists
            brandService.findById(brandId);

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products = productService.findByBrandId(
                    brandId, pageRequest, minPrice, maxPrice, categoryId, active);

            return ResponseEntity.ok(ApiResponse.success(products,
                    "Lấy sản phẩm theo thương hiệu thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo thương hiệu ID: {}", brandId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy sản phẩm theo thương hiệu"));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all brands for admin (including inactive)
     */
    @GetMapping("/admin/brands")
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getAllBrandsAdmin() {
        try {
            List<BrandDTO> brands = brandService.findAll();
            return ResponseEntity.ok(ApiResponse.success(brands,
                    "Lấy danh sách tất cả thương hiệu thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tất cả thương hiệu cho admin", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy danh sách thương hiệu"));
        }
    }

    /**
     * Create new brand (Admin only)
     */
    @PostMapping("/admin/brands")
    public ResponseEntity<ApiResponse<BrandDTO>> createBrand(@Valid @RequestBody BrandDTO brandDTO) {
        try {
            BrandDTO createdBrand = brandService.save(brandDTO);
            log.info("Đã tạo thương hiệu mới: {}", createdBrand.getName());
            return ResponseEntity.ok(ApiResponse.success(createdBrand,
                    "Tạo thương hiệu thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo thương hiệu: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo thương hiệu", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi tạo thương hiệu"));
        }
    }

    /**
     * Update brand (Admin only)
     */
    @PutMapping("/admin/brands/{id}")
    public ResponseEntity<ApiResponse<BrandDTO>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandDTO brandDTO) {
        try {
            brandDTO.setId(id);
            BrandDTO updatedBrand = brandService.update(brandDTO);
            log.info("Đã cập nhật thương hiệu ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedBrand,
                    "Cập nhật thương hiệu thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi cập nhật thương hiệu ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy thương hiệu ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật thương hiệu ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi cập nhật thương hiệu"));
        }
    }

    /**
     * Delete brand (Admin only)
     */
    @DeleteMapping("/admin/brands/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        try {
            brandService.deleteById(id);
            log.info("Đã xóa thương hiệu ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null, "Xóa thương hiệu thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy thương hiệu ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa thương hiệu ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi xóa thương hiệu"));
        }
    }

    /**
     * Get brand by ID
     */
    @GetMapping("/admin/brands/{id}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandById(@PathVariable Long id) {
        try {
            BrandDTO brand = brandService.findById(id);
            return ResponseEntity.ok(ApiResponse.success(brand, "Lấy thông tin thương hiệu thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy thương hiệu ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy thương hiệu ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thông tin thương hiệu"));
        }
    }

    /**
     * Toggle brand active status (Admin only)
     */
    @PutMapping("/admin/brands/{id}/toggle-status")
    public ResponseEntity<ApiResponse<BrandDTO>> toggleBrandStatus(@PathVariable Long id) {
        try {
            BrandDTO updatedBrand = brandService.toggleActiveStatus(id);
            String status = updatedBrand.isActive() ? "kích hoạt" : "vô hiệu hóa";
            log.info("Đã {} thương hiệu ID: {}", status, id);
            return ResponseEntity.ok(ApiResponse.success(updatedBrand,
                    "Thay đổi trạng thái thương hiệu thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy thương hiệu ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái thương hiệu ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi thay đổi trạng thái thương hiệu"));
        }
    }

    /**
     * Get brand statistics (Admin only)
     */
    @GetMapping("/admin/brands/{id}/statistics")
    public ResponseEntity<ApiResponse<Object>> getBrandStatistics(@PathVariable Long id) {
        try {
            // Validate brand exists
            brandService.findById(id);

            Object statistics = brandService.getBrandStatistics(id);
            return ResponseEntity.ok(ApiResponse.success(statistics,
                    "Lấy thống kê thương hiệu thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy thương hiệu ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê thương hiệu ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê thương hiệu"));
        }
    }
}