package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating conversations
 */
/**
 * author: LeTuBac
 */
public class CreateConversationRequest {

    @NotBlank(message = "Chủ đề cuộc trò chuyện không được để trống")
    private String subject;

    @NotBlank(message = "Tin nhắn đầu tiên không được để trống")
    private String initialMessage;

    // Constructors
    public CreateConversationRequest() {
    }

    public CreateConversationRequest(String subject, String initialMessage) {
        this.subject = subject;
        this.initialMessage = initialMessage;
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }
}