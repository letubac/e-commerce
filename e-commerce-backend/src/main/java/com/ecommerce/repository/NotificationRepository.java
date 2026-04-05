package com.ecommerce.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Notification;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

/**
 * NotificationRepository using Mirage SQL
 * Each method maps to a SQL file in resources/com/ecommerce/repository/
 */
@Repository
/**
 * author: LeTuBac
 */
public interface NotificationRepository extends DbRepository<Notification, Long> {

    /**
     * Find notification by ID
     * Maps to: NotificationRepository_findById.sql
     */
    Notification findById(@Param("id") Long id);

    /**
     * Find all notifications for a user (ordered by created_at DESC)
     * Maps to: NotificationRepository_findByUserIdOrderByCreatedAtDesc.sql
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find notifications for a user with pagination
     * Maps to: NotificationRepository_findByUserIdPaged.sql
     */
    Page<Notification> findByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user (ordered by created_at DESC)
     * Maps to:
     * NotificationRepository_findByUserIdAndIsReadFalseOrderByCreatedAtDesc.sql
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find unread notifications for a user with pagination
     * Maps to: NotificationRepository_findByUserIdAndIsReadFalsePaged.sql
     */
    Page<Notification> findByUserIdAndIsReadFalsePaged(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count unread notifications for a user
     * Maps to: NotificationRepository_countByUserIdAndIsReadFalse.sql
     */
    Long countByUserIdAndIsReadFalse(@Param("userId") Long userId);

    /**
     * Find notifications by user and type
     * Maps to: NotificationRepository_findByUserIdAndTypeOrderByCreatedAtDesc.sql
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(@Param("userId") Long userId,
            @Param("type") String type, Pageable pageable);

    /**
     * Find notifications by user and type with pagination
     * Maps to: NotificationRepository_findByUserIdAndTypePaged.sql
     */
    Page<Notification> findByUserIdAndTypePaged(@Param("userId") Long userId, @Param("type") String type,
            Pageable pageable);

    /**
     * Find broadcast notifications by role
     * Maps to: NotificationRepository_findBroadcastNotificationsByRole.sql
     */
    List<Notification> findBroadcastNotificationsByRole(@Param("role") String role);

    /**
     * Find notifications by user and priority
     * Maps to:
     * NotificationRepository_findByUserIdAndPriorityOrderByCreatedAtDesc.sql
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(@Param("userId") Long userId,
            @Param("priority") String priority);

    /**
     * Find notifications by entity type and entity ID
     * Maps to: NotificationRepository_findByEntityTypeAndEntityId.sql
     */
    List<Notification> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    /**
     * Search notifications by keyword (title or message)
     * Maps to: NotificationRepository_searchNotifications.sql
     */
    Page<Notification> searchNotifications(@Param("userId") Long userId, @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Find recent notifications (within date range)
     * Maps to: NotificationRepository_findRecentNotifications.sql
     */
    List<Notification> findRecentNotifications(@Param("userId") Long userId, @Param("sinceDate") Date sinceDate);

    /**
     * Mark a notification as read
     * Maps to: NotificationRepository_markAsRead.sql
     */
    @Modifying
    void markAsRead(@Param("id") Long id, @Param("readAt") Date readAt);

    /**
     * Mark all notifications as read for a user
     * Maps to: NotificationRepository_markAllAsReadForUser.sql
     */
    @Modifying
    int markAllAsReadForUser(@Param("userId") Long userId, @Param("readAt") Date readAt);

    /**
     * Insert a new notification
     * Maps to: NotificationRepository_insertNotification.sql
     */
    @Modifying
    void insertNotification(@Param("notification") Notification notification);

    /**
     * Update a notification
     * Maps to: NotificationRepository_updateNotification.sql
     */
    @Modifying
    void updateNotification(@Param("notification") Notification notification);

    /**
     * Delete notification by ID
     * Maps to: NotificationRepository_deleteById.sql
     */
    @Modifying
    void deleteById(@Param("id") Long id);

    /**
     * Delete old notifications
     * Maps to: NotificationRepository_deleteOldNotifications.sql
     */
    @Modifying
    int deleteOldNotifications(@Param("beforeDate") Date beforeDate);

    /**
     * Delete expired notifications
     * Maps to: NotificationRepository_deleteExpiredNotifications.sql
     */
    @Modifying
    int deleteExpiredNotifications(@Param("now") Date now);

    /**
     * Delete all notifications for a user
     * Maps to: NotificationRepository_deleteAllByUserId.sql
     */
    @Modifying
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Count total notifications for a user
     * Maps to: NotificationRepository_countTotalByUser.sql
     */
    Long countTotalByUser(@Param("userId") Long userId);

    /**
     * Count read notifications for a user
     * Maps to: NotificationRepository_countReadByUser.sql
     */
    Long countReadByUser(@Param("userId") Long userId);
}
