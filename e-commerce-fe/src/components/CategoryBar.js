/**
 * author: LeTuBac
 */
import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  Smartphone, Laptop, Monitor, Headphones, Watch, Camera, Gamepad2, Tv,
  Shirt, ShoppingBag, Zap, Sparkles, Package, ChevronRight, ChevronLeft, ChevronDown
} from 'lucide-react';
import api from '../api/api';

// Icon mapping
const categoryIcons = {
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
  'Quần áo': Shirt,
  'Thời trang': Shirt,
  'Giày dép': ShoppingBag,
  'Túi xách': ShoppingBag,
  'Phụ kiện thời trang': Sparkles,
  'Flash Sale': Zap,
  'Sản phẩm mới': Package,
};

// Build tree: group children under their parents
const buildTree = (flatList) => {
  const parents = flatList.filter(c => !c.parentId);
  const childMap = {};
  flatList.forEach(c => {
    if (c.parentId) {
      if (!childMap[c.parentId]) childMap[c.parentId] = [];
      childMap[c.parentId].push(c);
    }
  });
  return parents.map(p => ({ ...p, children: childMap[p.id] || [] }));
};

// Single category item with hover dropdown
const CategoryItem = ({ category, isSelected, onSelect, scrollSnapAlign }) => {
  const [open, setOpen] = useState(false);
  const timerRef = useRef(null);
  const hasChildren = category.children && category.children.length > 0;
  const IconComponent = categoryIcons[category.name] || Package;

  const handleMouseEnter = () => {
    clearTimeout(timerRef.current);
    if (hasChildren) setOpen(true);
  };
  const handleMouseLeave = () => {
    timerRef.current = setTimeout(() => setOpen(false), 150);
  };

  return (
    <div
      className="relative flex-shrink-0"
      style={{ scrollSnapAlign }}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <button
        onClick={() => onSelect(category)}
        className={`flex flex-col items-center justify-center p-2 sm:p-3 rounded-lg transition-all duration-200 min-w-[72px] sm:min-w-[90px] touch-manipulation w-full ${
          isSelected
            ? 'bg-red-50 border-2 border-red-500 shadow-md'
            : 'hover:bg-gray-50 border-2 border-transparent'
        }`}
      >
        <div className={`w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center rounded-full mb-1 ${
          isSelected ? 'bg-red-500' : 'bg-gray-100'
        }`}>
          <IconComponent size={20} className={isSelected ? 'text-white' : 'text-red-500'} />
        </div>
        <span className={`text-xs font-medium text-center line-clamp-2 leading-tight ${
          isSelected ? 'text-red-600' : 'text-gray-700'
        }`}>
          {category.name}
        </span>
        {hasChildren && (
          <ChevronDown
            size={12}
            className={`mt-0.5 transition-transform duration-200 ${
              open ? 'rotate-180 text-red-500' : 'text-gray-400'
            }`}
          />
        )}
      </button>

      {/* Dropdown */}
      {hasChildren && open && (
        <div
          className="absolute top-full left-1/2 -translate-x-1/2 bg-white rounded-xl shadow-2xl border border-gray-100 z-50 min-w-[160px] overflow-hidden"
          style={{ marginTop: '2px' }}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
        >
          {/* Arrow */}
          <div className="absolute -top-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-white border-l border-t border-gray-100 rotate-45" />
          <div className="pt-2 pb-1">
            {category.children.map((child) => {
              const ChildIcon = categoryIcons[child.name] || Package;
              return (
                <button
                  key={child.id}
                  onClick={() => { onSelect(child); setOpen(false); }}
                  className="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-red-50 hover:text-red-600 transition-colors text-left"
                >
                  <ChildIcon size={14} className="flex-shrink-0 text-red-400" />
                  <span className="truncate">{child.name}</span>
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

const CategoryBar = ({ onCategorySelect, selectedCategory }) => {
  const [tree, setTree] = useState([]);
  const [loading, setLoading] = useState(true);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);
  const scrollContainerRef = useRef(null);

  const checkScroll = useCallback(() => {
    const container = scrollContainerRef.current;
    if (container) {
      setCanScrollLeft(container.scrollLeft > 0);
      setCanScrollRight(container.scrollLeft < container.scrollWidth - container.clientWidth - 10);
    }
  }, []);

  const scroll = (direction) => {
    const container = scrollContainerRef.current;
    if (container) {
      container.scrollBy({ left: direction === 'left' ? -300 : 300, behavior: 'smooth' });
      setTimeout(checkScroll, 300);
    }
  };

  useEffect(() => {
    fetchCategories();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    checkScroll();
  }, [tree, checkScroll]);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await api.getCategories();
      const flat = Array.isArray(data) ? data : (data?.content || []);
      setTree(buildTree(flat));
    } catch (error) {
      console.error('Error fetching categories:', error);
      setTree([]);
    } finally {
      setLoading(false);
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
            className="flex items-center space-x-1 py-2 overflow-x-auto"
            style={{
              scrollbarWidth: 'none',
              msOverflowStyle: 'none',
              WebkitOverflowScrolling: 'touch',
              scrollSnapType: 'x mandatory',
            }}
          >
            {/* All Products */}
            <div className="relative flex-shrink-0" style={{ scrollSnapAlign: 'start' }}>
              <button
                onClick={() => onCategorySelect(null)}
                className={`flex flex-col items-center justify-center p-2 sm:p-3 rounded-lg transition-all duration-200 min-w-[72px] sm:min-w-[90px] touch-manipulation ${
                  !selectedCategory
                    ? 'bg-red-50 border-2 border-red-500 shadow-md'
                    : 'hover:bg-gray-50 border-2 border-transparent'
                }`}
              >
                <div className={`w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center rounded-full mb-1 ${
                  !selectedCategory ? 'bg-red-500' : 'bg-gray-100'
                }`}>
                  <Package size={20} className={!selectedCategory ? 'text-white' : 'text-red-500'} />
                </div>
                <span className={`text-xs font-medium text-center ${
                  !selectedCategory ? 'text-red-600' : 'text-gray-700'
                }`}>
                  Tất cả
                </span>
              </button>
            </div>

            {/* Parent categories with dropdown children */}
            {tree.map((category) => (
              <CategoryItem
                key={category.id}
                category={category}
                isSelected={
                  selectedCategory?.id === category.id ||
                  category.children.some(c => c.id === selectedCategory?.id)
                }
                onSelect={onCategorySelect}
                scrollSnapAlign="start"
              />
            ))}
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

      <style>{`
        .scrollbar-hide::-webkit-scrollbar { display: none; }
      `}</style>
    </div>
  );
};

export default CategoryBar;
