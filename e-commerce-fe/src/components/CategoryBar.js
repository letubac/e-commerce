/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Smartphone, Laptop, Monitor, Headphones, Watch, Camera, Gamepad2, Tv, Shirt, ShoppingBag, Zap, Sparkles, Package, ChevronRight, ChevronLeft } from 'lucide-react';
import api from '../api/api';

// Icon mapping cho các danh mục - Style Shopee
const categoryIcons = {
  // Điện tử
  'Điện thoại': Smartphone,
  'Laptop': Laptop,
  'Tablet': Monitor,
  'Đồng hồ thông minh': Watch,
  'Đồng hồ': Watch,
  'Tai nghe': Headphones,
  'Máy ảnh': Camera,
  'TV': Tv,
  'Màn hình': Monitor,
  'Gaming': Gamepad2,
  'Phụ kiện điện tử': Sparkles,
  
  // Thời trang
  'Quần áo': Shirt,
  'Thời trang': Shirt,
  'Giày dép': ShoppingBag,
  'Túi xách': ShoppingBag,
  'Phụ kiện thời trang': Sparkles,
  
  // Khác
  'Flash Sale': Zap,
  'Sản phẩm mới': Package,
};

const CategoryBar = ({ onCategorySelect, selectedCategory }) => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);
  const scrollContainerRef = React.useRef(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    checkScroll();
  }, [categories]);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await api.getCategories();
      const cats = Array.isArray(data) ? data : (data?.content || []);
      setCategories(cats);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setCategories([]);
    } finally {
      setLoading(false);
    }
  };

  const checkScroll = () => {
    const container = scrollContainerRef.current;
    if (container) {
      setCanScrollLeft(container.scrollLeft > 0);
      setCanScrollRight(
        container.scrollLeft < container.scrollWidth - container.clientWidth - 10
      );
    }
  };

  const scroll = (direction) => {
    const container = scrollContainerRef.current;
    if (container) {
      const scrollAmount = 300;
      container.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth'
      });
      setTimeout(checkScroll, 300);
    }
  };

  if (loading) {
    return (
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="container mx-auto px-4">
          <div className="flex items-center space-x-4 py-4 overflow-x-auto">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className="flex-shrink-0 animate-pulse">
                <div className="w-20 h-20 bg-gray-200 rounded-lg"></div>
                <div className="w-16 h-3 bg-gray-200 rounded mt-2 mx-auto"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-sm border-b border-gray-200 sticky top-16 z-40">
      <div className="container mx-auto px-4 relative">
        <div className="flex items-center">
          {/* Left Scroll Button */}
          {canScrollLeft && (
            <button
              onClick={() => scroll('left')}
              className="absolute left-0 z-10 bg-white shadow-lg rounded-full p-2 hover:bg-gray-100 transition-colors"
              style={{ transform: 'translateX(-50%)' }}
            >
              <ChevronLeft size={20} className="text-gray-600" />
            </button>
          )}

          {/* Categories Container */}
          <div 
            ref={scrollContainerRef}
            onScroll={checkScroll}
            className="flex items-center space-x-2 py-3 overflow-x-auto scrollbar-hide"
            style={{
              scrollbarWidth: 'none',
              msOverflowStyle: 'none',
              WebkitOverflowScrolling: 'touch',
              scrollSnapType: 'x mandatory',
            }}
          >
            {/* All Products */}
            <button
              onClick={() => onCategorySelect(null)}
              className={`flex-shrink-0 flex flex-col items-center justify-center p-2 sm:p-3 rounded-lg transition-all duration-200 min-w-[72px] sm:min-w-[90px] touch-manipulation ${
                !selectedCategory 
                  ? 'bg-red-50 border-2 border-red-500 shadow-md transform scale-105' 
                  : 'hover:bg-gray-50 border-2 border-transparent'
              }`}
              style={{ scrollSnapAlign: 'start' }}
            >
              <div className={`w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center rounded-full mb-1.5 ${
                !selectedCategory ? 'bg-red-500' : 'bg-gray-100'
              }`}>
                <Package size={20} className={!selectedCategory ? 'text-white' : 'text-gray-600'} />
              </div>
              <span className={`text-xs font-medium text-center ${
                !selectedCategory ? 'text-red-600' : 'text-gray-700'
              }`}>
                Tất cả
              </span>
            </button>

            {/* Category Items */}
            {categories.map((category) => {
              const IconComponent = categoryIcons[category.name] || Package;
              const isSelected = selectedCategory?.id === category.id;

              return (
                <button
                  key={category.id}
                  onClick={() => onCategorySelect(category)}
                  className={`flex-shrink-0 flex flex-col items-center justify-center p-2 sm:p-3 rounded-lg transition-all duration-200 min-w-[72px] sm:min-w-[90px] touch-manipulation ${
                    isSelected 
                      ? 'bg-red-50 border-2 border-red-500 shadow-md transform scale-105' 
                      : 'hover:bg-gray-50 border-2 border-transparent'
                  }`}
                  style={{ scrollSnapAlign: 'start' }}
                >
                  <div className={`w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center rounded-full mb-1.5 ${
                    isSelected ? 'bg-red-500' : 'bg-gray-100'
                  }`}>
                    <IconComponent size={20} className={isSelected ? 'text-white' : 'text-gray-600'} />
                  </div>
                  <span className={`text-xs font-medium text-center line-clamp-2 ${
                    isSelected ? 'text-red-600' : 'text-gray-700'
                  }`}>
                    {category.name}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Right Scroll Button */}
          {canScrollRight && (
            <button
              onClick={() => scroll('right')}
              className="absolute right-0 z-10 bg-white shadow-lg rounded-full p-2 hover:bg-gray-100 transition-colors"
              style={{ transform: 'translateX(50%)' }}
            >
              <ChevronRight size={20} className="text-gray-600" />
            </button>
          )}
        </div>
      </div>

      <style jsx>{`
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    </div>
  );
};

export default CategoryBar;
