import React, { useState } from 'react';
import { Eye, ShoppingCart } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { getImageUrl } from '../api/api';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='300' viewBox='0 0 300 300'%3E%3Crect width='300' height='300' fill='%23f0f0f0'/%3E%3Ctext x='150' y='156' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='14'%3ENo Image%3C/text%3E%3C/svg%3E";

export default function ProductCard({ product, onViewDetails }) {
  const { addToCart } = useCart();
  const [adding, setAdding] = useState(false);

  const handleAddToCart = async (e) => {
    e.stopPropagation();
    setAdding(true);
    try {
      await addToCart(product.id, 1);
    } catch (error) {
      console.error('Error adding to cart:', error);
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

  const fullImageUrl = getImageUrl(primaryImage);

  // Giá hiển thị: ưu tiên effectivePrice, sau đó salePrice, sau đó price
  const displayPrice = product.effectivePrice ?? product.salePrice ?? product.price ?? 0;

  return (
    <div
      className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow cursor-pointer group"
      onClick={handleViewDetails}
    >
      {/* Image with hover overlay */}
      <div className="relative aspect-square bg-gray-100 overflow-hidden">
        <img
          src={fullImageUrl || PLACEHOLDER_IMG}
          alt={product.name}
          className="object-cover w-full h-full transition-transform duration-300 group-hover:scale-105"
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = PLACEHOLDER_IMG;
          }}
        />
        {/* Hover overlay */}
        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col items-center justify-center gap-2">
          <button
            onClick={(e) => { e.stopPropagation(); handleViewDetails(); }}
            className="flex items-center gap-1.5 px-4 py-2 bg-white text-gray-800 text-sm font-medium rounded-full hover:bg-gray-100 transition"
          >
            <Eye size={15} />
            Xem chi tiết
          </button>
          <button
            onClick={handleAddToCart}
            disabled={adding || !product.stockQuantity}
            className="flex items-center gap-1.5 px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-full hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
          >
            <ShoppingCart size={15} />
            {adding ? 'Đang thêm...' : product.stockQuantity ? 'Thêm giỏ hàng' : 'Hết hàng'}
          </button>
        </div>
      </div>

      {/* Card info */}
      <div className="p-3">
        <h3 className="font-medium text-sm mb-1.5 line-clamp-2 h-10">{product.name}</h3>
        <div className="flex items-center justify-between">
          {product.salePrice ? (
            <>
              <span className="text-base font-bold text-red-600">
                {Number(product.salePrice).toLocaleString('vi-VN')}đ
              </span>
              <span className="text-xs text-gray-400 line-through">
                {Number(product.price).toLocaleString('vi-VN')}đ
              </span>
            </>
          ) : (
            <span className="text-base font-bold text-red-600">
              {Number(displayPrice).toLocaleString('vi-VN')}đ
            </span>
          )}
        </div>
      </div>
    </div>
  );
}