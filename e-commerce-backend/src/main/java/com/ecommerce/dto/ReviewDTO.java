package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class ReviewDTO {
    private Long id;

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    private Long userId;
    private String userName;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    private Integer rating;

    private String title;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(min = 1, max = 1000, message = "Nội dung đánh giá phải từ 1 đến 1000 ký tự")
    private String comment;

    private Boolean isAnonymous;
    private Boolean isVerified;
    private Boolean isApproved;
    private Date createdAt;
    private Date updatedAt;
}
