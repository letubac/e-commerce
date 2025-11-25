import React, { useState } from 'react';
import { Star, MessageCircle, ThumbsUp, ThumbsDown, Flag, Edit, Trash2 } from 'lucide-react';
import api from '../api/api';

export default function ReviewSection({ productId, reviews = [], reviewStats = {}, onReviewAdded, currentUser }) {
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [newReview, setNewReview] = useState({
    rating: 5,
    comment: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [sortBy, setSortBy] = useState('newest');

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      alert('Vui lòng đăng nhập để đánh giá sản phẩm');
      return;
    }

    setSubmitting(true);
    try {
      await api.createReview(productId, newReview);
      setNewReview({ rating: 5, comment: '' });
      setShowReviewForm(false);
      onReviewAdded();
      alert('Đánh giá của bạn đã được gửi thành công!');
    } catch (error) {
      console.error('Error submitting review:', error);
      alert(error.message || 'Có lỗi xảy ra khi gửi đánh giá');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;
    
    try {
      await api.deleteReview(reviewId);
      onReviewAdded(); // Refresh reviews
      alert('Đã xóa đánh giá thành công');
    } catch (error) {
      console.error('Error deleting review:', error);
      alert('Có lỗi xảy ra khi xóa đánh giá');
    }
  };

  const renderStars = (rating, size = 16, interactive = false, onStarClick = null) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        size={size}
        className={`${
          i < rating ? 'text-yellow-400 fill-current' : 'text-gray-300'
        } ${interactive ? 'cursor-pointer hover:text-yellow-400' : ''}`}
        onClick={() => interactive && onStarClick && onStarClick(i + 1)}
      />
    ));
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const sortedReviews = Array.isArray(reviews) ? [...reviews].sort((a, b) => {
    switch (sortBy) {
      case 'newest':
        return new Date(b.createdAt) - new Date(a.createdAt);
      case 'oldest':
        return new Date(a.createdAt) - new Date(b.createdAt);
      case 'highest':
        return b.rating - a.rating;
      case 'lowest':
        return a.rating - b.rating;
      default:
        return 0;
    }
  }) : [];

  return (
    <div className="border-t pt-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">Đánh giá sản phẩm</h2>
        {currentUser && !showReviewForm && (
          <button
            onClick={() => setShowReviewForm(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center space-x-2"
          >
            <MessageCircle size={16} />
            <span>Viết đánh giá</span>
          </button>
        )}
      </div>

      {/* Review Stats */}
      {reviewStats && (
        <div className="bg-gray-50 rounded-lg p-6 mb-6">
          <div className="grid md:grid-cols-2 gap-6">
            <div className="text-center">
              <div className="text-4xl font-bold text-blue-600 mb-2">
                {reviewStats.averageRating?.toFixed(1) || '0.0'}
              </div>
              <div className="flex items-center justify-center space-x-1 mb-2">
                {renderStars(Math.round(reviewStats.averageRating || 0), 20)}
              </div>
              <div className="text-gray-600">
                Trung bình từ {reviewStats.totalReviews} đánh giá
              </div>
            </div>
            
            <div className="space-y-2">
              {[5, 4, 3, 2, 1].map(star => {
                const count = reviewStats.ratingBreakdown?.[star] || 0;
                const percentage = reviewStats.totalReviews > 0 
                  ? (count / reviewStats.totalReviews * 100).toFixed(0) 
                  : 0;
                
                return (
                  <div key={star} className="flex items-center space-x-2">
                    <span className="text-sm w-8">{star} sao</span>
                    <div className="flex-1 bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-yellow-400 h-2 rounded-full"
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                    <span className="text-sm text-gray-600 w-12">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Review Form */}
      {showReviewForm && (
        <div className="bg-white border rounded-lg p-6 mb-6">
          <h3 className="text-lg font-semibold mb-4">Viết đánh giá của bạn</h3>
          <form onSubmit={handleSubmitReview} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Đánh giá</label>
              <div className="flex items-center space-x-1">
                {renderStars(newReview.rating, 24, true, (rating) => 
                  setNewReview(prev => ({ ...prev, rating }))
                )}
                <span className="ml-2 text-sm text-gray-600">
                  ({newReview.rating} sao)
                </span>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-2">Nhận xét</label>
              <textarea
                value={newReview.comment}
                onChange={(e) => setNewReview(prev => ({ ...prev, comment: e.target.value }))}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
                required
              />
            </div>
            
            <div className="flex space-x-3">
              <button
                type="submit"
                disabled={submitting}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300"
              >
                {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
              </button>
              <button
                type="button"
                onClick={() => setShowReviewForm(false)}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Hủy
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Sort Controls */}
      {reviews.length > 0 && (
        <div className="flex items-center space-x-4 mb-6">
          <span className="text-sm font-medium">Sắp xếp theo:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-3 py-1 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-blue-500"
          >
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="highest">Đánh giá cao nhất</option>
            <option value="lowest">Đánh giá thấp nhất</option>
          </select>
        </div>
      )}

      {/* Reviews List */}
      <div className="space-y-6">
        {sortedReviews.length > 0 ? (
          sortedReviews.map((review) => (
            <div key={review.id} className="border-b pb-6 last:border-b-0">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-gradient-to-r from-blue-400 to-purple-400 rounded-full flex items-center justify-center text-white font-bold">
                    {review.user?.fullName?.[0] || 'U'}
                  </div>
                  <div>
                    <div className="font-medium">{review.user?.fullName || 'Người dùng ẩn danh'}</div>
                    <div className="flex items-center space-x-2">
                      <div className="flex items-center space-x-1">
                        {renderStars(review.rating)}
                      </div>
                      <span className="text-sm text-gray-600">
                        {formatDate(review.createdAt)}
                      </span>
                    </div>
                  </div>
                </div>
                
                {currentUser && currentUser.id === review.user?.id && (
                  <div className="flex items-center space-x-2">
                    <button className="p-1 text-gray-400 hover:text-blue-600">
                      <Edit size={16} />
                    </button>
                    <button 
                      onClick={() => handleDeleteReview(review.id)}
                      className="p-1 text-gray-400 hover:text-red-600"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                )}
              </div>
              
              <p className="text-gray-700 mb-3 leading-relaxed">{review.comment}</p>
              
              <div className="flex items-center space-x-4 text-sm">
                <button className="flex items-center space-x-1 text-gray-500 hover:text-blue-600">
                  <ThumbsUp size={14} />
                  <span>Hữu ích ({review.helpfulCount || 0})</span>
                </button>
                <button className="flex items-center space-x-1 text-gray-500 hover:text-red-600">
                  <ThumbsDown size={14} />
                  <span>Không hữu ích</span>
                </button>
                <button className="flex items-center space-x-1 text-gray-500 hover:text-orange-600">
                  <Flag size={14} />
                  <span>Báo cáo</span>
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8">
            <MessageCircle size={48} className="text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              Chưa có đánh giá nào
            </h3>
            <p className="text-gray-600">
              Hãy là người đầu tiên đánh giá sản phẩm này!
            </p>
          </div>
        )}
      </div>
    </div>
  );
}