package com.ecommerce.service;

import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.entity.ConversationStatus;
import com.ecommerce.exception.DetailException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ConversationService {
    ConversationDTO createConversation(Long userId, String subject) throws DetailException;

    ConversationDTO createConversation(Long userId, String subject, String initialMessage) throws DetailException;

    ConversationDTO getConversationById(Long conversationId) throws DetailException;

    ConversationDTO getConversationByIdAndUserId(Long conversationId, Long userId) throws DetailException;

    List<ConversationDTO> getConversationsByUserId(Long userId) throws DetailException;

    Page<ConversationDTO> getConversationsByUserId(Long userId, Pageable pageable) throws DetailException;

    Page<ConversationDTO> getAllConversations(Pageable pageable) throws DetailException;

    Page<ConversationDTO> getConversationsByStatus(ConversationStatus status, Pageable pageable) throws DetailException;

    ConversationDTO assignConversationToAdmin(Long conversationId, Long adminId) throws DetailException;

    ConversationDTO updateConversationStatus(Long conversationId, ConversationStatus status) throws DetailException;

    List<ConversationDTO> getUnassignedConversations() throws DetailException;

    // Additional methods for controller compatibility
    Page<ConversationDTO> findByUsername(String username, Pageable pageable) throws DetailException;

    ConversationDTO findById(Long conversationId) throws DetailException;

    ConversationDTO createConversation(String username, String subject) throws DetailException;

    Page<ConversationDTO> findAllConversationsAdmin(Pageable pageable) throws DetailException;

    ConversationDTO closeConversation(Long conversationId) throws DetailException;

    ConversationDTO reopenConversation(Long conversationId) throws DetailException;

    boolean isUserOwnerOfConversation(String username, Long conversationId) throws DetailException;

    boolean canUserAccessConversation(Long userId, Long conversationId, boolean isAdmin) throws DetailException;

    ConversationDTO toggleAiForConversation(Long conversationId, boolean aiEnabled) throws DetailException;
}