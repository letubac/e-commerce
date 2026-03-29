package com.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.constant.NotificationConstant;
import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.NotificationService;
import com.ecommerce.webapp.BusinessApiResponse;

/**
 * Notification Controller
 * REST APIs for notification management
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    /**
     * Get current user ID from Spring Security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication
                    .getPrincipal();
            // Assuming username is the user ID - adjust if your implementation is different
            return Long.parseLong(user.getUsername());
        }
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Get all notifications for current user
     */
    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getNotifications(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId, pageable);
            return ResponseEntity.ok(successHandler.handlerSuccess(notifications, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get unread notifications count
     */
    @GetMapping("/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getUnreadCount() {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(count, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/notifications/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getUnreadNotifications() {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(notifications, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/notifications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getNotificationById(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            NotificationDTO notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(notification, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> markAsRead(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            NotificationDTO notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(notification, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> markAllAsRead() {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            int count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(count, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> deleteNotification(@PathVariable(name = "id") Long id) {
        long start = System.currentTimeMillis();
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete multiple notifications
     */
    @DeleteMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> deleteNotifications(@RequestBody List<Long> ids) {
        long start = System.currentTimeMillis();
        try {
            notificationService.deleteNotifications(ids);
            return ResponseEntity.ok(successHandler.handlerSuccess(null, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Clear all notifications for current user
     */
    @DeleteMapping("/notifications/clear-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> clearAll() {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            int count = notificationService.clearAllForUser(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(count, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/notifications/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getNotificationsByType(
            @PathVariable(name = "type") String type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDTO> notifications = notificationService.getNotificationsByType(userId, type, pageable);
            return ResponseEntity.ok(successHandler.handlerSuccess(notifications, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Search notifications
     */
    @GetMapping("/notifications/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> searchNotifications(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDTO> notifications = notificationService.searchNotifications(userId, keyword, pageable);
            return ResponseEntity.ok(successHandler.handlerSuccess(notifications, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get recent notifications (last N days)
     */
    @GetMapping("/notifications/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getRecentNotifications(
            @RequestParam(name = "days", defaultValue = "7") int days) {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            List<NotificationDTO> notifications = notificationService.getRecentNotifications(userId, days);
            return ResponseEntity.ok(successHandler.handlerSuccess(notifications, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/notifications/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getStatistics() {
        long start = System.currentTimeMillis();
        try {
            Long userId = getCurrentUserId();
            NotificationService.NotificationStatisticsDTO statistics = notificationService.getStatistics(userId);
            return ResponseEntity.ok(successHandler.handlerSuccess(statistics, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Send notification to specific user (Admin only)
     */
    @PostMapping("/admin/notifications/send-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> sendToUser(@RequestBody NotificationDTO notification) {
        long start = System.currentTimeMillis();
        try {
            NotificationDTO created = notificationService.createNotification(notification);
            return ResponseEntity.ok(successHandler.handlerSuccess(created, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Broadcast notification to role (Admin only)
     */
    @PostMapping("/admin/notifications/broadcast-to-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> broadcastToRole(
            @RequestBody NotificationDTO notification) {
        long start = System.currentTimeMillis();
        try {
            NotificationDTO created = notificationService.createNotification(notification);
            return ResponseEntity.ok(successHandler.handlerSuccess(created, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Broadcast notification to all users (Admin only)
     */
    @PostMapping("/admin/notifications/broadcast-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> broadcastToAll(
            @RequestBody NotificationDTO notification) {
        long start = System.currentTimeMillis();
        try {
            notification.setTargetRole("ALL");
            NotificationDTO created = notificationService.createNotification(notification);
            notificationService.broadcastToAll(created);
            return ResponseEntity.ok(successHandler.handlerSuccess(created, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Delete old notifications (Admin only)
     */
    @DeleteMapping("/admin/notifications/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> deleteOldNotifications(
            @RequestParam(name = "daysOld", defaultValue = "30") int daysOld) {
        long start = System.currentTimeMillis();
        try {
            int count = notificationService.deleteOldNotifications(daysOld);
            return ResponseEntity.ok(successHandler.handlerSuccess(count, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}
