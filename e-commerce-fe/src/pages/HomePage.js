import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Monitor, Smartphone, Laptop, Headphones, Watch, Camera, Gamepad2, Tv, Home, Zap } from 'lucide-react';
import FlashSale from '../components/FlashSale';
import NewArrivals from '../components/NewArrivals';
import TrendingSearch from '../components/TrendingSearch';
import Sidebar from '../components/Sidebar';
import api from '../api/api';

function HomePage() {
  const navigate = useNavigate();

  const handleCategorySelect = (category) => {
    if (category) {
      navigate(`/products?categoryId=${category.id}`);
    } else {
      navigate('/products');
    }
  };

  const handleBrandSelect = (brand) => {
    if (brand) {
      navigate(`/products?brandId=${brand.id}`);
    } else {
      navigate('/products');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <section className="bg-gradient-to-r from-red-600 to-red-800 text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-6xl font-bold mb-6">
            Chào mừng đến với E-SHOP
          </h1>
          <p className="text-xl md:text-2xl mb-8">
            Nơi mua sắm công nghệ hàng đầu Việt Nam
          </p>
          <button
            onClick={() => navigate('/products')}
            className="bg-white text-red-600 px-8 py-3 rounded-lg font-semibold text-lg hover:bg-gray-100 transition"
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

      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <div className="flex">
            <div className="w-64 flex-shrink-0 mr-6">
              <Sidebar 
                onCategorySelect={handleCategorySelect}
                onBrandSelect={handleBrandSelect}
                selectedCategory={null}
                selectedBrand={null}
              />
            </div>
            <div className="flex-1">
              <div className="text-center mb-12">
                <h2 className="text-3xl font-bold text-gray-800 mb-4">Sản phẩm nổi bật</h2>
                <p className="text-gray-600">Những sản phẩm công nghệ hot nhất hiện tại</p>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                {[
                  { icon: Smartphone, name: 'Điện thoại', category: 'Điện thoại', color: 'bg-blue-500' },
                  { icon: Laptop, name: 'Laptop', category: 'Laptop', color: 'bg-green-500' },
                  { icon: Headphones, name: 'Tai nghe', category: 'Phụ kiện', color: 'bg-purple-500' },
                  { icon: Watch, name: 'Đồng hồ', category: 'Đồng hồ thông minh', color: 'bg-yellow-500' },
                  { icon: Camera, name: 'Máy ảnh', category: 'Máy ảnh', color: 'bg-pink-500' },
                  { icon: Gamepad2, name: 'Gaming', category: 'Gaming', color: 'bg-indigo-500' },
                  { icon: Tv, name: 'TV', category: 'TV', color: 'bg-red-500' },
                  { icon: Monitor, name: 'Màn hình', category: 'Màn hình', color: 'bg-gray-500' }
                ].map((item, index) => (
                  <div
                    key={index}
                    onClick={() => navigate(`/products?category=${encodeURIComponent(item.category)}`)}
                    className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 cursor-pointer p-6 text-center border border-gray-200 hover:border-red-300"
                  >
                    <div className={`${item.color} rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4`}>
                      <item.icon className="text-white" size={28} />
                    </div>
                    <h3 className="font-semibold text-gray-800">{item.name}</h3>
                  </div>
                ))}
              </div>
              <div className="text-center mt-8">
                <button
                  onClick={() => navigate('/products')}
                  className="bg-red-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-red-700 transition"
                >
                  Xem tất cả sản phẩm
                </button>
              </div>
            </div>
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
