package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long adminId;
    private String adminName;
    private String subject;
    private String status; // OPEN, ASSIGNED, RESOLVED, CLOSED
    private List<ChatMessageDTO> messages;
    private Date lastMessageAt;
    private Date createdAt;
    private Date updatedAt;
}
