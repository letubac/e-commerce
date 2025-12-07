package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.exception.DetailException;

public interface ChatMessageService {
    ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) throws DetailException;

    List<ChatMessageDTO> getMessagesByConversationId(Long conversationId) throws DetailException;

    Page<ChatMessageDTO> getMessagesByConversationId(Long conversationId, Pageable pageable) throws DetailException;

    void markMessagesAsRead(Long conversationId, Long userId) throws DetailException;

    Long getUnreadMessageCount(Long conversationId, Long userId) throws DetailException;
}