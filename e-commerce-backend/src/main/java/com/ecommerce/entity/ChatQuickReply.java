package com.ecommerce.entity;

import jakarta.persistence.*;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;

import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Entity
@Table(name = TableConstant.CHAT_QUICK_REPLIES)
public class ChatQuickReply {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.CHAT_QUICK_REPLIES)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category")
    private String category; // greeting, faq, closing, etc.

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    // Constructors
    public ChatQuickReply() {
        this.createdAt = new Date();
    }

    public ChatQuickReply(String title, String content, String category, Long createdBy) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    public boolean isGreeting() {
        return "greeting".equals(category);
    }

    public boolean isFaq() {
        return "faq".equals(category);
    }

    public boolean isClosing() {
        return "closing".equals(category);
    }

    public String getDisplayTitle() {
        return title + " (" + category + ")";
    }
}
