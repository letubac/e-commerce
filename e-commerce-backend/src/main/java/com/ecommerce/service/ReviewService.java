package com.ecommerce.service;

import com.ecommerce.dto.CreateReviewRequest;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    ReviewDTO createReview(Long productId, Long userId, CreateReviewRequest request) throws DetailException;

    ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request) throws DetailException;

    void deleteReview(Long reviewId, Long userId) throws DetailException;

    ReviewDTO getReviewById(Long reviewId) throws DetailException;

    // Method for controller compatibility
    ReviewDTO findById(Long reviewId) throws DetailException;

    Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable) throws DetailException;

    // Method for controller compatibility - with rating filter
    Page<ReviewDTO> findByProductId(Long productId, Pageable pageable, Integer rating) throws DetailException;

    List<ReviewDTO> getReviewsByUserId(Long userId) throws DetailException;

    // Method for controller compatibility
    Page<ReviewDTO> findByUsername(String username, Pageable pageable) throws DetailException;

    boolean hasUserReviewedProduct(Long productId, Long userId) throws DetailException;

    // Method for controller compatibility
    boolean canUserReviewProduct(String username, Long productId) throws DetailException;

    Double getAverageRatingByProductId(Long productId) throws DetailException;

    Long getReviewCountByProductId(Long productId) throws DetailException;

    // Additional methods for controller support
    Object getProductReviewSummary(Long productId) throws DetailException;

    Page<ReviewDTO> findAllReviewsAdmin(Pageable pageable) throws DetailException;

    ReviewDTO updateReviewStatus(Long reviewId, String status) throws DetailException;

    Map<String, Object> getReviewStatistics() throws DetailException;

    // New methods for controller compatibility
    ReviewDTO createReview(ReviewDTO reviewDTO, String username) throws DetailException;

    ReviewDTO updateReview(ReviewDTO reviewDTO, String username) throws DetailException;

    void deleteReview(Long reviewId, String username) throws DetailException;

    Page<ReviewDTO> findAllWithFilters(Pageable pageable, Integer rating, Boolean reported, String keyword)
            throws DetailException;

    void adminDeleteReview(Long reviewId) throws DetailException;
}