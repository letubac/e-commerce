package com.ecommerce.mapper;

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDTO toDTO(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setIsAnonymous(review.getIsAnonymous());
        dto.setIsVerified(review.getIsVerifiedPurchase());
        dto.setIsApproved(review.getIsApproved());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }

    public Review toEntity(ReviewDTO dto) {
        if (dto == null) {
            return null;
        }

        Review review = new Review();
        review.setId(dto.getId());
        review.setProductId(dto.getProductId());
        review.setUserId(dto.getUserId());
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());
        review.setIsAnonymous(dto.getIsAnonymous());
        review.setIsVerifiedPurchase(dto.getIsVerified());
        review.setIsApproved(dto.getIsApproved());
        review.setCreatedAt(dto.getCreatedAt());
        review.setUpdatedAt(dto.getUpdatedAt());

        return review;
    }
}