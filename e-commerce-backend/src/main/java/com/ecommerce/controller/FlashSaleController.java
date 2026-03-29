package com.ecommerce.controller;

import com.ecommerce.constant.FlashSaleConstant;
import com.ecommerce.dto.FlashSaleDTO;
import com.ecommerce.dto.FlashSaleProductDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.FlashSaleService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing Flash Sales.
 * Provides endpoints for Flash Sale CRUD operations, product management, and
 * statistics.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Get current active Flash Sale (Public)
     */
    @GetMapping("/flash-sale/active")
    public ResponseEntity<BusinessApiResponse> getCurrentActiveFlashSale() {
        long start = System.currentTimeMillis();
        try {
            FlashSaleDTO flashSale = flashSaleService.getCurrentActiveFlashSale();
            return ResponseEntity.ok(successHandler.handlerSuccess(flashSale, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get current Flash Sale products (Public)
     */
    @GetMapping("/flash-sale/products")
    public ResponseEntity<BusinessApiResponse> getCurrentFlashSaleProducts() {
        long start = System.currentTimeMillis();
        try {
            List<FlashSaleProductDTO> products = flashSaleService.getCurrentFlashSaleProducts();
            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get specific Flash Sale product (Public)
     */
    @GetMapping("/flash-sale/{flashSaleId}/products/{productId}")
    public ResponseEntity<BusinessApiResponse> getFlashSaleProduct(
            @PathVariable(name = "flashSaleId") Long flashSaleId,
            @PathVariable(name = "productId") Long productId) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleProductDTO product = flashSaleService.getFlashSaleProduct(flashSaleId, productId);
            return ResponseEntity.ok(successHandler.handlerSuccess(product, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all Flash Sales with pagination (Admin)
     */
    @GetMapping("/admin/flash-sales")
    public ResponseEntity<BusinessApiResponse> getAllFlashSales(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection) {
        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<FlashSaleDTO> flashSales = flashSaleService.getAllFlashSales(pageRequest);
            return ResponseEntity.ok(successHandler.handlerSuccess(flashSales, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get Flash Sale by ID (Admin)
     */
    @GetMapping("/admin/flash-sales/{id}")
    public ResponseEntity<BusinessApiResponse> getFlashSaleById(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleDTO flashSale = flashSaleService.getFlashSaleById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(flashSale, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create new Flash Sale (Admin)
     */
    @PostMapping("/admin/flash-sales")
    public ResponseEntity<BusinessApiResponse> createFlashSale(@Valid @RequestBody FlashSaleDTO flashSaleDTO) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleDTO created = flashSaleService.createFlashSale(flashSaleDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(created, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update Flash Sale (Admin)
     */
    @PutMapping("/admin/flash-sales/{id}")
    public ResponseEntity<BusinessApiResponse> updateFlashSale(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody FlashSaleDTO flashSaleDTO) {
        long start = System.currentTimeMillis();
        try {
            flashSaleDTO.setId(id);
            FlashSaleDTO updated = flashSaleService.updateFlashSale(flashSaleDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete Flash Sale (Admin)
     */
    @DeleteMapping("/admin/flash-sales/{id}")
    public ResponseEntity<BusinessApiResponse> deleteFlashSale(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            flashSaleService.deleteFlashSale(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Activate Flash Sale (Admin)
     */
    @PutMapping("/admin/flash-sales/{id}/activate")
    public ResponseEntity<BusinessApiResponse> activateFlashSale(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleDTO activated = flashSaleService.activateFlashSale(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(activated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Deactivate Flash Sale (Admin)
     */
    @PutMapping("/admin/flash-sales/{id}/deactivate")
    public ResponseEntity<BusinessApiResponse> deactivateFlashSale(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleDTO deactivated = flashSaleService.deactivateFlashSale(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(deactivated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ==================== FLASH SALE PRODUCT MANAGEMENT ====================

    /**
     * Get products in Flash Sale (Admin)
     */
    @GetMapping("/admin/flash-sales/{flashSaleId}/products")
    public ResponseEntity<BusinessApiResponse> getFlashSaleProducts(
            @PathVariable(name = "flashSaleId") Long flashSaleId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "displayOrder") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<FlashSaleProductDTO> products = flashSaleService.getFlashSaleProducts(flashSaleId, pageRequest);
            return ResponseEntity.ok(successHandler.handlerSuccess(products, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Add product to Flash Sale (Admin)
     */
    @PostMapping("/admin/flash-sales/{flashSaleId}/products")
    public ResponseEntity<BusinessApiResponse> addProductToFlashSale(
            @PathVariable(name = "flashSaleId") Long flashSaleId,
            @Valid @RequestBody FlashSaleProductDTO productDTO) {
        long start = System.currentTimeMillis();
        try {
            FlashSaleProductDTO added = flashSaleService.addProductToFlashSale(flashSaleId, productDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(added, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update Flash Sale product (Admin)
     */
    @PutMapping("/admin/flash-sales/{flashSaleId}/products/{productId}")
    public ResponseEntity<BusinessApiResponse> updateFlashSaleProduct(
            @PathVariable(name = "flashSaleId") Long flashSaleId,
            @PathVariable(name = "productId") Long productId,
            @Valid @RequestBody FlashSaleProductDTO productDTO) {
        long start = System.currentTimeMillis();
        try {
            productDTO.setFlashSaleId(flashSaleId);
            productDTO.setProductId(productId);
            FlashSaleProductDTO updated = flashSaleService.updateFlashSaleProduct(productDTO);
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Remove product from Flash Sale (Admin)
     */
    @DeleteMapping("/admin/flash-sales/{flashSaleId}/products/{productId}")
    public ResponseEntity<BusinessApiResponse> removeProductFromFlashSale(
            @PathVariable(name = "flashSaleId") Long flashSaleId,
            @PathVariable(name = "productId") Long productId) {
        long start = System.currentTimeMillis();
        try {
            flashSaleService.removeProductFromFlashSale(flashSaleId, productId);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get Flash Sale statistics (Admin)
     */
    @GetMapping("/admin/flash-sales/{id}/statistics")
    public ResponseEntity<BusinessApiResponse> getFlashSaleStatistics(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            Long totalSales = flashSaleService.getTotalSalesForFlashSale(id);
            java.math.BigDecimal totalRevenue = flashSaleService.getTotalRevenueForFlashSale(id);

            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalSales", totalSales);
            statistics.put("totalRevenue", totalRevenue);

            return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}
