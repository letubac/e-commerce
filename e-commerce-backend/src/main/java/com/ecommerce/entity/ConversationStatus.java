package com.ecommerce.entity;

/**
 * author: LeTuBac
 */
public enum ConversationStatus {
    OPEN("Mở"),
    ASSIGNED("Đã phân công"),
    RESOLVED("Đã giải quyết"),
    CLOSED("Đã đóng");

    private final String displayName;

    ConversationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}