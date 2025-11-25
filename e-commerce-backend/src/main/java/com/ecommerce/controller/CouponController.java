package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CouponDTO;
import com.ecommerce.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller for managing coupons.
 * Provides endpoints for coupon CRUD operations (Admin only).
 */
@RestController
@RequestMapping("/api/v1")

public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private CouponService couponService;

    /**
     * Get all coupons (Admin only)
     */
    @GetMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CouponDTO>>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<CouponDTO> coupons;
            if (keyword != null && !keyword.trim().isEmpty()) {
                coupons = couponService.searchCoupons(keyword, pageRequest);
            } else {
                coupons = couponService.findAll(pageRequest);
            }

            return ResponseEntity.ok(ApiResponse.success(coupons, "Lấy danh sách coupon thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách coupon", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy danh sách coupon"));
        }
    }

    /**
     * Create new coupon (Admin only)
     */
    @PostMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        try {
            CouponDTO createdCoupon = couponService.createCoupon(couponDTO);

            log.info("Admin đã tạo coupon mới: {}", couponDTO.getCode());
            return ResponseEntity.ok(ApiResponse.success(createdCoupon, "Tạo coupon thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo coupon: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo coupon", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi tạo coupon"));
        }
    }

    /**
     * Update coupon (Admin only)
     */
    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponDTO couponDTO) {
        try {
            couponDTO.setId(id);
            CouponDTO updatedCoupon = couponService.updateCoupon(id, couponDTO);

            log.info("Admin đã cập nhật coupon ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCoupon, "Cập nhật coupon thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi cập nhật coupon ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy coupon ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật coupon ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi cập nhật coupon"));
        }
    }

    /**
     * Delete coupon (Admin only)
     */
    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            log.info("Admin đã xóa coupon ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Xóa coupon thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy coupon ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa coupon ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi xóa coupon"));
        }
    }

    /**
     * Get coupon by ID (Admin only)
     */
    @GetMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponById(@PathVariable Long id) {
        try {
            CouponDTO coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(ApiResponse.success(coupon, "Lấy thông tin coupon thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy coupon ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy coupon ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thông tin coupon"));
        }
    }

    /**
     * Validate coupon code
     */
    @PostMapping("/coupons/validate")
    public ResponseEntity<ApiResponse<CouponDTO>> validateCoupon(@RequestBody Map<String, String> request) {
        try {
            String couponCode = request.get("couponCode");
            if (couponCode == null || couponCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mã coupon không được để trống"));
            }

            CouponDTO coupon = couponService.validateCoupon(couponCode.trim());
            return ResponseEntity.ok(ApiResponse.success(coupon, "Coupon hợp lệ"));
        } catch (RuntimeException e) {
            log.warn("Coupon không hợp lệ: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi validate coupon", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi validate coupon"));
        }
    }

    /**
     * Toggle coupon active status (Admin only)
     */
    @PutMapping("/coupons/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> toggleCouponStatus(@PathVariable Long id) {
        try {
            CouponDTO updatedCoupon = couponService.toggleActiveStatus(id);
            log.info("Admin đã thay đổi trạng thái coupon ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCoupon, "Thay đổi trạng thái coupon thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy coupon ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái coupon ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi thay đổi trạng thái coupon"));
        }
    }

    /**
     * Get coupon usage statistics (Admin only)
     */
    @GetMapping("/coupons/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCouponStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = couponService.getCouponStatistics(id);
            return ResponseEntity.ok(ApiResponse.success(statistics, "Lấy thống kê coupon thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy coupon ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê coupon ID: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê coupon"));
        }
    }

    /**
     * Apply coupon to order
     */
    @PostMapping("/coupons/apply")
    public ResponseEntity<ApiResponse<Map<String, Object>>> applyCoupon(@RequestBody Map<String, Object> request) {
        try {
            String couponCode = (String) request.get("couponCode");
            Double orderAmount = ((Number) request.get("orderAmount")).doubleValue();

            if (couponCode == null || couponCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mã coupon không được để trống"));
            }

            if (orderAmount == null || orderAmount <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số tiền đơn hàng không hợp lệ"));
            }

            Map<String, Object> result = couponService.applyCoupon(couponCode.trim(), orderAmount);
            return ResponseEntity.ok(ApiResponse.success(result, "Áp dụng coupon thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi áp dụng coupon", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi áp dụng coupon"));
        }
    }
}