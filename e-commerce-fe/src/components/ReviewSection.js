import React, { useState } from 'react';
import { Star, MessageCircle, Edit, Trash2, Image as ImageIcon } from 'lucide-react';
import api from '../api/api';
import toast from '../utils/toast';
import './ReviewSection.css';

export default function ReviewSection({ productId, reviews = [], reviewStats = {}, onReviewAdded, currentUser }) {
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [newReview, setNewReview] = useState({
    rating: 5,
    comment: '',
    isAnonymous: false
  });
  const [submitting, setSubmitting] = useState(false);
  const [filterRating, setFilterRating] = useState(null); // null = all, 1-5 = specific rating

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      toast.error('Vui lòng đăng nhập để đánh giá sản phẩm');
      return;
    }

    if (!newReview.comment.trim()) {
      toast.error('Vui lòng nhập nội dung đánh giá');
      return;
    }

    setSubmitting(true);
    try {
      await api.createReview(productId, {
        ...newReview,
        productId: productId
      });
      setNewReview({ rating: 5, comment: '', isAnonymous: false });
      setShowReviewForm(false);
      onReviewAdded();
      toast.success('Đánh giá của bạn đã được gửi thành công!');
    } catch (error) {
      console.error('Error submitting review:', error);
      toast.error(error.message || 'Có lỗi xảy ra khi gửi đánh giá');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;
    
    try {
      await api.deleteReview(reviewId);
      onReviewAdded(); // Refresh reviews
      toast.success('Đã xóa đánh giá thành công');
    } catch (error) {
      console.error('Error deleting review:', error);
      toast.error('Có lỗi xảy ra khi xóa đánh giá');
    }
  };

  const renderStars = (rating, size = 16, interactive = false, onStarClick = null) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        size={size}
        className={`${
          i < rating ? 'star-filled' : 'star-empty'
        } ${interactive ? 'star-interactive' : ''}`}
        onClick={() => interactive && onStarClick && onStarClick(i + 1)}
      />
    ));
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Hôm nay';
    if (diffDays === 1) return 'Hôm qua';
    if (diffDays < 7) return `${diffDays} ngày trước`;
    
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  };

  const filteredReviews = filterRating 
    ? reviews.filter(r => r.rating === filterRating)
    : reviews;

  const totalReviews = reviewStats?.totalReviews || 0;
  const averageRating = reviewStats?.averageRating || 0;

  return (
    <div className="review-section-shopee">
      {/* Review Summary */}
      <div className="review-summary">
        <div className="review-summary-left">
          <div className="rating-overview">
            <div className="rating-score">
              <span className="rating-number">{averageRating.toFixed(1)}</span>
              <span className="rating-max">trên 5</span>
            </div>
            <div className="rating-stars">
              {renderStars(Math.round(averageRating), 18)}
            </div>
          </div>
        </div>

        <div className="review-summary-right">
          <div className="rating-bars">
            {[5, 4, 3, 2, 1].map(star => {
              const count = reviewStats?.ratingBreakdown?.[star] || 0;
              const percentage = totalReviews > 0 
                ? (count / totalReviews * 100).toFixed(0) 
                : 0;
              
              return (
                <button
                  key={star}
                  onClick={() => setFilterRating(filterRating === star ? null : star)}
                  className={`rating-bar-row ${filterRating === star ? 'active' : ''}`}
                >
                  <div className="rating-bar-label">
                    {renderStars(star, 12)}
                  </div>
                  <div className="rating-bar-container">
                    <div 
                      className="rating-bar-fill"
                      style={{ width: `${percentage}%` }}
                    ></div>
                  </div>
                  <div className="rating-bar-count">{count}</div>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Filter & Write Review */}
      <div className="review-actions">
        <div className="review-filters">
          <button
            onClick={() => setFilterRating(null)}
            className={`filter-btn ${filterRating === null ? 'active' : ''}`}
          >
            Tất cả ({totalReviews})
          </button>
          <button
            onClick={() => setFilterRating(5)}
            className={`filter-btn ${filterRating === 5 ? 'active' : ''}`}
          >
            5 sao ({reviewStats?.ratingBreakdown?.[5] || 0})
          </button>
          <button
            onClick={() => setFilterRating(4)}
            className={`filter-btn ${filterRating === 4 ? 'active' : ''}`}
          >
            4 sao ({reviewStats?.ratingBreakdown?.[4] || 0})
          </button>
          <button
            onClick={() => setFilterRating(3)}
            className={`filter-btn ${filterRating === 3 ? 'active' : ''}`}
          >
            3 sao ({reviewStats?.ratingBreakdown?.[3] || 0})
          </button>
          <button
            onClick={() => setFilterRating(2)}
            className={`filter-btn ${filterRating === 2 ? 'active' : ''}`}
          >
            2 sao ({reviewStats?.ratingBreakdown?.[2] || 0})
          </button>
          <button
            onClick={() => setFilterRating(1)}
            className={`filter-btn ${filterRating === 1 ? 'active' : ''}`}
          >
            1 sao ({reviewStats?.ratingBreakdown?.[1] || 0})
          </button>
        </div>

        {currentUser && !showReviewForm && (
          <button
            onClick={() => setShowReviewForm(true)}
            className="btn-write-review"
          >
            Viết đánh giá
          </button>
        )}
      </div>

      {/* Review Form */}
      {showReviewForm && (
        <div className="review-form-container">
          <div className="review-form-header">
            <h3>Đánh giá của bạn</h3>
          </div>
          <form onSubmit={handleSubmitReview} className="review-form">
            <div className="form-group">
              <label>Chất lượng sản phẩm</label>
              <div className="rating-input">
                {renderStars(newReview.rating, 32, true, (rating) => 
                  setNewReview(prev => ({ ...prev, rating }))
                )}
                <span className="rating-text">
                  {newReview.rating === 5 ? 'Tuyệt vời' :
                   newReview.rating === 4 ? 'Hài lòng' :
                   newReview.rating === 3 ? 'Bình thường' :
                   newReview.rating === 2 ? 'Không hài lòng' : 'Tệ'}
                </span>
              </div>
            </div>
            
            <div className="form-group">
              <label>Nhận xét của bạn *</label>
              <textarea
                value={newReview.comment}
                onChange={(e) => setNewReview(prev => ({ ...prev, comment: e.target.value }))}
                rows={5}
                className="review-textarea"
                placeholder="Hãy chia sẻ những điều bạn thích về sản phẩm này với những người mua khác nhé."
                required
              />
            </div>

            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={newReview.isAnonymous}
                  onChange={(e) => setNewReview(prev => ({ ...prev, isAnonymous: e.target.checked }))}
                  className="checkbox-input"
                />
                <span>Đăng với chế độ ẩn danh</span>
              </label>
            </div>
            
            <div className="form-actions">
              <button
                type="button"
                onClick={() => setShowReviewForm(false)}
                className="btn-cancel"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="btn-submit"
              >
                {submitting ? 'Đang gửi...' : 'Hoàn thành'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Reviews List */}
      <div className="reviews-list">
        {filteredReviews.length > 0 ? (
          filteredReviews.map((review) => (
            <div key={review.id} className="review-item">
              <div className="review-header">
                <div className="reviewer-info">
                  <div className="reviewer-avatar">
                    {review.isAnonymous ? 'A' : (review.userName?.[0]?.toUpperCase() || 'U')}
                  </div>
                  <div className="reviewer-details">
                    <div className="reviewer-name">{review.isAnonymous ? 'Người dùng ẩn danh' : (review.userName || 'Người dùng ẩn danh')}</div>
                    <div className="review-date">{formatDate(review.createdAt)}</div>
                  </div>
                </div>
                
                {currentUser && currentUser.username === review.userName && (
                  <button 
                    onClick={() => handleDeleteReview(review.id)}
                    className="btn-delete-review"
                    title="Xóa đánh giá"
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>
              
              <div className="review-rating">
                {renderStars(review.rating, 14)}
              </div>
              
              <div className="review-comment">
                {review.comment}
              </div>
            </div>
          ))
        ) : (
          <div className="empty-reviews">
            <MessageCircle size={64} className="empty-icon" />
            <h3>{filterRating ? `Chưa có đánh giá ${filterRating} sao` : 'Chưa có đánh giá nào'}</h3>
            <p>
              {filterRating 
                ? 'Hãy thử lọc theo số sao khác'
                : 'Hãy là người đầu tiên đánh giá sản phẩm này!'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}