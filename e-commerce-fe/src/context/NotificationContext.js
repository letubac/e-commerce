import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import notificationWebSocketService from '../services/notificationWebSocketService';
import api from '../api/api';

const NotificationContext = createContext();

export function NotificationProvider({ children }) {
  const { user, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  /**
   * Fetch notifications from API
   */
  const fetchNotifications = useCallback(async () => {
    if (!isAuthenticated) return;

    try {
      setLoading(true);
      const response = await api.request('/notifications', {
        params: { page: 0, size: 50 }
      });
      
      const notificationList = response.data?.content || response.content || response.data || [];
      setNotifications(notificationList);
      
      // Count unread
      const unread = notificationList.filter(n => !n.isRead).length;
      setUnreadCount(unread);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  /**
   * Fetch unread count
   */
  const fetchUnreadCount = useCallback(async () => {
    if (!isAuthenticated) return;

    try {
      const response = await api.request('/notifications/unread-count');
      const count = response.data || response || 0;
      setUnreadCount(count);
    } catch (error) {
      console.error('Error fetching unread count:', error);
    }
  }, [isAuthenticated]);

  /**
   * Handle new notification from WebSocket
   */
  const handleNewNotification = useCallback((notification) => {
    console.log('📬 New notification:', notification);
    
    // Add to list
    setNotifications(prev => [notification, ...prev]);
    
    // Increment unread count
    setUnreadCount(prev => prev + 1);
    
    // Show browser notification
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: notification.iconUrl || '/logo192.png',
        badge: '/logo192.png',
        tag: `notification-${notification.id}`,
      });
    }
    
    // Play notification sound
    const audio = new Audio('/sounds/notification.mp3');
    audio.volume = 0.5;
    audio.play().catch(e => console.log('Could not play sound:', e));
  }, []);

  /**
   * Mark notification as read
   */
  const markAsRead = useCallback(async (notificationId) => {
    try {
      await api.request(`/notifications/${notificationId}/read`, {
        method: 'PUT'
      });
      
      // Update local state
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true, readAt: new Date() } : n)
      );
      
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  }, []);

  /**
   * Mark all as read
   */
  const markAllAsRead = useCallback(async () => {
    try {
      await api.request('/notifications/read-all', {
        method: 'PUT'
      });
      
      // Update local state
      setNotifications(prev => 
        prev.map(n => ({ ...n, isRead: true, readAt: new Date() }))
      );
      
      setUnreadCount(0);
    } catch (error) {
      console.error('Error marking all as read:', error);
    }
  }, []);

  /**
   * Delete notification
   */
  const deleteNotification = useCallback(async (notificationId) => {
    try {
      await api.request(`/notifications/${notificationId}`, {
        method: 'DELETE'
      });
      
      // Remove from local state
      setNotifications(prev => {
        const notification = prev.find(n => n.id === notificationId);
        if (notification && !notification.isRead) {
          setUnreadCount(count => Math.max(0, count - 1));
        }
        return prev.filter(n => n.id !== notificationId);
      });
    } catch (error) {
      console.error('Error deleting notification:', error);
    }
  }, []);

  /**
   * Clear all notifications
   */
  const clearAll = useCallback(async () => {
    try {
      await api.request('/notifications/clear-all', {
        method: 'DELETE'
      });
      
      setNotifications([]);
      setUnreadCount(0);
    } catch (error) {
      console.error('Error clearing notifications:', error);
    }
  }, []);

  /**
   * Request browser notification permission
   */
  const requestNotificationPermission = useCallback(async () => {
    if ('Notification' in window && Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      console.log('Notification permission:', permission);
    }
  }, []);

  // Initialize WebSocket connection when user logs in
  useEffect(() => {
    if (isAuthenticated && user) {
      // Connect to WebSocket
      notificationWebSocketService.connect(user.id, user.role);
      
      // Register message handler
      const unsubscribe = notificationWebSocketService.addMessageHandler(handleNewNotification);
      
      // Fetch initial notifications
      fetchNotifications();
      
      // Request notification permission
      requestNotificationPermission();
      
      // Cleanup on unmount or logout
      return () => {
        unsubscribe();
        notificationWebSocketService.disconnect();
      };
    }
  }, [isAuthenticated, user, handleNewNotification, fetchNotifications, requestNotificationPermission]);

  // Fetch unread count periodically
  useEffect(() => {
    if (isAuthenticated) {
      const interval = setInterval(fetchUnreadCount, 60000); // Every minute
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, fetchUnreadCount]);

  const value = {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearAll,
    requestNotificationPermission,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within NotificationProvider');
  }
  return context;
}
