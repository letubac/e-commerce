import React, { useState, useEffect } from 'react';
import { 
  Users, 
  Package, 
  ShoppingCart, 
  DollarSign, 
  TrendingUp, 
  Activity,
  Plus,
  Edit,
  Trash2,
  Eye,
  TicketPercent,
  BarChart3,
  Tags,
  FolderTree,
  FileText,
  MessageCircle,
  Zap,
  CheckSquare,
  Bot,
  Calendar
} from 'lucide-react';
import AdminHeader from '../../components/AdminHeader';
import AdminFooter from '../../components/AdminFooter';
import DashboardOverview from '../../components/DashboardOverview';
import AdminChatManagement from '../../components/AdminChatManagement';
import ProductManagement from '../../components/ProductManagement';
import OrderManagement from '../../components/OrderManagement';
import UserManagement from '../../components/UserManagement';
import CouponManagement from '../../components/CouponManagement';
import FlashSaleManagement from '../../components/FlashSaleManagement';
import TaskManagement from '../../components/TaskManagement';
import AdminAnalyticsChat from '../../components/AdminAnalyticsChat';
import CronJobStatus from '../../components/CronJobStatus';
import adminApi from '../../api/adminApi';

function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  
  // Category Management State
  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [categoryForm, setCategoryForm] = useState({
    name: '',
    description: '',
    parentId: null
  });

  useEffect(() => {
    if (activeTab === 'categories') {
      fetchCategories();
    }
  }, [activeTab]);

  // Category Management Functions
  const fetchCategories = async () => {
    setCategoriesLoading(true);
    try {
      const data = await adminApi.getCategories();
      setCategories(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching categories:', error);
      alert(error.message || 'Không thể tải danh sách danh mục');
    } finally {
      setCategoriesLoading(false);
    }
  };

  const handleCreateCategory = () => {
    setEditingCategory(null);
    setCategoryForm({ name: '', description: '', parentId: null });
    setShowCategoryModal(true);
  };

  const handleEditCategory = (category) => {
    setEditingCategory(category);
    setCategoryForm({
      name: category.name,
      description: category.description || '',
      parentId: category.parentId || null
    });
    setShowCategoryModal(true);
  };

  const handleSaveCategory = async (e) => {
    e.preventDefault();
    try {
      if (editingCategory) {
        await adminApi.updateCategory(editingCategory.id, categoryForm);
        alert('Cập nhật danh mục thành công!');
      } else {
        await adminApi.createCategory(categoryForm);
        alert('Tạo danh mục mới thành công!');
      }
      setShowCategoryModal(false);
      fetchCategories();
    } catch (error) {
      console.error('Error saving category:', error);
      alert(error.message || 'Có lỗi xảy ra khi lưu danh mục');
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return;
    
    try {
      await adminApi.deleteCategory(categoryId);
      alert('Xóa danh mục thành công!');
      fetchCategories();
    } catch (error) {
      console.error('Error deleting category:', error);
      alert(error.message || 'Không thể xóa danh mục. Vui lòng kiểm tra xem danh mục có chứa sản phẩm không.');
    }
  };

  const handleToggleCategoryStatus = async (categoryId) => {
    try {
      await adminApi.request(`/admin/categories/${categoryId}/toggle-status`, { method: 'PUT' });
      fetchCategories();
    } catch (error) {
      console.error('Error toggling category status:', error);
      alert(error.message || 'Không thể thay đổi trạng thái danh mục');
    }
  };

  const getChildCategories = (parentId) => {
    return categories.filter(cat => cat.parentId === parentId);
  };

  const getParentCategories = () => {
    return categories.filter(cat => !cat.parentId || cat.parentId === null);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <AdminHeader />
      
      <div className="flex-1">
        <div className="max-w-7xl mx-auto p-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-gray-600">Tổng quan hệ thống quản lý</p>
          </div>

        {/* Navigation Tabs */}
        <div className="mb-8">
          <nav className="flex flex-wrap gap-4">
            {[
              { key: 'overview', label: 'Tổng quan', icon: Activity },
              { key: 'orders', label: 'Đơn hàng', icon: ShoppingCart },
              { key: 'products', label: 'Sản phẩm', icon: Package },
              { key: 'users', label: 'Người dùng', icon: Users },
              { key: 'chat', label: 'Chat hỗ trợ', icon: MessageCircle },
              { key: 'coupons', label: 'Mã giảm giá', icon: TicketPercent },
              { key: 'flashsale', label: 'Flash Sale', icon: Zap },
              { key: 'reports', label: 'Báo cáo', icon: BarChart3 },
              { key: 'brands', label: 'Thương hiệu', icon: Tags },
              { key: 'categories', label: 'Danh mục', icon: FolderTree },
              { key: 'logs', label: 'Log', icon: FileText },
              { key: 'tasks', label: 'Quản lý Task', icon: CheckSquare },
              { key: 'analytics-ai', label: 'AI Analytics', icon: Bot },
              { key: 'cron-jobs', label: 'Cron Jobs', icon: Calendar }
            ].map(({ key, label, icon: Icon }) => (
              <button
                key={key}
                onClick={() => setActiveTab(key)}
                className={`flex items-center gap-2 px-4 py-2 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === key
                    ? 'border-red-500 text-red-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                <Icon size={20} />
                {label}
              </button>
            ))}
          </nav>
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <DashboardOverview />
        )}

        {/* Orders Tab */}
        {activeTab === 'orders' && (
          <OrderManagement />
        )}

        {/* Products Tab */}
        {activeTab === 'products' && (
          <ProductManagement />
        )}

        {/* Users Tab */}
        {activeTab === 'users' && (
          <UserManagement />
        )}

        {/* Chat Tab */}
        {activeTab === 'chat' && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 h-96">
            <AdminChatManagement />
          </div>
        )}

        {/* Coupons Tab */}
        {activeTab === 'coupons' && (
          <CouponManagement />
        )}

        {/* Flash Sale Tab */}
        {activeTab === 'flashsale' && (
          <FlashSaleManagement />
        )}

        {/* Reports Tab */}
        {activeTab === 'reports' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Sales Report */}
              <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">Báo cáo doanh thu</h3>
                  <select className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
                    <option value="7">7 ngày qua</option>
                    <option value="30">30 ngày qua</option>
                    <option value="90">3 tháng qua</option>
                  </select>
                </div>
                <div className="text-center py-8 text-gray-500">
                  <BarChart3 size={48} className="mx-auto mb-4 text-gray-300" />
                  <p>Biểu đồ doanh thu sẽ được hiển thị tại đây</p>
                </div>
              </div>

              {/* Product Performance */}
              <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">Sản phẩm bán chạy</h3>
                  <select className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
                    <option value="week">Tuần này</option>
                    <option value="month">Tháng này</option>
                    <option value="year">Năm này</option>
                  </select>
                </div>
                <div className="space-y-3">
                  <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <span className="text-sm font-medium">iPhone 15 Pro Max</span>
                    <span className="text-sm text-gray-600">89 đã bán</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <span className="text-sm font-medium">Samsung Galaxy S24</span>
                    <span className="text-sm text-gray-600">67 đã bán</span>
                  </div>
                  <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <span className="text-sm font-medium">MacBook Pro M3</span>
                    <span className="text-sm text-gray-600">34 đã bán</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Detailed Analytics */}
            <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Phân tích chi tiết</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center p-4 bg-blue-50 rounded-lg">
                  <p className="text-2xl font-bold text-blue-600">15.2%</p>
                  <p className="text-sm text-gray-600">Tỷ lệ chuyển đổi</p>
                </div>
                <div className="text-center p-4 bg-green-50 rounded-lg">
                  <p className="text-2xl font-bold text-green-600">1,245,000₫</p>
                  <p className="text-sm text-gray-600">Giá trị đơn hàng TB</p>
                </div>
                <div className="text-center p-4 bg-purple-50 rounded-lg">
                  <p className="text-2xl font-bold text-purple-600">4.7</p>
                  <p className="text-sm text-gray-600">Đánh giá trung bình</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Brands Tab */}
        {activeTab === 'brands' && (
          <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Quản lý thương hiệu</h3>
              <button className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
                <Plus size={20} />
                Thêm thương hiệu
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {['Apple', 'Samsung', 'Xiaomi', 'Dell', 'HP', 'Asus', 'Logitech', 'Razer'].map((brand) => (
                <div key={brand} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                  <div className="flex items-center space-x-3 mb-3">
                    <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <Tags size={24} className="text-gray-400" />
                    </div>
                    <div>
                      <h4 className="font-semibold text-gray-900">{brand}</h4>
                      <p className="text-sm text-gray-500">45 sản phẩm</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button className="flex-1 px-3 py-1 text-sm border border-gray-300 rounded hover:bg-gray-50">
                      Xem
                    </button>
                    <button className="flex-1 px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700">
                      Sửa
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Categories Tab */}
        {activeTab === 'categories' && (
          <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Quản lý danh mục</h3>
              <button 
                onClick={handleCreateCategory}
                className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                <Plus size={20} />
                Thêm danh mục
              </button>
            </div>
            
            {categoriesLoading ? (
              <div className="text-center py-8">
                <p className="text-gray-500">Đang tải...</p>
              </div>
            ) : categories.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">Chưa có danh mục nào</p>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Parent categories */}
                {getParentCategories().map((parentCategory) => (
                  <div key={parentCategory.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center space-x-3">
                        <FolderTree size={20} className="text-gray-400" />
                        <div>
                          <h4 className="font-semibold text-gray-900">{parentCategory.name}</h4>
                          <p className="text-sm text-gray-500">{parentCategory.description || 'Không có mô tả'}</p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input 
                            type="checkbox" 
                            checked={parentCategory.active}
                            onChange={() => handleToggleCategoryStatus(parentCategory.id)}
                            className="sr-only peer" 
                          />
                          <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-red-600"></div>
                        </label>
                        <button 
                          onClick={() => handleEditCategory(parentCategory)}
                          className="text-gray-400 hover:text-gray-600"
                        >
                          <Edit size={16} />
                        </button>
                        <button 
                          onClick={() => handleDeleteCategory(parentCategory.id)}
                          className="text-red-400 hover:text-red-600"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </div>
                    
                    {/* Sub categories */}
                    {getChildCategories(parentCategory.id).length > 0 && (
                      <div className="ml-6 space-y-2">
                        {getChildCategories(parentCategory.id).map((subCategory) => (
                          <div key={subCategory.id} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                            <div>
                              <span className="text-sm text-gray-700">{subCategory.name}</span>
                              {subCategory.description && (
                                <p className="text-xs text-gray-500">{subCategory.description}</p>
                              )}
                            </div>
                            <div className="flex items-center space-x-2">
                              <label className="relative inline-flex items-center cursor-pointer">
                                <input 
                                  type="checkbox" 
                                  checked={subCategory.active}
                                  onChange={() => handleToggleCategoryStatus(subCategory.id)}
                                  className="sr-only peer" 
                                />
                                <div className="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-red-600"></div>
                              </label>
                              <button 
                                onClick={() => handleEditCategory(subCategory)}
                                className="text-gray-400 hover:text-gray-600"
                              >
                                <Edit size={14} />
                              </button>
                              <button 
                                onClick={() => handleDeleteCategory(subCategory.id)}
                                className="text-red-400 hover:text-red-600"
                              >
                                <Trash2 size={14} />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Category Modal */}
        {showCategoryModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">
                  {editingCategory ? 'Cập nhật danh mục' : 'Thêm danh mục mới'}
                </h3>
                <button 
                  onClick={() => setShowCategoryModal(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  ✕
                </button>
              </div>
              
              <form onSubmit={handleSaveCategory} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tên danh mục <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={categoryForm.name}
                    onChange={(e) => setCategoryForm({...categoryForm, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Mô tả
                  </label>
                  <textarea
                    value={categoryForm.description}
                    onChange={(e) => setCategoryForm({...categoryForm, description: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    rows="3"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Danh mục cha (nếu có)
                  </label>
                  <select
                    value={categoryForm.parentId || ''}
                    onChange={(e) => setCategoryForm({...categoryForm, parentId: e.target.value ? parseInt(e.target.value) : null})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  >
                    <option value="">-- Không có (Danh mục cha) --</option>
                    {getParentCategories().map((cat) => (
                      <option key={cat.id} value={cat.id} disabled={editingCategory?.id === cat.id}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                </div>
                
                <div className="flex gap-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowCategoryModal(false)}
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

        {/* Logs Tab */}
        {activeTab === 'logs' && (
          <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Log hệ thống</h3>
              <div className="flex gap-2">
                <select className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
                  <option value="">Tất cả loại log</option>
                  <option value="user">Hoạt động người dùng</option>
                  <option value="admin">Hoạt động admin</option>
                  <option value="system">Hệ thống</option>
                  <option value="error">Lỗi</option>
                </select>
                <input
                  type="date"
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
              </div>
            </div>
            
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Thời gian
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Người dùng
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Hành động
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Mô tả
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      IP Address
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Trạng thái
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date().toLocaleString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      admin@eshop.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      LOGIN
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      Đăng nhập vào admin dashboard
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      192.168.1.100
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">
                        Thành công
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(Date.now() - 300000).toLocaleString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      user1@gmail.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      ORDER_CREATE
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      Tạo đơn hàng #ORD-2024-000006
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      192.168.1.105
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">
                        Thành công
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(Date.now() - 600000).toLocaleString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      system
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      BACKUP
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      Tự động backup database
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      127.0.0.1
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">
                        Thành công
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(Date.now() - 900000).toLocaleString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      user2@gmail.com
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      LOGIN_FAILED
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      Đăng nhập thất bại - sai mật khẩu
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      192.168.1.110
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">
                        Thất bại
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Tasks Tab */}
        {activeTab === 'tasks' && <TaskManagement />}

        {/* AI Analytics Tab */}
        {activeTab === 'analytics-ai' && <AdminAnalyticsChat />}

        {/* Cron Jobs Tab */}
        {activeTab === 'cron-jobs' && <CronJobStatus />}

        </div>
      </div>
      
      <AdminFooter />
    </div>
  );
}

export default AdminDashboard;