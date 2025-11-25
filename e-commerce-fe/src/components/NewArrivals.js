import React, { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, ShoppingCart, Eye } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';

export default function NewArrivals() {
  const navigate = useNavigate();
  const [newProducts, setNewProducts] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);

  const fetchNewProducts = useCallback(async () => {
    try {
      // Fetch new arrivals from API
      const response = await api.getProducts({ 
        sort: 'createdAt,desc',
        size: 8
      });
      setNewProducts(response.content || []);
    } catch (error) {
      console.error('Error fetching new products:', error);
      // Mock data cho sản phẩm mới về (fallback)
      const mockNewProducts = [
        {
          id: 101,
          name: 'MacBook Air M2 2024',
          price: 29990000,
          originalPrice: 32990000,
          discount: 9,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=MacBook+Air+M2+2024',
          brand: { name: 'Apple' },
          isNew: true,
          colors: ['+2 Màu sắc']
        },
        {
          id: 102,
          name: 'Asus ROG Strix G16',
          price: 38990000,
          originalPrice: 42990000,
          discount: 9,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=Asus+ROG+Strix+G16',
          brand: { name: 'Asus' },
          isNew: true,
          colors: ['+1 Màu sắc']
        },
        {
          id: 103,
          name: 'iPhone 15 Pro Max 256GB',
          price: 32990000,
          originalPrice: 35990000,
          discount: 8,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=iPhone+15+Pro+Max',
          brand: { name: 'Apple' },
          isNew: true,
          colors: ['+4 Màu sắc']
        },
        {
          id: 104,
          name: 'Samsung Galaxy S24 Ultra',
          price: 28990000,
          originalPrice: 31990000,
          discount: 9,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=Samsung+Galaxy+S24',
          brand: { name: 'Samsung' },
          isNew: true,
          colors: ['+3 Màu sắc']
        }
      ];
      setNewProducts(mockNewProducts);
    }
  }, []);

  useEffect(() => {
    fetchNewProducts();
  }, [fetchNewProducts]);

  const itemsPerSlide = 4;
  const totalSlides = Math.ceil(newProducts.length / itemsPerSlide);

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % totalSlides);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + totalSlides) % totalSlides);
  };

  const currentProducts = newProducts.slice(
    currentSlide * itemsPerSlide,
    (currentSlide + 1) * itemsPerSlide
  );

  if (newProducts.length === 0) {
    return null;
  }

  return (
    <div className="py-8 mb-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Sản phẩm mới về</h2>
          <button
            onClick={() => navigate('/products?sort=newest')}
            className="text-red-600 hover:text-red-700 font-medium"
          >
            Xem tất cả →
          </button>
        </div>

        {/* Products Grid */}
        <div className="relative">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {currentProducts.map((product) => (
              <div
                key={product.id}
                className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
              >
                <div className="relative">
                  <img
                    src={product.imageUrl || product.image || `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.name)}`}
                    alt={product.name}
                    className="w-full h-48 object-cover"
                    onError={(e) => {
                      e.target.src = `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.name)}`;
                    }}
                  />
                  {product.isNew && (
                    <div className="absolute top-2 left-2 bg-green-500 text-white px-2 py-1 rounded-full text-xs font-bold">
                      NEW
                    </div>
                  )}
                  {product.discount && product.discount > 0 && (
                    <div className="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded-full text-xs font-bold">
                      -{product.discount}%
                    </div>
                  )}
                  
                  {/* Hover Actions */}
                  <div className="absolute inset-0 bg-black bg-opacity-50 opacity-0 hover:opacity-100 transition-opacity flex items-center justify-center space-x-2">
                    <button
                      onClick={() => navigate(`/product/${product.id}`)}
                      className="bg-white text-gray-900 p-2 rounded-full hover:bg-gray-100 transition-colors"
                    >
                      <Eye className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => console.log('Add to cart:', product)}
                      className="bg-red-600 text-white p-2 rounded-full hover:bg-red-700 transition-colors"
                    >
                      <ShoppingCart className="h-4 w-4" />
                    </button>
                  </div>
                </div>
                
                <div className="p-4">
                  <h3 
                    className="font-semibold text-sm mb-2 line-clamp-2 cursor-pointer hover:text-red-600"
                    onClick={() => navigate(`/product/${product.id}`)}
                  >
                    {product.name}
                  </h3>
                  
                  <p className="text-xs text-gray-500 mb-2">{product.brand?.name || 'Chưa có thương hiệu'}</p>
                  
                  {product.colors && product.colors.length > 0 && (
                    <p className="text-xs text-gray-600 mb-2">{product.colors[0]}</p>
                  )}
                  
                  <div className="flex items-center space-x-2">
                    <span className="text-lg font-bold text-red-600">
                      {product.price ? product.price.toLocaleString('vi-VN') : '0'}đ
                    </span>
                    {product.originalPrice && product.originalPrice > product.price && (
                      <span className="text-sm text-gray-500 line-through">
                        {product.originalPrice.toLocaleString('vi-VN')}đ
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Navigation Arrows */}
          {totalSlides > 1 && (
            <>
              <button
                onClick={prevSlide}
                className="absolute left-0 top-1/2 transform -translate-y-1/2 -translate-x-4 bg-white text-gray-600 p-2 rounded-full shadow-lg hover:bg-gray-50 transition-colors"
              >
                <ChevronLeft className="h-6 w-6" />
              </button>
              <button
                onClick={nextSlide}
                className="absolute right-0 top-1/2 transform -translate-y-1/2 translate-x-4 bg-white text-gray-600 p-2 rounded-full shadow-lg hover:bg-gray-50 transition-colors"
              >
                <ChevronRight className="h-6 w-6" />
              </button>
            </>
          )}
        </div>

        {/* Slide Indicators */}
        {totalSlides > 1 && (
          <div className="flex justify-center mt-6 space-x-2">
            {[...Array(totalSlides)].map((_, index) => (
              <button
                key={index}
                onClick={() => setCurrentSlide(index)}
                className={`w-2 h-2 rounded-full transition-colors ${
                  currentSlide === index ? 'bg-red-600' : 'bg-gray-300'
                }`}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}