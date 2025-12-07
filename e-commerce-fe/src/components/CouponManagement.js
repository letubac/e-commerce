import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, TicketPercent, Search, X } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

function CouponManagement() {
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState(null);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 0,
    minOrderAmount: 0,
    maxDiscountAmount: null,
    usageLimit: null,
    usagePerUser: 1,
    startDate: '',
    endDate: '',
    active: true
  });
  const [filters, setFilters] = useState({
    search: '',
    page: 0,
    size: 10
  });
  const [pagination, setPagination] = useState({
    totalPages: 0,
    totalElements: 0
  });

  useEffect(() => {
    fetchCoupons();
  }, [filters]);

  const fetchCoupons = async () => {
    setLoading(true);
    try {
      const params = {
        page: filters.page,
        size: filters.size,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      };

      if (filters.search) params.keyword = filters.search;

      const response = await adminApi.getCoupons(params);
      
      // parseBusinessResponse đã trả về data
      setCoupons(response.content || []);
      setPagination({
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      });
    } catch (error) {
      console.error('Error fetching coupons:', error);
      toast.error('Không thể tải danh sách mã giảm giá');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (coupon = null) => {
    if (coupon) {
      setEditingCoupon(coupon);
      setFormData({
        code: coupon.code || '',
        name: coupon.name || '',
        description: coupon.description || '',
        discountType: coupon.discountType || 'PERCENTAGE',
        discountValue: coupon.discountValue || 0,
        minOrderAmount: coupon.minOrderAmount || 0,
        maxDiscountAmount: coupon.maxDiscountAmount || null,
        usageLimit: coupon.usageLimit || null,
        usagePerUser: coupon.usagePerUser || 1,
        startDate: coupon.startDate ? new Date(coupon.startDate).toISOString().slice(0, 16) : '',
        endDate: coupon.endDate ? new Date(coupon.endDate).toISOString().slice(0, 16) : '',
        active: coupon.active !== false
      });
    } else {
      setEditingCoupon(null);
      setFormData({
        code: '',
        name: '',
        description: '',
        discountType: 'PERCENTAGE',
        discountValue: 0,
        minOrderAmount: 0,
        maxDiscountAmount: null,
        usageLimit: null,
        usagePerUser: 1,
        startDate: '',
        endDate: '',
        active: true
      });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const submitData = {
        ...formData,
        startDate: formData.startDate ? new Date(formData.startDate).toISOString() : null,
        endDate: formData.endDate ? new Date(formData.endDate).toISOString() : null
      };

      if (editingCoupon) {
        await adminApi.updateCoupon(editingCoupon.id, submitData);
        toast.success('Cập nhật mã giảm giá thành công');
      } else {
        await adminApi.createCoupon(submitData);
        toast.success('Tạo mã giảm giá thành công');
      }
      
      setShowModal(false);
      fetchCoupons();
    } catch (error) {
      console.error('Error saving coupon:', error);
      toast.error(editingCoupon ? 'Không thể cập nhật mã giảm giá' : 'Không thể tạo mã giảm giá');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa mã giảm giá này?')) return;
    
    try {
      await adminApi.deleteCoupon(id);
      toast.success('Xóa mã giảm giá thành công');
      fetchCoupons();
    } catch (error) {
      console.error('Error deleting coupon:', error);
      toast.error('Không thể xóa mã giảm giá');
    }
  };

  const getDiscountTypeBadge = (type) => {
    return type === 'PERCENTAGE' ? (
      <span className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">Phần trăm</span>
    ) : (
      <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">Cố định</span>
    );
  };

  const getStatusBadge = (active, startDate, endDate) => {
    const now = new Date();
    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;

    if (!active) {
      return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">Vô hiệu</span>;
    }
    if (start && now < start) {
      return <span className="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-800">Sắp diễn ra</span>;
    }
    if (end && now > end) {
      return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">Hết hạn</span>;
    }
    return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">Hoạt động</span>;
  };

  if (loading && coupons.length === 0) {
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
          <h3 className="text-lg font-semibold text-gray-900">Quản lý mã giảm giá</h3>
          
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Tìm kiếm mã giảm giá..."
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value, page: 0 })}
              />
            </div>
            
            <button 
              onClick={() => handleOpenModal()}
              className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
            >
              <Plus size={20} />
              Tạo mã giảm giá
            </button>
          </div>
        </div>
      </div>

      {/* Coupons Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Mã
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tên
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Loại giảm giá
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Giá trị
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Số lần sử dụng
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {coupons.length > 0 ? coupons.map((coupon) => (
              <tr key={coupon.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <TicketPercent className="text-red-500 mr-2" size={18} />
                    <span className="text-sm font-medium text-gray-900">{coupon.code}</span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900">{coupon.name}</div>
                  <div className="text-sm text-gray-500 truncate max-w-xs">{coupon.description}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getDiscountTypeBadge(coupon.discountType)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {coupon.discountType === 'PERCENTAGE' 
                    ? `${coupon.discountValue}%`
                    : `${coupon.discountValue?.toLocaleString('vi-VN')}₫`
                  }
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {coupon.usageCount || 0} / {coupon.usageLimit || '∞'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getStatusBadge(coupon.active, coupon.startDate, coupon.endDate)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex gap-2">
                    <button 
                      onClick={() => handleOpenModal(coupon)}
                      className="text-blue-600 hover:text-blue-900 flex items-center gap-1"
                    >
                      <Edit size={16} />
                      Sửa
                    </button>
                    <button 
                      onClick={() => handleDelete(coupon.id)}
                      className="text-red-600 hover:text-red-900 flex items-center gap-1"
                    >
                      <Trash2 size={16} />
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            )) : (
              <tr>
                <td colSpan="7" className="px-6 py-8 text-center text-gray-500">
                  <TicketPercent className="mx-auto mb-2 text-gray-300" size={48} />
                  <p>Chưa có mã giảm giá nào</p>
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
            Hiển thị {coupons.length} / {pagination.totalElements} mã giảm giá
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

      {/* Coupon Form Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <form onSubmit={handleSubmit}>
              <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold text-gray-900">
                  {editingCoupon ? 'Chỉnh sửa mã giảm giá' : 'Tạo mã giảm giá mới'}
                </h3>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={24} />
                </button>
              </div>

              <div className="p-6 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Mã giảm giá *
                    </label>
                    <input
                      type="text"
                      required
                      value={formData.code}
                      onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent uppercase"
                      placeholder="VD: WELCOME10"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Tên hiển thị *
                    </label>
                    <input
                      type="text"
                      required
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="VD: Chào mừng khách hàng mới"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Mô tả
                  </label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    rows="2"
                    placeholder="Mô tả về mã giảm giá..."
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Loại giảm giá *
                    </label>
                    <select
                      value={formData.discountType}
                      onChange={(e) => setFormData({ ...formData, discountType: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    >
                      <option value="PERCENTAGE">Phần trăm (%)</option>
                      <option value="FIXED_AMOUNT">Cố định (₫)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Giá trị giảm *
                    </label>
                    <input
                      type="number"
                      required
                      min="0"
                      max={formData.discountType === 'PERCENTAGE' ? 100 : undefined}
                      value={formData.discountValue}
                      onChange={(e) => setFormData({ ...formData, discountValue: parseFloat(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder={formData.discountType === 'PERCENTAGE' ? '0-100' : '0'}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Đơn hàng tối thiểu (₫)
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={formData.minOrderAmount}
                      onChange={(e) => setFormData({ ...formData, minOrderAmount: parseFloat(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="0"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Giảm tối đa (₫)
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={formData.maxDiscountAmount || ''}
                      onChange={(e) => setFormData({ ...formData, maxDiscountAmount: e.target.value ? parseFloat(e.target.value) : null })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Không giới hạn"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Số lượt sử dụng
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={formData.usageLimit || ''}
                      onChange={(e) => setFormData({ ...formData, usageLimit: e.target.value ? parseInt(e.target.value) : null })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="Không giới hạn"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Số lần/người dùng
                    </label>
                    <input
                      type="number"
                      min="1"
                      value={formData.usagePerUser}
                      onChange={(e) => setFormData({ ...formData, usagePerUser: parseInt(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Ngày bắt đầu
                    </label>
                    <input
                      type="datetime-local"
                      value={formData.startDate}
                      onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Ngày kết thúc
                    </label>
                    <input
                      type="datetime-local"
                      value={formData.endDate}
                      onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="active"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    className="w-4 h-4 text-red-600 border-gray-300 rounded focus:ring-red-500"
                  />
                  <label htmlFor="active" className="ml-2 text-sm text-gray-700">
                    Kích hoạt mã giảm giá
                  </label>
                </div>
              </div>

              <div className="sticky bottom-0 bg-gray-50 px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                >
                  {editingCoupon ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default CouponManagement;
