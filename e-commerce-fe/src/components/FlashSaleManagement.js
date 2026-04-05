/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Power, PowerOff, Package, Calendar, TrendingUp, Clock, AlertCircle, Search } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';
import FlashSaleProductModal from './FlashSaleProductModal';

function FlashSaleManagement() {
  const [flashSales, setFlashSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showProductModal, setShowProductModal] = useState(false);
  const [editingFlashSale, setEditingFlashSale] = useState(null);
  const [selectedFlashSale, setSelectedFlashSale] = useState(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    isActive: true,
    bannerImageUrl: '',
    backgroundColor: '#EF4444'
  });

  useEffect(() => {
    fetchFlashSales();
  }, [page, searchQuery]);

  const fetchFlashSales = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size: 10,
        sortBy: 'startTime',
        sortDirection: 'desc'
      };
      
      if (searchQuery) {
        params.keyword = searchQuery;
      }

      const data = await adminApi.getFlashSales(params);
      const sales = Array.isArray(data) ? data : (data?.content || []);
      setFlashSales(sales);
      setTotalPages(data?.totalPages || 1);
    } catch (error) {
      console.error('Error fetching flash sales:', error);
      toast.error('Lỗi khi tải danh sách Flash Sale');
      setFlashSales([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      // Validation
      if (!formData.name || !formData.startTime || !formData.endTime) {
        toast.error('Vui lòng điền đầy đủ thông tin bắt buộc');
        return;
      }

      const startTime = new Date(formData.startTime);
      const endTime = new Date(formData.endTime);
      
      if (endTime <= startTime) {
        toast.error('Thời gian kết thúc phải sau thời gian bắt đầu');
        return;
      }

      if (editingFlashSale) {
        await adminApi.updateFlashSale(editingFlashSale.id, formData);
        toast.success('Cập nhật Flash Sale thành công');
      } else {
        await adminApi.createFlashSale(formData);
        toast.success('Tạo Flash Sale thành công');
      }

      setShowModal(false);
      resetForm();
      fetchFlashSales();
    } catch (error) {
      console.error('Error saving flash sale:', error);
      toast.error(error.message || 'Lỗi khi lưu Flash Sale');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa Flash Sale này?')) return;

    try {
      await adminApi.deleteFlashSale(id);
      toast.success('Xóa Flash Sale thành công');
      fetchFlashSales();
    } catch (error) {
      console.error('Error deleting flash sale:', error);
      toast.error(error.message || 'Lỗi khi xóa Flash Sale');
    }
  };

  const handleToggleStatus = async (flashSale) => {
    try {
      if (flashSale.isActive) {
        await adminApi.deactivateFlashSale(flashSale.id);
        toast.success('Đã tắt Flash Sale');
      } else {
        await adminApi.activateFlashSale(flashSale.id);
        toast.success('Đã kích hoạt Flash Sale');
      }
      fetchFlashSales();
    } catch (error) {
      console.error('Error toggling flash sale status:', error);
      toast.error(error.message || 'Lỗi khi thay đổi trạng thái');
    }
  };

  const handleEdit = (flashSale) => {
    setEditingFlashSale(flashSale);
    setFormData({
      name: flashSale.name,
      description: flashSale.description || '',
      startTime: formatDateTimeForInput(flashSale.startTime),
      endTime: formatDateTimeForInput(flashSale.endTime),
      isActive: flashSale.isActive,
      bannerImageUrl: flashSale.bannerImageUrl || '',
      backgroundColor: flashSale.backgroundColor || '#EF4444'
    });
    setShowModal(true);
  };

  const handleManageProducts = async (flashSale) => {
    setSelectedFlashSale(flashSale);
    setShowProductModal(true);
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      startTime: '',
      endTime: '',
      isActive: true,
      bannerImageUrl: '',
      backgroundColor: '#EF4444'
    });
    setEditingFlashSale(null);
  };

  const formatDateTimeForInput = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('vi-VN');
  };

  const getStatusBadge = (flashSale) => {
    const now = new Date();
    const start = new Date(flashSale.startTime);
    const end = new Date(flashSale.endTime);

    if (!flashSale.isActive) {
      return <span className="px-2 py-1 bg-gray-200 text-gray-700 rounded text-xs">Đã tắt</span>;
    }
    if (now < start) {
      return <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">Sắp diễn ra</span>;
    }
    if (now >= start && now <= end) {
      return <span className="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">Đang diễn ra</span>;
    }
    return <span className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">Đã kết thúc</span>;
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <TrendingUp size={28} className="text-red-600" />
            Quản lý Flash Sale
          </h1>
          <p className="text-gray-600 mt-1">Thiết lập và quản lý các chương trình Flash Sale</p>
        </div>
        <button
          onClick={() => {
            resetForm();
            setShowModal(true);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
        >
          <Plus size={20} />
          Tạo Flash Sale mới
        </button>
      </div>

      {/* Search */}
      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm Flash Sale theo tên..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
          />
        </div>
      </div>

      {/* Flash Sales List */}
      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      ) : flashSales.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm p-12 text-center">
          <AlertCircle size={48} className="mx-auto text-gray-400 mb-4" />
          <p className="text-gray-600 text-lg mb-2">Chưa có Flash Sale nào</p>
          <p className="text-gray-500 mb-4">Tạo Flash Sale đầu tiên để bắt đầu</p>
          <button
            onClick={() => setShowModal(true)}
            className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            Tạo ngay
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tên chương trình
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thời gian
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Trạng thái
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Sản phẩm
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {flashSales.map((flashSale) => (
                <tr key={flashSale.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div
                        className="w-3 h-3 rounded-full mr-3"
                        style={{ backgroundColor: flashSale.backgroundColor }}
                      ></div>
                      <div>
                        <div className="text-sm font-medium text-gray-900">{flashSale.name}</div>
                        {flashSale.description && (
                          <div className="text-sm text-gray-500 line-clamp-1">{flashSale.description}</div>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div className="flex items-center gap-1">
                      <Calendar size={14} />
                      <span>{formatDateTime(flashSale.startTime)}</span>
                    </div>
                    <div className="flex items-center gap-1 mt-1">
                      <Clock size={14} />
                      <span>{formatDateTime(flashSale.endTime)}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {getStatusBadge(flashSale)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <button
                      onClick={() => handleManageProducts(flashSale)}
                      className="flex items-center gap-1 text-blue-600 hover:text-blue-800"
                    >
                      <Package size={16} />
                      {flashSale.totalProducts || 0} sản phẩm
                    </button>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => handleToggleStatus(flashSale)}
                        className={`p-2 rounded-lg transition ${
                          flashSale.isActive
                            ? 'bg-green-100 text-green-600 hover:bg-green-200'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                        }`}
                        title={flashSale.isActive ? 'Tắt' : 'Bật'}
                      >
                        {flashSale.isActive ? <Power size={18} /> : <PowerOff size={18} />}
                      </button>
                      <button
                        onClick={() => handleEdit(flashSale)}
                        className="p-2 bg-blue-100 text-blue-600 rounded-lg hover:bg-blue-200 transition"
                        title="Sửa"
                      >
                        <Edit size={18} />
                      </button>
                      <button
                        onClick={() => handleDelete(flashSale.id)}
                        className="p-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 transition"
                        title="Xóa"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="px-6 py-4 flex justify-between items-center border-t border-gray-200">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Trước
              </button>
              <span className="text-sm text-gray-700">
                Trang {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page === totalPages - 1}
                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Sau
              </button>
            </div>
          )}
        </div>
      )}

      {/* Create/Edit Modal - Will be implemented in next part */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">
              {editingFlashSale ? 'Sửa Flash Sale' : 'Tạo Flash Sale mới'}
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tên chương trình <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    rows="3"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Thời gian bắt đầu <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="datetime-local"
                      value={formData.startTime}
                      onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Thời gian kết thúc <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="datetime-local"
                      value={formData.endTime}
                      onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Banner URL</label>
                  <input
                    type="text"
                    value={formData.bannerImageUrl}
                    onChange={(e) => setFormData({ ...formData, bannerImageUrl: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                    placeholder="https://example.com/banner.jpg"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Màu nền</label>
                  <div className="flex items-center gap-2">
                    <input
                      type="color"
                      value={formData.backgroundColor}
                      onChange={(e) => setFormData({ ...formData, backgroundColor: e.target.value })}
                      className="w-20 h-10 border border-gray-300 rounded cursor-pointer"
                    />
                    <input
                      type="text"
                      value={formData.backgroundColor}
                      onChange={(e) => setFormData({ ...formData, backgroundColor: e.target.value })}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                      placeholder="#EF4444"
                    />
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id="isActive"
                    checked={formData.isActive}
                    onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                    className="w-4 h-4 text-red-600 focus:ring-red-500 border-gray-300 rounded"
                  />
                  <label htmlFor="isActive" className="text-sm text-gray-700">
                    Kích hoạt ngay
                  </label>
                </div>
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowModal(false);
                    resetForm();
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                >
                  {editingFlashSale ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Product Management Modal */}
      {showProductModal && selectedFlashSale && (
        <FlashSaleProductModal
          flashSale={selectedFlashSale}
          isOpen={showProductModal}
          onClose={() => {
            setShowProductModal(false);
            setSelectedFlashSale(null);
          }}
          onUpdate={fetchFlashSales}
        />
      )}
    </div>
  );
}

export default FlashSaleManagement;
