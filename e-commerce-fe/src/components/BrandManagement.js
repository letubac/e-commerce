/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Tags, Globe, Search } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';
import ConfirmDialog, { ACTION_TYPES } from './ConfirmDialog';

function BrandManagement() {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingBrand, setEditingBrand] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    logoUrl: '',
    websiteUrl: ''
  });

  useEffect(() => {
    fetchBrands();
  }, []);

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getBrands();
      setBrands(Array.isArray(data) ? data : []);
    } catch (error) {
      toast.error(error.message || 'Không thể tải danh sách thương hiệu');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingBrand(null);
    setFormData({ name: '', description: '', logoUrl: '', websiteUrl: '' });
    setShowModal(true);
  };

  const handleEdit = (brand) => {
    setEditingBrand(brand);
    setFormData({
      name: brand.name || '',
      description: brand.description || '',
      logoUrl: brand.logoUrl || '',
      websiteUrl: brand.websiteUrl || ''
    });
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      if (editingBrand) {
        await adminApi.updateBrand(editingBrand.id, formData);
        toast.success('Cập nhật thương hiệu thành công');
      } else {
        await adminApi.createBrand(formData);
        toast.success('Tạo thương hiệu mới thành công');
      }
      setShowModal(false);
      fetchBrands();
    } catch (error) {
      toast.error(error.message || 'Có lỗi xảy ra khi lưu thương hiệu');
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await adminApi.deleteBrand(deleteTarget.id);
      toast.success('Xóa thương hiệu thành công');
      setDeleteTarget(null);
      fetchBrands();
    } catch (error) {
      toast.error(error.message || 'Không thể xóa thương hiệu');
    } finally {
      setDeleteLoading(false);
    }
  };

  const filtered = brands.filter(b =>
    b.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="grid grid-cols-4 gap-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-24 bg-gray-200 rounded-lg"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        {/* Header */}
        <div className="p-6 border-b border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <h3 className="text-lg font-semibold text-gray-900">
            Quản lý thương hiệu ({brands.length})
          </h3>
          <div className="flex gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                placeholder="Tìm kiếm..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
            <button
              onClick={handleCreate}
              className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              <Plus size={18} />
              Thêm thương hiệu
            </button>
          </div>
        </div>

        {/* Grid */}
        {filtered.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            <Tags size={48} className="mx-auto mb-3 text-gray-300" />
            <p>{searchTerm ? 'Không tìm thấy kết quả' : 'Chưa có thương hiệu nào'}</p>
          </div>
        ) : (
          <div className="p-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filtered.map((brand) => (
              <div
                key={brand.id}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex items-center space-x-3 mb-3">
                  <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden flex-shrink-0">
                    {brand.logoUrl ? (
                      <img
                        src={brand.logoUrl}
                        alt={brand.name}
                        className="w-full h-full object-contain"
                      />
                    ) : (
                      <Tags size={24} className="text-gray-400" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <h4 className="font-semibold text-gray-900 truncate">{brand.name}</h4>
                    {brand.websiteUrl && (
                      <a
                        href={brand.websiteUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="text-xs text-blue-500 hover:underline flex items-center gap-1 truncate"
                      >
                        <Globe size={12} /> Website
                      </a>
                    )}
                  </div>
                </div>
                {brand.description && (
                  <p className="text-xs text-gray-500 mb-3 line-clamp-2">{brand.description}</p>
                )}
                <div className="flex gap-2">
                  <button
                    onClick={() => handleEdit(brand)}
                    className="flex-1 flex items-center justify-center gap-1 px-3 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-50 transition-colors"
                  >
                    <Edit size={14} /> Sửa
                  </button>
                  <button
                    onClick={() => setDeleteTarget(brand)}
                    className="flex-1 flex items-center justify-center gap-1 px-3 py-1.5 text-sm bg-red-50 text-red-600 border border-red-200 rounded hover:bg-red-100 transition-colors"
                  >
                    <Trash2 size={14} /> Xóa
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">
                {editingBrand ? 'Cập nhật thương hiệu' : 'Thêm thương hiệu mới'}
              </h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                ✕
              </button>
            </div>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tên thương hiệu <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={e => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
                <textarea
                  value={formData.description}
                  onChange={e => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  rows="2"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">URL Logo</label>
                <input
                  type="url"
                  value={formData.logoUrl}
                  onChange={e => setFormData({ ...formData, logoUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  placeholder="https://..."
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Website</label>
                <input
                  type="url"
                  value={formData.websiteUrl}
                  onChange={e => setFormData({ ...formData, websiteUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  placeholder="https://..."
                />
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                >
                  {editingBrand ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirm */}
      <ConfirmDialog
        isOpen={!!deleteTarget}
        action={ACTION_TYPES.DELETE}
        target={`thương hiệu "${deleteTarget?.name}"`}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        loading={deleteLoading}
      />
    </>
  );
}

export default BrandManagement;
