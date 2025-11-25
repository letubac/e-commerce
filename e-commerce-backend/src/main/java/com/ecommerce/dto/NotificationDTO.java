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
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type; // ORDER, PRODUCT, PROMOTION, SYSTEM
    private String link;
    private Boolean isRead;
    private Date readAt;
    private Date createdAt;

    // Business methods
    public boolean isUnread() {
        return !Boolean.TRUE.equals(isRead);
    }
}
