import React, { useState } from 'react';
import { Bell, Check, CheckCheck, Trash2, Filter, Search, X, Calendar, Tag } from 'lucide-react';
import { useNotifications } from '../context/NotificationContext';
import { useNavigate } from 'react-router-dom';
import { format, formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';

export default function NotificationListPage() {
  const { 
    notifications, 
    unreadCount, 
    loading,
    markAsRead, 
    markAllAsRead, 
    deleteNotification,
    clearAll 
  } = useNotifications();
  
  const navigate = useNavigate();
  const [filter, setFilter] = useState('all'); // all, unread, read
  const [typeFilter, setTypeFilter] = useState('all'); // all, ORDER, FLASH_SALE, COUPON, etc.
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]);

  // Filter notifications
  const filteredNotifications = notifications
    .filter(n => {
      // Status filter
      if (filter === 'unread' && n.isRead) return false;
      if (filter === 'read' && !n.isRead) return false;
      
      // Type filter
      if (typeFilter !== 'all' && n.type !== typeFilter) return false;
      
      // Search filter
      if (searchQuery) {
        const query = searchQuery.toLowerCase();
        return n.title?.toLowerCase().includes(query) || 
               n.message?.toLowerCase().includes(query);
      }
      
      return true;
    })
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  const notificationTypes = [
    { value: 'all', label: 'Tất cả', icon: '📋' },
    { value: 'ORDER', label: 'Đơn hàng', icon: '📦' },
    { value: 'FLASH_SALE', label: 'Flash Sale', icon: '🔥' },
    { value: 'COUPON', label: 'Mã giảm giá', icon: '🎟️' },
    { value: 'PROMOTION', label: 'Khuyến mãi', icon: '🎉' },
    { value: 'PRODUCT', label: 'Sản phẩm', icon: '🛍️' },
    { value: 'SYSTEM', label: 'Hệ thống', icon: '⚙️' },
  ];

  const handleNotificationClick = async (notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }
    
    if (notification.link) {
      navigate(notification.link);
    }
  };

  const handleSelectAll = () => {
    if (selectedIds.length === filteredNotifications.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredNotifications.map(n => n.id));
    }
  };

  const handleSelectOne = (id) => {
    setSelectedIds(prev => 
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const handleBulkDelete = async () => {
    if (window.confirm(`Xóa ${selectedIds.length} thông báo đã chọn?`)) {
      for (const id of selectedIds) {
        await deleteNotification(id);
      }
      setSelectedIds([]);
    }
  };

  const handleBulkMarkAsRead = async () => {
    const unreadSelected = selectedIds.filter(id => {
      const notification = notifications.find(n => n.id === id);
      return notification && !notification.isRead;
    });
    
    for (const id of unreadSelected) {
      await markAsRead(id);
    }
    setSelectedIds([]);
  };

  const getNotificationIcon = (type) => {
    const typeObj = notificationTypes.find(t => t.value === type);
    return typeObj?.icon || '🔔';
  };

  const getPriorityBadge = (priority) => {
    const badges = {
      URGENT: { color: 'bg-red-100 text-red-800 border-red-300', text: 'Khẩn cấp' },
      HIGH: { color: 'bg-orange-100 text-orange-800 border-orange-300', text: 'Quan trọng' },
      NORMAL: { color: 'bg-blue-100 text-blue-800 border-blue-300', text: 'Bình thường' },
      LOW: { color: 'bg-gray-100 text-gray-800 border-gray-300', text: 'Thấp' },
    };
    return badges[priority] || badges.NORMAL;
  };

  const groupNotificationsByDate = (notifications) => {
    const groups = {};
    notifications.forEach(notification => {
      const date = format(new Date(notification.createdAt), 'yyyy-MM-dd');
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(notification);
    });
    return groups;
  };

  const groupedNotifications = groupNotificationsByDate(filteredNotifications);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-5xl">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-3">
              <Bell className="h-8 w-8 text-blue-600" />
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Thông báo</h1>
                <p className="text-sm text-gray-500">
                  {unreadCount > 0 ? `${unreadCount} thông báo chưa đọc` : 'Tất cả đã đọc'}
                </p>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex items-center space-x-2">
              {selectedIds.length > 0 && (
                <>
                  <button
                    onClick={handleBulkMarkAsRead}
                    className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                  >
                    <Check className="h-4 w-4" />
                    <span>Đánh dấu đã đọc</span>
                  </button>
                  <button
                    onClick={handleBulkDelete}
                    className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors flex items-center space-x-2"
                  >
                    <Trash2 className="h-4 w-4" />
                    <span>Xóa ({selectedIds.length})</span>
                  </button>
                </>
              )}
              
              {selectedIds.length === 0 && unreadCount > 0 && (
                <button
                  onClick={markAllAsRead}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                >
                  <CheckCheck className="h-4 w-4" />
                  <span>Đọc tất cả</span>
                </button>
              )}

              <button
                onClick={() => setShowFilters(!showFilters)}
                className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors flex items-center space-x-2"
              >
                <Filter className="h-4 w-4" />
                <span>Bộ lọc</span>
              </button>
            </div>
          </div>

          {/* Filters */}
          {showFilters && (
            <div className="border-t pt-4 space-y-4">
              {/* Status Filter */}
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium text-gray-700 w-24">Trạng thái:</span>
                <div className="flex space-x-2">
                  {['all', 'unread', 'read'].map(status => (
                    <button
                      key={status}
                      onClick={() => setFilter(status)}
                      className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                        filter === status
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      {status === 'all' && 'Tất cả'}
                      {status === 'unread' && 'Chưa đọc'}
                      {status === 'read' && 'Đã đọc'}
                    </button>
                  ))}
                </div>
              </div>

              {/* Type Filter */}
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium text-gray-700 w-24">Loại:</span>
                <div className="flex flex-wrap gap-2">
                  {notificationTypes.map(type => (
                    <button
                      key={type.value}
                      onClick={() => setTypeFilter(type.value)}
                      className={`px-3 py-1.5 text-sm rounded-lg transition-colors flex items-center space-x-1 ${
                        typeFilter === type.value
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      <span>{type.icon}</span>
                      <span>{type.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Search */}
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium text-gray-700 w-24">Tìm kiếm:</span>
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Tìm kiếm theo tiêu đề hoặc nội dung..."
                    className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      <X className="h-5 w-5" />
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Search Bar */}
          {!showFilters && (
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Tìm kiếm thông báo..."
                className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  <X className="h-5 w-5" />
                </button>
              )}
            </div>
          )}
        </div>

        {/* Bulk Actions Bar */}
        {selectedIds.length > 0 && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6 flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <input
                type="checkbox"
                checked={selectedIds.length === filteredNotifications.length}
                onChange={handleSelectAll}
                className="h-5 w-5 text-blue-600 rounded focus:ring-blue-500"
              />
              <span className="text-sm font-medium text-gray-900">
                Đã chọn {selectedIds.length} thông báo
              </span>
            </div>
            <button
              onClick={() => setSelectedIds([])}
              className="text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              Bỏ chọn
            </button>
          </div>
        )}

        {/* Notifications List */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-gray-300 border-t-blue-600"></div>
            <p className="mt-4 text-gray-600">Đang tải...</p>
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <Bell className="h-16 w-16 mx-auto mb-4 text-gray-300" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Không có thông báo</h3>
            <p className="text-gray-500">
              {searchQuery || typeFilter !== 'all' || filter !== 'all'
                ? 'Không tìm thấy thông báo phù hợp với bộ lọc'
                : 'Bạn chưa có thông báo nào'}
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {Object.entries(groupedNotifications).map(([date, notifs]) => (
              <div key={date}>
                {/* Date Header */}
                <div className="flex items-center space-x-3 mb-3">
                  <Calendar className="h-5 w-5 text-gray-400" />
                  <h2 className="text-sm font-semibold text-gray-700">
                    {format(new Date(date), 'EEEE, dd MMMM yyyy', { locale: vi })}
                  </h2>
                  <div className="flex-1 border-t border-gray-200"></div>
                </div>

                {/* Notifications */}
                <div className="space-y-3">
                  {notifs.map((notification) => (
                    <div
                      key={notification.id}
                      className={`bg-white rounded-lg shadow-sm border-l-4 transition-all hover:shadow-md ${
                        notification.isRead
                          ? 'border-gray-300'
                          : 'border-blue-500 bg-blue-50/30'
                      }`}
                    >
                      <div className="p-4">
                        <div className="flex items-start space-x-4">
                          {/* Checkbox */}
                          <input
                            type="checkbox"
                            checked={selectedIds.includes(notification.id)}
                            onChange={() => handleSelectOne(notification.id)}
                            className="mt-1 h-5 w-5 text-blue-600 rounded focus:ring-blue-500"
                          />

                          {/* Icon */}
                          <div className="flex-shrink-0 text-3xl">
                            {getNotificationIcon(notification.type)}
                          </div>

                          {/* Content */}
                          <div 
                            className="flex-1 min-w-0 cursor-pointer"
                            onClick={() => handleNotificationClick(notification)}
                          >
                            <div className="flex items-start justify-between mb-2">
                              <h3 className={`text-base font-semibold ${
                                notification.isRead ? 'text-gray-700' : 'text-gray-900'
                              }`}>
                                {notification.title}
                              </h3>
                              <span className="ml-2 text-xs text-gray-500 whitespace-nowrap">
                                {formatDistanceToNow(new Date(notification.createdAt), { 
                                  addSuffix: true, 
                                  locale: vi 
                                })}
                              </span>
                            </div>

                            <p className={`text-sm mb-3 ${
                              notification.isRead ? 'text-gray-600' : 'text-gray-800'
                            }`}>
                              {notification.message}
                            </p>

                            {/* Badges */}
                            <div className="flex flex-wrap items-center gap-2">
                              {!notification.isRead && (
                                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                  Mới
                                </span>
                              )}
                              {notification.priority && notification.priority !== 'NORMAL' && (
                                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${
                                  getPriorityBadge(notification.priority).color
                                }`}>
                                  {getPriorityBadge(notification.priority).text}
                                </span>
                              )}
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-700">
                                <Tag className="h-3 w-3 mr-1" />
                                {notificationTypes.find(t => t.value === notification.type)?.label || notification.type}
                              </span>
                            </div>
                          </div>

                          {/* Actions */}
                          <div className="flex items-center space-x-2">
                            {!notification.isRead && (
                              <button
                                onClick={() => markAsRead(notification.id)}
                                className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                title="Đánh dấu đã đọc"
                              >
                                <Check className="h-5 w-5" />
                              </button>
                            )}
                            <button
                              onClick={() => deleteNotification(notification.id)}
                              className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                              title="Xóa"
                            >
                              <Trash2 className="h-5 w-5" />
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Clear All Button */}
        {notifications.length > 0 && (
          <div className="mt-8 text-center">
            <button
              onClick={async () => {
                if (window.confirm('Xóa tất cả thông báo? Hành động này không thể hoàn tác.')) {
                  await clearAll();
                }
              }}
              className="px-6 py-3 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors border border-red-300"
            >
              Xóa tất cả thông báo
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
