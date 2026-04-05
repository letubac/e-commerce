package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Review;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface ReviewRepository extends DbRepository<Review, Long> {

    // Maps to: reviewRepository_findById.sql
    Optional<Review> findById(@Param("id") Long id);

    // Maps to: reviewRepository_findByProductId.sql
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    // Maps to: reviewRepository_findByUserId.sql
    List<Review> findByUserId(@Param("userId") Long userId);

    // Maps to: reviewRepository_findByProductIdAndUserId.sql
    Optional<Review> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    // Maps to: reviewRepository_existsByProductIdAndUserId.sql
    boolean existsByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    // Maps to: reviewRepository_getAverageRatingByProductId.sql
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    // Maps to: reviewRepository_getReviewCountByProductId.sql
    Long getReviewCountByProductId(@Param("productId") Long productId);

    // Maps to: reviewRepository_getReviewCountByProductIdAndRating.sql
    Long getReviewCountByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);

    // Maps to: reviewRepository_findRecentReviewsByProductId.sql
    List<Review> findRecentReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // Maps to: reviewRepository_countByCreatedAtAfter.sql
    Long countByCreatedAtAfter(@Param("date") Date date);

    // Maps to: reviewRepository_getAverageRatingAfter.sql
    Double getAverageRatingAfter(@Param("date") Date date);

    @Modifying
    void insertReview(@Param("review") Review review);

    @Modifying
    void updateReview(@Param("review") Review review);

    @Modifying
    void deleteReviewById(@Param("id") Long id);
}
