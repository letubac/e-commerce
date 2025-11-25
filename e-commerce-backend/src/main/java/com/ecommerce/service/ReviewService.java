package com.ecommerce.service;

import com.ecommerce.dto.CreateReviewRequest;
import com.ecommerce.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    ReviewDTO createReview(Long productId, Long userId, CreateReviewRequest request);

    ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request);

    void deleteReview(Long reviewId, Long userId);

    ReviewDTO getReviewById(Long reviewId);

    // Method for controller compatibility
    ReviewDTO findById(Long reviewId);

    Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable);

    // Method for controller compatibility - with rating filter
    Page<ReviewDTO> findByProductId(Long productId, Pageable pageable, Integer rating);

    List<ReviewDTO> getReviewsByUserId(Long userId);

    // Method for controller compatibility
    Page<ReviewDTO> findByUsername(String username, Pageable pageable);

    boolean hasUserReviewedProduct(Long productId, Long userId);

    // Method for controller compatibility
    boolean canUserReviewProduct(String username, Long productId);

    Double getAverageRatingByProductId(Long productId);

    Long getReviewCountByProductId(Long productId);

    // Additional methods for controller support
    Object getProductReviewSummary(Long productId);

    Page<ReviewDTO> findAllReviewsAdmin(Pageable pageable);

    ReviewDTO updateReviewStatus(Long reviewId, String status);

    Map<String, Object> getReviewStatistics();

    // New methods for controller compatibility
    ReviewDTO createReview(ReviewDTO reviewDTO, String username);

    ReviewDTO updateReview(ReviewDTO reviewDTO, String username);

    void deleteReview(Long reviewId, String username);

    Page<ReviewDTO> findAllWithFilters(Pageable pageable, Integer rating, Boolean reported, String keyword);

    void adminDeleteReview(Long reviewId);
}