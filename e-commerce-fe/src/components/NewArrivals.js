/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, ShoppingCart, Eye, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api, { getImageUrl } from '../api/api';
import SectionBlock from './SectionBlock';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200' viewBox='0 0 300 200'%3E%3Crect width='300' height='200' fill='%23f0f0f0'/%3E%3Ctext x='150' y='106' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='14'%3ENo Image%3C/text%3E%3C/svg%3E";

export default function NewArrivals() {
  const navigate = useNavigate();
  const [newProducts, setNewProducts] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);

  const fetchNewProducts = useCallback(async () => {
    try {
      // Fetch new arrivals from API
      const response = await api.getProducts({ 
        sortBy: 'createdAt',
        sortDirection: 'desc',
        size: 8
      });
      console.log('New Arrivals API Response:', response);
      // response có thể là { items, totalPages, totalElements, raw } hoặc trực tiếp data
      const products = response.items || response.content || response.data?.content || [];
      console.log('Parsed products:', products);
      setNewProducts(products);
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

  const itemsPerSlide = 5;
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
    return (
      <SectionBlock
        title="Sản phẩm mới về"
        icon={<Sparkles className="w-5 h-5 text-yellow-300 fill-yellow-300" />}
        gradient="from-green-500 to-teal-600"
      >
        <div className="text-center py-12">
          <p className="text-gray-400">Đang tải sản phẩm...</p>
        </div>
      </SectionBlock>
    );
  }

  return (
    <SectionBlock
      title="Sản phẩm mới về"
      icon={<Sparkles className="w-5 h-5 text-yellow-300 fill-yellow-300" />}
      onViewAll={() => navigate('/products?sort=newest')}
      gradient="from-green-500 to-teal-600"
    >
      <div className="relative px-8">
        {/* Left arrow */}
        {totalSlides > 1 && (
          <button
            onClick={prevSlide}
            className="absolute left-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow-lg rounded-full w-8 h-8 flex items-center justify-center hover:bg-gray-50 transition border border-gray-200"
          >
            <ChevronLeft className="h-5 w-5 text-gray-700" />
          </button>
        )}

        {/* Products */}
        <div className="flex justify-center gap-3">
          {currentProducts.map((product) => {
              // Lấy primary image hoặc image đầu tiên
              const primaryImage = product.productImages?.find(img => img.primary) || product.productImages?.[0];
              const imageUrl = primaryImage?.imageUrl || product.imageUrl || product.image;
              const fullImageUrl = getImageUrl(imageUrl) || PLACEHOLDER_IMG;
              
              // Tính discount nếu có salePrice
              const discount = product.salePrice && product.price 
                ? Math.round(((product.price - product.salePrice) / product.price) * 100)
                : 0;
              const displayPrice = product.salePrice || product.effectivePrice || product.price;
              
              return (
                <div
                  key={product.id}
                  className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow cursor-pointer flex-shrink-0"
                  style={{ width: '18%', minWidth: '140px', maxWidth: '200px' }}
                  onClick={() => navigate(`/product/${product.id}`)}
                >
                  <div className="relative group">
                    <img
                      src={fullImageUrl || PLACEHOLDER_IMG}
                      alt={primaryImage?.altText || product.name}
                      className="w-full object-cover"
                      style={{ height: '150px' }}
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = PLACEHOLDER_IMG;
                      }}
                    />
                    <div className="absolute top-1.5 left-1.5 bg-green-500 text-white px-1.5 py-0.5 rounded-full text-xs font-bold">
                      NEW
                    </div>
                    {discount > 0 && (
                      <div className="absolute top-1.5 right-1.5 bg-red-500 text-white px-1.5 py-0.5 rounded-full text-xs font-bold">
                        -{discount}%
                      </div>
                    )}
                    {/* Hover Actions */}
                    <div className="absolute inset-0 bg-black bg-opacity-40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                      <button
                        onClick={(e) => { e.stopPropagation(); navigate(`/product/${product.id}`); }}
                        className="bg-white text-gray-900 p-1.5 rounded-full hover:bg-gray-100 transition-colors"
                      >
                        <Eye className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={(e) => e.stopPropagation()}
                        className="bg-red-600 text-white p-1.5 rounded-full hover:bg-red-700 transition-colors"
                      >
                        <ShoppingCart className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>
                  
                  <div className="p-2.5">
                    <h3 className="font-semibold text-xs mb-1 line-clamp-2 hover:text-red-600">
                      {product.name}
                    </h3>
                    <p className="text-xs text-gray-400 mb-1">{product.brand?.name || ''}</p>
                    <p className="text-sm font-bold text-red-600">
                      {displayPrice ? displayPrice.toLocaleString('vi-VN') : '0'}đ
                    </p>
                    {product.salePrice && product.price > product.salePrice && (
                      <p className="text-xs text-gray-400 line-through">
                        {product.price.toLocaleString('vi-VN')}đ
                      </p>
                    )}
                  </div>
                </div>
              );
          })}
        </div>

        {/* Right arrow */}
        {totalSlides > 1 && (
          <button
            onClick={nextSlide}
            className="absolute right-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow-lg rounded-full w-8 h-8 flex items-center justify-center hover:bg-gray-50 transition border border-gray-200"
          >
            <ChevronRight className="h-5 w-5 text-gray-700" />
          </button>
        )}

        {/* Slide Indicators */}
        {totalSlides > 1 && (
          <div className="flex justify-center mt-4 gap-1.5">
            {[...Array(totalSlides)].map((_, index) => (
              <button
                key={index}
                onClick={() => setCurrentSlide(index)}
                className={`w-2 h-2 rounded-full transition-colors ${
                  currentSlide === index ? 'bg-teal-600' : 'bg-gray-300'
                }`}
              />
            ))}
          </div>
        )}
      </div>
    </SectionBlock>
  );
}