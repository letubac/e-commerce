package com.ecommerce.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

import com.ecommerce.dto.BrandDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.BrandService;
import com.ecommerce.service.ProductService;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.validation.Valid;

/**
 * REST controller for managing brands.
 * Provides endpoints for brand CRUD operations and admin management.
 */
@RestController
@RequestMapping("/api/v1")
/**
 * author: LeTuBac
 */
public class BrandController {

    private static final Logger log = LoggerFactory.getLogger(BrandController.class);

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get all active brands for public use
     */
    @GetMapping("/brands")
    public ResponseEntity<BusinessApiResponse> getAllBrands(Locale locale) {
        long start = System.currentTimeMillis();
        try {
            List<BrandDTO> brands = brandService.findActiveBrands();
            return ResponseEntity.ok(successHandler.handlerSuccess(brands, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi lấy danh sách thương hiệu", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get products by brand with pagination and filtering
     */
    @GetMapping("/brands/{brandId}/products")
    public ResponseEntity<BusinessApiResponse> getBrandProducts(
            @PathVariable(name = "brandId") Long brandId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "active", required = false, defaultValue = "true") Boolean active,
            Locale locale) {

        long start = System.currentTimeMillis();
        try {
            // Validate brand exists
            brandService.findById(brandId);

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ProductDTO> products = productService.findByBrandId(
                    brandId, pageRequest, minPrice, maxPrice, categoryId, active);

            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy sản phẩm theo thương hiệu ID: {}", brandId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all brands for admin (including inactive)
     */
    @GetMapping("/admin/brands")
    public ResponseEntity<BusinessApiResponse> getAllBrandsAdmin(Locale locale) {
        long start = System.currentTimeMillis();
        try {
            List<BrandDTO> brands = brandService.findAll();
            return ResponseEntity.ok(successHandler.handlerSuccess(brands, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy tất cả thương hiệu cho admin", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create new brand (Admin only)
     */
    @PostMapping("/admin/brands")
    public ResponseEntity<BusinessApiResponse> createBrand(
            @Valid @RequestBody BrandDTO brandDTO,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            BrandDTO createdBrand = brandService.save(brandDTO);
            log.info("Đã tạo thương hiệu mới: {}", createdBrand.getName());
            return ResponseEntity.ok(successHandler.handlerSuccess(createdBrand, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi tạo thương hiệu", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update brand (Admin only)
     */
    @PutMapping("/admin/brands/{id}")
    public ResponseEntity<BusinessApiResponse> updateBrand(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody BrandDTO brandDTO,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            brandDTO.setId(id);
            BrandDTO updatedBrand = brandService.update(brandDTO);
            log.info("Đã cập nhật thương hiệu ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedBrand, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi cập nhật thương hiệu ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete brand (Admin only)
     */
    @DeleteMapping("/admin/brands/{id}")
    public ResponseEntity<BusinessApiResponse> deleteBrand(
            @PathVariable(name = "id") Long id,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            brandService.deleteById(id);
            log.info("Đã xóa thương hiệu ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi xóa thương hiệu ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get brand by ID
     */
    @GetMapping("/admin/brands/{id}")
    public ResponseEntity<BusinessApiResponse> getBrandById(
            @PathVariable(name = "id") Long id,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            BrandDTO brand = brandService.findById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(brand, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi lấy thông tin thương hiệu ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Toggle brand active status (Admin only)
     */
    @PutMapping("/admin/brands/{id}/toggle-status")
    public ResponseEntity<BusinessApiResponse> toggleBrandStatus(
            @PathVariable(name = "id") Long id,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            BrandDTO updatedBrand = brandService.toggleActiveStatus(id);
            String status = updatedBrand.isActive() ? "kích hoạt" : "vô hiệu hóa";
            log.info("Đã {} thương hiệu ID: {}", status, id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedBrand, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi thay đổi trạng thái thương hiệu ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get brand statistics (Admin only)
     */
    @GetMapping("/admin/brands/{id}/statistics")
    public ResponseEntity<BusinessApiResponse> getBrandStatistics(
            @PathVariable(name = "id") Long id,
            Locale locale) {
        long start = System.currentTimeMillis();
        try {
            // Validate brand exists
            brandService.findById(id);

            Object statistics = brandService.getBrandStatistics(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi không mong muốn khi lấy thống kê thương hiệu ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}