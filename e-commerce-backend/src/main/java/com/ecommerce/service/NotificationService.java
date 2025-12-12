package com.ecommerce.service;

import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Notification Service Interface
 * Manages real-time notifications for users and admins
 */
public interface NotificationService {

    /**
     * Create and send notification to a specific user
     */
    NotificationDTO createNotification(NotificationDTO notificationDTO) throws DetailException;

    /**
     * Send notification to a specific user via WebSocket
     */
    void sendToUser(Long userId, NotificationDTO notification) throws DetailException;

    /**
     * Broadcast notification to all users with a specific role (ADMIN, USER)
     */
    void broadcastToRole(String role, NotificationDTO notification) throws DetailException;

    /**
     * Broadcast notification to all users
     */
    void broadcastToAll(NotificationDTO notification) throws DetailException;

    /**
     * Get notification by ID
     */
    NotificationDTO getNotificationById(Long id) throws DetailException;

    /**
     * Get all notifications for a user (paginated)
     */
    Page<NotificationDTO> getNotificationsForUser(Long userId, Pageable pageable) throws DetailException;

    /**
     * Get unread notifications for a user
     */
    List<NotificationDTO> getUnreadNotifications(Long userId) throws DetailException;

    /**
     * Get unread notification count for a user
     */
    long getUnreadCount(Long userId) throws DetailException;

    /**
     * Mark notification as read
     */
    NotificationDTO markAsRead(Long notificationId) throws DetailException;

    /**
     * Mark all notifications as read for a user
     */
    int markAllAsRead(Long userId) throws DetailException;

    /**
     * Delete notification
     */
    void deleteNotification(Long id) throws DetailException;

    /**
     * Delete multiple notifications
     */
    void deleteNotifications(List<Long> ids) throws DetailException;

    /**
     * Clear all notifications for a user
     */
    int clearAllForUser(Long userId) throws DetailException;

    /**
     * Get notifications by type
     */
    Page<NotificationDTO> getNotificationsByType(Long userId, String type, Pageable pageable) throws DetailException;

    /**
     * Search notifications
     */
    Page<NotificationDTO> searchNotifications(Long userId, String keyword, Pageable pageable) throws DetailException;

    /**
     * Get recent notifications (last N days)
     */
    List<NotificationDTO> getRecentNotifications(Long userId, int days) throws DetailException;

    /**
     * Get notification statistics for a user
     */
    NotificationStatisticsDTO getStatistics(Long userId) throws DetailException;

    /**
     * Delete old notifications (cleanup task)
     */
    int deleteOldNotifications(int daysOld) throws DetailException;

    /**
     * Delete expired notifications
     */
    int deleteExpiredNotifications() throws DetailException;

    /**
     * Statistics DTO
     */
    class NotificationStatisticsDTO {
        private long totalCount;
        private long unreadCount;
        private long readCount;

        public NotificationStatisticsDTO(long totalCount, long unreadCount, long readCount) {
            this.totalCount = totalCount;
            this.unreadCount = unreadCount;
            this.readCount = readCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        public long getReadCount() {
            return readCount;
        }

        public void setReadCount(long readCount) {
            this.readCount = readCount;
        }
    }
}
