package com.ecommerce.service;

import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChatMessageService {
    ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request);

    List<ChatMessageDTO> getMessagesByConversationId(Long conversationId);

    Page<ChatMessageDTO> getMessagesByConversationId(Long conversationId, Pageable pageable);

    void markMessagesAsRead(Long conversationId, Long userId);

    Long getUnreadMessageCount(Long conversationId, Long userId);
}