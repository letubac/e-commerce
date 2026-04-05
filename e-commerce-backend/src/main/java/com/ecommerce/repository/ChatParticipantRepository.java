package com.ecommerce.repository;

import com.ecommerce.entity.ChatParticipant;
import com.ecommerce.repository.base.DbRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * author: LeTuBac
 */
public interface ChatParticipantRepository extends DbRepository<ChatParticipant, Long> {

    // Maps to: chatParticipantRepository_findByConversationId.sql
    List<ChatParticipant> findByConversationId(@Param("conversationId") Long conversationId);

    // Maps to: chatParticipantRepository_findByUserId.sql
    List<ChatParticipant> findByUserId(@Param("userId") Long userId);

    // Maps to: chatParticipantRepository_findByConversationAndUser.sql
    ChatParticipant findByConversationAndUser(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    // Maps to: chatParticipantRepository_markAsRead.sql
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}