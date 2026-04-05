package com.ecommerce.entity;

import jakarta.persistence.*;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;

import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Entity
@Table(name = TableConstant.NEW_ARRIVALS)
/**
 * author: LeTuBac
 */
public class NewArrival {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.NEW_ARRIVALS)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "badge_text")
    private String badgeText;

    @Column(name = "badge_color")
    private String badgeColor;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    // Constructors
    public NewArrival() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public NewArrival(Long productId, String title) {
        this();
        this.productId = productId;
        this.title = title;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public boolean isCurrentlyDisplayed() {
        return isActive;
    }

    public String getBadgeStyle() {
        return badgeColor != null ? badgeColor : "#ef4444"; // default red
    }

    public String getDisplayBadge() {
        return badgeText != null ? badgeText : "NEW";
    }
}
