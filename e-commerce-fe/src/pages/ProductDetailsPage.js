import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ShoppingCart, Heart, Star, Minus, Plus, Package, Shield, Truck } from 'lucide-react';
import { useCart } from '../context/CartContext';
import ReviewSection from '../components/ReviewSection';
import api, { API_BASE_URL } from '../api/api';
import toast from '../utils/toast';

function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  
  const [product, setProduct] = useState(null);
  const [selectedImage, setSelectedImage] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);

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
  }, [fetchProductDetails]);

  const handleAddToCart = () => {
    if (product) {
      addToCart(product.id, quantity);
      alert('Đã thêm sản phẩm vào giỏ hàng!');
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
        url: img.imageUrl.startsWith('http') ? img.imageUrl : `${API_BASE_URL}/files${img.imageUrl}`,
        imageUrl: img.imageUrl.startsWith('http') ? img.imageUrl : `${API_BASE_URL}/files${img.imageUrl}`,
        altText: img.altText
      }))
    : product.imageUrl 
      ? [{ 
          url: product.imageUrl.startsWith('http') ? product.imageUrl : `${API_BASE_URL}/files${product.imageUrl}`,
          imageUrl: product.imageUrl.startsWith('http') ? product.imageUrl : `${API_BASE_URL}/files${product.imageUrl}`
        }] 
      : [{ url: 'https://via.placeholder.com/400x400/f0f0f0/666666?text=No+Image' }];

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="container mx-auto px-4">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <button onClick={() => navigate('/')} className="hover:text-red-600">Trang chủ</button>
          <span>/</span>
          <button onClick={() => navigate('/products')} className="hover:text-red-600">Sản phẩm</button>
          <span>/</span>
          <span className="text-gray-800">{product.name}</span>
        </nav>

        {/* Product Details */}
        <div className="bg-white rounded-lg shadow-sm p-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Product Images */}
            <div>
              <div className="mb-4">
                <img
                  src={displayImages[selectedImage]?.url || displayImages[selectedImage]?.imageUrl || '/images/placeholder-product.svg'}
                  alt={product.name}
                  className="w-full h-96 object-cover rounded-lg border border-gray-200"
                />
              </div>
              
              {displayImages.length > 1 && (
                <div className="flex gap-2 overflow-x-auto">
                  {displayImages.map((image, index) => (
                    <button
                      key={index}
                      onClick={() => setSelectedImage(index)}
                      className={`flex-shrink-0 w-20 h-20 border-2 rounded-lg overflow-hidden ${
                        selectedImage === index ? 'border-red-500' : 'border-gray-200'
                      }`}
                    >
                      <img
                        src={image.url || image.imageUrl || '/images/placeholder-product.svg'}
                        alt={`${product.name} ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Product Info */}
            <div>
              <h1 className="text-3xl font-bold text-gray-800 mb-4">{product.name}</h1>
              
              {/* Rating */}
              <div className="flex items-center mb-4">
                <div className="flex items-center">
                  {[...Array(5)].map((_, index) => (
                    <Star
                      key={index}
                      size={20}
                      className={index < Math.floor(product.averageRating || 0) ? 'text-yellow-400 fill-current' : 'text-gray-300'}
                    />
                  ))}
                </div>
                <span className="ml-2 text-gray-600">
                  ({product.averageRating?.toFixed(1) || '0'} - {product.reviewCount || 0} đánh giá)
                </span>
              </div>

              {/* Price */}
              <div className="mb-6">
                <div className="text-3xl font-bold text-red-600">
                  {(product.effectivePrice || product.price)?.toLocaleString('vi-VN')}₫
                </div>
                {product.salePrice && product.price > product.salePrice && (
                  <div className="flex items-center gap-2 mt-2">
                    <span className="text-gray-500 line-through">
                      {product.price.toLocaleString('vi-VN')}₫
                    </span>
                    <span className="bg-red-100 text-red-600 px-2 py-1 rounded text-sm font-semibold">
                      Giảm {Math.round((1 - product.salePrice / product.price) * 100)}%
                    </span>
                  </div>
                )}
              </div>

              {/* Stock Status */}
              <div className="mb-6">
                <div className="flex items-center gap-2 mb-2">
                  <Package size={20} className="text-green-600" />
                  <span className="font-semibold">Tình trạng kho:</span>
                  <span className={product.stockQuantity > 0 ? 'text-green-600' : 'text-red-600'}>
                    {product.stockQuantity > 0 ? `Còn ${product.stockQuantity} sản phẩm` : 'Hết hàng'}
                  </span>
                </div>
              </div>

              {/* Quantity Selector */}
              {product.stockQuantity > 0 && (
                <div className="mb-6">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Số lượng:
                  </label>
                  <div className="flex items-center gap-3">
                    <button
                      onClick={decreaseQuantity}
                      className="w-10 h-10 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-50"
                      disabled={quantity <= 1}
                    >
                      <Minus size={16} />
                    </button>
                    <span className="w-16 text-center font-semibold">{quantity}</span>
                    <button
                      onClick={increaseQuantity}
                      className="w-10 h-10 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-50"
                      disabled={quantity >= product.stockQuantity}
                    >
                      <Plus size={16} />
                    </button>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-4 mb-8">
                {product.stockQuantity > 0 ? (
                  <>
                    <button
                      onClick={handleAddToCart}
                      className="flex-1 flex items-center justify-center gap-2 px-6 py-3 border-2 border-red-600 text-red-600 rounded-lg hover:bg-red-50 transition"
                    >
                      <ShoppingCart size={20} />
                      Thêm vào giỏ
                    </button>
                    <button
                      onClick={handleBuyNow}
                      className="flex-1 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                    >
                      Mua ngay
                    </button>
                  </>
                ) : (
                  <button
                    disabled
                    className="flex-1 px-6 py-3 bg-gray-300 text-gray-500 rounded-lg cursor-not-allowed"
                  >
                    Hết hàng
                  </button>
                )}
                
                <button
                  onClick={() => setIsFavorite(!isFavorite)}
                  className={`px-6 py-3 border-2 rounded-lg transition ${
                    isFavorite
                      ? 'border-red-600 bg-red-50 text-red-600'
                      : 'border-gray-300 text-gray-600 hover:border-red-600 hover:text-red-600'
                  }`}
                >
                  <Heart size={20} className={isFavorite ? 'fill-current' : ''} />
                </button>
              </div>

              {/* Features */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg">
                  <Shield className="text-green-600" size={24} />
                  <div>
                    <div className="font-semibold text-sm">Bảo hành chính hãng</div>
                    <div className="text-xs text-gray-600">12 tháng</div>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg">
                  <Truck className="text-blue-600" size={24} />
                  <div>
                    <div className="font-semibold text-sm">Giao hàng nhanh</div>
                    <div className="text-xs text-gray-600">2-24h</div>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg">
                  <Package className="text-purple-600" size={24} />
                  <div>
                    <div className="font-semibold text-sm">Đổi trả dễ dàng</div>
                    <div className="text-xs text-gray-600">15 ngày</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Product Description */}
          {product.description && (
            <div className="mt-8 pt-8 border-t border-gray-200">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Mô tả sản phẩm</h3>
              <div className="prose max-w-none">
                <p className="text-gray-600 leading-relaxed whitespace-pre-line">
                  {product.description}
                </p>
              </div>
            </div>
          )}

          {/* Specifications */}
          {product.specifications && Object.keys(product.specifications).length > 0 && (
            <div className="mt-8 pt-8 border-t border-gray-200">
              <h3 className="text-xl font-bold text-gray-800 mb-4">Thông số kỹ thuật</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {Object.entries(product.specifications).map(([key, value]) => (
                  <div key={key} className="flex justify-between py-2 border-b border-gray-100">
                    <span className="font-medium text-gray-700">{key}:</span>
                    <span className="text-gray-600">{value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Reviews Section */}
        <div className="mt-8">
          <ReviewSection productId={id} />
        </div>
      </div>
    </div>
  );
}

export default ProductDetailsPage;