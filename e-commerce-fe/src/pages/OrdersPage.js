import React, { useState, useEffect } from 'react';
import { Package, Clock, CheckCircle, XCircle, Calendar, Truck, ShoppingBag } from 'lucide-react';
import api, { getImageUrl } from '../api/api';
import toast from '../utils/toast';

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80' viewBox='0 0 80 80'%3E%3Crect width='80' height='80' fill='%23f3f4f6'/%3E%3Cpath d='M30 25h20v20H30z' fill='%23d1d5db'/%3E%3Ccircle cx='40' cy='50' r='8' fill='%23d1d5db'/%3E%3C/svg%3E";

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // all, pending, confirmed, shipping, delivered, cancelled
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Fetch orders from API
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await api.getOrders();
      console.log('📦 Orders from API:', data);
      setOrders(data || []);
    } catch (error) {
      console.error('❌ Error fetching orders:', error);
      if (error.message !== 'Authentication required') {
        toast.error('Không thể tải danh sách đơn hàng: ' + error.message);
      }
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    // Confirmation dialog
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
      return;
    }

    try {
      await api.cancelOrder(orderId);
      toast.success('Hủy đơn hàng thành công!');
      
      // Update the order in the list
      setOrders(prevOrders => 
        prevOrders.map(order => 
          order.id === orderId 
            ? { ...order, status: 'CANCELLED' }
            : order
        )
      );

      // Update selectedOrder if it's the one being cancelled
      if (selectedOrder?.id === orderId) {
        setSelectedOrder({ ...selectedOrder, status: 'CANCELLED' });
      }

      // Refresh orders to get latest data
      fetchOrders();
    } catch (error) {
      console.error('❌ Error cancelling order:', error);
      toast.error('Không thể hủy đơn hàng: ' + error.message);
    }
  };

  const canCancelOrder = (status) => {
    const statusLower = status?.toLowerCase();
    return statusLower === 'pending' || statusLower === 'confirmed';
  };

  const getStatusIcon = (status) => {
    const statusLower = status?.toLowerCase();
    switch (statusLower) {
      case 'pending':
        return <Clock className="h-4 w-4 text-yellow-500" />;
      case 'confirmed':
        return <CheckCircle className="h-4 w-4 text-blue-500" />;
      case 'shipping':
        return <Truck className="h-4 w-4 text-purple-500" />;
      case 'delivered':
        return <ShoppingBag className="h-4 w-4 text-green-500" />;
      case 'cancelled':
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <Clock className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusText = (status) => {
    const statusLower = status?.toLowerCase();
    switch (statusLower) {
      case 'pending':
        return 'Chờ xử lý';
      case 'confirmed':
        return 'Đã xác nhận';
      case 'shipping':
        return 'Đang giao';
      case 'delivered':
        return 'Đã giao';
      case 'cancelled':
        return 'Đã hủy';
      default:
        return 'Không xác định';
    }
  };

  const getStatusColor = (status) => {
    const statusLower = status?.toLowerCase();
    switch (statusLower) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'confirmed':
        return 'bg-blue-100 text-blue-800';
      case 'shipping':
        return 'bg-purple-100 text-purple-800';
      case 'delivered':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowDetailModal(true);
  };

  const closeDetailModal = () => {
    setShowDetailModal(false);
    setSelectedOrder(null);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(price || 0);
  };

  const filteredOrders = orders.filter(order => 
    filter === 'all' || order.status?.toLowerCase() === filter
  );

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-24 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-6">
        <h1 className="text-2xl font-medium text-gray-900 mb-6">Đơn hàng của tôi</h1>
        
        {/* Shopee Style Tabs */}
        <div className="bg-white rounded-lg shadow-sm mb-4">
          <div className="flex border-b border-gray-200 overflow-x-auto">
            <button
              onClick={() => setFilter('all')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'all'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Tất cả
            </button>
            <button
              onClick={() => setFilter('pending')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'pending'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Chờ xác nhận
            </button>
            <button
              onClick={() => setFilter('confirmed')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'confirmed'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Đã xác nhận
            </button>
            <button
              onClick={() => setFilter('shipping')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'shipping'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Đang giao
            </button>
            <button
              onClick={() => setFilter('delivered')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'delivered'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Hoàn thành
            </button>
            <button
              onClick={() => setFilter('cancelled')}
              className={`flex-1 min-w-[120px] py-4 px-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                filter === 'cancelled'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Đã hủy
            </button>
          </div>

          {/* Search bar (optional) */}
          <div className="p-4 border-b border-gray-200">
            <div className="relative">
              <input
                type="text"
                placeholder="Bạn có thể tìm kiếm theo tên Shop, ID đơn hàng hoặc Tên Sản phẩm"
                className="w-full px-4 py-2 pl-10 pr-4 border border-gray-300 rounded-sm text-sm focus:outline-none focus:border-red-500"
              />
              <svg
                className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
          </div>
        </div>

        {/* Orders List */}
        {filteredOrders.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <Package className="h-20 w-20 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              Chưa có đơn hàng
            </h3>
            <p className="text-gray-500 mb-4">
              {filter === 'all' 
                ? 'Bạn chưa có đơn hàng nào. Hãy bắt đầu mua sắm ngay!'
                : `Không có đơn hàng nào ở trạng thái "${getStatusText(filter)}"`
              }
            </p>
          </div>
        ) : (
        <div className="space-y-4">
          {filteredOrders.map(order => (
            <div key={order.id} className="bg-white rounded-sm shadow-sm border border-gray-200 overflow-hidden">
              {/* Order Header - Shop name & Status */}
              <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-white">
                <div className="flex items-center space-x-2">
                  <ShoppingBag className="h-4 w-4 text-gray-400" />
                  <span className="font-medium text-gray-900">Shop</span>
                  <button className="text-red-600 text-sm hover:text-red-700">
                    <svg className="w-4 h-4 inline mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z" />
                      <path d="M15 7v2a4 4 0 01-4 4H9.828l-1.766 1.767c.28.149.599.233.938.233h2l3 3v-3h2a2 2 0 002-2V9a2 2 0 00-2-2h-1z" />
                    </svg>
                    Chat
                  </button>
                </div>
                <div className="flex items-center space-x-2">
                  {getStatusIcon(order.status)}
                  <span className="text-red-600 font-medium text-sm uppercase">
                    {getStatusText(order.status)}
                  </span>
                </div>
              </div>

              {/* Order Items */}
              <div className="px-6 py-4 border-b border-gray-200">
                {order.items && order.items.length > 0 ? (
                  <div className="space-y-4">
                    {order.items.map((item, index) => (
                      <div key={index} className="flex items-start space-x-4">
                        <img
                          src={getImageUrl(item.productImageUrl) || PLACEHOLDER_IMG}
                          alt={item.productName || item.name}
                          className="w-20 h-20 object-cover border border-gray-200"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = PLACEHOLDER_IMG;
                          }}
                        />
                        <div className="flex-1 min-w-0">
                          <h3 className="text-sm text-gray-900 font-normal line-clamp-2">
                            {item.productName || item.name}
                          </h3>
                          <p className="text-xs text-gray-500 mt-1">x{item.quantity}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm text-gray-900">{formatPrice(item.price)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-500">Không có sản phẩm</p>
                )}
              </div>

              {/* Order Footer - Total & Actions */}
              <div className="px-6 py-4 bg-[#fffefb]">
                <div className="flex items-center justify-between">
                  <div className="flex items-center text-sm text-gray-600">
                    <Calendar className="h-4 w-4 mr-1" />
                    {formatDate(order.createdAt || order.date)}
                  </div>
                  <div className="flex items-center space-x-6">
                    <div className="text-right">
                      <span className="text-sm text-gray-600">Tổng thanh toán: </span>
                      <span className="text-xl font-medium text-red-600">
                        {formatPrice(order.totalPrice || order.total)}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2">
                      {canCancelOrder(order.status) && (
                        <button
                          onClick={() => handleCancelOrder(order.id)}
                          className="px-6 py-2 border border-gray-300 rounded-sm text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                          Hủy đơn
                        </button>
                      )}
                      <button
                        onClick={() => handleViewDetails(order)}
                        className="px-6 py-2 bg-red-600 text-white rounded-sm text-sm hover:bg-red-700 transition-colors"
                      >
                        Xem chi tiết
                      </button>
                      {order.status?.toLowerCase() === 'delivered' && (
                        <button className="px-6 py-2 bg-red-600 text-white rounded-sm text-sm hover:bg-red-700 transition-colors">
                          Đánh giá
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
      </div>
      
      {/* Detail Modal */}
      {showDetailModal && selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">
                Chi tiết đơn hàng #{selectedOrder.orderNumber || selectedOrder.id}
              </h2>
              <button
                onClick={closeDetailModal}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <XCircle className="h-6 w-6" />
              </button>
            </div>

            <div className="p-6 space-y-6">
              {/* Order Info */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-500 mb-1">Trạng thái</p>
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(selectedOrder.status)}`}>
                    {getStatusIcon(selectedOrder.status)}
                    <span className="ml-2">{getStatusText(selectedOrder.status)}</span>
                  </span>
                </div>
                <div>
                  <p className="text-sm text-gray-500 mb-1">Ngày đặt hàng</p>
                  <p className="font-medium">{formatDate(selectedOrder.createdAt || selectedOrder.date)}</p>
                </div>
              </div>

              {/* Shipping Address */}
              {selectedOrder.shippingAddress && (
                <div className="border-t border-gray-200 pt-4">
                  <h3 className="font-semibold text-gray-900 mb-3">Địa chỉ giao hàng</h3>
                  <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                    <p><span className="font-medium">Họ tên:</span> {selectedOrder.shippingAddress.fullName}</p>
                    <p><span className="font-medium">Số điện thoại:</span> {selectedOrder.shippingAddress.phone}</p>
                    <p><span className="font-medium">Địa chỉ:</span> {selectedOrder.shippingAddress.address}</p>
                    {selectedOrder.shippingAddress.city && (
                      <p><span className="font-medium">Thành phố:</span> {selectedOrder.shippingAddress.city}</p>
                    )}
                  </div>
                </div>
              )}

              {/* Order Items */}
              <div className="border-t border-gray-200 pt-4">
                <h3 className="font-semibold text-gray-900 mb-3">Sản phẩm</h3>
                <div className="space-y-3">
                  {selectedOrder.items?.map((item, index) => (
                    <div key={index} className="flex items-center justify-between border-b border-gray-100 pb-3 last:border-0">
                      <div className="flex items-center space-x-4 flex-1">
                        {item.productImageUrl && (
                          <img 
                            src={getImageUrl(item.productImageUrl) || PLACEHOLDER_IMG} 
                            alt={item.productName || item.name}
                            className="w-16 h-16 object-cover rounded"
                            onError={(e) => {
                              e.target.onerror = null;
                              e.target.src = PLACEHOLDER_IMG;
                            }}
                          />
                        )}
                        <div className="flex-1">
                          <p className="font-medium text-gray-900">{item.productName || item.name}</p>
                          {item.productSku && (
                            <p className="text-xs text-gray-500">SKU: {item.productSku}</p>
                          )}
                          <p className="text-sm text-gray-600">Số lượng: {item.quantity}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-medium text-gray-900">{formatPrice(item.price)}</p>
                        <p className="text-sm text-gray-500">
                          Tổng: {formatPrice(item.price * item.quantity)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Order Summary */}
              <div className="border-t border-gray-200 pt-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính</span>
                    <span>{formatPrice(selectedOrder.totalPrice || selectedOrder.total)}</span>
                  </div>
                  {selectedOrder.shippingFee && (
                    <div className="flex justify-between text-gray-600">
                      <span>Phí vận chuyển</span>
                      <span>{formatPrice(selectedOrder.shippingFee)}</span>
                    </div>
                  )}
                  {selectedOrder.discount && (
                    <div className="flex justify-between text-green-600">
                      <span>Giảm giá</span>
                      <span>-{formatPrice(selectedOrder.discount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-lg font-bold text-gray-900 pt-2 border-t border-gray-300">
                    <span>Tổng cộng</span>
                    <span className="text-red-600">{formatPrice(selectedOrder.totalPrice || selectedOrder.total)}</span>
                  </div>
                </div>
              </div>

              {/* Payment Method */}
              {selectedOrder.paymentMethod && (
                <div className="border-t border-gray-200 pt-4">
                  <h3 className="font-semibold text-gray-900 mb-2">Phương thức thanh toán</h3>
                  <p className="text-gray-600">{selectedOrder.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng (COD)' : selectedOrder.paymentMethod}</p>
                </div>
              )}

              {/* Notes */}
              {selectedOrder.notes && (
                <div className="border-t border-gray-200 pt-4">
                  <h3 className="font-semibold text-gray-900 mb-2">Ghi chú</h3>
                  <p className="text-gray-600 bg-gray-50 rounded p-3">{selectedOrder.notes}</p>
                </div>
              )}
            </div>

            <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4">
              <div className="flex gap-3">
                {canCancelOrder(selectedOrder.status) && (
                  <button
                    onClick={() => handleCancelOrder(selectedOrder.id)}
                    className="flex-1 bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition-colors font-medium"
                  >
                    Hủy đơn hàng
                  </button>
                )}
                <button
                  onClick={closeDetailModal}
                  className={`${canCancelOrder(selectedOrder.status) ? 'flex-1' : 'w-full'} bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors font-medium`}
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrdersPage;