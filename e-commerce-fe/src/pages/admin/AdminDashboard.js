/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Users, 
  Package, 
  ShoppingCart, 
  Activity,
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
import AiAgentsDashboard from '../../components/AiAgentsDashboard';
import CronJobStatus from '../../components/CronJobStatus';
import BrandManagement from '../../components/BrandManagement';
import CategoryManagement from '../../components/CategoryManagement';
import LogManagement from '../../components/LogManagement';
import toast from '../../utils/toast';

function AdminDashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');

  // Session-expired event listener (fired by adminApi when 401 received)
  useEffect(() => {
    const handleSessionExpired = () => {
      toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      navigate('/login');
    };
    window.addEventListener('admin:session-expired', handleSessionExpired);
    return () => window.removeEventListener('admin:session-expired', handleSessionExpired);
  }, [navigate]);
  
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
              { key: 'analytics-ai', label: 'AI Agents', icon: Bot },
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
        {activeTab === 'brands' && <BrandManagement />}

        {/* Categories Tab */}
        {activeTab === 'categories' && <CategoryManagement />}

        {/* Logs Tab */}
        {activeTab === 'logs' && <LogManagement />}

        {/* Tasks Tab */}
        {activeTab === 'tasks' && <TaskManagement />}

        {/* AI Agents Tab */}
        {activeTab === 'analytics-ai' && <AiAgentsDashboard />}

        {/* Cron Jobs Tab */}
        {activeTab === 'cron-jobs' && <CronJobStatus />}

        </div>
      </div>
      
      <AdminFooter />
    </div>
  );
}

export default AdminDashboard;