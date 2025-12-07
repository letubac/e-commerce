import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown, Cpu, Monitor, HardDrive, MemoryStick, Gamepad2, Mouse, Headphones, Keyboard, Zap, Fan, Smartphone, Laptop, Watch, Shirt, Sparkles } from 'lucide-react';
import api from '../api/api';

// Icon mapping cho các danh mục
const categoryIcons = {
  // Điện tử
  'Điện thoại': Smartphone,
  'Laptop': Laptop,
  'Tablet': Monitor,
  'Đồng hồ thông minh': Watch,
  'Tai nghe': Headphones,
  'Phụ kiện điện tử': Sparkles,
  
  // Máy tính
  'CPU': Cpu,
  'Card Màn hình': Monitor, 
  'Bo mạch chủ': HardDrive,
  'Ram': MemoryStick,
  'Nguồn - PSU': Zap,
  'SSD & HDD': HardDrive,
  'CASE': Monitor,
  'Tản nhiệt': Fan,
  'Tản nhiệt khí': Fan,
  'Tản nhiệt nước': Fan,
  'Linh kiện máy tính': Cpu,
  'Màn hình': Monitor,
  'Máy tính bàn': Monitor,
  'Bàn phím': Keyboard,
  'Chuột': Mouse,
  'Bàn di chuột': Mouse,
  'ARM giá treo': Monitor,
  'Âm Thanh': Headphones,
  'Bàn & Ghế': Monitor,
  'Phụ kiện máy tính': Gamepad2,
  
  // Thời trang
  'Quần áo': Shirt,
  'Giày dép': Shirt,
  'Túi xách': Shirt,
  'Phụ kiện thời trang': Shirt,
};

export default function Sidebar({ onCategorySelect, onBrandSelect, selectedCategory, selectedBrand }) {
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [expandedCategories, setExpandedCategories] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCategoriesAndBrands();
  }, []);

  const fetchCategoriesAndBrands = async () => {
    try {
      setLoading(true);
      const [categoriesData, brandsData] = await Promise.all([
        api.getCategories(),
        api.getBrands()
      ]);
      
      // parseBusinessResponse đã trả về data trực tiếp
      const cats = Array.isArray(categoriesData) ? categoriesData : (categoriesData?.content || []);
      const brds = Array.isArray(brandsData) ? brandsData : (brandsData?.content || []);
      
      setCategories(cats);
      setBrands(brds);
    } catch (error) {
      console.error('Error fetching categories and brands:', error);
      setCategories([]);
      setBrands([]);
    } finally {
      setLoading(false);
    }
  };

  const toggleCategory = (categoryId) => {
    setExpandedCategories(prev => ({
      ...prev,
      [categoryId]: !prev[categoryId]
    }));
  };

  const handleCategoryClick = (category) => {
    if (category.children && category.children.length > 0) {
      toggleCategory(category.id);
    } else {
      onCategorySelect(category);
    }
  };

  const renderCategory = (category, level = 0) => {
    const hasChildren = category.children && category.children.length > 0;
    const isExpanded = expandedCategories[category.id];
    const IconComponent = categoryIcons[category?.name] || Monitor;
    const isSelected = selectedCategory?.id === category.id;

    return (
      <div key={category.id} className={`${level > 0 ? 'ml-4' : ''}`}>
        <button
          onClick={() => handleCategoryClick(category)}
          className={`w-full flex items-center justify-between py-2 px-3 text-left hover:bg-gray-100 rounded-lg transition-colors ${
            isSelected ? 'bg-red-50 text-red-600 border-l-4 border-red-500' : 'text-gray-700'
          }`}
        >
          <div className="flex items-center space-x-2">
            {level === 0 && <IconComponent size={16} className={isSelected ? 'text-red-600' : 'text-gray-500'} />}
            <span className={`text-sm ${level > 0 ? 'text-xs' : ''} ${isSelected ? 'font-semibold' : ''}`}>
              {category?.name || 'Chưa có tên'}
            </span>
          </div>
          {hasChildren && (
            isExpanded ? 
              <ChevronDown size={14} className="text-gray-400" /> : 
              <ChevronRight size={14} className="text-gray-400" />
          )}
        </button>
        
        {hasChildren && isExpanded && (
          <div className="mt-1 space-y-1">
            {category.children.map(child => renderCategory(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="w-64 bg-white border-r border-gray-200 p-4">
        <div className="animate-pulse space-y-4">
          <div className="h-6 bg-gray-200 rounded"></div>
          <div className="space-y-2">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="h-8 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="w-64 bg-white border-r border-gray-200 h-full overflow-y-auto">
      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-bold text-gray-900 flex items-center space-x-2">
          <Monitor size={20} className="text-red-600" />
          <span>DANH MỤC SẢN PHẨM</span>
        </h2>
      </div>

      {/* Categories */}
      <div className="p-4 space-y-2">
        {/* All Products */}
        <button
          onClick={() => onCategorySelect(null)}
          className={`w-full flex items-center space-x-2 py-2 px-3 text-left hover:bg-gray-100 rounded-lg transition-colors ${
            !selectedCategory ? 'bg-red-50 text-red-600 border-l-4 border-red-500 font-semibold' : 'text-gray-700'
          }`}
        >
          <Monitor size={16} className={!selectedCategory ? 'text-red-600' : 'text-gray-500'} />
          <span className="text-sm">Tất cả sản phẩm</span>
        </button>

        {/* Category List */}
        {categories.map(category => renderCategory(category))}
      </div>

      {/* Brands Section */}
      {brands.length > 0 && (
        <div className="border-t border-gray-200 p-4">
          <h3 className="text-md font-semibold text-gray-900 mb-3">THƯƠNG HIỆU</h3>
          <div className="space-y-1 max-h-64 overflow-y-auto">
            <button
              onClick={() => onBrandSelect(null)}
              className={`w-full text-left py-2 px-3 text-sm hover:bg-gray-100 rounded-lg transition-colors ${
                !selectedBrand ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700'
              }`}
            >
              Tất cả thương hiệu
            </button>
            {brands.map(brand => (
              <button
                key={brand.id}
                onClick={() => onBrandSelect(brand)}
                className={`w-full text-left py-2 px-3 text-sm hover:bg-gray-100 rounded-lg transition-colors ${
                  selectedBrand?.id === brand.id ? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700'
                }`}
              >
                {brand?.name || 'Chưa có tên'}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}