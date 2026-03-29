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
public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderType; // USER, ADMIN, AI
    private String content;
    private Date createdAt;
    private Date updatedAt;

    public boolean isAiMessage() {
        return "AI".equals(senderType);
    }
}
