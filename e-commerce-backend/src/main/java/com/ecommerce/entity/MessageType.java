package com.ecommerce.entity;

public enum MessageType {
    TEXT("Tin nhắn"),
    IMAGE("Hình ảnh"),
    FILE("Tập tin"),
    SYSTEM("Hệ thống");

    private final String displayName;

    MessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}