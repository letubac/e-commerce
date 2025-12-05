import React, { useState, useEffect } from 'react';
import { Package, Clock, CheckCircle, XCircle, Eye, Filter, Calendar, Truck, ShoppingBag, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import toast from '../utils/toast';

const OrdersPage = () => {
  const navigate = useNavigate();
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
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Đơn hàng của tôi</h1>
        
        <div className="flex items-center space-x-2">
          <Filter className="h-4 w-4 text-gray-500" />
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="border border-gray-300 rounded-md px-3 py-1 text-sm"
          >
            <option value="all">Tất cả</option>
            <option value="pending">Chờ xử lý</option>
            <option value="confirmed">Đã xác nhận</option>
            <option value="shipping">Đang giao</option>
            <option value="delivered">Đã giao</option>
            <option value="cancelled">Đã hủy</option>
          </select>
        </div>
      </div>

      {filteredOrders.length === 0 ? (
        <div className="text-center py-12">
          <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Chưa có đơn hàng nào
          </h3>
          <p className="text-gray-500">
            Bạn chưa có đơn hàng nào. Hãy bắt đầu mua sắm ngay!
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredOrders.map(order => (
            <div key={order.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-4">
                  <h3 className="font-semibold text-gray-900">
                    Đơn hàng #{order.orderNumber || order.id}
                  </h3>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(order.status)}`}>
                    {getStatusIcon(order.status)}
                    <span className="ml-1">{getStatusText(order.status)}</span>
                  </span>
                </div>
                
                <div className="flex items-center text-sm text-gray-500">
                  <Calendar className="h-4 w-4 mr-1" />
                  {formatDate(order.createdAt || order.date)}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <p className="text-sm text-gray-500">Tổng tiền</p>
                  <p className="font-semibold text-red-600">
                    {formatPrice(order.totalPrice || order.total)}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Số sản phẩm</p>
                  <p className="font-semibold">{order.items?.length || order.items || 0} sản phẩm</p>
                </div>
                <div className="flex justify-end">
                  <button 
                    onClick={() => handleViewDetails(order)}
                    className="inline-flex items-center px-3 py-1 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                  >
                    <Eye className="h-4 w-4 mr-1" />
                    Xem chi tiết
                  </button>
                </div>
              </div>

              {order.items && order.items.length > 0 && (
                <div className="border-t border-gray-200 pt-4">
                  <p className="text-sm text-gray-500 mb-2">Sản phẩm trong đơn hàng:</p>
                  <div className="space-y-1">
                    {order.items.slice(0, 3).map((item, index) => (
                      <div key={index} className="flex justify-between text-sm">
                        <span>{item.productName || item.name} x{item.quantity}</span>
                        <span className="text-gray-600">
                          {formatPrice(item.price)}
                        </span>
                      </div>
                    ))}
                    {order.items.length > 3 && (
                      <p className="text-xs text-gray-500 mt-1">
                        ... và {order.items.length - 3} sản phẩm khác
                      </p>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

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
                        {item.productImage && (
                          <img 
                            src={item.productImage} 
                            alt={item.productName || item.name}
                            className="w-16 h-16 object-cover rounded"
                            onError={(e) => {
                              e.target.onerror = null;
                              e.target.src = '/images/placeholder.jpg';
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
              <button
                onClick={closeDetailModal}
                className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrdersPage;