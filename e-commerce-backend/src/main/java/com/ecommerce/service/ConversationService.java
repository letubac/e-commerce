package com.ecommerce.service;

import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.entity.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ConversationService {
    ConversationDTO createConversation(Long userId, String subject);

    ConversationDTO getConversationById(Long conversationId);

    ConversationDTO getConversationByIdAndUserId(Long conversationId, Long userId);

    List<ConversationDTO> getConversationsByUserId(Long userId);

    Page<ConversationDTO> getConversationsByUserId(Long userId, Pageable pageable);

    Page<ConversationDTO> getAllConversations(Pageable pageable);

    Page<ConversationDTO> getConversationsByStatus(ConversationStatus status, Pageable pageable);

    ConversationDTO assignConversationToAdmin(Long conversationId, Long adminId);

    ConversationDTO updateConversationStatus(Long conversationId, ConversationStatus status);

    List<ConversationDTO> getUnassignedConversations();

    // Additional methods for controller compatibility
    Page<ConversationDTO> findByUsername(String username, Pageable pageable);

    ConversationDTO findById(Long conversationId);

    ConversationDTO createConversation(String username, String subject);

    Page<ConversationDTO> findAllConversationsAdmin(Pageable pageable);

    ConversationDTO closeConversation(Long conversationId);

    ConversationDTO reopenConversation(Long conversationId);

    boolean isUserOwnerOfConversation(String username, Long conversationId);
}