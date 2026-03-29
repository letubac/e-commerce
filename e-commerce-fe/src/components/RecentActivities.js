import React from 'react';
import {
  ShoppingCart,
  User,
  Star,
  AlertCircle,
  Package,
  TrendingUp,
  Clock
} from 'lucide-react';

/**
 * Recent Activities Component
 * Displays recent system activities and events
 * Handles both Array format and Map/Object format from backend
 */
function RecentActivities({ data }) {
  // Convert BE Map structure to activity array
  let validData = [];

  if (Array.isArray(data)) {
    // Already an array, use as-is
    validData = data;
  } else if (data?.recentActivities && Array.isArray(data.recentActivities)) {
    // Check for nested array in recentActivities property
    validData = data.recentActivities;
  } else if (data?.activities && Array.isArray(data.activities)) {
    // Check for nested array in activities property
    validData = data.activities;
  } else if (typeof data === 'object' && data !== null) {
    // Convert BE Map/Object structure to array of activities
    const now = new Date();
    const activityArray = [];

    // Recent Orders Activity
    if (data.recentOrders) {
      const ordersData = data.recentOrders;
      activityArray.push({
        type: 'ORDER',
        title: `${ordersData.count || 0} Đơn hàng mới`,
        details: `Tổng giá trị: ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ordersData.totalValue || 0)}`,
        createdAt: now,
        status: 'SUCCESS'
      });
    }

    // Recent Users Activity
    if (data.recentUsers) {
      const usersData = data.recentUsers;
      activityArray.push({
        type: 'USER',
        title: `${usersData.count || 0} Người dùng mới`,
        details: usersData.details || 'Người dùng mới đăng ký',
        createdAt: now,
        status: 'SUCCESS'
      });
    }

    // Recent Reviews Activity
    if (data.recentReviews) {
      const reviewsData = data.recentReviews;
      activityArray.push({
        type: 'REVIEW',
        title: `${reviewsData.count || 0} Đánh giá mới`,
        details: `Trung bình ${(reviewsData.averageRating || 0).toFixed(1)} sao`,
        createdAt: now,
        status: 'SUCCESS'
      });
    }

    // Stock Alerts Activity
    if (data.stockAlerts) {
      const alertsData = data.stockAlerts;
      const totalAlerts = (alertsData.lowStock || 0) + (alertsData.outOfStock || 0);
      if (totalAlerts > 0) {
        activityArray.push({
          type: 'ALERT',
          title: 'Cảnh báo kho hàng',
          details: `${alertsData.lowStock || 0} sản phẩm sắp hết, ${alertsData.outOfStock || 0} đã hết hàng`,
          createdAt: now,
          status: 'PENDING'
        });
      }
    }

    // System Health Activity
    if (data.systemHealth) {
      const healthData = data.systemHealth;
      activityArray.push({
        type: 'default',
        title: 'Trạng thái hệ thống',
        details: `Status: ${healthData.status || 'Unknown'} | Uptime: ${healthData.uptime || 'N/A'}`,
        createdAt: now,
        status: healthData.status === 'UP' ? 'SUCCESS' : 'PENDING'
      });
    }

    validData = activityArray;
  }
  
  const getActivityIcon = (type) => {
    const iconMap = {
      'ORDER': ShoppingCart,
      'USER': User,
      'REVIEW': Star,
      'ALERT': AlertCircle,
      'PRODUCT': Package,
      'SALE': TrendingUp,
      'default': Clock
    };

    return iconMap[type] || iconMap['default'];
  };

  const getActivityColor = (type) => {
    const colorMap = {
      'ORDER': 'bg-blue-100 text-blue-600',
      'USER': 'bg-purple-100 text-purple-600',
      'REVIEW': 'bg-yellow-100 text-yellow-600',
      'ALERT': 'bg-red-100 text-red-600',
      'PRODUCT': 'bg-green-100 text-green-600',
      'SALE': 'bg-emerald-100 text-emerald-600',
      'default': 'bg-gray-100 text-gray-600'
    };

    return colorMap[type] || colorMap['default'];
  };

  const formatTime = (dateString) => {
    if (!dateString) return 'Vừa xong';

    try {
      const date = new Date(dateString);
      const now = new Date();
      const diff = now - date;

      // Minutes
      const minutes = Math.floor(diff / 60000);
      if (minutes < 1) return 'Vừa xong';
      if (minutes < 60) return `${minutes} phút trước`;

      // Hours
      const hours = Math.floor(diff / 3600000);
      if (hours < 24) return `${hours} giờ trước`;

      // Days
      const days = Math.floor(diff / 86400000);
      if (days < 7) return `${days} ngày trước`;

      // Format date
      return new Intl.DateTimeFormat('vi-VN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    } catch {
      return dateString;
    }
  };

  if (!validData || validData.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
        <h2 className="text-lg font-bold text-gray-900 mb-6">Hoạt động gần đây</h2>
        <div className="flex flex-col items-center justify-center py-12 text-gray-500">
          <Clock size={32} className="mb-3 opacity-50" />
          <p>Không có hoạt động nào</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
      <h2 className="text-lg font-bold text-gray-900 mb-6">Hoạt động gần đây</h2>

      <div className="space-y-4">
        {validData.map((activity, index) => {
          const IconComponent = getActivityIcon(activity.type);
          const colorClass = getActivityColor(activity.type);

          return (
            <div key={index} className="flex items-start gap-4 pb-4 border-b border-gray-100 last:border-b-0 last:pb-0">
              {/* Icon */}
              <div className={`${colorClass} p-3 rounded-lg flex-shrink-0 mt-1`}>
                <IconComponent size={20} />
              </div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {activity.title || activity.description || 'Hoạt động hệ thống'}
                    </p>
                    {activity.details && (
                      <p className="text-xs text-gray-600 mt-1 truncate">
                        {activity.details}
                      </p>
                    )}
                  </div>
                  <span className="text-xs text-gray-500 whitespace-nowrap">
                    {formatTime(activity.createdAt || activity.timestamp)}
                  </span>
                </div>

                {/* Status Badge */}
                {activity.status && (
                  <div className="mt-2 inline-block">
                    <span
                      className={`text-xs font-medium px-2 py-1 rounded ${
                        activity.status === 'SUCCESS'
                          ? 'bg-green-50 text-green-700'
                          : activity.status === 'PENDING'
                          ? 'bg-yellow-50 text-yellow-700'
                          : activity.status === 'FAILED'
                          ? 'bg-red-50 text-red-700'
                          : 'bg-gray-50 text-gray-700'
                      }`}
                    >
                      {activity.status === 'SUCCESS'
                        ? 'Thành công'
                        : activity.status === 'PENDING'
                        ? 'Chờ xử lý'
                        : activity.status === 'FAILED'
                        ? 'Thất bại'
                        : activity.status}
                    </span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* View All Link */}
      <div className="mt-6 text-center">
        <a
          href="/admin/activities"
          className="text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors"
        >
          Xem tất cả hoạt động →
        </a>
      </div>
    </div>
  );
}

export default RecentActivities;
