import React from 'react';
import { AlertTriangle, CheckCircle, AlertCircle } from 'lucide-react';

/**
 * System Health Card Component
 * Displays system status and health information
 */
function SystemHealthCard({ data }) {
  // Ensure data is object
  const validData = (typeof data === 'object' && data !== null) ? data : {};
  
  const getHealthStatus = () => {
    const status = validData?.status || 'UNKNOWN';

    if (status === 'HEALTHY') {
      return {
        label: 'Tốt',
        color: 'bg-green-50',
        textColor: 'text-green-700',
        borderColor: 'border-green-200',
        icon: CheckCircle,
        iconColor: 'text-green-600'
      };
    } else if (status === 'WARNING') {
      return {
        label: 'Cảnh báo',
        color: 'bg-yellow-50',
        textColor: 'text-yellow-700',
        borderColor: 'border-yellow-200',
        icon: AlertTriangle,
        iconColor: 'text-yellow-600'
      };
    } else {
      return {
        label: 'Có lỗi',
        color: 'bg-red-50',
        textColor: 'text-red-700',
        borderColor: 'border-red-200',
        icon: AlertCircle,
        iconColor: 'text-red-600'
      };
    }
  };

  const health = getHealthStatus();
  const HealthIcon = health.icon;

  return (
    <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
      <h2 className="text-lg font-bold text-gray-900 mb-6">Trạng thái hệ thống</h2>

      {/* Status */}
      <div className={`${health.color} border ${health.borderColor} rounded-lg p-4 mb-6`}>
        <div className="flex items-center gap-3">
          <HealthIcon className={`${health.iconColor} flex-shrink-0`} size={24} />
          <div>
            <p className={`text-sm font-medium ${health.textColor}`}>{health.label}</p>
            <p className="text-xs text-gray-600 mt-1">Hệ thống đang hoạt động bình thường</p>
          </div>
        </div>
      </div>

      {/* Server Information */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">Uptime:</span>
          <span className="text-sm font-medium text-gray-900">{validData?.uptime || 'N/A'}</span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">CPU:</span>
          <span className="text-sm font-medium text-gray-900">{validData?.cpuUsage || 'N/A'}%</span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">Bộ nhớ:</span>
          <span className="text-sm font-medium text-gray-900">{validData?.memoryUsage || 'N/A'}%</span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">Disk:</span>
          <span className="text-sm font-medium text-gray-900">{validData?.diskUsage || 'N/A'}%</span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">Cơ sở dữ liệu:</span>
          <span className={`text-sm font-medium ${
            validData?.databaseStatus === 'CONNECTED' ? 'text-green-600' : 'text-red-600'
          }`}>
            {validData?.databaseStatus === 'CONNECTED' ? 'Kết nối' : 'Mất kết nối'}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-gray-600">Thời gian phản hồi:</span>
          <span className="text-sm font-medium text-gray-900">{validData?.responseTime || 'N/A'}ms</span>
        </div>
      </div>

      {/* Last Check */}
      <div className="mt-6 pt-4 border-t border-gray-100">
        <p className="text-xs text-gray-500">
          Kiểm tra lần cuối: {formatLastCheck(validData?.lastCheck)}
        </p>
      </div>
    </div>
  );
}

function formatLastCheck(timestamp) {
  if (!timestamp) return 'Vừa xong';

  try {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'Vừa xong';
    if (minutes < 60) return `${minutes} phút trước`;

    const hours = Math.floor(diff / 3600000);
    if (hours < 24) return `${hours} giờ trước`;

    return new Intl.DateTimeFormat('vi-VN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch {
    return timestamp;
  }
}

export default SystemHealthCard;
