import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingCart, Minus, Plus, Trash2, ArrowLeft, AlertCircle, Tag, Ticket } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import api, { getImageUrl } from '../api/api';
import toast from '../utils/toast';
import './CartPage.css';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80' viewBox='0 0 80 80'%3E%3Crect width='80' height='80' fill='%23f0f0f0'/%3E%3Ctext x='40' y='44' text-anchor='middle' fill='%23999' font-family='sans-serif' font-size='10'%3ENo Image%3C/text%3E%3C/svg%3E";

function CartPage() {
  const navigate = useNavigate();
  const { cartItems, updateItemQuantity, removeFromCart, clearCart, loading } = useCart();
  const { user } = useAuth();
  const [updatingItems, setUpdatingItems] = useState({}); // Track which items are being updated
  const [selectedItems, setSelectedItems] = useState({}); // Track selected items
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [availableCoupons, setAvailableCoupons] = useState([]);
  const [showCouponModal, setShowCouponModal] = useState(false);
  const [loadingCoupons, setLoadingCoupons] = useState(false);

  // Đảm bảo cartItems luôn là một array
  const safeCartItems = cartItems || [];

  console.log('CartPage - cartItems:', cartItems); // Debug log
  console.log('CartPage - safeCartItems:', safeCartItems); // Debug log

  // Handle direct input quantity change
  const handleQuantityInputChange = async (item, value) => {
    // Only allow numbers
    const numValue = parseInt(value) || 0;
    
    if (numValue < 0) return;
    
    if (numValue === 0) {
      if (window.confirm('Số lượng bằng 0. Bạn có muốn xóa sản phẩm này?')) {
        await handleRemoveItem(item.id);
      }
      return;
    }

    if (numValue > item.stockQuantity) {
      toast.error(`Chỉ còn ${item.stockQuantity} sản phẩm trong kho!`);
      return;
    }

    setUpdatingItems(prev => ({ ...prev, [item.id]: true }));
    try {
      await updateItemQuantity(item.id, numValue);
    } finally {
      setUpdatingItems(prev => ({ ...prev, [item.id]: false }));
    }
  };

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

  // Toggle select all items
  const handleSelectAll = () => {
    if (Object.keys(selectedItems).length === safeCartItems.length) {
      setSelectedItems({});
    } else {
      const allSelected = {};
      safeCartItems.forEach(item => {
        allSelected[item.id] = true;
      });
      setSelectedItems(allSelected);
    }
  };

  // Toggle individual item
  const handleSelectItem = (itemId) => {
    setSelectedItems(prev => ({
      ...prev,
      [itemId]: !prev[itemId]
    }));
  };

  // Tính tổng tiền chỉ cho sản phẩm đã chọn
  const getSelectedTotal = () => {
    const selectedCartItems = safeCartItems.filter(item => selectedItems[item.id]);
    return selectedCartItems.reduce((total, item) => {
      return total + ((item.subtotal || (item.price || item.unitPrice || 0) * item.quantity));
    }, 0);
  };

  const selectedCount = Object.keys(selectedItems).filter(id => selectedItems[id]).length;

  // Apply coupon with backend API
  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) {
      toast.error('Vui lòng nhập mã giảm giá');
      return;
    }

    try {
      const selectedTotal = getSelectedTotal();
      if (selectedTotal === 0) {
        toast.error('Vui lòng chọn sản phẩm trước khi áp dụng mã giảm giá');
        return;
      }

      const couponData = await api.request('/public/coupons/apply', {
        method: 'POST',
        body: JSON.stringify({
          couponCode: couponCode.trim(),
          orderAmount: selectedTotal
        })
      });

      if (couponData && couponData.valid) {
        setAppliedCoupon({
          code: couponCode.trim(),
          discount: couponData.discountAmount,
          type: couponData.discountType,
          value: couponData.discountValue
        });
        toast.success(`Áp dụng mã giảm giá thành công! Giảm ${couponData.discountAmount.toLocaleString('vi-VN')}₫`);
      } else {
        toast.error('Mã giảm giá không hợp lệ');
      }
    } catch (error) {
      console.error('Error applying coupon:', error);
      toast.error(error.message || 'Lỗi khi áp dụng mã giảm giá');
    }
  };

  // Fetch available coupons from backend
  const fetchAvailableCoupons = async () => {
    setLoadingCoupons(true);
    try {
      const couponsData = await api.request('/public/coupons?size=50');
      if (couponsData && couponsData.content) {
        setAvailableCoupons(couponsData.content);
        setShowCouponModal(true);
      } else if (Array.isArray(couponsData)) {
        setAvailableCoupons(couponsData);
        setShowCouponModal(true);
      }
    } catch (error) {
      console.error('Error fetching coupons:', error);
      toast.error('Không thể tải danh sách mã giảm giá');
    } finally {
      setLoadingCoupons(false);
    }
  };

  const handleCheckout = () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để tiếp tục thanh toán');
      navigate('/login', { state: { from: '/cart' } });
      return;
    }
    
    const selectedCount = Object.keys(selectedItems).filter(id => selectedItems[id]).length;
    if (selectedCount === 0) {
      toast.error('Vui lòng chọn ít nhất một sản phẩm để thanh toán!');
      return;
    }

    // Lọc chỉ những sản phẩm đã chọn để thanh toán
    const itemsToCheckout = safeCartItems.filter(item => selectedItems[item.id]);
    navigate('/checkout', { state: { selectedItems: itemsToCheckout } });
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
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="p-2 hover:bg-white rounded-lg transition shadow-sm"
              title="Quay lại"
            >
              <ArrowLeft size={24} className="text-gray-700" />
            </button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Giỏ hàng</h1>
              <p className="text-sm text-gray-500 mt-1">
                {safeCartItems.length} sản phẩm trong giỏ hàng
              </p>
            </div>
          </div>
          
          {safeCartItems.length > 0 && (
            <button
              onClick={clearCart}
              className="px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg transition flex items-center gap-2 font-medium"
            >
              <Trash2 size={18} />
              Xóa tất cả
            </button>
          )}
        </div>

        {/* Full Width Cart Items */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden mb-32">
          <div>
              {/* Professional Table Layout */}
              <div className="overflow-x-auto">
                <table className="w-full cart-table">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left w-12">
                        <input
                          type="checkbox"
                          checked={Object.keys(selectedItems).length === safeCartItems.length && safeCartItems.length > 0}
                          onChange={handleSelectAll}
                          className="w-5 h-5 text-red-600 border-gray-300 rounded focus:ring-red-500"
                        />
                      </th>
                      <th className="px-4 py-3 text-left font-semibold text-gray-900">Sản phẩm</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900 w-32">Đơn giá</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900 w-40">Số lượng</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900 w-32">Số tiền</th>
                      <th className="px-4 py-3 text-center font-semibold text-gray-900 w-20">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {safeCartItems.map((item) => (
                      <tr key={item.id} className="hover:bg-gray-50 transition">
                        {/* Checkbox Column */}
                        <td className="px-4 py-4 align-middle">
                          <input
                            type="checkbox"
                            checked={!!selectedItems[item.id]}
                            onChange={() => handleSelectItem(item.id)}
                            className="w-5 h-5 text-red-600 border-gray-300 rounded focus:ring-red-500"
                          />
                        </td>

                        {/* Product Info Column */}
                        <td className="px-4 py-4 align-middle">
                          <div className="flex items-center gap-3">
                            {/* Product Image */}
                            <div className="w-20 h-20 flex-shrink-0">
                              <img
                                src={getImageUrl(item.productImage) || PLACEHOLDER_IMG}
                                alt={item.productName || 'Product'}
                                className="w-full h-full object-cover rounded-lg border border-gray-200"
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.src = PLACEHOLDER_IMG;
                                }}
                              />
                            </div>
                            
                            {/* Product Details */}
                            <div className="flex-1 min-w-0">
                              <h3 
                                className="font-semibold text-gray-900 mb-1 line-clamp-2 cursor-pointer hover:text-red-600 transition text-sm"
                                onClick={() => navigate(`/product/${item.productId}`)}
                                title={item.productName || 'Sản phẩm'}
                              >
                                {item.productName || 'Sản phẩm'}
                              </h3>
                              <div className="flex items-center gap-3 text-xs text-gray-500">
                                <span>SKU: {item.productSku || 'N/A'}</span>
                                <span className="flex items-center gap-1">
                                  <AlertCircle size={12} />
                                  Còn {item.stockQuantity || 0}
                                </span>
                              </div>
                            </div>
                          </div>
                        </td>

                        {/* Unit Price Column */}
                        <td className="px-4 py-4 align-middle text-center">
                          <div className="text-red-600 font-bold text-base">
                            {(item.price || 0).toLocaleString('vi-VN')}₫
                          </div>
                        </td>

                        {/* Quantity Column - FIXED WIDTH */}
                        <td className="px-4 py-4 align-middle">
                          <div className="flex items-center justify-center gap-2">
                            <button
                              onClick={() => handleQuantityChange(item, item.quantity - 1)}
                              disabled={updatingItems[item.id] || item.quantity <= 1}
                              className="w-8 h-8 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-100 hover:border-red-500 disabled:opacity-50 disabled:cursor-not-allowed transition flex-shrink-0"
                            >
                              <Minus size={16} className="text-gray-600" />
                            </button>
                            
                            <input
                              type="number"
                              min="1"
                              max={item.stockQuantity}
                              value={updatingItems[item.id] ? '' : item.quantity}
                              onChange={(e) => handleQuantityInputChange(item, e.target.value)}
                              disabled={updatingItems[item.id]}
                              className="quantity-input h-8 text-center border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed font-semibold flex-shrink-0"
                              placeholder={updatingItems[item.id] ? '...' : ''}
                            />
                            
                            <button
                              onClick={() => handleQuantityChange(item, item.quantity + 1)}
                              disabled={updatingItems[item.id] || item.quantity >= item.stockQuantity}
                              className="w-8 h-8 border border-gray-300 rounded-lg flex items-center justify-center hover:bg-gray-100 hover:border-red-500 disabled:opacity-50 disabled:cursor-not-allowed transition flex-shrink-0"
                              title={item.quantity >= item.stockQuantity ? 'Hết hàng trong kho' : 'Tăng số lượng'}
                            >
                              <Plus size={16} className="text-gray-600" />
                            </button>
                          </div>
                        </td>

                        {/* Subtotal Column */}
                        <td className="px-4 py-4 align-middle text-center">
                          <div className="font-bold text-gray-900 text-base">
                            {(item.subtotal || (item.price || 0) * item.quantity).toLocaleString('vi-VN')}₫
                          </div>
                        </td>

                        {/* Action Column */}
                        <td className="px-4 py-4 align-middle text-center">
                          <button
                            onClick={() => handleRemoveItem(item.id)}
                            disabled={updatingItems[item.id]}
                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center"
                            title="Xóa sản phẩm"
                          >
                            {updatingItems[item.id] ? (
                              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-red-600"></div>
                            ) : (
                              <Trash2 size={18} />
                            )}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Sticky Bottom Summary Bar - Shopee Style */}
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t-2 border-gray-200 shadow-2xl z-40">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="py-4">
              <div className="flex items-center justify-between gap-6">
                {/* Left: Coupon Section - Compact */}
                <div className="flex items-center gap-3 flex-1">
                  <Tag className="text-red-600 flex-shrink-0" size={20} />
                  <input
                    type="text"
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                    onKeyPress={(e) => e.key === 'Enter' && handleApplyCoupon()}
                    placeholder="Mã giảm giá"
                    className="w-48 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 text-sm uppercase"
                  />
                  <button
                    onClick={handleApplyCoupon}
                    disabled={!couponCode.trim()}
                    className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium text-sm disabled:opacity-50"
                  >
                    Áp dụng
                  </button>
                  <button
                    onClick={fetchAvailableCoupons}
                    disabled={loadingCoupons}
                    className="text-sm text-blue-600 hover:text-blue-700 font-medium underline"
                  >
                    {loadingCoupons ? 'Đang tải...' : 'Xem mã'}
                  </button>
                  {appliedCoupon && (
                    <div className="flex items-center gap-2 px-3 py-1 bg-green-100 text-green-700 rounded-lg text-sm">
                      <Ticket size={14} />
                      <span className="font-semibold">{appliedCoupon.code}</span>
                      <span>-{appliedCoupon.discount.toLocaleString('vi-VN')}₫</span>
                      <button
                        onClick={() => {
                          setAppliedCoupon(null);
                          setCouponCode('');
                        }}
                        className="ml-1 text-red-600 hover:text-red-800"
                      >
                        ×
                      </button>
                    </div>
                  )}
                </div>

                {/* Right: Summary + Checkout */}
                <div className="flex items-center gap-6">
                  {/* Selected Count */}
                  <div className="text-left">
                    <div className="text-sm text-gray-600">Đã chọn</div>
                    <div className="text-lg font-bold text-gray-900">{selectedCount} sản phẩm</div>
                  </div>

                  {/* Divider */}
                  <div className="w-px h-12 bg-gray-300"></div>

                  {/* Total Amount */}
                  <div className="text-right">
                    <div className="text-sm text-gray-600">Tổng thanh toán</div>
                    <div className="text-2xl font-bold text-red-600">
                      {(getSelectedTotal() - (appliedCoupon?.discount || 0)).toLocaleString('vi-VN')}₫
                    </div>
                  </div>

                  {/* Checkout Button */}
                  <button
                    onClick={handleCheckout}
                    disabled={selectedCount === 0}
                    className="px-12 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-bold text-lg shadow-lg hover:shadow-xl disabled:opacity-50 disabled:bg-gray-300 disabled:cursor-not-allowed"
                  >
                    Mua hàng
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

      {/* Available Coupons Modal */}
      {showCouponModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 modal-backdrop">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[80vh] overflow-hidden modal-content">
            {/* Modal Header */}
            <div className="p-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">Mã giảm giá khả dụng</h2>
              <button
                onClick={() => setShowCouponModal(false)}
                className="p-2 hover:bg-gray-100 rounded-lg transition"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-4 overflow-y-auto max-h-[calc(80vh-120px)]">
              {availableCoupons.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <Tag size={48} className="mx-auto mb-2 opacity-50" />
                  <p>Hiện tại không có mã giảm giá nào</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {availableCoupons.map((coupon) => (
                    <div
                      key={coupon.id}
                      className="coupon-card border border-gray-200 rounded-lg p-4 hover:border-red-500 hover:shadow-md transition cursor-pointer"
                      onClick={() => {
                        setCouponCode(coupon.code);
                        setShowCouponModal(false);
                      }}
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <span className="px-3 py-1 bg-red-100 text-red-700 font-bold text-sm rounded-lg">
                              {coupon.code}
                            </span>
                            {coupon.discountType === 'PERCENTAGE' ? (
                              <span className="text-red-600 font-bold">Giảm {coupon.discountValue}%</span>
                            ) : (
                              <span className="text-red-600 font-bold">Giảm {coupon.discountValue.toLocaleString('vi-VN')}₫</span>
                            )}
                          </div>
                          <p className="text-sm text-gray-600 mb-2">{coupon.description || 'Mã giảm giá đặc biệt'}</p>
                          <div className="flex items-center gap-4 text-xs text-gray-500">
                            {coupon.minOrderAmount > 0 && (
                              <span>Đơn tối thiểu: {coupon.minOrderAmount.toLocaleString('vi-VN')}₫</span>
                            )}
                            {coupon.maxDiscountAmount > 0 && (
                              <span>Giảm tối đa: {coupon.maxDiscountAmount.toLocaleString('vi-VN')}₫</span>
                            )}
                            {coupon.usageLimit > 0 && (
                              <span>Còn: {coupon.usageLimit - (coupon.usedCount || 0)} lượt</span>
                            )}
                          </div>
                          {coupon.expiryDate && (
                            <div className="text-xs text-orange-600 mt-1">
                              HSD: {new Date(coupon.expiryDate).toLocaleDateString('vi-VN')}
                            </div>
                          )}
                        </div>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setCouponCode(coupon.code);
                            setShowCouponModal(false);
                            handleApplyCoupon();
                          }}
                          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition text-sm font-medium flex-shrink-0"
                        >
                          Áp dụng
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CartPage;