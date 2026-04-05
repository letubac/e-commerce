package com.ecommerce.repository;

import com.ecommerce.entity.ChatQuickReply;
import com.ecommerce.repository.base.DbRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * author: LeTuBac
 */
public interface ChatQuickReplyRepository extends DbRepository<ChatQuickReply, Long> {

    // Maps to: chatQuickReplyRepository_findActive.sql
    List<ChatQuickReply> findActive();

    // Maps to: chatQuickReplyRepository_findByCategory.sql
    List<ChatQuickReply> findByCategory(@Param("category") String category);

    // Maps to: chatQuickReplyRepository_findByCreatedBy.sql
    List<ChatQuickReply> findByCreatedBy(@Param("createdBy") Long createdBy);
}