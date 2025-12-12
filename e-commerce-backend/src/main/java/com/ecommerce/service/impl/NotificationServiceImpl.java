package com.ecommerce.service.impl;

import com.ecommerce.constant.NotificationConstant;
import com.ecommerce.dto.NotificationDTO;
import com.ecommerce.entity.Notification;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.NotificationRepository;
import com.ecommerce.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationDTO createNotification(NotificationDTO notificationDTO) throws DetailException {
        try {
            Notification notification = convertToEntity(notificationDTO);
            notification.setCreatedAt(new Date());
            notification.setUpdatedAt(new Date());
            notification.setIsRead(false);

            // Use Mirage SQL create method - returns entity with generated ID
            Notification saved = notificationRepository.create(notification);

            // Send via WebSocket
            NotificationDTO result = convertToDTO(saved);
            if (saved.getUserId() != null) {
                sendToUser(saved.getUserId(), result);
            } else if (saved.getTargetRole() != null) {
                broadcastToRole(saved.getTargetRole(), result);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DetailException(NotificationConstant.E801_NOTIFICATION_CREATE_FAILED);
        }
    }

    @Override
    public void sendToUser(Long userId, NotificationDTO notification) throws DetailException {
        try {
            // Send to specific user queue: /user/{userId}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/notifications",
                    notification);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E805_NOTIFICATION_SEND_FAILED);
        }
    }

    @Override
    public void broadcastToRole(String role, NotificationDTO notification) throws DetailException {
        try {
            // Broadcast to role topic: /topic/notifications/{role}
            messagingTemplate.convertAndSend("/topic/notifications/" + role.toLowerCase(), notification);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E837_WEBSOCKET_BROADCAST_FAILED);
        }
    }

    @Override
    public void broadcastToAll(NotificationDTO notification) throws DetailException {
        try {
            // Broadcast to all: /topic/notifications/all
            messagingTemplate.convertAndSend("/topic/notifications/all", notification);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E837_WEBSOCKET_BROADCAST_FAILED);
        }
    }

    @Override
    public NotificationDTO getNotificationById(Long id) throws DetailException {
        Notification notification = notificationRepository.findById(id);
        if (notification == null) {
            throw new DetailException(NotificationConstant.E800_NOTIFICATION_NOT_FOUND);
        }
        return convertToDTO(notification);
    }

    @Override
    public Page<NotificationDTO> getNotificationsForUser(Long userId, Pageable pageable) throws DetailException {
        try {
            Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                    pageable);
            return notifications.map(this::convertToDTO);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) throws DetailException {
        try {
            List<Notification> notifications = notificationRepository
                    .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
            return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public long getUnreadCount(Long userId) throws DetailException {
        try {
            return notificationRepository.countByUserIdAndIsReadFalse(userId);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public NotificationDTO markAsRead(Long notificationId) throws DetailException {
        try {
            Notification notification = notificationRepository.findById(notificationId);
            if (notification == null) {
                throw new DetailException(NotificationConstant.E800_NOTIFICATION_NOT_FOUND);
            }

            // Use Mirage SQL mark as read method
            notificationRepository.markAsRead(notificationId, new Date());

            // Fetch updated notification
            notification = notificationRepository.findById(notificationId);
            return convertToDTO(notification);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E806_NOTIFICATION_MARK_READ_FAILED);
        }
    }

    @Override
    public int markAllAsRead(Long userId) throws DetailException {
        try {
            return notificationRepository.markAllAsReadForUser(userId, new Date());
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E807_NOTIFICATION_MARK_ALL_READ_FAILED);
        }
    }

    @Override
    public void deleteNotification(Long id) throws DetailException {
        try {
            Notification notification = notificationRepository.findById(id);
            if (notification == null) {
                throw new DetailException(NotificationConstant.E800_NOTIFICATION_NOT_FOUND);
            }
            notificationRepository.deleteById(id);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E803_NOTIFICATION_DELETE_FAILED);
        }
    }

    @Override
    public void deleteNotifications(List<Long> ids) throws DetailException {
        try {
            for (Long id : ids) {
                notificationRepository.deleteById(id);
            }
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E808_NOTIFICATION_BATCH_DELETE_FAILED);
        }
    }

    @Override
    public int clearAllForUser(Long userId) throws DetailException {
        try {
            return notificationRepository.deleteAllByUserId(userId);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E809_NOTIFICATION_CLEAR_ALL_FAILED);
        }
    }

    @Override
    public Page<NotificationDTO> getNotificationsByType(Long userId, String type, Pageable pageable)
            throws DetailException {
        try {
            Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId,
                    type, pageable);
            return notifications.map(this::convertToDTO);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public Page<NotificationDTO> searchNotifications(Long userId, String keyword, Pageable pageable)
            throws DetailException {
        try {
            Page<Notification> notifications = notificationRepository.searchNotifications(userId, keyword, pageable);
            return notifications.map(this::convertToDTO);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public List<NotificationDTO> getRecentNotifications(Long userId, int days) throws DetailException {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date fromDate = calendar.getTime();

            List<Notification> notifications = notificationRepository.findRecentNotifications(userId, fromDate);
            return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public NotificationStatisticsDTO getStatistics(Long userId) throws DetailException {
        try {
            long total = notificationRepository.countTotalByUser(userId);
            long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
            long read = notificationRepository.countReadByUser(userId);

            return new NotificationStatisticsDTO(total, unread, read);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E804_NOTIFICATION_FETCH_FAILED);
        }
    }

    @Override
    public int deleteOldNotifications(int daysOld) throws DetailException {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -daysOld);
            Date cutoffDate = calendar.getTime();

            return notificationRepository.deleteOldNotifications(cutoffDate);
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E803_NOTIFICATION_DELETE_FAILED);
        }
    }

    @Override
    public int deleteExpiredNotifications() throws DetailException {
        try {
            return notificationRepository.deleteExpiredNotifications(new Date());
        } catch (Exception e) {
            throw new DetailException(NotificationConstant.E803_NOTIFICATION_DELETE_FAILED);
        }
    }

    // Helper methods
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTargetRole(notification.getTargetRole());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setIconUrl(notification.getIconUrl());
        dto.setEntityType(notification.getEntityType());
        dto.setEntityId(notification.getEntityId());
        dto.setPriority(notification.getPriority());
        dto.setIsRead(notification.getIsRead());
        dto.setReadAt(notification.getReadAt());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        dto.setExpiresAt(notification.getExpiresAt());

        // Computed fields
        dto.setExpired(notification.isExpired());
        dto.setBroadcast(notification.isBroadcast());
        dto.setHighPriority(notification.isHighPriority());

        return dto;
    }

    private Notification convertToEntity(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setUserId(dto.getUserId());
        notification.setTargetRole(dto.getTargetRole());
        notification.setType(dto.getType());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setLink(dto.getLink());
        notification.setIconUrl(dto.getIconUrl());
        notification.setEntityType(dto.getEntityType());
        notification.setEntityId(dto.getEntityId());
        notification.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        notification.setIsRead(dto.getIsRead() != null ? dto.getIsRead() : false);
        notification.setReadAt(dto.getReadAt());
        notification.setCreatedAt(dto.getCreatedAt());
        notification.setUpdatedAt(dto.getUpdatedAt());
        notification.setExpiresAt(dto.getExpiresAt());

        return notification;
    }
}
