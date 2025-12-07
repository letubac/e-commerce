import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Smartphone, Laptop, Monitor, Headphones, Watch, Camera, Gamepad2, Tv, Home, Zap, Package } from 'lucide-react';
import FlashSale from '../components/FlashSale';
import NewArrivals from '../components/NewArrivals';
import TrendingSearch from '../components/TrendingSearch';
import CategoryBar from '../components/CategoryBar';
import WelcomePopup from '../components/WelcomePopup';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';

// Icon mapping cho các danh mục
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
  'Quần áo': Home,
  'Thời trang': Home,
};

function HomePage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [showWelcomePopup, setShowWelcomePopup] = useState(false);
  const [hasShownPopup, setHasShownPopup] = useState(false);
  const [featuredCategories, setFeaturedCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Show welcome popup when user first logs in (only once per session)
    if (user && !hasShownPopup) {
      setShowWelcomePopup(true);
      setHasShownPopup(true);
    }
  }, [user, hasShownPopup]);

  useEffect(() => {
    fetchFeaturedCategories();
  }, []);

  const fetchFeaturedCategories = async () => {
    try {
      setLoading(true);
      const data = await api.getCategories();
      const categories = Array.isArray(data) ? data : (data?.content || []);
      // Lấy 8 categories đầu tiên để hiển thị
      setFeaturedCategories(categories.slice(0, 8));
    } catch (error) {
      console.error('Error fetching categories:', error);
      setFeaturedCategories([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCategorySelect = (category) => {
    if (category) {
      navigate(`/products?categoryId=${category.id}`);
    } else {
      navigate('/products');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Welcome Popup */}
      <WelcomePopup 
        isOpen={showWelcomePopup}
        onClose={() => setShowWelcomePopup(false)}
        userName={user?.fullName || user?.username}
      />

      {/* Compact Hero Section */}
      <section className="bg-gradient-to-r from-red-600 to-red-800 text-white py-12">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-3xl md:text-4xl font-bold mb-3">
            E-SHOP - Công nghệ hàng đầu
          </h1>
          <p className="text-lg md:text-xl mb-4">
            Sản phẩm chính hãng • Giao hàng nhanh • Bảo hành tận nơi
          </p>
          <button
            onClick={() => navigate('/products')}
            className="bg-white text-red-600 px-6 py-2 rounded-lg font-semibold hover:bg-gray-100 transition"
          >
            Khám phá ngay
          </button>
        </div>
      </section>

      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
            Tại sao chọn E-SHOP?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="bg-red-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <Monitor className="text-red-600" size={32} />
              </div>
              <h3 className="text-xl font-semibold mb-2">Sản phẩm chính hãng</h3>
              <p className="text-gray-600">100% sản phẩm chính hãng từ các thương hiệu uy tín</p>
            </div>
            <div className="text-center">
              <div className="bg-red-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <Zap className="text-red-600" size={32} />
              </div>
              <h3 className="text-xl font-semibold mb-2">Giao hàng nhanh</h3>
              <p className="text-gray-600">Giao hàng trong 24h tại TP.HCM và Hà Nội</p>
            </div>
            <div className="text-center">
              <div className="bg-red-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <Home className="text-red-600" size={32} />
              </div>
              <h3 className="text-xl font-semibold mb-2">Bảo hành tận nơi</h3>
              <p className="text-gray-600">Dịch vụ bảo hành và hỗ trợ tận nơi</p>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <FlashSale />
        </div>
      </section>

      {/* Category Bar */}
      <CategoryBar 
        onCategorySelect={handleCategorySelect}
        selectedCategory={null}
      />

      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-800 mb-4">Sản phẩm nổi bật</h2>
            <p className="text-gray-600">Những sản phẩm công nghệ hot nhất hiện tại</p>
          </div>
          
          {loading ? (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              {Array.from({ length: 8 }).map((_, index) => (
                <div key={index} className="animate-pulse">
                  <div className="bg-gray-200 rounded-lg p-6 h-40"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              {featuredCategories.map((category) => {
                const IconComponent = categoryIcons[category.name] || Package;
                const colors = [
                  'bg-blue-500', 'bg-green-500', 'bg-purple-500', 'bg-yellow-500',
                  'bg-pink-500', 'bg-indigo-500', 'bg-red-500', 'bg-gray-500'
                ];
                const colorIndex = category.id % colors.length;

                return (
                  <div
                    key={category.id}
                    onClick={() => navigate(`/products?categoryId=${category.id}`)}
                    className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 cursor-pointer p-6 text-center border border-gray-200 hover:border-red-300"
                  >
                    <div className={`${colors[colorIndex]} rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4`}>
                      <IconComponent className="text-white" size={28} />
                    </div>
                    <h3 className="font-semibold text-gray-800">{category.name}</h3>
                  </div>
                );
              })}
            </div>
          )}
          
          <div className="text-center mt-8">
                <button
                  onClick={() => navigate('/products')}
                  className="bg-red-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-red-700 transition"
                >
                  Xem tất cả sản phẩm
                </button>
              </div>
        </div>
      </section>

      <section className="bg-gray-100 py-16">
        <div className="container mx-auto px-4">
          <NewArrivals />
        </div>
      </section>

      <section className="bg-white py-16">
        <div className="container mx-auto px-4">
          <TrendingSearch />
        </div>
      </section>
    </div>
  );
}

export default HomePage;
