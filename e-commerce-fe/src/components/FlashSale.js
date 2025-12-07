import React, { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, ShoppingCart, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api, { API_BASE_URL } from '../api/api';

export default function FlashSale() {
  const navigate = useNavigate();
  const [flashSaleProducts, setFlashSaleProducts] = useState([]);
  const [activeFlashSale, setActiveFlashSale] = useState(null);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [timeLeft, setTimeLeft] = useState({
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0
  });
  const [loading, setLoading] = useState(true);

  const fetchFlashSaleData = useCallback(async () => {
    try {
      setLoading(true);
      // Fetch active Flash Sale
      const flashSaleData = await api.request('/flash-sale/active');
      if (flashSaleData) {
        setActiveFlashSale(flashSaleData);
        
        // Fetch Flash Sale products
        const productsData = await api.request('/flash-sale/products');
        const products = Array.isArray(productsData) ? productsData : [];
        setFlashSaleProducts(products);
      }
    } catch (error) {
      console.error('Error fetching flash sale data:', error);
      setFlashSaleProducts([]);
      setActiveFlashSale(null);
    } finally {
      setLoading(false);
    }
  }, []);

  // Update countdown timer based on active Flash Sale
  useEffect(() => {
    if (!activeFlashSale) return;

    const updateTimer = () => {
      const now = new Date().getTime();
      const endTime = new Date(activeFlashSale.endTime).getTime();
      const distance = endTime - now;

      if (distance > 0) {
        setTimeLeft({
          days: Math.floor(distance / (1000 * 60 * 60 * 24)),
          hours: Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
          minutes: Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60)),
          seconds: Math.floor((distance % (1000 * 60)) / 1000)
        });
      } else {
        // Flash sale ended, refresh data
        fetchFlashSaleData();
      }
    };

    updateTimer();
    const timer = setInterval(updateTimer, 1000);
    return () => clearInterval(timer);
  }, [activeFlashSale, fetchFlashSaleData]);

  // Fetch flash sale data on component mount
  useEffect(() => {
    fetchFlashSaleData();
  }, [fetchFlashSaleData]);

  const itemsPerSlide = 4;
  const totalSlides = Math.ceil(flashSaleProducts.length / itemsPerSlide);

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % totalSlides);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + totalSlides) % totalSlides);
  };

  const currentProducts = flashSaleProducts.slice(
    currentSlide * itemsPerSlide,
    (currentSlide + 1) * itemsPerSlide
  );

  if (loading) {
    return (
      <div className="bg-gradient-to-r from-red-600 to-red-700 text-white py-8 mb-8">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!activeFlashSale || flashSaleProducts.length === 0) {
    return null;
  }

  return (
    <div className="bg-gradient-to-r from-red-600 to-red-700 text-white py-8 mb-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-3">
            <Zap className="h-8 w-8 text-yellow-400" />
            <div>
              <h2 className="text-3xl font-bold">Flash Sale</h2>
              {activeFlashSale?.name && (
                <p className="text-sm text-white/80 mt-1">{activeFlashSale.name}</p>
              )}
            </div>
          </div>
          
          {/* Countdown Timer */}
          <div className="flex items-center space-x-4">
            <span className="text-lg font-medium">Kết thúc sau:</span>
            <div className="flex space-x-2">
              {[
                { label: 'Ngày', value: timeLeft.days },
                { label: 'Giờ', value: timeLeft.hours },
                { label: 'Phút', value: timeLeft.minutes },
                { label: 'Giây', value: timeLeft.seconds }
              ].map((unit, index) => (
                <div key={index} className="text-center">
                  <div className="bg-white text-red-600 px-3 py-2 rounded-lg font-bold text-lg min-w-[50px]">
                    {unit.value.toString().padStart(2, '0')}
                  </div>
                  <div className="text-xs mt-1">{unit.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Products Grid */}
        <div className="relative">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {currentProducts.map((product) => (
              <div
                key={product.id}
                className="bg-white rounded-lg shadow-lg overflow-hidden transform transition-transform hover:scale-105"
              >
                <div className="relative">
                  <img
                    src={
                      product.productImageUrl 
                        ? (product.productImageUrl.startsWith('http') 
                          ? product.productImageUrl 
                          : `${API_BASE_URL}/files${product.productImageUrl}`)
                        : `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.productName || product.name || 'Product')}`
                    }
                    alt={product.productName || product.name}
                    className="w-full h-48 object-cover cursor-pointer"
                    onClick={() => navigate(`/product/${product.productId || product.id}`)}
                    onError={(e) => {
                      e.target.src = `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.productName || product.name || 'Product')}`;
                    }}
                  />
                  <div className="absolute top-2 left-2 bg-red-500 text-white px-2 py-1 rounded-full text-sm font-bold">
                    -{product.discountPercentage 
                      ? Math.round(product.discountPercentage)
                      : (product.originalPrice && product.flashPrice 
                        ? Math.round(((product.originalPrice - product.flashPrice) / product.originalPrice) * 100)
                        : 0)}%
                  </div>
                  {product.remainingStock !== undefined && product.remainingStock <= 10 && (
                    <div className="absolute top-2 right-2 bg-yellow-500 text-white px-2 py-1 rounded text-xs font-semibold">
                      Còn {product.remainingStock}
                    </div>
                  )}
                </div>
                
                <div className="p-4 text-gray-900">
                  <h3 
                    className="font-semibold text-sm mb-2 line-clamp-2 cursor-pointer hover:text-red-600"
                    onClick={() => navigate(`/product/${product.productId || product.id}`)}
                  >
                    {product.productName || product.name}
                  </h3>
                  
                  <div className="flex items-center space-x-2 mb-3">
                    <span className="text-lg font-bold text-red-600">
                      {(product.flashPrice || product.salePrice || product.effectivePrice || product.price || 0).toLocaleString('vi-VN')}đ
                    </span>
                    {(product.originalPrice || product.price) && product.flashPrice && product.originalPrice > product.flashPrice && (
                      <span className="text-sm text-gray-500 line-through ml-2">
                        {product.originalPrice.toLocaleString('vi-VN')}đ
                      </span>
                    )}
                  </div>

                  {/* Stock Progress Bar */}
                  {product.stockLimit && (
                    <div className="mb-3">
                      <div className="flex justify-between text-xs text-gray-600 mb-1">
                        <span>Đã bán {product.stockSold || 0}</span>
                        <span>{product.stockLimit} sản phẩm</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div 
                          className="bg-red-500 h-2 rounded-full transition-all" 
                          style={{ width: `${Math.min(((product.stockSold || 0) / product.stockLimit) * 100, 100)}%` }}
                        ></div>
                      </div>
                    </div>
                  )}

                  <button
                    onClick={() => console.log('Add to cart:', product)}
                    disabled={product.soldOut || !product.canPurchase}
                    className={`w-full py-2 px-4 rounded-lg transition-colors flex items-center justify-center space-x-2 ${
                      product.soldOut || !product.canPurchase
                        ? 'bg-gray-400 cursor-not-allowed text-gray-700'
                        : 'bg-red-600 hover:bg-red-700 text-white'
                    }`}
                  >
                    <ShoppingCart className="h-4 w-4" />
                    <span>{product.soldOut ? 'Đã hết hàng' : 'Thêm vào giỏ'}</span>
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Navigation Arrows */}
          {totalSlides > 1 && (
            <>
              <button
                onClick={prevSlide}
                className="absolute left-0 top-1/2 transform -translate-y-1/2 -translate-x-4 bg-white text-red-600 p-2 rounded-full shadow-lg hover:bg-gray-50 transition-colors"
              >
                <ChevronLeft className="h-6 w-6" />
              </button>
              <button
                onClick={nextSlide}
                className="absolute right-0 top-1/2 transform -translate-y-1/2 translate-x-4 bg-white text-red-600 p-2 rounded-full shadow-lg hover:bg-gray-50 transition-colors"
              >
                <ChevronRight className="h-6 w-6" />
              </button>
            </>
          )}
        </div>

        {/* View All Button */}
        <div className="text-center mt-6">
          <button
            onClick={() => navigate('/flash-sale')}
            className="bg-white text-red-600 px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
          >
            Xem tất cả Flash Sale
          </button>
        </div>
      </div>
    </div>
  );
}