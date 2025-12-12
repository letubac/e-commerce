import React, { useState, useRef, useEffect } from 'react';
import { Bell, X, Check, CheckCheck, Trash2, Settings } from 'lucide-react';
import { useNotifications } from '../context/NotificationContext';
import { useNavigate } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';

export default function NotificationBell({ variant = 'light' }) {
  const { notifications, unreadCount, markAsRead, markAllAsRead, deleteNotification } = useNotifications();
  const [isOpen, setIsOpen] = useState(false);
  const [filter, setFilter] = useState('all'); // all, unread
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  // Variant styles
  const styles = {
    light: {
      button: 'text-white hover:bg-red-700',
      badge: 'text-red-600 bg-white'
    },
    dark: {
      button: 'text-gray-600 hover:text-gray-900 hover:bg-gray-50',
      badge: 'text-white bg-red-500'
    }
  };

  const currentStyle = styles[variant] || styles.light;

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen]);

  const filteredNotifications = filter === 'unread' 
    ? notifications.filter(n => !n.isRead)
    : notifications;

  const recentNotifications = filteredNotifications.slice(0, 10);

  const handleNotificationClick = async (notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }
    
    if (notification.link) {
      navigate(notification.link);
      setIsOpen(false);
    }
  };

  const handleDelete = async (e, notificationId) => {
    e.stopPropagation();
    await deleteNotification(notificationId);
  };

  const getNotificationIcon = (type) => {
    const icons = {
      ORDER: '📦',
      FLASH_SALE: '🔥',
      COUPON: '🎟️',
      PROMOTION: '🎉',
      PRODUCT: '🛍️',
      SYSTEM: '⚙️',
    };
    return icons[type] || '🔔';
  };

  const getPriorityColor = (priority) => {
    const colors = {
      URGENT: 'bg-red-100 border-red-500',
      HIGH: 'bg-orange-100 border-orange-500',
      NORMAL: 'bg-blue-50 border-blue-300',
      LOW: 'bg-gray-50 border-gray-300',
    };
    return colors[priority] || colors.NORMAL;
  };

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell Icon with Badge */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`relative p-2 rounded-lg transition-colors ${currentStyle.button}`}
        aria-label="Thông báo"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className={`absolute -top-1 -right-1 inline-flex items-center justify-center w-5 h-5 text-xs font-medium rounded-full ${currentStyle.badge}`}>
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 z-50">
          {/* Header */}
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-semibold text-gray-900">Thông báo</h3>
              <button
                onClick={() => setIsOpen(false)}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* Filter Tabs */}
            <div className="flex space-x-2">
              <button
                onClick={() => setFilter('all')}
                className={`flex-1 px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                  filter === 'all'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                Tất cả ({notifications.length})
              </button>
              <button
                onClick={() => setFilter('unread')}
                className={`flex-1 px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                  filter === 'unread'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                Chưa đọc ({unreadCount})
              </button>
            </div>
          </div>

          {/* Actions */}
          {notifications.length > 0 && (
            <div className="px-4 py-2 border-b border-gray-200 flex items-center justify-between">
              <button
                onClick={markAllAsRead}
                disabled={unreadCount === 0}
                className="flex items-center space-x-1 text-sm text-blue-600 hover:text-blue-700 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                <CheckCheck className="h-4 w-4" />
                <span>Đánh dấu tất cả đã đọc</span>
              </button>
              <button
                onClick={() => navigate('/notifications')}
                className="text-sm text-gray-600 hover:text-gray-900 transition-colors"
              >
                <Settings className="h-4 w-4" />
              </button>
            </div>
          )}

          {/* Notifications List */}
          <div className="max-h-96 overflow-y-auto">
            {recentNotifications.length === 0 ? (
              <div className="p-8 text-center text-gray-500">
                <Bell className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                <p className="text-sm">Không có thông báo nào</p>
              </div>
            ) : (
              <div className="divide-y divide-gray-100">
                {recentNotifications.map((notification) => (
                  <div
                    key={notification.id}
                    onClick={() => handleNotificationClick(notification)}
                    className={`p-4 hover:bg-gray-50 cursor-pointer transition-colors border-l-4 ${
                      notification.isRead
                        ? 'bg-white border-transparent'
                        : getPriorityColor(notification.priority)
                    }`}
                  >
                    <div className="flex items-start space-x-3">
                      {/* Icon */}
                      <div className="flex-shrink-0 text-2xl">
                        {getNotificationIcon(notification.type)}
                      </div>

                      {/* Content */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                          <h4 className={`text-sm font-medium ${
                            notification.isRead ? 'text-gray-700' : 'text-gray-900'
                          }`}>
                            {notification.title}
                          </h4>
                          <button
                            onClick={(e) => handleDelete(e, notification.id)}
                            className="ml-2 text-gray-400 hover:text-red-600 transition-colors"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                        <p className={`text-sm mt-1 ${
                          notification.isRead ? 'text-gray-500' : 'text-gray-700'
                        }`}>
                          {notification.message}
                        </p>
                        <div className="flex items-center mt-2 space-x-2">
                          <span className="text-xs text-gray-400">
                            {formatDistanceToNow(new Date(notification.createdAt), { 
                              addSuffix: true, 
                              locale: vi 
                            })}
                          </span>
                          {!notification.isRead && (
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                              Mới
                            </span>
                          )}
                          {notification.isHighPriority && (
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                              Quan trọng
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Footer */}
          {notifications.length > 10 && (
            <div className="p-3 border-t border-gray-200 text-center">
              <button
                onClick={() => {
                  navigate('/notifications');
                  setIsOpen(false);
                }}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
              >
                Xem tất cả thông báo
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
