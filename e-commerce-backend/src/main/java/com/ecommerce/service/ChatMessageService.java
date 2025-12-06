package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.request.SendMessageRequest;

public interface ChatMessageService {
    ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request);

    List<ChatMessageDTO> getMessagesByConversationId(Long conversationId);

    Page<ChatMessageDTO> getMessagesByConversationId(Long conversationId, Pageable pageable);

    void markMessagesAsRead(Long conversationId, Long userId);

    Long getUnreadMessageCount(Long conversationId, Long userId);
}