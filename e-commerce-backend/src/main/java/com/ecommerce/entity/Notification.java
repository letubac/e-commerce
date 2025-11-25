package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "type")
    private String type; // ORDER, PRODUCT, PROMOTION, SYSTEM

    @Column(name = "link")
    private String link; // Link to related page

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private Date readAt;

    @Column(name = "created_at")
    private Date createdAt;

    // Business methods
    public boolean isUnread() {
        return !Boolean.TRUE.equals(isRead);
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = new Date();
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
}
