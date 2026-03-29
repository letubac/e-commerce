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
@Table(name = TableConstant.CONVERSATIONS)
public class Conversation {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.CONVERSATIONS)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "status")
    private String status; // OPEN, ASSIGNED, RESOLVED, CLOSED

    @Column(name = "priority")
    private String priority; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "last_message_at")
    private Date lastMessageAt;

    @Column(name = "ai_enabled")
    private Boolean aiEnabled = true;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    public boolean isAssigned() {
        return "ASSIGNED".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    public boolean hasAdmin() {
        return adminId != null;
    }

    public boolean isAiEnabled() {
        return !Boolean.FALSE.equals(aiEnabled);
    }

    public void enableAi() {
        this.aiEnabled = true;
    }

    public void disableAi() {
        this.aiEnabled = false;
    }

    public void assignToAdmin(Long adminId) {
        this.adminId = adminId;
        this.status = "ASSIGNED";
    }

    public void close() {
        this.status = "CLOSED";
    }

    public void reopen() {
        this.status = "OPEN";
        this.adminId = null;
    }

    public void updateLastMessage() {
        this.lastMessageAt = new Date();
    }

    public void incrementUnreadCount() {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + 1;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }
}
