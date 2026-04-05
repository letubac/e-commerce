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

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.ReviewService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;

/**
 * REST controller for managing product reviews.
 * Provides endpoints for review CRUD operations.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
/**
 * author: LeTuBac
 */
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get reviews for a specific product with pagination
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<BusinessApiResponse> getProductReviews(
            @PathVariable(name = "productId") Long productId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "rating", required = false) Integer rating) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findByProductId(
                    productId, pageRequest, rating);

            return ResponseEntity.ok(successHandler.handlerSuccess(reviews, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create a new review for a product (Authenticated users only)
     */
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> createReview(
            @PathVariable(name = "productId") Long productId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {

        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            reviewDTO.setProductId(productId);

            ReviewDTO createdReview = reviewService.createReview(reviewDTO, username);
            return ResponseEntity.ok(successHandler.handlerSuccess(createdReview, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update an existing review (Review owner only)
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> updateReview(
            @PathVariable(name = "reviewId") Long reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {

        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            reviewDTO.setId(reviewId);

            ReviewDTO updatedReview = reviewService.updateReview(reviewDTO, username);
            return ResponseEntity.ok(successHandler.handlerSuccess(updatedReview, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete a review (Review owner or admin only)
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> deleteReview(
            @PathVariable(name = "reviewId") Long reviewId,
            Authentication authentication) {

        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            reviewService.deleteReview(reviewId, username);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get reviews by user (Authenticated user only)
     */
    @GetMapping("/users/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getMyReviews(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            Authentication authentication) {

        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findByUsername(username, pageRequest);
            return ResponseEntity.ok(successHandler.handlerSuccess(reviews, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get product rating summary
     */
    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<BusinessApiResponse> getProductReviewSummary(
            @PathVariable(name = "productId") Long productId) {
        long start = System.currentTimeMillis();
        try {
            Object summary = reviewService.getProductReviewSummary(productId);
            return ResponseEntity.ok(successHandler.handlerSuccess(summary, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Check if user can review a product
     */
    @GetMapping("/products/{productId}/reviews/can-review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> canReviewProduct(
            @PathVariable(name = "productId") Long productId,
            Authentication authentication) {

        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            boolean canReview = reviewService.canUserReviewProduct(username, productId);
            return ResponseEntity.ok(successHandler.handlerSuccess(canReview, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ADMIN ENDPOINTS

    /**
     * Get all reviews with admin filters (Admin only)
     */
    @GetMapping("/admin/reviews")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAllReviews(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "reported", required = false) Boolean reported,
            @RequestParam(name = "keyword", required = false) String keyword) {

        long start = System.currentTimeMillis();
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);

            Page<ReviewDTO> reviews = reviewService.findAllWithFilters(
                    pageRequest, rating, reported, keyword);

            return ResponseEntity.ok(successHandler.handlerSuccess(reviews, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Admin delete review
     */
    @DeleteMapping("/admin/reviews/{reviewId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> adminDeleteReview(@PathVariable(name = "reviewId") Long reviewId) {
        long start = System.currentTimeMillis();
        try {
            reviewService.adminDeleteReview(reviewId);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}