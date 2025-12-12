import React, { useState } from 'react';
import { Eye } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { API_BASE_URL } from '../api/api';
import toast from '../utils/toast';

export default function ProductCard({ product, onViewDetails }) {
  const { addToCart } = useCart();
  const [adding, setAdding] = useState(false);

  const handleAddToCart = async (e) => {
    e.stopPropagation(); // Prevent triggering view details
    setAdding(true);
    try {
      await addToCart(product.id, 1);
      // Success toast already shown in CartContext
    } catch (error) {
      console.error('Error adding to cart:', error);
      // Error toast already shown in api.js
    }
    setAdding(false);
  };

  const handleViewDetails = () => {
    if (onViewDetails) {
      onViewDetails(product.id);
    }
  };

  const primaryImage =
    product.imageUrl ||
    (product.productImages && product.productImages.length > 0
      ? product.productImages.find((img) => img.primary)?.imageUrl || product.productImages[0].imageUrl
      : null);

  // Add backend URL if image path is relative
  const fullImageUrl = primaryImage 
    ? (primaryImage.startsWith('http') ? primaryImage : `${API_BASE_URL}/files${primaryImage}`)
    : null;

  // Giá hiển thị: ưu tiên effectivePrice, sau đó salePrice, sau đó price
  const displayPrice = product.effectivePrice ?? product.salePrice ?? product.price ?? 0;

  return (
    <div 
      className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition cursor-pointer"
      onClick={handleViewDetails}
    >
      <div className="aspect-square bg-gray-100 flex items-center justify-center">
        {fullImageUrl ? (
          <img
            src={fullImageUrl}
            alt={product.name}
            className="object-cover w-full h-full"
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = `https://via.placeholder.com/300x300/f0f0f0/666666?text=${encodeURIComponent(product.name)}`; // ảnh fallback
            }}
          />
        ) : (
          <img
            src={`https://via.placeholder.com/300x300/f0f0f0/666666?text=${encodeURIComponent(product.name)}`}
            alt={product.name}
            className="object-cover w-full h-full"
          />
        )}
      </div>

      <div className="p-3">
        <h3 className="font-medium text-sm mb-1.5 line-clamp-2 h-10">{product.name}</h3>
        <div className="flex items-center justify-between mb-2">
          {product.salePrice ? (
            <>
              <span className="text-lg font-bold text-red-600">
                {Number(product.salePrice).toLocaleString('vi-VN')}đ
              </span>
              <span className="text-xs text-gray-500 line-through">
                {Number(product.price).toLocaleString('vi-VN')}đ
              </span>
            </>
          ) : (
            <span className="text-lg font-bold text-red-600">
              {Number(displayPrice).toLocaleString('vi-VN')}đ
            </span>
          )}
        </div>

        <div className="flex space-x-1.5">
          <button
            onClick={handleViewDetails}
            className="flex-1 py-1.5 text-xs bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition flex items-center justify-center space-x-1"
          >
            <Eye size={14} />
            <span>Xem</span>
          </button>
          <button
            onClick={handleAddToCart}
            disabled={adding || !product.stockQuantity}
            className="flex-1 py-1.5 text-xs bg-red-600 text-white rounded-md hover:bg-red-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
          >
            {adding ? 'Đang thêm...' : product.stockQuantity ? 'Thêm giỏ' : 'Hết hàng'}
          </button>
        </div>
      </div>
    </div>
  );
}