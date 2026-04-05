package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String targetRole; // For broadcast notifications
    private String title;
    private String message;
    private String type; // ORDER, PRODUCT, PROMOTION, SYSTEM, FLASH_SALE, COUPON
    private String link;
    private String iconUrl;
    private String entityType;
    private Long entityId;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private Boolean isRead;
    private Date readAt;
    private Date createdAt;
    private Date updatedAt;
    private Date expiresAt;

    // Computed fields
    private boolean isExpired;
    private boolean isBroadcast;
    private boolean isHighPriority;

    // Business methods
    public boolean isUnread() {
        return !Boolean.TRUE.equals(isRead);
    }
}
