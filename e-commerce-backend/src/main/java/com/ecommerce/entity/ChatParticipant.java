package com.ecommerce.entity;

import jakarta.persistence.*;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;

import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Entity
@Table(name = TableConstant.CHAT_PARTICIPANTS)
public class ChatParticipant {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.CHAT_PARTICIPANTS)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", nullable = false)
    private String role; // 'customer', 'admin', 'supervisor'

    @Column(name = "joined_at", nullable = false)
    private Date joinedAt;

    @Column(name = "last_read_at")
    private Date lastReadAt;

    // Constructors
    public ChatParticipant() {
        this.joinedAt = new Date();
        this.lastReadAt = new Date();
    }

    public ChatParticipant(Long conversationId, Long userId, String role) {
        this();
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Date getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Date lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    // Business methods
    public boolean isAdmin() {
        return "admin".equals(role) || "supervisor".equals(role);
    }

    public boolean isCustomer() {
        return "customer".equals(role);
    }

    public boolean isSupervisor() {
        return "supervisor".equals(role);
    }

    public void markAsRead() {
        this.lastReadAt = new Date();
    }
}
