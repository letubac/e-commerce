/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, FolderTree, ChevronDown, ChevronRight, Eye, EyeOff, PlusCircle } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';
import ConfirmDialog, { ACTION_TYPES } from './ConfirmDialog';

function CategoryManagement() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [expandedNodes, setExpandedNodes] = useState(new Set());
  const [togglingId, setTogglingId] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    parentId: null,
    imageUrl: '',
    sortOrder: 0
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getCategories();
      setCategories(Array.isArray(data) ? data : []);
    } catch (error) {
      toast.error(error.message || 'Không thể tải danh sách danh mục');
    } finally {
      setLoading(false);
    }
  };

  const getRootCategories = () => categories.filter(c => !c.parentId);
  const getChildren = (parentId) => categories.filter(c => c.parentId === parentId);
  const getRootCategoriesForSelect = (excludeId) =>
    categories.filter(c => !c.parentId && c.id !== excludeId);

  const toggleExpand = (id) => {
    setExpandedNodes(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleCreate = (parentId = null) => {
    setEditingCategory(null);
    setFormData({ name: '', description: '', parentId, imageUrl: '', sortOrder: 0 });
    setShowModal(true);
  };

  const handleEdit = (category) => {
    setEditingCategory(category);
    setFormData({
      name: category.name || '',
      description: category.description || '',
      parentId: category.parentId || null,
      imageUrl: category.imageUrl || '',
      sortOrder: category.sortOrder || 0
    });
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      if (editingCategory) {
        await adminApi.updateCategory(editingCategory.id, formData);
        toast.success('Cập nhật danh mục thành công');
      } else {
        await adminApi.createCategory(formData);
        toast.success('Tạo danh mục mới thành công');
      }
      setShowModal(false);
      fetchCategories();
    } catch (error) {
      toast.error(error.message || 'Có lỗi xảy ra khi lưu danh mục');
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await adminApi.deleteCategory(deleteTarget.id);
      toast.success('Xóa danh mục thành công');
      setDeleteTarget(null);
      fetchCategories();
    } catch (error) {
      toast.error(error.message || 'Không thể xóa danh mục. Vui lòng kiểm tra xem danh mục có chứa sản phẩm không.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleToggle = async (category) => {
    setTogglingId(category.id);
    try {
      await adminApi.toggleCategoryStatus(category.id);
      const newStatus = !category.active;
      toast.success(`${newStatus ? 'Kích hoạt' : 'Vô hiệu hóa'} danh mục thành công`);
      fetchCategories();
    } catch (error) {
      toast.error(error.message || 'Không thể thay đổi trạng thái danh mục');
    } finally {
      setTogglingId(null);
    }
  };

  const renderNode = (category, level = 0) => {
    const children = getChildren(category.id);
    const isExpanded = expandedNodes.has(category.id);
    const isToggling = togglingId === category.id;
    const indent = level * 24;

    return (
      <div key={category.id}>
        <div
          className={`flex items-center justify-between px-4 py-3 border-b border-gray-100 hover:bg-gray-50 ${
            !category.active ? 'opacity-60' : ''
          }`}
          style={{ paddingLeft: `${16 + indent}px` }}
        >
          <div className="flex items-center gap-2 min-w-0">
            {/* Expand toggle */}
            {children.length > 0 ? (
              <button
                onClick={() => toggleExpand(category.id)}
                className="text-gray-400 hover:text-gray-600 flex-shrink-0"
              >
                {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </button>
            ) : (
              <span className="w-4 flex-shrink-0" />
            )}

            <FolderTree size={16} className={`flex-shrink-0 ${level === 0 ? 'text-red-400' : 'text-gray-400'}`} />

            <div className="min-w-0">
              <span className={`font-medium text-gray-900 ${level === 0 ? 'text-sm' : 'text-xs'}`}>
                {category.name}
              </span>
              {category.description && (
                <p className="text-xs text-gray-400 truncate max-w-xs">{category.description}</p>
              )}
            </div>

            {children.length > 0 && (
              <span className="ml-1 text-xs bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded-full flex-shrink-0">
                {children.length}
              </span>
            )}

            <span className={`text-xs px-1.5 py-0.5 rounded-full flex-shrink-0 ${
              category.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
            }`}>
              {category.active ? 'Hoạt động' : 'Ẩn'}
            </span>
          </div>

          <div className="flex items-center gap-1 flex-shrink-0">
            {/* Toggle active */}
            <button
              onClick={() => handleToggle(category)}
              disabled={isToggling}
              title={category.active ? 'Vô hiệu hóa' : 'Kích hoạt'}
              className={`p-1.5 rounded hover:bg-gray-100 transition-colors ${
                isToggling ? 'opacity-50 cursor-not-allowed' : ''
              } ${category.active ? 'text-green-500 hover:text-green-700' : 'text-gray-400 hover:text-gray-600'}`}
            >
              {category.active ? <Eye size={15} /> : <EyeOff size={15} />}
            </button>

            {/* Add child */}
            <button
              onClick={() => handleCreate(category.id)}
              title="Thêm danh mục con"
              className="p-1.5 rounded hover:bg-gray-100 text-blue-400 hover:text-blue-600 transition-colors"
            >
              <PlusCircle size={15} />
            </button>

            {/* Edit */}
            <button
              onClick={() => handleEdit(category)}
              title="Chỉnh sửa"
              className="p-1.5 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-700 transition-colors"
            >
              <Edit size={15} />
            </button>

            {/* Delete */}
            <button
              onClick={() => setDeleteTarget(category)}
              title="Xóa"
              className="p-1.5 rounded hover:bg-red-50 text-red-400 hover:text-red-600 transition-colors"
            >
              <Trash2 size={15} />
            </button>
          </div>
        </div>

        {/* Children */}
        {isExpanded && children.length > 0 && (
          <div className="bg-gray-50/50">
            {children.map(child => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div className="animate-pulse space-y-3">
          <div className="h-8 bg-gray-200 rounded w-1/4" />
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-12 bg-gray-200 rounded" />
          ))}
        </div>
      </div>
    );
  }

  const rootCategories = getRootCategories();

  return (
    <>
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        {/* Header */}
        <div className="p-6 border-b border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Cây danh mục ({categories.length})
            </h3>
            <p className="text-sm text-gray-500 mt-0.5">
              {rootCategories.length} danh mục cha • {categories.length - rootCategories.length} danh mục con
            </p>
          </div>
          <button
            onClick={() => handleCreate(null)}
            className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
          >
            <Plus size={18} />
            Thêm danh mục cha
          </button>
        </div>

        {/* Tree */}
        {rootCategories.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            <FolderTree size={48} className="mx-auto mb-3 text-gray-300" />
            <p>Chưa có danh mục nào</p>
          </div>
        ) : (
          <div>
            {rootCategories.map(cat => renderNode(cat, 0))}
          </div>
        )}
      </div>

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">
                {editingCategory ? 'Cập nhật danh mục' : 'Thêm danh mục mới'}
              </h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                ✕
              </button>
            </div>

            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tên danh mục <span className="text-red-500">*</span>
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
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Danh mục cha (nếu có)
                </label>
                <select
                  value={formData.parentId || ''}
                  onChange={e => setFormData({ ...formData, parentId: e.target.value ? parseInt(e.target.value) : null })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                >
                  <option value="">-- Không có (Danh mục cha) --</option>
                  {getRootCategoriesForSelect(editingCategory?.id).map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">URL Ảnh</label>
                <input
                  type="url"
                  value={formData.imageUrl}
                  onChange={e => setFormData({ ...formData, imageUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  placeholder="https://..."
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Thứ tự hiển thị</label>
                <input
                  type="number"
                  value={formData.sortOrder}
                  onChange={e => setFormData({ ...formData, sortOrder: parseInt(e.target.value) || 0 })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                  min="0"
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
                  {editingCategory ? 'Cập nhật' : 'Tạo mới'}
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
        target={`danh mục "${deleteTarget?.name}"`}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
        loading={deleteLoading}
      />
    </>
  );
}

export default CategoryManagement;
