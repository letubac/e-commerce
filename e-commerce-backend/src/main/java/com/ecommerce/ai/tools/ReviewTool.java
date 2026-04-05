package com.ecommerce.ai.tools;

import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.service.ReviewService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Tool cho AI Agent: xem đánh giá sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class ReviewTool {

    private final ReviewService reviewService;

    @Tool("Lấy đánh giá và điểm trung bình của sản phẩm theo productId. "
            + "Dùng khi khách hỏi về chất lượng sản phẩm, review, đánh giá, feedback.")
    public String getProductReviews(Long productId) {
        try {
            log.info("AI Tool - getProductReviews: productId={}", productId);
            Double avgRating = reviewService.getAverageRatingByProductId(productId);
            Long count = reviewService.getReviewCountByProductId(productId);

            if (count == null || count == 0) {
                return "Sản phẩm này chưa có đánh giá nào.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Đánh giá sản phẩm #%d: %.1f/5 ⭐ (%d đánh giá)\n",
                    productId, avgRating != null ? avgRating : 0.0, count));

            // Get latest reviews (top 3)
            Page<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, PageRequest.of(0, 3));
            if (reviews != null && reviews.hasContent()) {
                sb.append("Đánh giá gần đây:\n");
                for (ReviewDTO r : reviews.getContent()) {
                    String stars = "⭐".repeat(Math.max(0, r.getRating() != null ? r.getRating() : 0));
                    String comment = r.getComment() != null && r.getComment().length() > 80
                            ? r.getComment().substring(0, 80) + "..." : r.getComment();
                    sb.append(String.format("- %s %s: %s\n",
                            stars,
                            r.getUserName() != null ? r.getUserName() : "Khách hàng",
                            comment != null ? comment : "(Không có nhận xét)"));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AI Tool - getProductReviews error: {}", e.getMessage());
            return "Không thể lấy đánh giá sản phẩm lúc này. Vui lòng thử lại sau.";
        }
    }
}
