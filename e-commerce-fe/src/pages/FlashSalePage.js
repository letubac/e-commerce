import React from 'react';
import { useNavigate } from 'react-router-dom';
import FlashSale from '../components/FlashSale';
import Sidebar from '../components/Sidebar';

function FlashSalePage() {
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
      <div className="container mx-auto px-4 py-8">
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
            <div className="mb-6">
              <h1 className="text-3xl font-bold text-gray-800 mb-2">Flash Sale</h1>
              <p className="text-gray-600">Ưu đãi giới hạn - Nhanh tay kẻo lỡ!</p>
            </div>

            <FlashSale showTitle={false} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default FlashSalePage;