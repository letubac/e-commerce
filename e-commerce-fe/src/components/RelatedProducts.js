import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Star } from 'lucide-react';
import { API_BASE_URL } from '../api/api';
import api from '../api/api';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='300' viewBox='0 0 300 300'%3E%3Crect width='300' height='300' fill='%23f0f0f0'/%3E%3Ctext x='150' y='156' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='14'%3ENo Image%3C/text%3E%3C/svg%3E";

function RelatedProducts({ categoryId, currentProductId }) {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);

  const fetchRelatedProducts = useCallback(async () => {
    setLoading(true);
    try {
      // Fetch products from same category, exclude current product
      const response = await api.getProductsByCategory(categoryId, { page: 0, size: 12 });
      const filteredProducts = (response.content || []).filter(p => p.id !== currentProductId);
      setProducts(filteredProducts);
    } catch (error) {
      console.error('Error fetching related products:', error);
    } finally {
      setLoading(false);
    }
  }, [categoryId, currentProductId]);

  useEffect(() => {
    if (categoryId) {
      fetchRelatedProducts();
    }
  }, [categoryId, fetchRelatedProducts]);

  const itemsPerView = 4; // Show 4 products at a time
  const maxIndex = Math.max(0, products.length - itemsPerView);

  const handlePrev = () => {
    setCurrentIndex(prev => Math.max(0, prev - 1));
  };

  const handleNext = () => {
    setCurrentIndex(prev => Math.min(maxIndex, prev + 1));
  };

  if (loading) {
    return (
      <div className="py-12">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Sản phẩm liên quan</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className="bg-gray-200 rounded-lg h-72 animate-pulse"></div>
          ))}
        </div>
      </div>
    );
  }

  if (!products || products.length === 0) {
    return null;
  }

  return (
    <div className="py-12 bg-gray-50 -mx-4 px-4">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Sản phẩm liên quan</h2>
          <div className="flex gap-2">
            <button
              onClick={handlePrev}
              disabled={currentIndex === 0}
              className="w-10 h-10 border border-gray-300 rounded-full flex items-center justify-center hover:bg-white hover:shadow-md transition disabled:opacity-30 disabled:cursor-not-allowed"
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={handleNext}
              disabled={currentIndex >= maxIndex}
              className="w-10 h-10 border border-gray-300 rounded-full flex items-center justify-center hover:bg-white hover:shadow-md transition disabled:opacity-30 disabled:cursor-not-allowed"
            >
              <ChevronRight size={20} />
            </button>
          </div>
        </div>

        <div className="relative overflow-hidden">
          <div
            className="flex gap-4 transition-transform duration-300 ease-in-out"
            style={{
              transform: `translateX(-${currentIndex * (100 / itemsPerView)}%)`
            }}
          >
            {products.map((product) => (
              <div
                key={product.id}
                className="flex-shrink-0 w-1/4 min-w-[calc(25%-12px)]"
                style={{ flexBasis: `calc(${100 / itemsPerView}% - 12px)` }}
              >
                <div
                  onClick={() => navigate(`/product/${product.id}`)}
                  className="bg-white rounded-lg shadow-sm hover:shadow-lg transition cursor-pointer overflow-hidden h-full flex flex-col"
                >
                  {/* Product Image */}
                  <div className="relative pt-[100%] bg-gray-100">
                    <img
                      src={
                        product.imageUrl
                          ? `${API_BASE_URL}/files${product.imageUrl}`
                          : PLACEHOLDER_IMG
                      }
                      alt={product.name}
                      className="absolute inset-0 w-full h-full object-cover"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = PLACEHOLDER_IMG;
                      }}
                    />
                    {product.discount > 0 && (
                      <div className="absolute top-2 right-2 bg-red-600 text-white text-xs font-bold px-2 py-1 rounded">
                        -{product.discount}%
                      </div>
                    )}
                  </div>

                  {/* Product Info */}
                  <div className="p-3 flex-1 flex flex-col">
                    <h3 className="font-semibold text-gray-800 mb-2 line-clamp-2 text-sm min-h-[40px]">
                      {product.name}
                    </h3>

                    {/* Rating */}
                    {product.averageRating > 0 && (
                      <div className="flex items-center gap-1 mb-2">
                        <div className="flex">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              size={12}
                              className={i < Math.floor(product.averageRating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}
                            />
                          ))}
                        </div>
                        <span className="text-xs text-gray-500">
                          ({product.reviewCount || 0})
                        </span>
                      </div>
                    )}

                    {/* Price */}
                    <div className="mt-auto">
                      <div className="flex items-baseline gap-2">
                        <span className="text-red-600 font-bold text-lg">
                          {(product.price || 0).toLocaleString('vi-VN')}₫
                        </span>
                        {product.originalPrice && product.originalPrice > product.price && (
                          <span className="text-gray-400 text-sm line-through">
                            {product.originalPrice.toLocaleString('vi-VN')}₫
                          </span>
                        )}
                      </div>

                      {/* Stock Status */}
                      <div className="mt-2 text-xs">
                        {product.stockQuantity > 0 ? (
                          <span className="text-green-600">Còn {product.stockQuantity} sản phẩm</span>
                        ) : (
                          <span className="text-red-600">Hết hàng</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Dots Indicator */}
        {products.length > itemsPerView && (
          <div className="flex justify-center gap-2 mt-6">
            {[...Array(maxIndex + 1)].map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrentIndex(i)}
                className={`w-2 h-2 rounded-full transition ${
                  i === currentIndex ? 'bg-red-600 w-6' : 'bg-gray-300'
                }`}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default RelatedProducts;
