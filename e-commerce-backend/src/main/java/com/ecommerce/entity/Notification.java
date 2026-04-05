package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.NOTIFICATIONS)
/**
 * author: LeTuBac
 */
public class Notification {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.NOTIFICATIONS)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    /**
     * Target role for broadcast (ADMIN, USER, or null for specific user)
     */
    @Column(name = "target_role", length = 20)
    private String targetRole;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "type")
    private String type; // ORDER, PRODUCT, PROMOTION, SYSTEM, FLASH_SALE, COUPON

    @Column(name = "link")
    private String link; // Link to related page

    /**
     * Optional icon/image URL
     */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Related entity type (Order, FlashSale, Coupon, etc.)
     */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /**
     * Related entity ID
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Priority: LOW, NORMAL, HIGH, URGENT
     */
    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private Date readAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Expiration date for the notification
     */
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    // Business methods
    public boolean isUnread() {
        return !Boolean.TRUE.equals(isRead);
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = new Date();
        this.updatedAt = new Date();
    }

    public boolean isOrderNotification() {
        return "ORDER".equals(type);
    }

    public boolean isProductNotification() {
        return "PRODUCT".equals(type);
    }

    public boolean isPromotionNotification() {
        return "PROMOTION".equals(type);
    }

    public boolean isSystemNotification() {
        return "SYSTEM".equals(type);
    }

    public boolean isFlashSaleNotification() {
        return "FLASH_SALE".equals(type);
    }

    public boolean isCouponNotification() {
        return "COUPON".equals(type);
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Date());
    }

    public boolean isBroadcast() {
        return targetRole != null;
    }

    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }
}
