import React, { useState, useEffect } from 'react';
import {
  Users,
  Package,
  ShoppingCart,
  DollarSign,
  Activity,
  AlertCircle,
  CheckCircle,
  Clock,
  Zap
} from 'lucide-react';
import DashboardMetricCard from './DashboardMetricCard';
import SalesChart from './SalesChart';
import OrderStatusChart from './OrderStatusChart';
import UserGrowthChart from './UserGrowthChart';
import RecentActivities from './RecentActivities';
import SystemHealthCard from './SystemHealthCard';
import adminApi from '../api/adminApi';

/**
 * Professional Admin Dashboard Overview
 * Main dashboard component with real-time metrics and charts
 */
function DashboardOverview() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
    userGrowth: 0,
    productGrowth: 0,
    orderGrowth: 0,
    revenueGrowth: 0
  });

  const [salesData, setSalesData] = useState([]);
  const [orderStats, setOrderStats] = useState({});
  const [userStats, setUserStats] = useState({});
  const [productStats, setProductStats] = useState({});
  const [recentActivities, setRecentActivities] = useState([]);
  const [systemHealth, setSystemHealth] = useState({});
  const [loading, setLoading] = useState(true);
  const [timeFilter, setTimeFilter] = useState(7); // 7 days by default

  useEffect(() => {
    fetchAllDashboardData();
    // Auto-refresh every 30 seconds
    const interval = setInterval(fetchAllDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    fetchSalesData(timeFilter);
  }, [timeFilter]);

  const fetchAllDashboardData = async () => {
    setLoading(true);
    try {
      const [overview, orders, users, products, activities, health] = await Promise.all([
        adminApi.getDashboardOverview(),
        adminApi.getOrderStatistics(),
        adminApi.getUserStatistics(),
        adminApi.getProductStatistics(),
        adminApi.getRecentActivities(10),
        adminApi.getSystemHealth()
      ]);

      setStats(overview);
      setOrderStats(orders);
      setUserStats(users);
      setProductStats(products);
      setRecentActivities(activities); // Pass full activities object, component will handle Map→Array conversion
      setSystemHealth(health);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchSalesData = async (days) => {
    try {
      const data = await adminApi.getSalesStatistics(days);
      // BE returns: { salesByDay: {...}, ordersByStatus: {...}, ... }
      setSalesData(data);
    } catch (error) {
      console.error('Error fetching sales data:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-12">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
              <p className="text-gray-600 text-sm mt-1">Tổng quan hệ thống quản lý bán hàng</p>
            </div>
            <button
              onClick={fetchAllDashboardData}
              className="flex items-center gap-2 px-4 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors"
            >
              <Activity size={18} />
              <span className="text-sm font-medium">Làm mới</span>
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Key Metrics - Row 1 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <DashboardMetricCard
            title="Tổng doanh thu"
            value={stats.totalRevenue}
            unit="đ"
            icon={DollarSign}
            color="bg-emerald-500"
            trend={stats.revenueGrowth}
            trendIs={stats.revenueGrowth >= 0 ? 'positive' : 'negative'}
            subtitle="So với tuần trước"
          />
          <DashboardMetricCard
            title="Tổng đơn hàng"
            value={stats.totalOrders}
            icon={ShoppingCart}
            color="bg-blue-500"
            trend={stats.orderGrowth}
            trendIs={stats.orderGrowth >= 0 ? 'positive' : 'negative'}
            subtitle="So với tuần trước"
          />
          <DashboardMetricCard
            title="Tổng người dùng"
            value={stats.totalUsers}
            icon={Users}
            color="bg-purple-500"
            trend={stats.userGrowth}
            trendIs={stats.userGrowth >= 0 ? 'positive' : 'negative'}
            subtitle="So với tuần trước"
          />
          <DashboardMetricCard
            title="Tổng sản phẩm"
            value={stats.totalProducts}
            icon={Package}
            color="bg-orange-500"
            trend={stats.productGrowth}
            trendIs={stats.productGrowth >= 0 ? 'positive' : 'negative'}
            subtitle="So với tuần trước"
          />
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          {/* Sales Chart - Takes more space */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h2 className="text-lg font-bold text-gray-900">Doanh số bán hàng</h2>
                  <p className="text-sm text-gray-600">Doanh thu theo ngày</p>
                </div>
                <div className="flex gap-2">
                  {[7, 14, 30].map((days) => (
                    <button
                      key={days}
                      onClick={() => setTimeFilter(days)}
                      className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                        timeFilter === days
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      {days}d
                    </button>
                  ))}
                </div>
              </div>
              <SalesChart data={salesData} />
            </div>
          </div>

          {/* Order Status Chart */}
          <div>
            <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
              <div>
                <h2 className="text-lg font-bold text-gray-900 mb-6">Trạng thái đơn hàng</h2>
              </div>
              <OrderStatusChart data={orderStats} />
            </div>
          </div>
        </div>

        {/* Additional Metrics Row */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
            <div className="flex items-center gap-4">
              <div className="bg-red-100 p-3 rounded-lg">
                <AlertCircle className="text-red-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Đơn hàng chờ xử lý</p>
                <p className="text-2xl font-bold text-gray-900">{orderStats.pendingOrders || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
            <div className="flex items-center gap-4">
              <div className="bg-yellow-100 p-3 rounded-lg">
                <Clock className="text-yellow-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Sản phẩm hạn chế</p>
                <p className="text-2xl font-bold text-gray-900">{productStats.lowStockProducts || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
            <div className="flex items-center gap-4">
              <div className="bg-green-100 p-3 rounded-lg">
                <CheckCircle className="text-green-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Người dùng hoạt động</p>
                <p className="text-2xl font-bold text-gray-900">{userStats.activeUsers || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
            <div className="flex items-center gap-4">
              <div className="bg-blue-100 p-3 rounded-lg">
                <Zap className="text-blue-600" size={24} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Trạng thái hệ thống</p>
                <p className="text-2xl font-bold text-green-600">
                  {systemHealth.status === 'HEALTHY' ? 'Tốt' : 'Cảnh báo'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* User Growth & Recent Activities */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* User Growth Chart */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
              <div>
                <h2 className="text-lg font-bold text-gray-900 mb-6">Tăng trưởng người dùng</h2>
              </div>
              <UserGrowthChart data={userStats} />
            </div>
          </div>

          {/* System Health */}
          <div>
            <SystemHealthCard data={systemHealth} />
          </div>
        </div>

        {/* Recent Activities */}
        <div className="mt-8">
          <RecentActivities data={recentActivities} />
        </div>
      </div>
    </div>
  );
}

export default DashboardOverview;
