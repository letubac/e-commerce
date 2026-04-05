package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.ecommerce.entity.Conversation;
import com.ecommerce.entity.ConversationStatus;
import com.ecommerce.repository.base.DbRepository;

@Repository
/**
 * author: LeTuBac
 */
public interface ConversationRepository extends DbRepository<Conversation, Long> {

    // Maps to: conversationRepository_findById.sql
    Optional<Conversation> findById(@Param("id") Long id);

    // Maps to: conversationRepository_findAllPaged.sql
    Page<Conversation> findAllPaged(Pageable pageable);

    // Maps to: conversationRepository_findByUserId.sql
    List<Conversation> findByUserId(@Param("userId") Long userId);

    // Maps to: conversationRepository_findByUserIdPaged.sql
    Page<Conversation> findByUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    // Maps to: conversationRepository_findByStatus.sql
    List<Conversation> findByStatus(@Param("status") ConversationStatus status);

    // Maps to: conversationRepository_findByStatusPaged.sql
    Page<Conversation> findByStatusPaged(@Param("status") ConversationStatus status, Pageable pageable);

    // Maps to: conversationRepository_findByAdminId.sql
    Page<Conversation> findByAdminId(@Param("adminId") Long adminId, Pageable pageable);

    // Maps to: conversationRepository_findByIdWithMessages.sql
    Optional<Conversation> findByIdWithMessages(@Param("id") Long id);

    // Maps to: conversationRepository_findByIdAndUserId.sql
    Optional<Conversation> findByIdAndUserId(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    // Maps to: conversationRepository_findUnassignedConversations.sql
    List<Conversation> findUnassignedConversations(Pageable pageable);

    // Maps to: conversationRepository_countByStatus.sql
    Long countByStatus(@Param("status") ConversationStatus status);
}