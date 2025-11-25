package com.ecommerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;

/**
 * REST controller for managing product reviews.
 * Provides endpoints for review CRUD operations.
 */
@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    /**
     * Get reviews for a specific product with pagination
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer rating) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findByProductId(
                    productId, pageRequest, rating);

            return ResponseEntity.ok(ApiResponse.success(reviews,
                    "Lấy đánh giá sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy đánh giá sản phẩm ID: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy đánh giá sản phẩm"));
        }
    }

    /**
     * Create a new review for a product (Authenticated users only)
     */
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            reviewDTO.setProductId(productId);

            ReviewDTO createdReview = reviewService.createReview(reviewDTO, username);
            log.info("Người dùng {} đã tạo đánh giá mới cho sản phẩm ID: {}", username, productId);

            return ResponseEntity.ok(ApiResponse.success(createdReview,
                    "Tạo đánh giá thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo đánh giá: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Không thể tạo đánh giá: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo đánh giá cho sản phẩm ID: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi tạo đánh giá"));
        }
    }

    /**
     * Update an existing review (Review owner only)
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            reviewDTO.setId(reviewId);

            ReviewDTO updatedReview = reviewService.updateReview(reviewDTO, username);
            log.info("Người dùng {} đã cập nhật đánh giá ID: {}", username, reviewId);

            return ResponseEntity.ok(ApiResponse.success(updatedReview,
                    "Cập nhật đánh giá thành công"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi cập nhật đánh giá: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Không có quyền cập nhật đánh giá ID: {}", reviewId);
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Bạn không có quyền cập nhật đánh giá này"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy đánh giá ID: {}", reviewId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật đánh giá ID: {}", reviewId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi cập nhật đánh giá"));
        }
    }

    /**
     * Delete a review (Review owner or admin only)
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            reviewService.deleteReview(reviewId, username);
            log.info("Người dùng {} đã xóa đánh giá ID: {}", username, reviewId);

            return ResponseEntity.ok(ApiResponse.success(null, "Xóa đánh giá thành công"));
        } catch (SecurityException e) {
            log.warn("Không có quyền xóa đánh giá ID: {}", reviewId);
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Bạn không có quyền xóa đánh giá này"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy đánh giá ID: {}", reviewId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi xóa đánh giá ID: {}", reviewId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi xóa đánh giá"));
        }
    }

    /**
     * Get review by ID
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(@PathVariable Long reviewId) {
        try {
            ReviewDTO review = reviewService.findById(reviewId);
            return ResponseEntity.ok(ApiResponse.success(review,
                    "Lấy thông tin đánh giá thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy đánh giá ID: {}", reviewId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy đánh giá ID: {}", reviewId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thông tin đánh giá"));
        }
    }

    /**
     * Get reviews by user (Authenticated user only)
     */
    @GetMapping("/users/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findByUsername(username, pageRequest);

            return ResponseEntity.ok(ApiResponse.success(reviews,
                    "Lấy đánh giá của bạn thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy đánh giá của người dùng", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy đánh giá của bạn"));
        }
    }

    /**
     * Get product rating summary
     */
    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ApiResponse<Object>> getProductReviewSummary(@PathVariable Long productId) {
        try {
            Object summary = reviewService.getProductReviewSummary(productId);
            return ResponseEntity.ok(ApiResponse.success(summary,
                    "Lấy thống kê đánh giá sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê đánh giá sản phẩm ID: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy thống kê đánh giá"));
        }
    }

    /**
     * Check if user can review a product
     */
    @GetMapping("/products/{productId}/reviews/can-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> canReviewProduct(
            @PathVariable Long productId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            boolean canReview = reviewService.canUserReviewProduct(username, productId);

            return ResponseEntity.ok(ApiResponse.success(canReview,
                    "Kiểm tra quyền đánh giá thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền đánh giá sản phẩm ID: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi kiểm tra quyền đánh giá"));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all reviews with admin filters (Admin only)
     */
    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean reported,
            @RequestParam(required = false) String keyword) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findAllWithFilters(
                    pageRequest, rating, reported, keyword);

            return ResponseEntity.ok(ApiResponse.success(reviews,
                    "Lấy danh sách tất cả đánh giá thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đánh giá cho admin", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi lấy danh sách đánh giá"));
        }
    }

    /**
     * Admin delete review
     */
    @DeleteMapping("/admin/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.adminDeleteReview(reviewId);
            log.info("Admin đã xóa đánh giá ID: {}", reviewId);

            return ResponseEntity.ok(ApiResponse.success(null, "Xóa đánh giá thành công"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy đánh giá ID: {}", reviewId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi admin xóa đánh giá ID: {}", reviewId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi hệ thống khi xóa đánh giá"));
        }
    }
}