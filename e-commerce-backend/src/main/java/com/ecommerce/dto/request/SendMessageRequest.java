package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for sending messages
 */
/**
 * author: LeTuBac
 */
public class SendMessageRequest {

    @NotNull(message = "ID cuộc trò chuyện không được để trống")
    private Long conversationId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;

    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    private String attachmentUrl;

    private String attachmentName;

    // Constructors
    public SendMessageRequest() {
    }

    public SendMessageRequest(Long conversationId, String content, String messageType) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
    }

    // Getters and Setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}