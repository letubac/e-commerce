package com.ecommerce.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId; // Optional link to order for verified purchases

    @Column(name = "rating")
    private Integer rating; // 1-5 stars

    @Column(name = "title")
    private String title;

    @Column(name = "comment")
    private String comment;

    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "helpful_count")
    private Integer helpfulCount;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isPositiveRating() {
        return rating != null && rating >= 4;
    }

    public boolean isVerified() {
        return Boolean.TRUE.equals(isVerifiedPurchase) && Boolean.TRUE.equals(isApproved);
    }

    public boolean isHelpful() {
        return helpfulCount != null && helpfulCount > 0;
    }
}