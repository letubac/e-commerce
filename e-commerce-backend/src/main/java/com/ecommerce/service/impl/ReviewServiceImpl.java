package com.ecommerce.service.impl;

import com.ecommerce.dto.CreateReviewRequest;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;

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

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.BadRequestException;
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
	public ReviewDTO createReview(Long productId, Long userId, CreateReviewRequest request) {
		// Check if product exists
		Optional<Product> productOpt = productRepository.findById(productId);
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		}

		// Check if user exists
		Optional<User> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with id: " + userId);
		}

		// Check if user has already reviewed this product
		if (hasUserReviewedProduct(productId, userId)) {
			throw new BadRequestException("User has already reviewed this product");
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
		return reviewMapper.toDTO(savedReview);
	}

	@Override
	public ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

		// Check if the review belongs to the user
		if (!review.getUserId().equals(userId)) {
			throw new BadRequestException("User can only update their own reviews");
		}

		review.setRating(request.getRating());
		review.setComment(request.getComment());
		review.setUpdatedAt(new Date());

		Review updatedReview = reviewRepository.save(review);
		return reviewMapper.toDTO(updatedReview);
	}

	@Override
	public void deleteReview(Long reviewId, Long userId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

		// Check if the review belongs to the user
		if (!review.getUserId().equals(userId)) {
			throw new BadRequestException("User can only delete their own reviews");
		}

		reviewRepository.delete(review);
	}

	@Override
	public ReviewDTO getReviewById(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
		return reviewMapper.toDTO(review);
	}

	@Override
	public Page<ReviewDTO> getReviewsByProductId(Long productId, Pageable pageable) {
		Page<Review> reviewsPage = reviewRepository.findByProductId(productId, pageable);
		return reviewsPage.map(reviewMapper::toDTO);
	}

	@Override
	public List<ReviewDTO> getReviewsByUserId(Long userId) {
		List<Review> reviews = reviewRepository.findByUserId(userId);
		return reviews.stream().map(reviewMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public boolean hasUserReviewedProduct(Long productId, Long userId) {
		return reviewRepository.existsByProductIdAndUserId(productId, userId);
	}

	@Override
	public Double getAverageRatingByProductId(Long productId) {
		Double average = reviewRepository.getAverageRatingByProductId(productId);
		return average != null ? average : 0.0;
	}

	@Override
	public Long getReviewCountByProductId(Long productId) {
		return reviewRepository.getReviewCountByProductId(productId);
	}

	// Additional methods for controller compatibility

	@Override
	public ReviewDTO findById(Long reviewId) {
		return getReviewById(reviewId);
	}

	@Override
	public Page<ReviewDTO> findByProductId(Long productId, Pageable pageable, Integer rating) {
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
	}

	@Override
	public Page<ReviewDTO> findByUsername(String username, Pageable pageable) {
		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		List<Review> reviews = reviewRepository.findByUserId(userOpt.get().getId());
		List<ReviewDTO> reviewDTOs = reviews.stream().map(reviewMapper::toDTO).collect(Collectors.toList());

		// Simple pagination implementation
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), reviewDTOs.size());
		List<ReviewDTO> pageContent = reviewDTOs.subList(start, end);

		return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, reviewDTOs.size());
	}

	@Override
	public boolean canUserReviewProduct(String username, Long productId) {
		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			return false;
		}
		return !hasUserReviewedProduct(productId, userOpt.get().getId());
	}

	@Override
	public Object getProductReviewSummary(Long productId) {
		Double averageRating = getAverageRatingByProductId(productId);
		Long reviewCount = getReviewCountByProductId(productId);

		// Get rating distribution (mock data for now)
		return java.util.Map.of("averageRating", averageRating != null ? averageRating : 0.0, "reviewCount",
				reviewCount, "ratingDistribution",
				java.util.Map.of("5", (long) (reviewCount * 0.4), "4", (long) (reviewCount * 0.3), "3",
						(long) (reviewCount * 0.2), "2", (long) (reviewCount * 0.08), "1",
						(long) (reviewCount * 0.02)));
	}

	@Override
	public Page<ReviewDTO> findAllReviewsAdmin(Pageable pageable) {
		// Use basic findAll with pagination
		Page<Review> reviews = reviewRepository.findAll(pageable);
		return reviews.map(reviewMapper::toDTO);
	}

	@Override
	public ReviewDTO updateReviewStatus(Long reviewId, String status) {
		Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
		if (reviewOpt.isEmpty()) {
			throw new ResourceNotFoundException("Review not found with id: " + reviewId);
		}

		Review review = reviewOpt.get();
		// Note: Review entity might not have status field, using comment as workaround
		review.setComment(review.getComment() + " [Status: " + status + "]");
		review.setUpdatedAt(new Date());

		Review updatedReview = reviewRepository.save(review);
		return reviewMapper.toDTO(updatedReview);
	}

	@Override
	public java.util.Map<String, Object> getReviewStatistics() {
		Long totalReviews = reviewRepository.count();
		// Calculate average from all products
		Double averageRating = 4.2; // Mock data, implement proper calculation later

		return java.util.Map.of("totalReviews", totalReviews, "averageRating", averageRating, "pendingReviews",
				(long) (totalReviews * 0.05), // Mock: 5% pending
				"approvedReviews", (long) (totalReviews * 0.92), // Mock: 92% approved
				"rejectedReviews", (long) (totalReviews * 0.03) // Mock: 3% rejected
		);
	}

	// New methods for controller compatibility

	@Override
	public ReviewDTO createReview(ReviewDTO reviewDTO, String username) {
		log.debug("Tạo đánh giá mới cho sản phẩm ID: {} bởi user: {}", reviewDTO.getProductId(), username);

		// Find user by username
		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		User user = userOpt.get();

		// Find product
		Optional<Product> productOpt = productRepository.findById(reviewDTO.getProductId());
		if (productOpt.isEmpty()) {
			throw new ResourceNotFoundException("Product not found with id: " + reviewDTO.getProductId());
		}

		// Check if user has already reviewed this product
		if (hasUserReviewedProduct(reviewDTO.getProductId(), user.getId())) {
			throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
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
	}

	@Override
	public ReviewDTO updateReview(ReviewDTO reviewDTO, String username) {
		// Find user by username
		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		Review review = reviewRepository.findById(reviewDTO.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewDTO.getId()));

		// Check if the review belongs to the user
		User user = userOpt.get();
		if (!review.getUserId().equals(user.getId())) {
			throw new BadRequestException("Bạn chỉ có thể cập nhật đánh giá của chính mình");
		}

		// Update review
		review.setRating(reviewDTO.getRating());
		review.setComment(reviewDTO.getComment());
		review.setUpdatedAt(new Date());

		Review updatedReview = reviewRepository.save(review);
		return reviewMapper.toDTO(updatedReview);
	}

	@Override
	public void deleteReview(Long reviewId, String username) {
		// Find user by username
		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

		// Check if the review belongs to the user or user is admin
		User user = userOpt.get();
		if (!review.getUserId().equals(user.getId())) {
			// Check if user is admin (simple role check)
			if (!"ADMIN".equals(user.getRole())) {
				throw new BadRequestException("Bạn chỉ có thể xóa đánh giá của chính mình");
			}
		}

		reviewRepository.delete(review);
	}

	@Override
	public Page<ReviewDTO> findAllWithFilters(Pageable pageable, Integer rating, Boolean reported, String keyword) {
		// Basic implementation - in real scenario, you'd use Criteria API or custom
		// queries
		Page<Review> reviewsPage = reviewRepository.findAll(pageable);

		if (rating != null || keyword != null) {
			// Filter implementation would go here
			// For simplicity, returning all reviews
		}

		return reviewsPage.map(reviewMapper::toDTO);
	}

	@Override
	public void adminDeleteReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

		reviewRepository.delete(review);
	}
}
