/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ShoppingCart, Heart, Star, Minus, Plus, Package, Shield, Truck } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import ReviewSection from '../components/ReviewSection';
import RelatedProducts from '../components/RelatedProducts';
import ImageLightbox from '../components/ImageLightbox';
import api, { getImageUrl } from '../api/api';
import toast from '../utils/toast';
import { isFavorite as isFav, toggleFavorite } from '../utils/favoritesUtils';
import './ProductDetailsPage.css';

function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { user: currentUser } = useAuth();
  
  const [product, setProduct] = useState(null);
  const [selectedImage, setSelectedImage] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);
  const [showLightbox, setShowLightbox] = useState(false);
  const [activeTab, setActiveTab] = useState('details'); // details, description, reviews
  const [reviews, setReviews] = useState([]);
  const [reviewStats, setReviewStats] = useState(null);

  const fetchReviews = useCallback(async () => {
    try {
      const response = await api.request(`/products/${id}/reviews?page=0&size=100&sortBy=createdAt&sortDirection=desc`);
      setReviews(response.content || []);
      
      // Calculate review stats from loaded content (totalElements from BE = Integer.MAX_VALUE)
      const totalReviews = response.content?.length || 0;
      const contentLength = response.content?.length || 0;
      const avgRating = contentLength > 0 
        ? response.content.reduce((sum, r) => sum + r.rating, 0) / contentLength
        : 0;
      
      const ratingBreakdown = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
      response.content?.forEach(r => {
        ratingBreakdown[r.rating] = (ratingBreakdown[r.rating] || 0) + 1;
      });
      
      setReviewStats({
        averageRating: avgRating,
        totalReviews: totalReviews,
        ratingBreakdown: ratingBreakdown
      });

      // Sync header stats with actual review data
      setProduct(prev => prev ? { ...prev, reviewCount: totalReviews, averageRating: avgRating } : prev);
    } catch (error) {
      console.error('Error fetching reviews:', error);
    }
  }, [id]);

  const fetchProductDetails = useCallback(async () => {
    setLoading(true);
    try {
      const productData = await api.getProductDetails(id);
      console.log('Product Details:', productData);
      setProduct(productData);
      setSelectedImage(0);
    } catch (error) {
      console.error('Error fetching product details:', error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchProductDetails();
    fetchReviews();
  }, [fetchProductDetails, fetchReviews]);

  // Sync heart state with localStorage whenever product changes
  useEffect(() => {
    if (product) {
      setIsFavorite(isFav(product.id));
    }
  }, [product]);

  // SEO: Update page title and meta description when product loads
  useEffect(() => {
    if (product) {
      document.title = `${product.name} | E-SHOP`;
      // Update/create meta description
      let metaDesc = document.querySelector('meta[name="description"]');
      if (!metaDesc) {
        metaDesc = document.createElement('meta');
        metaDesc.name = 'description';
        document.head.appendChild(metaDesc);
      }
      metaDesc.content = product.description
        ? product.description.replace(/<[^>]+>/g, '').slice(0, 160)
        : `Mua ${product.name} chính hãng tại E-SHOP với giá tốt nhất, giao hàng nhanh toàn quốc.`;
    }
    return () => {
      // Restore defaults when leaving the page
      document.title = 'E-SHOP';
      const metaDesc = document.querySelector('meta[name="description"]');
      if (metaDesc) metaDesc.content = 'Mua sắm điện tử chính hãng tại E-SHOP';
    };
  }, [product]);

  const handleAddToCart = () => {
    if (product) {
      addToCart(product.id, quantity);
    }
  };

  const handleBuyNow = () => {
    if (product) {
      addToCart(product.id, quantity);
      navigate('/cart');
    }
  };

  const increaseQuantity = () => {
    if (quantity < (product?.stockQuantity || 99)) {
      setQuantity(prev => prev + 1);
    }
  };

  const decreaseQuantity = () => {
    if (quantity > 1) {
      setQuantity(prev => prev - 1);
    }
  };

  const handleQuantityInput = (value) => {
    const numValue = parseInt(value) || 1;
    if (numValue < 1) {
      setQuantity(1);
    } else if (numValue > (product?.stockQuantity || 99)) {
      setQuantity(product?.stockQuantity || 99);
      toast.error(`Chỉ còn ${product?.stockQuantity || 99} sản phẩm trong kho!`);
    } else {
      setQuantity(numValue);
    }
  };

  const handleImageClick = () => {
    setShowLightbox(true);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <div className="bg-white rounded-lg shadow-sm p-8 animate-pulse">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div>
                <div className="bg-gray-300 h-96 rounded-lg mb-4"></div>
                <div className="flex gap-2">
                  {[...Array(4)].map((_, index) => (
                    <div key={index} className="bg-gray-300 h-20 w-20 rounded-lg"></div>
                  ))}
                </div>
              </div>
              <div>
                <div className="bg-gray-300 h-8 rounded mb-4"></div>
                <div className="bg-gray-300 h-6 rounded w-2/3 mb-6"></div>
                <div className="bg-gray-300 h-12 rounded w-1/2 mb-6"></div>
                <div className="bg-gray-300 h-32 rounded mb-6"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-2xl font-bold text-gray-800 mb-4">Không tìm thấy sản phẩm</h1>
          <button
            onClick={() => navigate('/products')}
            className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            Quay lại danh sách sản phẩm
          </button>
        </div>
      </div>
    );
  }

  const displayImages = product.productImages?.length > 0 
    ? product.productImages.map(img => ({
        url: getImageUrl(img.imageUrl),
        imageUrl: getImageUrl(img.imageUrl),
        altText: img.altText
      }))
    : product.imageUrl 
      ? [{ 
          url: getImageUrl(product.imageUrl),
          imageUrl: getImageUrl(product.imageUrl)
        }] 
      : [{ url: null }];

  const imageUrls = displayImages.map(img => img.url || img.imageUrl);

  return (
    <div className="product-details-page">
      <div className="container mx-auto px-4 py-6">
        {/* Breadcrumb */}
        <nav className="breadcrumb">
          <button onClick={() => navigate('/')} className="breadcrumb-link">Trang chủ</button>
          <span className="breadcrumb-separator">/</span>
          <button onClick={() => navigate('/products')} className="breadcrumb-link">Sản phẩm</button>
          <span className="breadcrumb-separator">/</span>
          <span className="breadcrumb-current">{product.name}</span>
        </nav>

        {/* Main Product Section - Shopee Style */}
        <div className="product-main-section">
          <div className="product-grid">
            {/* Left: Image Gallery */}
            <div className="product-image-section">
              <div className="product-main-image" onClick={handleImageClick}>
                <img
                  src={displayImages[selectedImage]?.url || displayImages[selectedImage]?.imageUrl || undefined}
                  alt={product.name}
                  className="main-image"
                />
                <div className="image-overlay">
                  <span className="zoom-hint">Click để phóng to</span>
                </div>
              </div>
              
              {displayImages.length > 1 && (
                <div className="product-thumbnails">
                  {displayImages.map((image, index) => (
                    <button
                      key={index}
                      onClick={() => setSelectedImage(index)}
                      className={`thumbnail ${selectedImage === index ? 'active' : ''}`}
                    >
                      <img
                        src={image.url || image.imageUrl || undefined}
                        alt={`${product.name} ${index + 1}`}
                      />
                    </button>
                  ))}
                </div>
              )}

              {/* Share Section */}
              <div className="share-section">
                <span className="share-label">Chia sẻ:</span>
                <div className="share-buttons">
                  <button className="share-btn" title="Facebook">
                    <svg width="20" height="20" fill="#3b5998" viewBox="0 0 24 24"><path d="M9 8h-3v4h3v12h5v-12h3.642l.358-4h-4v-1.667c0-.955.192-1.333 1.115-1.333h2.885v-5h-3.808c-3.596 0-5.192 1.583-5.192 4.615v3.385z"/></svg>
                  </button>
                  <button className="share-btn" title="Twitter">
                    <svg width="20" height="20" fill="#1da1f2" viewBox="0 0 24 24"><path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/></svg>
                  </button>
                  <button className="share-btn" title="Pinterest">
                    <svg width="20" height="20" fill="#bd081c" viewBox="0 0 24 24"><path d="M12 0c-6.627 0-12 5.372-12 12 0 5.084 3.163 9.426 7.627 11.174-.105-.949-.2-2.405.042-3.441.218-.937 1.407-5.965 1.407-5.965s-.359-.719-.359-1.782c0-1.668.967-2.914 2.171-2.914 1.023 0 1.518.769 1.518 1.69 0 1.029-.655 2.568-.994 3.995-.283 1.194.599 2.169 1.777 2.169 2.133 0 3.772-2.249 3.772-5.495 0-2.873-2.064-4.882-5.012-4.882-3.414 0-5.418 2.561-5.418 5.207 0 1.031.397 2.138.893 2.738.098.119.112.224.083.345l-.333 1.36c-.053.22-.174.267-.402.161-1.499-.698-2.436-2.889-2.436-4.649 0-3.785 2.75-7.262 7.929-7.262 4.163 0 7.398 2.967 7.398 6.931 0 4.136-2.607 7.464-6.227 7.464-1.216 0-2.359-.631-2.75-1.378l-.748 2.853c-.271 1.043-1.002 2.350-1.492 3.146 1.124.347 2.317.535 3.554.535 6.627 0 12-5.373 12-12 0-6.628-5.373-12-12-12z"/></svg>
                  </button>
                </div>
              </div>
            </div>

            {/* Right: Product Info */}
            <div className="product-info-section">
              {/* Product Name */}
              <h1 className="product-title">{product.name}</h1>
              
              {/* Rating & Stats */}
              <div className="product-stats">
                <div className="rating-display">
                  <span className="rating-number">{product.averageRating?.toFixed(1) || '0'}</span>
                  <div className="stars">
                    {[...Array(5)].map((_, index) => (
                      <Star
                        key={index}
                        size={16}
                        className={index < Math.floor(product.averageRating || 0) ? 'star-filled' : 'star-empty'}
                      />
                    ))}
                  </div>
                </div>
                <div className="stat-divider"></div>
                <div className="stat-item">
                  <span className="stat-number">{product.reviewCount || 0}</span>
                  <span className="stat-label">Đánh giá</span>
                </div>
                <div className="stat-divider"></div>
                <div className="stat-item">
                  <span className="stat-number">{product.soldCount || 0}</span>
                  <span className="stat-label">Đã bán</span>
                </div>
              </div>

              {/* Price Section */}
              <div className="price-section">
                <div className="price-row">
                  {product.salePrice && product.price > product.salePrice ? (
                    <>
                      <span className="original-price">{product.price.toLocaleString('vi-VN')}₫</span>
                      <span className="current-price">{product.salePrice.toLocaleString('vi-VN')}₫</span>
                      <span className="discount-badge">
                        {Math.round((1 - product.salePrice / product.price) * 100)}% GIẢM
                      </span>
                    </>
                  ) : (
                    <span className="current-price">{(product.effectivePrice || product.price)?.toLocaleString('vi-VN')}₫</span>
                  )}
                </div>
              </div>

              {/* Stock & Delivery Info */}
              <div className="info-row">
                <span className="info-label">Vận chuyển</span>
                <div className="info-content">
                  <Truck size={16} className="info-icon" />
                  <span>Miễn phí vận chuyển</span>
                </div>
              </div>

              <div className="info-row">
                <span className="info-label">Tình trạng</span>
                <div className="info-content">
                  <Package size={16} className={product.stockQuantity > 0 ? 'info-icon-success' : 'info-icon-danger'} />
                  <span className={product.stockQuantity > 0 ? 'text-success' : 'text-danger'}>
                    {product.stockQuantity > 0 ? `Còn ${product.stockQuantity} sản phẩm` : 'Hết hàng'}
                  </span>
                </div>
              </div>

              {/* Quantity Selector */}
              {product.stockQuantity > 0 && (
                <div className="info-row">
                  <span className="info-label">Số lượng</span>
                  <div className="quantity-selector">
                    <button
                      onClick={decreaseQuantity}
                      className="qty-btn"
                      disabled={quantity <= 1}
                    >
                      <Minus size={14} />
                    </button>
                    <input
                      type="number"
                      min="1"
                      max={product.stockQuantity}
                      value={quantity}
                      onChange={(e) => handleQuantityInput(e.target.value)}
                      className="qty-input"
                    />
                    <button
                      onClick={increaseQuantity}
                      className="qty-btn"
                      disabled={quantity >= product.stockQuantity}
                    >
                      <Plus size={14} />
                    </button>
                    <span className="stock-info">{product.stockQuantity} sản phẩm có sẵn</span>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="action-buttons">
                <button
                  onClick={() => {
                    if (product) {
                      const newState = toggleFavorite({
                        id: product.id,
                        name: product.name,
                        price: product.salePrice || product.price,
                        imageUrl: product.images?.[0]?.imageUrl || product.imageUrl || null,
                      });
                      setIsFavorite(newState);
                      toast.success(newState ? 'Đã thêm vào yêu thích' : 'Đã bỏ khỏi yêu thích');
                    }
                  }}
                  className={`btn-favorite ${isFavorite ? 'active' : ''}`}
                  title="Thêm vào yêu thích"
                >
                  <Heart size={20} className={isFavorite ? 'fill-current' : ''} />
                </button>
                
                {product.stockQuantity > 0 ? (
                  <>
                    <button
                      onClick={handleAddToCart}
                      className="btn-add-cart"
                    >
                      <ShoppingCart size={20} />
                      <span>Thêm vào giỏ hàng</span>
                    </button>
                    <button
                      onClick={handleBuyNow}
                      className="btn-buy-now"
                    >
                      Mua ngay
                    </button>
                  </>
                ) : (
                  <button disabled className="btn-out-of-stock">
                    Hết hàng
                  </button>
                )}
              </div>

              {/* Trust Badges */}
              <div className="trust-badges">
                <div className="badge-item">
                  <Shield size={20} className="badge-icon" />
                  <div className="badge-text">
                    <div className="badge-title">Bảo hành chính hãng</div>
                    <div className="badge-subtitle">12 tháng</div>
                  </div>
                </div>
                <div className="badge-item">
                  <Truck size={20} className="badge-icon" />
                  <div className="badge-text">
                    <div className="badge-title">Giao hàng nhanh</div>
                    <div className="badge-subtitle">2-24 giờ</div>
                  </div>
                </div>
                <div className="badge-item">
                  <Package size={20} className="badge-icon" />
                  <div className="badge-text">
                    <div className="badge-title">Đổi trả dễ dàng</div>
                    <div className="badge-subtitle">15 ngày</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tabbed Content Section */}
        <div className="product-tabs-section">
          <div className="tabs-header">
            <button
              className={`tab-btn ${activeTab === 'details' ? 'active' : ''}`}
              onClick={() => setActiveTab('details')}
            >
              Chi tiết sản phẩm
            </button>
            <button
              className={`tab-btn ${activeTab === 'description' ? 'active' : ''}`}
              onClick={() => setActiveTab('description')}
            >
              Mô tả sản phẩm
            </button>
            <button
              className={`tab-btn ${activeTab === 'reviews' ? 'active' : ''}`}
              onClick={() => setActiveTab('reviews')}
            >
              Đánh giá sản phẩm ({product.reviewCount || 0})
            </button>
          </div>

          <div className="tabs-content">
            {/* Chi tiết sản phẩm Tab */}
            {activeTab === 'details' && (
              <div className="tab-panel">
                {product.specifications && Object.keys(product.specifications).length > 0 ? (
                  <div className="specifications-table">
                    <h3 className="section-title">Thông số kỹ thuật</h3>
                    <table className="specs-table">
                      <tbody>
                        {Object.entries(product.specifications).map(([key, value]) => (
                          <tr key={key}>
                            <td className="spec-label">{key}</td>
                            <td className="spec-value">{value}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>Chưa có thông tin chi tiết</p>
                  </div>
                )}
              </div>
            )}

            {/* Mô tả sản phẩm Tab */}
            {activeTab === 'description' && (
              <div className="tab-panel">
                {product.description ? (
                  <div className="product-description">
                    <h3 className="section-title">Mô tả sản phẩm</h3>
                    <div className="description-content">
                      <p className="whitespace-pre-line">{product.description}</p>
                    </div>
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>Chưa có mô tả sản phẩm</p>
                  </div>
                )}
              </div>
            )}

            {/* Đánh giá sản phẩm Tab */}
            {activeTab === 'reviews' && (
              <div className="tab-panel">
                <ReviewSection 
                  productId={id}
                  reviews={reviews}
                  reviewStats={reviewStats}
                  currentUser={currentUser}
                  onReviewAdded={fetchReviews}
                />
              </div>
            )}
          </div>
        </div>

        {/* Related Products */}
        {(product.categoryId || product.brandId) && (
          <RelatedProducts 
            categoryId={product.categoryId}
            brandId={product.brandId}
            currentProductId={product.id} 
          />
        )}
      </div>

      {/* Image Lightbox */}
      {showLightbox && (
        <ImageLightbox
          images={imageUrls}
          initialIndex={selectedImage}
          onClose={() => setShowLightbox(false)}
        />
      )}
    </div>
  );
}

export default ProductDetailsPage;