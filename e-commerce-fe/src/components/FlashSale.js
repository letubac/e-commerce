import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { ChevronLeft, ChevronRight, ShoppingCart, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';

export default function FlashSale() {
  const navigate = useNavigate();
  const [flashSaleProducts, setFlashSaleProducts] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [timeLeft, setTimeLeft] = useState({
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0
  });

  // Flash sale end time (example: 24 hours from now)
  const flashSaleEndTime = useMemo(() => {
    const endTime = new Date();
    endTime.setHours(endTime.getHours() + 24);
    return endTime;
  }, []);

  const fetchFlashSaleProducts = useCallback(async () => {
    try {
      // Fetch flash sale products from API
      const response = await api.getProducts({ 
        category: 'flash-sale',
        size: 10,
        sort: 'discount,desc'
      });
      setFlashSaleProducts(response.content || []);
    } catch (error) {
      console.error('Error fetching flash sale products:', error);
      // Fallback to mock data if API fails
      const mockProducts = [
        {
          id: 1,
          name: 'ASUS ROG Strix GeForce RTX 4080 SUPER OC 16GB',
          price: 22990000,
          originalPrice: 25990000,
          discount: 12,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=ASUS+ROG+RTX+4080',
          brand: { name: 'ASUS' },
          stock: 15,
          totalStock: 50
        },
        {
          id: 2,
          name: 'AMD Ryzen 9 7950X 16-Core, 32-Thread AM5',
          price: 13490000,
          originalPrice: 15990000,
          discount: 16,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=AMD+Ryzen+9+7950X',
          brand: { name: 'AMD' },
          stock: 8,
          totalStock: 30
        },
        {
          id: 3,
          name: 'Samsung 980 PRO 2TB NVMe M.2 SSD PCIe 4.0',
          price: 4590000,
          originalPrice: 5990000,
          discount: 23,
          imageUrl: 'https://via.placeholder.com/300x200/f0f0f0/666666?text=Samsung+980+PRO+SSD',
          brand: { name: 'Samsung' },
          stock: 25,
          totalStock: 100
        }
      ];
      setFlashSaleProducts(mockProducts);
    }
  }, []);

  // Update countdown timer
  useEffect(() => {
    const updateTimer = () => {
      const now = new Date().getTime();
      const distance = flashSaleEndTime.getTime() - now;

      if (distance > 0) {
        setTimeLeft({
          days: Math.floor(distance / (1000 * 60 * 60 * 24)),
          hours: Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
          minutes: Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60)),
          seconds: Math.floor((distance % (1000 * 60)) / 1000)
        });
      }
    };

    updateTimer();
    const timer = setInterval(updateTimer, 1000);
    return () => clearInterval(timer);
  }, [flashSaleEndTime]);

  // Fetch products on component mount
  useEffect(() => {
    fetchFlashSaleProducts();
  }, [fetchFlashSaleProducts]);

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

  if (flashSaleProducts.length === 0) {
    return null;
  }

  return (
    <div className="bg-gradient-to-r from-red-600 to-red-700 text-white py-8 mb-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-3">
            <Zap className="h-8 w-8 text-yellow-400" />
            <h2 className="text-3xl font-bold">Flash Sale</h2>
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
                      product.images && product.images.length > 0 
                        ? product.images.find(img => img.isPrimary)?.imageUrl || product.images[0]?.imageUrl
                        : product.imageUrl || product.image || `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.name)}`
                    }
                    alt={product.name}
                    className="w-full h-48 object-cover cursor-pointer"
                    onClick={() => navigate(`/product/${product.id}`)}
                    onError={(e) => {
                      e.target.src = `https://via.placeholder.com/300x200/f0f0f0/666666?text=${encodeURIComponent(product.name)}`;
                    }}
                  />
                  <div className="absolute top-2 left-2 bg-red-500 text-white px-2 py-1 rounded-full text-sm font-bold">
                    -{product.discount || 
                      (product.compareAtPrice && product.price 
                        ? Math.round(((product.compareAtPrice - product.price) / product.compareAtPrice) * 100)
                        : 0)}%
                  </div>
                </div>
                
                <div className="p-4 text-gray-900">
                  <h3 
                    className="font-semibold text-sm mb-2 line-clamp-2 cursor-pointer hover:text-red-600"
                    onClick={() => navigate(`/product/${product.id}`)}
                  >
                    {product.name}
                  </h3>
                  
                  <div className="flex items-center space-x-2 mb-3">
                    <span className="text-lg font-bold text-red-600">
                      {product.salePrice ? product.salePrice.toLocaleString('vi-VN') : (product.price ? product.price.toLocaleString('vi-VN') : '0')}đ
                    </span>
                    {product.salePrice && (
                      <span className="text-sm text-gray-500 line-through ml-2">
                        {product.price.toLocaleString('vi-VN')}đ
                      </span>
                    )}
                  </div>

                  <button
                    onClick={() => console.log('Add to cart:', product)}
                    className="w-full bg-red-600 hover:bg-red-700 text-white py-2 px-4 rounded-lg transition-colors flex items-center justify-center space-x-2"
                  >
                    <ShoppingCart className="h-4 w-4" />
                    <span>Thêm vào giỏ</span>
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