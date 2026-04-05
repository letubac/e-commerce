/**
 * author: LeTuBac
 */
import React, { useState, useEffect } from 'react';
import { Activity, RefreshCw, ShoppingCart, Users, Star, AlertTriangle, Server } from 'lucide-react';
import adminApi from '../api/adminApi';
import toast from '../utils/toast';

function LogManagement() {
  const [activities, setActivities] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchActivities();
  }, []);

  const fetchActivities = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getRecentActivities(50);
      setActivities(data);
    } catch (error) {
      toast.error(error.message || 'Không thể tải hoạt động hệ thống');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200 animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-28 bg-gray-200 rounded-lg"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  const { recentOrders, recentUsers, recentReviews, stockAlerts, systemHealth } = activities || {};

  return (
    <div className="space-y-6">
      {/* Activity Summary */}
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-2">
            <Activity size={20} className="text-gray-600" />
            <h3 className="text-lg font-semibold text-gray-900">Hoạt động hệ thống (24h qua)</h3>
          </div>
          <button
            onClick={fetchActivities}
            className="flex items-center gap-2 px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <RefreshCw size={16} />
            Làm mới
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Recent Orders */}
          <div className="bg-blue-50 rounded-xl p-4 border border-blue-100">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-2 bg-blue-100 rounded-lg">
                <ShoppingCart size={20} className="text-blue-600" />
              </div>
              <span className="font-medium text-blue-900">Đơn hàng mới</span>
            </div>
            <p className="text-3xl font-bold text-blue-700">{recentOrders?.count ?? 0}</p>
            <p className="text-xs text-blue-500 mt-1">đơn trong 24h qua</p>
            {recentOrders?.totalValue > 0 && (
              <p className="text-sm font-medium text-blue-600 mt-2">
                {recentOrders.totalValue.toLocaleString('vi-VN')}₫
              </p>
            )}
          </div>

          {/* Recent Users */}
          <div className="bg-green-50 rounded-xl p-4 border border-green-100">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-2 bg-green-100 rounded-lg">
                <Users size={20} className="text-green-600" />
              </div>
              <span className="font-medium text-green-900">Người dùng mới</span>
            </div>
            <p className="text-3xl font-bold text-green-700">{recentUsers?.count ?? 0}</p>
            <p className="text-xs text-green-500 mt-1">đăng ký trong 24h qua</p>
          </div>

          {/* Recent Reviews */}
          <div className="bg-yellow-50 rounded-xl p-4 border border-yellow-100">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <Star size={20} className="text-yellow-600" />
              </div>
              <span className="font-medium text-yellow-900">Đánh giá mới</span>
            </div>
            <p className="text-3xl font-bold text-yellow-700">{recentReviews?.count ?? 0}</p>
            <p className="text-xs text-yellow-500 mt-1">
              Điểm TB: {recentReviews?.averageRating > 0 ? recentReviews.averageRating.toFixed(1) : 'N/A'} ⭐
            </p>
          </div>

          {/* Stock Alerts */}
          <div className={`rounded-xl p-4 border ${
            stockAlerts?.outOfStock > 0
              ? 'bg-red-50 border-red-100'
              : stockAlerts?.lowStock > 5
              ? 'bg-orange-50 border-orange-100'
              : 'bg-gray-50 border-gray-100'
          }`}>
            <div className="flex items-center gap-3 mb-3">
              <div className={`p-2 rounded-lg ${
                stockAlerts?.outOfStock > 0 ? 'bg-red-100' : 'bg-gray-100'
              }`}>
                <AlertTriangle
                  size={20}
                  className={stockAlerts?.outOfStock > 0 ? 'text-red-600' : 'text-gray-400'}
                />
              </div>
              <span className={`font-medium ${
                stockAlerts?.outOfStock > 0 ? 'text-red-900' : 'text-gray-700'
              }`}>
                Cảnh báo kho
              </span>
            </div>
            <p className={`text-3xl font-bold ${
              stockAlerts?.outOfStock > 0 ? 'text-red-700' : 'text-gray-600'
            }`}>
              {stockAlerts?.outOfStock ?? 0}
            </p>
            <p className={`text-xs mt-1 ${stockAlerts?.outOfStock > 0 ? 'text-red-500' : 'text-gray-400'}`}>
              hết hàng · {stockAlerts?.lowStock ?? 0} sắp hết
            </p>
          </div>
        </div>
      </div>

      {/* System Health */}
      {systemHealth && (
        <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
          <div className="flex items-center gap-3 mb-5">
            <Server size={20} className="text-gray-600" />
            <h3 className="text-lg font-semibold text-gray-900">Trạng thái hệ thống</h3>
            <span className={`ml-auto px-3 py-1 text-xs rounded-full font-medium ${
              systemHealth.status === 'healthy'
                ? 'bg-green-100 text-green-700'
                : 'bg-red-100 text-red-700'
            }`}>
              {systemHealth.status === 'healthy' ? '● Hoạt động bình thường' : '● Có sự cố'}
            </span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {[
              { label: 'Bộ nhớ sử dụng', value: systemHealth.memoryUsage },
              { label: 'CPU', value: systemHealth.cpuUsage },
              { label: 'Ổ đĩa', value: systemHealth.diskUsage },
              { label: 'Phản hồi TB', value: systemHealth.averageResponseTime },
              { label: 'Tỷ lệ lỗi', value: systemHealth.errorRate },
              { label: 'Cache hit', value: systemHealth.cacheHitRate },
              { label: 'Req/phút', value: String(systemHealth.requestsPerMinute) },
              { label: 'Uptime', value: systemHealth.uptime },
            ].filter(item => item.value != null).map(({ label, value }) => (
              <div key={label} className="bg-gray-50 rounded-lg p-3 text-center">
                <p className="text-base font-bold text-gray-800">{value}</p>
                <p className="text-xs text-gray-500 mt-1">{label}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default LogManagement;
