package com.ecommerce.controller;

import java.util.Map;

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

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.CouponService;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.validation.Valid;

/**
 * REST controller for managing coupons.
 * Provides endpoints for coupon CRUD operations.
 * Admin endpoints: /api/v1/admin/coupons/*
 * Public endpoints: /api/v1/public/coupons/*
 */
@RestController
@RequestMapping("/api/v1")

/**
 * author: LeTuBac
 */
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private CouponService couponService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get all coupons (Admin only)
     */
    @GetMapping("/admin/coupons")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getCoupons(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<CouponDTO> coupons;
            if (keyword != null && !keyword.trim().isEmpty()) {
                coupons = couponService.searchCoupons(keyword, pageRequest);
            } else {
                coupons = couponService.findAll(pageRequest);
            }

            return ResponseEntity.ok(successHandler.handlerSuccess(coupons, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách coupon", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create new coupon (Admin only)
     */
    @PostMapping("/admin/coupons")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        long start = System.currentTimeMillis();
        try {
            CouponDTO createdCoupon = couponService.createCoupon(couponDTO);
            log.info("Admin đã tạo coupon mới: {}", couponDTO.getCode());
            return ResponseEntity.ok(successHandler.handlerSuccess(createdCoupon, start));
        } catch (Exception e) {
            log.error("Lỗi khi tạo coupon", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update coupon (Admin only)
     */
    @PutMapping("/admin/coupons/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> updateCoupon(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody CouponDTO couponDTO) {
        long start = System.currentTimeMillis();
        try {
            couponDTO.setId(id);
            CouponDTO updatedCoupon = couponService.updateCoupon(id, couponDTO);
            log.info("Admin đã cập nhật coupon ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedCoupon, start));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật coupon ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete coupon (Admin only)
     */
    @DeleteMapping("/admin/coupons/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> deleteCoupon(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            couponService.deleteCoupon(id);
            log.info("Admin đã xóa coupon ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            log.error("Lỗi khi xóa coupon ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get coupon by ID (Admin only)
     */
    @GetMapping("/admin/coupons/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getCouponById(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            CouponDTO coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(coupon, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy coupon ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Validate coupon code (Admin only)
     */
    @PostMapping("/admin/coupons/validate")
    public ResponseEntity<BusinessApiResponse> validateCoupon(@RequestBody Map<String, String> request) {
        long start = System.currentTimeMillis();
        try {
            String couponCode = request.get("couponCode");
            if (couponCode == null || couponCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã coupon không được để trống");
            }

            CouponDTO coupon = couponService.validateCoupon(couponCode.trim());
            return ResponseEntity.ok(successHandler.handlerSuccess(coupon, start));
        } catch (Exception e) {
            log.error("Lỗi khi validate coupon", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Toggle coupon active status (Admin only)
     */
    @PutMapping("/admin/coupons/{id}/toggle-status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> toggleCouponStatus(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            CouponDTO updatedCoupon = couponService.toggleActiveStatus(id);
            log.info("Admin đã thay đổi trạng thái coupon ID: {}", id);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedCoupon, start));
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái coupon ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get coupon usage statistics (Admin only)
     */
    @GetMapping("/admin/coupons/{id}/statistics")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getCouponStatistics(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> statistics = couponService.getCouponStatistics(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê coupon ID: {}", id, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Apply coupon to order (Admin only)
     */
    @PostMapping("/admin/coupons/apply")
    public ResponseEntity<BusinessApiResponse> applyCoupon(@RequestBody Map<String, Object> request) {
        long start = System.currentTimeMillis();
        try {
            String couponCode = (String) request.get("couponCode");
            Double orderAmount = ((Number) request.get("orderAmount")).doubleValue();

            if (couponCode == null || couponCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã coupon không được để trống");
            }

            if (orderAmount == null || orderAmount <= 0) {
                throw new IllegalArgumentException("Số tiền đơn hàng không hợp lệ");
            }

            Map<String, Object> result = couponService.applyCoupon(couponCode.trim(), orderAmount);
            return ResponseEntity.ok(successHandler.handlerSuccess(result, start));
        } catch (Exception e) {
            log.error("Lỗi khi áp dụng coupon", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ========== PUBLIC ENDPOINTS FOR USERS ==========

    /**
     * Get all active coupons (Public - for users)
     */
    @GetMapping("/public/coupons")
    public ResponseEntity<BusinessApiResponse> getActiveCoupons(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<CouponDTO> coupons = couponService.findAll(pageRequest);

            // Filter only active coupons
            Page<CouponDTO> activeCoupons = coupons.map(coupon -> {
                if (coupon.getIsActive() != null && coupon.getIsActive()) {
                    return coupon;
                }
                return null;
            });

            return ResponseEntity.ok(successHandler.handlerSuccess(activeCoupons, start));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách coupon công khai", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Apply coupon (Public - for users)
     */
    @PostMapping("/public/coupons/apply")
    public ResponseEntity<BusinessApiResponse> applyCouponPublic(@RequestBody Map<String, Object> request) {
        long start = System.currentTimeMillis();
        try {
            String couponCode = (String) request.get("couponCode");
            Double orderAmount = ((Number) request.get("orderAmount")).doubleValue();

            if (couponCode == null || couponCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã coupon không được để trống");
            }

            if (orderAmount == null || orderAmount <= 0) {
                throw new IllegalArgumentException("Số tiền đơn hàng không hợp lệ");
            }

            Map<String, Object> result = couponService.applyCoupon(couponCode.trim(), orderAmount);
            return ResponseEntity.ok(successHandler.handlerSuccess(result, start));
        } catch (Exception e) {
            log.error("Lỗi khi áp dụng coupon", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}