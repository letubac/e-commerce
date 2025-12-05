import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingCart, Minus, Plus, Trash2, ArrowLeft, AlertCircle } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { API_BASE_URL } from '../api/api';
import toast from '../utils/toast';

function CartPage() {
  const navigate = useNavigate();
  const { cartItems, updateItemQuantity, removeFromCart, getTotalPrice, clearCart, loading } = useCart();
  const { user } = useAuth();
  const [updatingItems, setUpdatingItems] = useState({}); // Track which items are being updated

  // Đảm bảo cartItems luôn là một array
  const safeCartItems = cartItems || [];

  console.log('CartPage - cartItems:', cartItems); // Debug log
  console.log('CartPage - safeCartItems:', safeCartItems); // Debug log

  const handleQuantityChange = async (item, newQuantity) => {
    // Validate stock before updating
    if (newQuantity > item.stockQuantity) {
      toast.error(`Chỉ còn ${item.stockQuantity} sản phẩm trong kho!`);
      return;
    }

    if (newQuantity <= 0) {
      if (window.confirm('Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?')) {
        setUpdatingItems(prev => ({ ...prev, [item.id]: true }));
        try {
          await removeFromCart(item.id);
        } finally {
          setUpdatingItems(prev => ({ ...prev, [item.id]: false }));
        }
      }
    } else {
      setUpdatingItems(prev => ({ ...prev, [item.id]: true }));
      try {
        await updateItemQuantity(item.id, newQuantity);
      } finally {
        setUpdatingItems(prev => ({ ...prev, [item.id]: false }));
      }
    }
  };

  const handleRemoveItem = async (itemId) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
      setUpdatingItems(prev => ({ ...prev, [itemId]: true }));
      try {
        await removeFromCart(itemId);
      } finally {
        setUpdatingItems(prev => ({ ...prev, [itemId]: false }));
      }
    }
  };

  const handleCheckout = () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để tiếp tục thanh toán');
      navigate('/login', { state: { from: '/cart' } });
      return;
    }
    
    if (safeCartItems.length === 0) {
      toast.error('Giỏ hàng trống');
      return;
    }

    // Redirect to checkout page or handle checkout logic
    navigate('/checkout');
  };

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <div className="bg-white rounded-lg shadow-sm p-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600 mx-auto mb-4"></div>
            <p className="text-gray-600">Đang tải giỏ hàng...</p>
          </div>
        </div>
      </div>
    );
  }

  if (safeCartItems.length === 0) {
    return (
      <div className="min-h-screen bg-gray-100 py-8">
        <div className="container mx-auto px-4">
          <div className="bg-white rounded-lg shadow-sm p-8 text-center">
            <ShoppingCart size={64} className="mx-auto text-gray-400 mb-4" />
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Giỏ hàng trống</h2>
            <p className="text-gray-600 mb-8">
              Bạn chưa có sản phẩm nào trong giỏ hàng. Hãy tiếp tục mua sắm để thêm sản phẩm.
            </p>
            <button
              onClick={() => navigate('/products')}
              className="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
            >
              Tiếp tục mua sắm
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex items-center mb-6">
          <button
            onClick={() => navigate(-1)}
            className="mr-4 p-2 hover:bg-gray-200 rounded-lg transition"
          >
            <ArrowLeft size={24} />
          </button>
          <h1 className="text-2xl font-bold text-gray-800">Giỏ hàng ({safeCartItems.length})</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-sm overflow-hidden">
              <div className="p-4 border-b border-gray-200">
                <div className="flex justify-between items-center">
                  <h2 className="text-lg font-semibold">Sản phẩm</h2>
                  <button
                    onClick={clearCart}
                    className="text-red-600 hover:text-red-700 text-sm flex items-center gap-1"
                  >
                    <Trash2 size={16} />
                    Xóa tất cả
                  </button>
                </div>
              </div>

              <div className="divide-y divide-gray-200">
                {safeCartItems.map((item) => (
                  <div key={item.id} className="p-4">
                    <div className="flex items-center gap-4">
                      {/* Product Image */}
                      <div className="w-20 h-20 flex-shrink-0">
                        <img
                          src={
                            item.productImage
                              ? `${API_BASE_URL}/files${item.productImage}`
                              : `https://via.placeholder.com/80x80/f0f0f0/666666?text=${encodeURIComponent(item.productName || 'Product')}`
                          }
                          alt={item.productName || 'Product'}
                          className="w-full h-full object-cover rounded-lg border border-gray-200"
                          onError={(e) => {
                            e.target.src = `https://via.placeholder.com/80x80/f0f0f0/666666?text=${encodeURIComponent(item.productName || 'Product')}`;
                          }}
                        />
                      </div>

                      {/* Product Info */}
                      <div className="flex-1 min-w-0">
                        <h3 
                          className="font-semibold text-gray-800 mb-1 truncate cursor-help" 
                          title={item.productName || 'Sản phẩm'}
                        >
                          {item.productName || 'Sản phẩm'}
                        </h3>
                        <p className="text-sm text-gray-500 mb-1">
                          SKU: {item.productSku || 'N/A'}
                        </p>
                        <p className="text-red-600 font-bold mb-1">
                          {(item.price || 0).toLocaleString('vi-VN')}₫
                        </p>
                        <p className="text-xs text-gray-500 flex items-center gap-1">
                          <AlertCircle size={12} />
                          Còn {item.stockQuantity || 0} sản phẩm
                        </p>
                      </div>

                      {/* Quantity Controls */}
                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => handleQuantityChange(item, item.quantity - 1)}
                          disabled={updatingItems[item.id]}
                          className="w-8 h-8 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          <Minus size={14} />
                        </button>
                        <span className="w-12 text-center font-semibold">
                          {updatingItems[item.id] ? (
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-red-600 mx-auto"></div>
                          ) : (
                            item.quantity
                          )}
                        </span>
                        <button
                          onClick={() => handleQuantityChange(item, item.quantity + 1)}
                          disabled={updatingItems[item.id] || item.quantity >= item.stockQuantity}
                          className="w-8 h-8 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                          title={item.quantity >= item.stockQuantity ? 'Hết hàng trong kho' : 'Tăng số lượng'}
                        >
                          <Plus size={14} />
                        </button>
                      </div>

                      {/* Subtotal */}
                      <div className="text-right w-24">
                        <p className="font-bold text-gray-800">
                          {(item.subtotal || (item.price || 0) * item.quantity).toLocaleString('vi-VN')}₫
                        </p>
                      </div>

                      {/* Remove Button */}
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        disabled={updatingItems[item.id]}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {updatingItems[item.id] ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-red-600"></div>
                        ) : (
                          <Trash2 size={16} />
                        )}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-8">
              <h2 className="text-lg font-semibold mb-4">Tóm tắt đơn hàng</h2>
              
              <div className="space-y-3 mb-6">
                <div className="flex justify-between">
                  <span>Tạm tính:</span>
                  <span>{(getTotalPrice() || 0).toLocaleString('vi-VN')}₫</span>
                </div>
                <div className="flex justify-between">
                  <span>Phí vận chuyển:</span>
                  <span className="text-green-600">Miễn phí</span>
                </div>
                <div className="border-t border-gray-200 pt-3">
                  <div className="flex justify-between font-bold text-lg">
                    <span>Tổng cộng:</span>
                    <span className="text-red-600">
                      {(getTotalPrice() || 0).toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                </div>
              </div>

              <button
                onClick={handleCheckout}
                className="w-full py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-semibold"
              >
                Tiến hành thanh toán
              </button>

              <button
                onClick={() => navigate('/products')}
                className="w-full mt-3 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
              >
                Tiếp tục mua sắm
              </button>

              {/* Security Features */}
              <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                <h3 className="font-semibold text-sm mb-2">Cam kết bảo mật</h3>
                <ul className="text-xs text-gray-600 space-y-1">
                  <li>• Thanh toán an toàn với SSL</li>
                  <li>• Bảo vệ thông tin khách hàng</li>
                  <li>• Giao hàng tận nơi</li>
                  <li>• Đổi trả trong 15 ngày</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CartPage;