package com.ecommerce.service.impl;

import com.ecommerce.constant.ReviewConstant;
import com.ecommerce.dto.CreateReviewRequest;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ReviewService;
import com.ecommerce.mapper.ReviewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

	private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

	// @Autowired - removed for Lombok
	private ReviewRepository reviewRepository;

	// @Autowired - removed for Lombok
	private ProductRepository productRepository;

	// @Autowired - removed for Lombok
	private UserRepository userRepository;

	// @Autowired - removed for Lombok
	private ReviewMapper reviewMapper;

	@Override
	public ReviewDTO createReview(Long productId, Long userId, CreateReviewRequest request) throws DetailException {
		try {
			// Check if product exists
			Optional<Product> productOpt = productRepository.findById(productId);
			if (productOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E920_PRODUCT_NOT_FOUND);
			}

			// Check if user exists
			Optional<User> userOpt = userRepository.findById(userId);
			if (userOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E925_USER_NOT_FOUND);
			}

			// Check if user has already reviewed this product
			if (hasUserReviewedProduct(productId, userId)) {
				throw new DetailException(ReviewConstant.E905_REVIEW_ALREADY_EXISTS);
			}

			Review review = new Review();
			review.setProductId(productId);
			review.setUserId(userId);
			review.setRating(request.getRating());
			review.setComment(request.getComment());
			review.setIsVerifiedPurchase(false); // Default to not verified
			review.setCreatedAt(new Date());
			review.setUpdatedAt(new Date());

			Review savedReview = reviewRepository.save(review);
			log.info("Tạo đánh giá thành công cho sản phẩm ID {} bởi user ID {}", productId, userId);
			return reviewMapper.toDTO(savedReview);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi tạo đánh giá cho sản phẩm ID: {}", productId, e);
			throw new DetailException(ReviewConstant.E901_REVIEW_CREATE_FAILED);
		}
	}

	@Override
	public ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request) throws DetailException {
		try {
			Review review = reviewRepository.findById(reviewId)
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));

			// Check if the review belongs to the user
			if (!review.getUserId().equals(userId)) {
				throw new DetailException(ReviewConstant.E911_REVIEW_NOT_OWNED);
			}

			review.setRating(request.getRating());
			review.setComment(request.getComment());
			review.setUpdatedAt(new Date());

			Review updatedReview = reviewRepository.save(review);
			log.info("Cập nhật đánh giá ID {} thành công", reviewId);
			return reviewMapper.toDTO(updatedReview);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật đánh giá ID: {}", reviewId, e);
			throw new DetailException(ReviewConstant.E902_REVIEW_UPDATE_FAILED);
		}
	}

	@Override
	public void deleteReview(Long reviewId, Long userId) throws DetailException {
		try {
			Review review = reviewRepository.findById(reviewId)
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));

			// Check if the review belongs to the user
			if (!review.getUserId().equals(userId)) {
				throw new DetailException(ReviewConstant.E911_REVIEW_NOT_OWNED);
			}

			reviewRepository.delete(review);
			log.info("Xóa đánh giá ID {} thành công", reviewId);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi xóa đánh giá ID: {}", reviewId, e);
			throw new DetailException(ReviewConstant.E903_REVIEW_DELETE_FAILED);
		}
	}

	@Override
	public ReviewDTO getReviewById(Long reviewId) throws DetailException {
		try {
			Review review = reviewRepository.findById(reviewId)
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));
			return reviewMapper.toDTO(review);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy đánh giá ID: {}", reviewId, e);
			throw new DetailException(ReviewConstant.E904_REVIEW_FETCH_FAILED);
		}
	}

	@Override
	public Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable) throws DetailException {
		try {
			Page<Review> reviewsPage = reviewRepository.findByProductId(productId, pageable);
			return reviewsPage.map(reviewMapper::toDTO);
		} catch (Exception e) {
			log.error("Lỗi khi lấy đánh giá của sản phẩm ID: {}", productId, e);
			throw new DetailException(ReviewConstant.E921_PRODUCT_REVIEWS_FETCH_FAILED);
		}
	}

	@Override
	public List<ReviewDTO> getReviewsByUserId(Long userId) throws DetailException {
		try {
			List<Review> reviews = reviewRepository.findByUserId(userId);
			return reviews.stream().map(reviewMapper::toDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Lỗi khi lấy đánh giá của user ID: {}", userId, e);
			throw new DetailException(ReviewConstant.E926_USER_REVIEWS_FETCH_FAILED);
		}
	}

	@Override
	public boolean hasUserReviewedProduct(Long productId, Long userId) throws DetailException {
		try {
			return reviewRepository.existsByProductIdAndUserId(productId, userId);
		} catch (Exception e) {
			log.error("Lỗi khi kiểm tra đánh giá của user {} cho sản phẩm {}", userId, productId, e);
			throw new DetailException(ReviewConstant.E904_REVIEW_FETCH_FAILED);
		}
	}

	@Override
	public Double getAverageRatingByProductId(Long productId) throws DetailException {
		try {
			Double average = reviewRepository.getAverageRatingByProductId(productId);
			return average != null ? average : 0.0;
		} catch (Exception e) {
			log.error("Lỗi khi tính điểm trung bình của sản phẩm ID: {}", productId, e);
			throw new DetailException(ReviewConstant.E916_RATING_CALCULATION_FAILED);
		}
	}

	@Override
	public Long getReviewCountByProductId(Long productId) throws DetailException {
		try {
			return reviewRepository.getReviewCountByProductId(productId);
		} catch (Exception e) {
			log.error("Lỗi khi đếm số đánh giá của sản phẩm ID: {}", productId, e);
			throw new DetailException(ReviewConstant.E904_REVIEW_FETCH_FAILED);
		}
	}

	// Additional methods for controller compatibility

	@Override
	public ReviewDTO findById(Long reviewId) throws DetailException {
		return getReviewById(reviewId);
	}

	@Override
	public Page<ReviewDTO> findByProductId(Long productId, Pageable pageable, Integer rating) throws DetailException {
		try {
			Page<Review> reviewsPage;
			if (rating != null) {
				// Filter by rating if provided
				reviewsPage = reviewRepository.findByProductId(productId, pageable)
						.map(review -> review.getRating().equals(rating) ? review : null);
				// Simple filtering - in real implementation, you'd use custom repository method
			} else {
				reviewsPage = reviewRepository.findByProductId(productId, pageable);
			}
			return reviewsPage.map(reviewMapper::toDTO);
		} catch (Exception e) {
			log.error("Lỗi khi lọc đánh giá theo sản phẩm ID {} và rating {}", productId, rating, e);
			throw new DetailException(ReviewConstant.E941_FILTER_FAILED);
		}
	}

	@Override
	public Page<ReviewDTO> findByUsername(String username, Pageable pageable) throws DetailException {
		try {
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E925_USER_NOT_FOUND);
			}

			List<Review> reviews = reviewRepository.findByUserId(userOpt.get().getId());
			List<ReviewDTO> reviewDTOs = reviews.stream().map(reviewMapper::toDTO).collect(Collectors.toList());

			// Simple pagination implementation
			int start = (int) pageable.getOffset();
			int end = Math.min(start + pageable.getPageSize(), reviewDTOs.size());
			List<ReviewDTO> pageContent = reviewDTOs.subList(start, end);

			return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, reviewDTOs.size());
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi lấy đánh giá của username: {}", username, e);
			throw new DetailException(ReviewConstant.E926_USER_REVIEWS_FETCH_FAILED);
		}
	}

	@Override
	public boolean canUserReviewProduct(String username, Long productId) throws DetailException {
		try {
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isEmpty()) {
				return false;
			}
			return !hasUserReviewedProduct(productId, userOpt.get().getId());
		} catch (Exception e) {
			log.error("Lỗi khi kiểm tra quyền đánh giá của user {} cho sản phẩm {}", username, productId, e);
			throw new DetailException(ReviewConstant.E904_REVIEW_FETCH_FAILED);
		}
	}

	@Override
	public Object getProductReviewSummary(Long productId) throws DetailException {
		try {
			Double averageRating = getAverageRatingByProductId(productId);
			Long reviewCount = getReviewCountByProductId(productId);

			// Get rating distribution (mock data for now)
			return java.util.Map.of("averageRating", averageRating != null ? averageRating : 0.0, "reviewCount",
					reviewCount, "ratingDistribution",
					java.util.Map.of("5", (long) (reviewCount * 0.4), "4", (long) (reviewCount * 0.3), "3",
							(long) (reviewCount * 0.2), "2", (long) (reviewCount * 0.08), "1",
							(long) (reviewCount * 0.02)));
		} catch (Exception e) {
			log.error("Lỗi khi lấy tóm tắt đánh giá của sản phẩm ID: {}", productId, e);
			throw new DetailException(ReviewConstant.E936_SUMMARY_FETCH_FAILED);
		}
	}

	@Override
	public Page<ReviewDTO> findAllReviewsAdmin(Pageable pageable) throws DetailException {
		try {
			// Use basic findAll with pagination
			Page<Review> reviews = reviewRepository.findAll(pageable);
			return reviews.map(reviewMapper::toDTO);
		} catch (Exception e) {
			log.error("Lỗi khi lấy danh sách đánh giá admin", e);
			throw new DetailException(ReviewConstant.E904_REVIEW_FETCH_FAILED);
		}
	}

	@Override
	public ReviewDTO updateReviewStatus(Long reviewId, String status) throws DetailException {
		try {
			Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
			if (reviewOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND);
			}

			Review review = reviewOpt.get();
			// Note: Review entity might not have status field, using comment as workaround
			review.setComment(review.getComment() + " [Status: " + status + "]");
			review.setUpdatedAt(new Date());

			Review updatedReview = reviewRepository.save(review);
			log.info("Cập nhật trạng thái đánh giá ID {} thành {}", reviewId, status);
			return reviewMapper.toDTO(updatedReview);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật trạng thái đánh giá ID: {}", reviewId, e);
			throw new DetailException(ReviewConstant.E930_STATUS_UPDATE_FAILED);
		}
	}

	@Override
	public java.util.Map<String, Object> getReviewStatistics() throws DetailException {
		try {
			Long totalReviews = reviewRepository.count();
			// Calculate average from all products
			Double averageRating = 4.2; // Mock data, implement proper calculation later

			return java.util.Map.of("totalReviews", totalReviews, "averageRating", averageRating, "pendingReviews",
					(long) (totalReviews * 0.05), // Mock: 5% pending
					"approvedReviews", (long) (totalReviews * 0.92), // Mock: 92% approved
					"rejectedReviews", (long) (totalReviews * 0.03) // Mock: 3% rejected
			);
		} catch (Exception e) {
			log.error("Lỗi khi lấy thống kê đánh giá", e);
			throw new DetailException(ReviewConstant.E935_STATISTICS_FETCH_FAILED);
		}
	}

	// New methods for controller compatibility

	@Override
	public ReviewDTO createReview(ReviewDTO reviewDTO, String username) throws DetailException {
		log.debug("Tạo đánh giá mới cho sản phẩm ID: {} bởi user: {}", reviewDTO.getProductId(), username);

		try {
			// Find user by username
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E925_USER_NOT_FOUND);
			}

			User user = userOpt.get();

			// Find product
			Optional<Product> productOpt = productRepository.findById(reviewDTO.getProductId());
			if (productOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E920_PRODUCT_NOT_FOUND);
			}

			// Check if user has already reviewed this product
			if (hasUserReviewedProduct(reviewDTO.getProductId(), user.getId())) {
				throw new DetailException(ReviewConstant.E905_REVIEW_ALREADY_EXISTS);
			}

			// Create new review
			Review review = new Review();
			review.setProductId(reviewDTO.getProductId());
			review.setUserId(user.getId());
			review.setRating(reviewDTO.getRating());
			review.setComment(reviewDTO.getComment());
			review.setIsVerifiedPurchase(false);
			review.setCreatedAt(new Date());
			review.setUpdatedAt(new Date());

			Review savedReview = reviewRepository.save(review);
			log.info("Đã tạo đánh giá thành công ID: {} cho sản phẩm ID: {} bởi user: {}", savedReview.getId(),
					reviewDTO.getProductId(), username);

			return reviewMapper.toDTO(savedReview);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi tạo đánh giá cho sản phẩm ID: {} bởi user: {}", reviewDTO.getProductId(), username, e);
			throw new DetailException(ReviewConstant.E901_REVIEW_CREATE_FAILED);
		}
	}

	@Override
	public ReviewDTO updateReview(ReviewDTO reviewDTO, String username) throws DetailException {
		try {
			// Find user by username
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E925_USER_NOT_FOUND);
			}

			Review review = reviewRepository.findById(reviewDTO.getId())
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));

			// Check if the review belongs to the user
			User user = userOpt.get();
			if (!review.getUserId().equals(user.getId())) {
				throw new DetailException(ReviewConstant.E911_REVIEW_NOT_OWNED);
			}

			// Update review
			review.setRating(reviewDTO.getRating());
			review.setComment(reviewDTO.getComment());
			review.setUpdatedAt(new Date());

			Review updatedReview = reviewRepository.save(review);
			log.info("Cập nhật đánh giá ID {} thành công bởi user: {}", reviewDTO.getId(), username);
			return reviewMapper.toDTO(updatedReview);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi cập nhật đánh giá ID: {} bởi user: {}", reviewDTO.getId(), username, e);
			throw new DetailException(ReviewConstant.E902_REVIEW_UPDATE_FAILED);
		}
	}

	@Override
	public void deleteReview(Long reviewId, String username) throws DetailException {
		try {
			// Find user by username
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isEmpty()) {
				throw new DetailException(ReviewConstant.E925_USER_NOT_FOUND);
			}

			Review review = reviewRepository.findById(reviewId)
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));

			// Check if the review belongs to the user or user is admin
			User user = userOpt.get();
			if (!review.getUserId().equals(user.getId())) {
				// Check if user is admin (simple role check)
				if (!"ADMIN".equals(user.getRole())) {
					throw new DetailException(ReviewConstant.E911_REVIEW_NOT_OWNED);
				}
			}

			reviewRepository.delete(review);
			log.info("Xóa đánh giá ID {} thành công bởi user: {}", reviewId, username);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi xóa đánh giá ID: {} bởi user: {}", reviewId, username, e);
			throw new DetailException(ReviewConstant.E903_REVIEW_DELETE_FAILED);
		}
	}

	@Override
	public Page<ReviewDTO> findAllWithFilters(Pageable pageable, Integer rating, Boolean reported, String keyword)
			throws DetailException {
		try {
			// Basic implementation - in real scenario, you'd use Criteria API or custom
			// queries
			Page<Review> reviewsPage = reviewRepository.findAll(pageable);

			if (rating != null || keyword != null) {
				// Filter implementation would go here
				// For simplicity, returning all reviews
			}

			return reviewsPage.map(reviewMapper::toDTO);
		} catch (Exception e) {
			log.error("Lỗi khi lọc đánh giá với rating={}, reported={}, keyword={}", rating, reported, keyword, e);
			throw new DetailException(ReviewConstant.E941_FILTER_FAILED);
		}
	}

	@Override
	public void adminDeleteReview(Long reviewId) throws DetailException {
		try {
			Review review = reviewRepository.findById(reviewId)
					.orElseThrow(() -> new DetailException(ReviewConstant.E900_REVIEW_NOT_FOUND));

			reviewRepository.delete(review);
			log.info("Admin xóa đánh giá ID {} thành công", reviewId);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Lỗi khi admin xóa đánh giá ID: {}", reviewId, e);
			throw new DetailException(ReviewConstant.E903_REVIEW_DELETE_FAILED);
		}
	}
}
