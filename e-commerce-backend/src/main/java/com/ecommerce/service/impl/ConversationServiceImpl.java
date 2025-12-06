package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.entity.Conversation;
import com.ecommerce.entity.ConversationStatus;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ConversationRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ConversationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;

	@Override
	public ConversationDTO createConversation(Long userId, String subject) {
		log.debug("Creating conversation for user {} with subject: {}", userId, subject);

		// Create conversation without user validation for now
		// In production, you would validate user exists through a service call
		Conversation conversation = new Conversation();
		conversation.setUserId(userId);
		conversation.setSubject(subject);
		conversation.setStatus("OPEN");
		conversation.setPriority("NORMAL");
		conversation.setUnreadCount(0);
		conversation.setCreatedAt(new Date());
		conversation.setUpdatedAt(new Date());

		Conversation savedConversation = conversationRepository.create(conversation);
		log.info("Conversation created with id: {}", savedConversation.getId());

		return convertToDTO(savedConversation);
	}

	@Override
	public ConversationDTO createConversation(Long userId, String subject, String initialMessage) {
		log.debug("Creating conversation for user {} with subject: {} and initial message", userId, subject);

		// Create conversation
		Conversation conversation = new Conversation();
		conversation.setUserId(userId);
		conversation.setSubject(subject);
		conversation.setStatus("OPEN");
		conversation.setPriority("NORMAL");
		conversation.setUnreadCount(0);
		conversation.setCreatedAt(new Date());
		conversation.setUpdatedAt(new Date());

		Conversation savedConversation = conversationRepository.create(conversation);
		log.info("Conversation created with id: {} and initial message", savedConversation.getId());

		return convertToDTO(savedConversation);
	}

	@Override
	public ConversationDTO getConversationById(Long conversationId) {
		log.debug("Fetching conversation with id: {}", conversationId);

		// Use findAll and filter since specific findById doesn't exist
		List<Conversation> conversations = (List<Conversation>) conversationRepository.findAll();
		Conversation conversation = conversations.stream().filter(c -> c.getId().equals(conversationId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

		return convertToDTO(conversation);
	}

	@Override
	public ConversationDTO getConversationByIdAndUserId(Long conversationId, Long userId) {
		log.debug("Fetching conversation {} for user {}", conversationId, userId);

		Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Conversation not found with id: " + conversationId + " for user: " + userId));

		return convertToDTO(conversation);
	}

	@Override
	public List<ConversationDTO> getConversationsByUserId(Long userId) {
		log.debug("Fetching conversations for user: {}", userId);

		List<Conversation> conversations = conversationRepository.findByUserId(userId);
		return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public Page<ConversationDTO> getConversationsByUserId(Long userId, Pageable pageable) {
		log.debug("Fetching paginated conversations for user: {}", userId);

		Page<Conversation> conversationsPage = conversationRepository.findByUserIdPaged(userId, pageable);
		return conversationsPage.map(this::convertToDTO);
	}

	@Override
	public Page<ConversationDTO> getAllConversations(Pageable pageable) {
		log.debug("Fetching all conversations with pagination");

		Page<Conversation> conversationsPage = conversationRepository.findAll(pageable);
		return conversationsPage.map(this::convertToDTO);
	}

	// Internal method for String status
	public Page<ConversationDTO> getConversationsByStatus(String status, Pageable pageable) {
		log.debug("Fetching conversations by status: {}", status);

		// Use findAll and filter by status
		List<Conversation> allConversations = (List<Conversation>) conversationRepository.findAll();
		List<Conversation> filteredConversations = allConversations.stream().filter(c -> status.equals(c.getStatus()))
				.collect(Collectors.toList());

		// Manual pagination
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filteredConversations.size());
		List<Conversation> pageContent = filteredConversations.subList(start, end);

		List<ConversationDTO> dtoContent = pageContent.stream().map(this::convertToDTO).collect(Collectors.toList());

		return new org.springframework.data.domain.PageImpl<>(dtoContent, pageable, filteredConversations.size());
	}

	// Interface-required method with enum parameter
	@Override
	public Page<ConversationDTO> getConversationsByStatus(ConversationStatus status, Pageable pageable) {
		return getConversationsByStatus(status.name(), pageable);
	}

	@Override
	public ConversationDTO assignConversationToAdmin(Long conversationId, Long adminId) {
		log.debug("Assigning conversation {} to admin {}", conversationId, adminId);

		// Find conversation by filtering findAll results
		List<Conversation> conversations = (List<Conversation>) conversationRepository.findAll();
		Conversation conversation = conversations.stream().filter(c -> c.getId().equals(conversationId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

		// Skip admin validation for now - in production would use a service call
		conversation.setAdminId(adminId);
		conversation.setStatus("ASSIGNED");
		conversation.setUpdatedAt(new Date());

		Conversation updatedConversation = conversationRepository.update(conversation);
		log.info("Conversation {} assigned to admin {}", conversationId, adminId);

		return convertToDTO(updatedConversation);
	}

	// String version for internal use
	public ConversationDTO updateConversationStatus(Long conversationId, String status) {
		log.debug("Updating conversation {} status to: {}", conversationId, status);

		// Find conversation by filtering findAll results
		List<Conversation> conversations = (List<Conversation>) conversationRepository.findAll();
		Conversation conversation = conversations.stream().filter(c -> c.getId().equals(conversationId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

		conversation.setStatus(status);
		conversation.setUpdatedAt(new Date());

		Conversation updatedConversation = conversationRepository.update(conversation);
		log.info("Conversation {} status updated to: {}", conversationId, status);

		return convertToDTO(updatedConversation);
	}

	// Interface-required method with enum parameter
	@Override
	public ConversationDTO updateConversationStatus(Long conversationId, ConversationStatus status) {
		return updateConversationStatus(conversationId, status.name());
	}

	@Override
	public List<ConversationDTO> getUnassignedConversations() {
		log.debug("Fetching unassigned conversations");

		// Use findAll to get all conversations and filter
		List<Conversation> allConversations = (List<Conversation>) conversationRepository.findAll();
		List<Conversation> unassignedConversations = allConversations.stream()
				.filter(c -> c.getAdminId() == null && "OPEN".equals(c.getStatus())).collect(Collectors.toList());

		return unassignedConversations.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// Additional methods for controller compatibility

	@Override
	public Page<ConversationDTO> findByUsername(String username, Pageable pageable) {
		log.debug("Finding conversations by username: {}", username);

		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		return getConversationsByUserId(userOpt.get().getId(), pageable);
	}

	@Override
	public ConversationDTO findById(Long conversationId) {
		return getConversationById(conversationId);
	}

	@Override
	public ConversationDTO createConversation(String username, String subject) {
		log.debug("Creating conversation for username {} with subject: {}", username, subject);

		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}

		return createConversation(userOpt.get().getId(), subject);
	}

	@Override
	public Page<ConversationDTO> findAllConversationsAdmin(Pageable pageable) {
		return getAllConversations(pageable);
	}

	@Override
	public ConversationDTO closeConversation(Long conversationId) {
		return updateConversationStatus(conversationId, "CLOSED");
	}

	@Override
	public ConversationDTO reopenConversation(Long conversationId) {
		return updateConversationStatus(conversationId, "OPEN");
	}

	@Override
	public boolean isUserOwnerOfConversation(String username, Long conversationId) {
		log.debug("Checking if user {} owns conversation {}", username, conversationId);

		Optional<User> userOpt = userRepository.findByUsername(username);
		if (userOpt.isEmpty()) {
			return false;
		}

		Optional<Conversation> conversationOpt = conversationRepository.findByIdAndUserId(conversationId,
				userOpt.get().getId());
		return conversationOpt.isPresent();
	}

	@Override
	public boolean canUserAccessConversation(Long userId, Long conversationId, boolean isAdmin) {
		log.debug("Checking if user {} can access conversation {}", userId, conversationId);

		if (isAdmin) {
			return true; // Admins can access all conversations
		}

		Optional<Conversation> conversationOpt = conversationRepository.findByIdAndUserId(conversationId, userId);
		return conversationOpt.isPresent();
	}

	private ConversationDTO convertToDTO(Conversation conversation) {
		ConversationDTO dto = new ConversationDTO();
		dto.setId(conversation.getId());
		dto.setUserId(conversation.getUserId());
		dto.setAdminId(conversation.getAdminId());
		dto.setSubject(conversation.getSubject());
		dto.setStatus(conversation.getStatus());
		dto.setPriority(conversation.getPriority());
		dto.setUnreadCount(conversation.getUnreadCount());
		dto.setLastMessageAt(conversation.getLastMessageAt());
		dto.setCreatedAt(conversation.getCreatedAt());
		dto.setUpdatedAt(conversation.getUpdatedAt());

		// Set user name
		if (conversation.getUserId() != null) {
			User user = userRepository.findById(conversation.getUserId()).orElse(null);
			dto.setUserName(user != null ? user.getUsername() : "Unknown");
		}

		// Set admin name
		if (conversation.getAdminId() != null) {
			User admin = userRepository.findById(conversation.getAdminId()).orElse(null);
			dto.setAdminName(admin != null ? admin.getUsername() : "Unknown");
		}

		return dto;
	}

	// Override method that accepts ConversationStatus enum (but we'll use String)
	public Page<ConversationDTO> getConversationsByStatus(Object status, Pageable pageable) {
		String statusString = status != null ? status.toString() : "OPEN";
		return getConversationsByStatus(statusString, pageable);
	}
}
