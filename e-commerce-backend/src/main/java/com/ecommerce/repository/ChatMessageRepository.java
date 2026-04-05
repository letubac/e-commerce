package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.ChatMessage;
import com.ecommerce.repository.base.DbRepository;
import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface ChatMessageRepository extends DbRepository<ChatMessage, Long> {

        // Maps to: chatMessageRepository_findByConversationId.sql
        Page<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

        // Maps to: chatMessageRepository_findByConversationIdOrderByCreatedAtAsc.sql
        List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

        // Maps to: chatMessageRepository_findByConversationIdOrderByCreatedAtDesc.sql
        List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId);

        // Maps to: chatMessageRepository_findUnreadMessagesByConversationId.sql
        List<ChatMessage> findUnreadMessagesByConversationId(@Param("conversationId") Long conversationId);

        // Maps to:
        // chatMessageRepository_countUnreadMessagesByConversationIdAndNotSender.sql
        Long countUnreadMessagesByConversationIdAndNotSender(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

        // Maps to: chatMessageRepository_markMessagesAsReadByConversationId.sql
        @Modifying
        void markMessagesAsReadByConversationId(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

        // Maps to: chatMessageRepository_findLatestMessageByConversationId.sql
        List<ChatMessage> findLatestMessageByConversationId(@Param("conversationId") Long conversationId,
                        Pageable pageable);

        // Maps to: chatMessageRepository_insertChatMessage.sql
        ChatMessage insertChatMessage(
                        @Param("conversationId") Long conversationId,
                        @Param("senderId") Long senderId,
                        @Param("senderType") String senderType,
                        @Param("content") String content,
                        @Param("messageType") String messageType,
                        @Param("attachmentUrl") String attachmentUrl,
                        @Param("attachmentName") String attachmentName,
                        @Param("isRead") Boolean isRead,
                        @Param("createdAt") java.util.Date createdAt,
                        @Param("updatedAt") java.util.Date updatedAt);

        // Maps to: chatMessageRepository_insertAiMessage.sql
        ChatMessage insertAiMessage(
                        @Param("conversationId") Long conversationId,
                        @Param("content") String content,
                        @Param("createdAt") java.util.Date createdAt,
                        @Param("updatedAt") java.util.Date updatedAt);
}