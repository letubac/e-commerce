import React, { useState, useEffect } from 'react';
import { Eye, Package, Truck, Check, X, Search, Filter } from 'lucide-react';
import adminApi from '../api/adminApi';
import { getImageUrl } from '../api/api';
import toast from '../utils/toast';

function OrderManagement() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [filters, setFilters] = useState({
    status: '',
    search: '',
    page: 0,
    size: 10
  });
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0
  });

  useEffect(() => {
    fetchOrders();
  }, [filters]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const params = {
        page: filters.page,
        size: filters.size,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      };

      const response = filters.status 
        ? await adminApi.getOrdersByStatus(filters.status, params)
        : await adminApi.getAllOrders(params);
      
      // parseBusinessResponse đã trả về data
      setOrders(response.content || []);
      setPagination({
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      });
    } catch (error) {
      console.error('Error fetching orders:', error);
      toast.error('Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleViewOrder = async (orderId) => {
    try {
      const orderDetails = await adminApi.getOrderDetails(orderId);
      setSelectedOrder(orderDetails);
      setShowModal(true);
    } catch (error) {
      console.error('Error fetching order details:', error);
      toast.error('Không thể tải thông tin đơn hàng');
    }
  };

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      await adminApi.updateOrderStatus(orderId, newStatus);
      toast.success('Cập nhật trạng thái đơn hàng thành công');
      fetchOrders();
      if (selectedOrder && selectedOrder.id === orderId) {
        setShowModal(false);
        setSelectedOrder(null);
      }
    } catch (error) {
      console.error('Error updating order status:', error);
      toast.error('Không thể cập nhật trạng thái đơn hàng');
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { bg: 'bg-yellow-100', text: 'text-yellow-800', label: 'Chờ xử lý' },
      CONFIRMED: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'Đã xác nhận' },
      PROCESSING: { bg: 'bg-indigo-100', text: 'text-indigo-800', label: 'Đang xử lý' },
      SHIPPING: { bg: 'bg-purple-100', text: 'text-purple-800', label: 'Đang giao' },
      DELIVERED: { bg: 'bg-green-100', text: 'text-green-800', label: 'Đã giao' },
      CANCELLED: { bg: 'bg-red-100', text: 'text-red-800', label: 'Đã hủy' },
      RETURNED: { bg: 'bg-gray-100', text: 'text-gray-800', label: 'Đã trả' }
    };

    const config = statusConfig[status] || { bg: 'bg-gray-100', text: 'text-gray-800', label: status };
    return (
      <span className={`px-2 py-1 text-xs rounded-full ${config.bg} ${config.text}`}>
        {config.label}
      </span>
    );
  };

  const getPaymentStatusBadge = (status) => {
    const config = status === 'PAID' 
      ? { bg: 'bg-green-100', text: 'text-green-800', label: 'Đã thanh toán' }
      : { bg: 'bg-orange-100', text: 'text-orange-800', label: 'Chưa thanh toán' };
    
    return (
      <span className={`px-2 py-1 text-xs rounded-full ${config.bg} ${config.text}`}>
        {config.label}
      </span>
    );
  };

  if (loading && orders.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div className="animate-pulse space-y-4">
          <div className="h-10 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <h3 className="text-lg font-semibold text-gray-900">Quản lý đơn hàng</h3>
          
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Tìm kiếm đơn hàng..."
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value, page: 0 })}
              />
            </div>
            
            <select 
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value, page: 0 })}
            >
              <option value="">Tất cả trạng thái</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="CONFIRMED">Đã xác nhận</option>
              <option value="PROCESSING">Đang xử lý</option>
              <option value="SHIPPING">Đang giao</option>
              <option value="DELIVERED">Đã giao</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>
        </div>
      </div>

      {/* Orders Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Mã đơn hàng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Khách hàng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Sản phẩm
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tổng tiền
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thanh toán
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ngày tạo
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {orders.length > 0 ? orders.map((order) => (
              <tr key={order.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  #{order.id || order.orderNumber}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">
                    {order.customerName || order.shippingAddress?.fullName || 'N/A'}
                  </div>
                  <div className="text-sm text-gray-500">
                    {order.customerEmail || order.user?.email || ''}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {order.totalItems || order.orderItems?.length || 0} sản phẩm
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {(order.totalAmount || order.total || 0).toLocaleString('vi-VN')}₫
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getStatusBadge(order.status)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getPaymentStatusBadge(order.paymentStatus)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <button 
                    onClick={() => handleViewOrder(order.id)}
                    className="text-blue-600 hover:text-blue-900 flex items-center gap-1"
                  >
                    <Eye size={16} />
                    Xem
                  </button>
                </td>
              </tr>
            )) : (
              <tr>
                <td colSpan="8" className="px-6 py-8 text-center text-gray-500">
                  <Package className="mx-auto mb-2 text-gray-300" size={48} />
                  <p>Không có đơn hàng nào</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-500">
            Hiển thị {orders.length} / {pagination.totalElements} đơn hàng
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setFilters({ ...filters, page: filters.page - 1 })}
              disabled={filters.page === 0}
              className="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Trước
            </button>
            <span className="px-3 py-1">
              Trang {filters.page + 1} / {pagination.totalPages}
            </span>
            <button
              onClick={() => setFilters({ ...filters, page: filters.page + 1 })}
              disabled={filters.page >= pagination.totalPages - 1}
              className="px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Sau
            </button>
          </div>
        </div>
      )}

      {/* Order Detail Modal */}
      {showModal && selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">
                Chi tiết đơn hàng #{selectedOrder.id}
              </h3>
              <button
                onClick={() => {
                  setShowModal(false);
                  setSelectedOrder(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X size={24} />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-6">
              {/* Order Status and Actions */}
              <div className="flex items-center justify-between bg-gray-50 rounded-lg p-4">
                <div className="flex items-center gap-4">
                  <div>
                    <div className="text-sm text-gray-500 mb-1">Trạng thái đơn hàng</div>
                    {getStatusBadge(selectedOrder.status)}
                  </div>
                  <div>
                    <div className="text-sm text-gray-500 mb-1">Thanh toán</div>
                    {getPaymentStatusBadge(selectedOrder.paymentStatus)}
                  </div>
                </div>
                
                {selectedOrder.status !== 'DELIVERED' && selectedOrder.status !== 'CANCELLED' && (
                  <div className="flex gap-2">
                    {selectedOrder.status === 'PENDING' && (
                      <button
                        onClick={() => handleUpdateStatus(selectedOrder.id, 'CONFIRMED')}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
                      >
                        <Check size={16} />
                        Xác nhận
                      </button>
                    )}
                    {selectedOrder.status === 'CONFIRMED' && (
                      <button
                        onClick={() => handleUpdateStatus(selectedOrder.id, 'PROCESSING')}
                        className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 flex items-center gap-2"
                      >
                        <Package size={16} />
                        Xử lý
                      </button>
                    )}
                    {selectedOrder.status === 'PROCESSING' && (
                      <button
                        onClick={() => handleUpdateStatus(selectedOrder.id, 'SHIPPING')}
                        className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 flex items-center gap-2"
                      >
                        <Truck size={16} />
                        Giao hàng
                      </button>
                    )}
                    {selectedOrder.status === 'SHIPPING' && (
                      <button
                        onClick={() => handleUpdateStatus(selectedOrder.id, 'DELIVERED')}
                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center gap-2"
                      >
                        <Check size={16} />
                        Hoàn thành
                      </button>
                    )}
                    <button
                      onClick={() => handleUpdateStatus(selectedOrder.id, 'CANCELLED')}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 flex items-center gap-2"
                    >
                      <X size={16} />
                      Hủy
                    </button>
                  </div>
                )}
              </div>

              {/* Customer Info */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="font-semibold text-gray-900 mb-3">Thông tin khách hàng</h4>
                  <div className="space-y-2 text-sm">
                    <div>
                      <span className="text-gray-500">Tên:</span>{' '}
                      <span className="text-gray-900 font-medium">
                        {selectedOrder.shippingAddress?.fullName || 'N/A'}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-500">Email:</span>{' '}
                      <span className="text-gray-900">{selectedOrder.user?.email || 'N/A'}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">SĐT:</span>{' '}
                      <span className="text-gray-900">
                        {selectedOrder.shippingAddress?.phoneNumber || 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>

                <div>
                  <h4 className="font-semibold text-gray-900 mb-3">Địa chỉ giao hàng</h4>
                  <div className="text-sm text-gray-700">
                    {selectedOrder.shippingAddress ? (
                      <>
                        <p>{selectedOrder.shippingAddress.addressLine1}</p>
                        {selectedOrder.shippingAddress.addressLine2 && (
                          <p>{selectedOrder.shippingAddress.addressLine2}</p>
                        )}
                        <p>{selectedOrder.shippingAddress.city}</p>
                        {selectedOrder.shippingAddress.country && (
                          <p>{selectedOrder.shippingAddress.country}</p>
                        )}
                      </>
                    ) : (
                      <p className="text-gray-500">Chưa có địa chỉ</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Order Items */}
              <div>
                <h4 className="font-semibold text-gray-900 mb-3">Sản phẩm</h4>
                <div className="border border-gray-200 rounded-lg divide-y divide-gray-200">
                  {selectedOrder.orderItems?.map((item, index) => (
                    <div key={index} className="p-4 flex items-center gap-4">
                      <img
                        src={getImageUrl(item.productImageUrl) || '/images/placeholder.jpg'}
                        alt={item.productName}
                        className="w-16 h-16 object-cover rounded-lg border border-gray-200"
                      />
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">{item.productName}</div>
                        <div className="text-sm text-gray-500">
                          {item.price?.toLocaleString('vi-VN')}₫ x {item.quantity}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-medium text-gray-900">
                          {(item.price * item.quantity).toLocaleString('vi-VN')}₫
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Order Summary */}
              <div className="border-t border-gray-200 pt-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Tạm tính:</span>
                    <span className="text-gray-900">
                      {(selectedOrder.subtotal || 0).toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Phí vận chuyển:</span>
                    <span className="text-gray-900">
                      {(selectedOrder.shippingCost || 0).toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                  {selectedOrder.discountAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Giảm giá:</span>
                      <span className="text-red-600">
                        -{(selectedOrder.discountAmount || 0).toLocaleString('vi-VN')}₫
                      </span>
                    </div>
                  )}
                  <div className="flex justify-between text-base font-semibold border-t border-gray-200 pt-2">
                    <span className="text-gray-900">Tổng cộng:</span>
                    <span className="text-red-600">
                      {(selectedOrder.total || 0).toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                </div>
              </div>

              {/* Order Timeline */}
              <div>
                <h4 className="font-semibold text-gray-900 mb-3">Lịch sử đơn hàng</h4>
                <div className="text-sm text-gray-500">
                  <p>Ngày tạo: {new Date(selectedOrder.createdAt).toLocaleString('vi-VN')}</p>
                  {selectedOrder.updatedAt && (
                    <p>Cập nhật lần cuối: {new Date(selectedOrder.updatedAt).toLocaleString('vi-VN')}</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderManagement;
