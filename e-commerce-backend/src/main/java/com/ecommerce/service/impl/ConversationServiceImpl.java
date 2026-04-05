package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.constant.ChatConstant;
import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.entity.Conversation;
import com.ecommerce.entity.ConversationStatus;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.ConversationRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ConversationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
/**
 * author: LeTuBac
 */
public class ConversationServiceImpl implements ConversationService {

	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;

	@Override
	public ConversationDTO createConversation(Long userId, String subject) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Creating conversation for user {} with subject: {}", userId, subject);

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			if (subject == null || subject.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E531_INVALID_SUBJECT);
			}

			// Verify user exists
			if (!userRepository.findById(userId).isPresent()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			Conversation conversation = new Conversation();
			conversation.setUserId(userId);
			conversation.setSubject(subject.trim());
			conversation.setStatus("OPEN");
			conversation.setPriority("NORMAL");
			conversation.setUnreadCount(0);
			conversation.setCreatedAt(new Date());
			conversation.setUpdatedAt(new Date());

			Conversation savedConversation = conversationRepository.create(conversation);
			log.info("Conversation created with id: {} - took: {}ms", savedConversation.getId(),
					System.currentTimeMillis() - start);

			return convertToDTO(savedConversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating conversation for user {}", userId, e);
			throw new DetailException(ChatConstant.E503_CONVERSATION_CREATION_FAILED);
		}
	}

	@Override
	public ConversationDTO createConversation(Long userId, String subject, String initialMessage)
			throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Creating conversation for user {} with subject: {} and initial message", userId, subject);

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			if (subject == null || subject.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E531_INVALID_SUBJECT);
			}

			if (initialMessage == null || initialMessage.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E532_INVALID_MESSAGE_CONTENT);
			}

			// Verify user exists
			if (!userRepository.findById(userId).isPresent()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			Conversation conversation = new Conversation();
			conversation.setUserId(userId);
			conversation.setSubject(subject.trim());
			conversation.setStatus("OPEN");
			conversation.setPriority("NORMAL");
			conversation.setUnreadCount(0);
			conversation.setCreatedAt(new Date());
			conversation.setUpdatedAt(new Date());

			Conversation savedConversation = conversationRepository.create(conversation);
			log.info("Conversation created with id: {} and initial message - took: {}ms",
					savedConversation.getId(), System.currentTimeMillis() - start);

			return convertToDTO(savedConversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating conversation for user {} with initial message", userId, e);
			throw new DetailException(ChatConstant.E503_CONVERSATION_CREATION_FAILED);
		}
	}

	@Override
	public ConversationDTO getConversationById(Long conversationId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching conversation with id: {}", conversationId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			Conversation conversation = conversationRepository.findById(conversationId)
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));

			log.info("Fetched conversation {} - took: {}ms", conversationId, System.currentTimeMillis() - start);
			return convertToDTO(conversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching conversation {}", conversationId, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public ConversationDTO getConversationByIdAndUserId(Long conversationId, Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching conversation {} for user {}", conversationId, userId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
					.orElseThrow(() -> new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER));

			log.info("Fetched conversation {} for user {} - took: {}ms", conversationId, userId,
					System.currentTimeMillis() - start);
			return convertToDTO(conversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching conversation {} for user {}", conversationId, userId, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public List<ConversationDTO> getConversationsByUserId(Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching conversations for user: {}", userId);

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			// Verify user exists
			if (!userRepository.findById(userId).isPresent()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			List<Conversation> conversations = conversationRepository.findByUserId(userId);
			log.info("Fetched {} conversations for user {} - took: {}ms", conversations.size(), userId,
					System.currentTimeMillis() - start);
			return conversations.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching conversations for user {}", userId, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public Page<ConversationDTO> getConversationsByUserId(Long userId, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching paginated conversations for user: {}", userId);

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			// Verify user exists
			if (!userRepository.findById(userId).isPresent()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			Page<Conversation> conversationsPage = conversationRepository.findByUserIdPaged(userId, pageable);
			log.info("Fetched {} conversations (page {}) for user {} - took: {}ms",
					conversationsPage.getContent().size(), pageable.getPageNumber(), userId,
					System.currentTimeMillis() - start);
			return conversationsPage.map(this::convertToDTO);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching paginated conversations for user {}", userId, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public Page<ConversationDTO> getAllConversations(Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching all conversations with pagination");

			Page<Conversation> conversationsPage = conversationRepository.findAllPaged(pageable);
			log.info("Fetched {} conversations (page {}) - took: {}ms",
					conversationsPage.getContent().size(), pageable.getPageNumber(),
					System.currentTimeMillis() - start);
			return conversationsPage.map(this::convertToDTO);
		} catch (Exception e) {
			log.error("Error fetching all conversations", e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	// Internal method for String status
	public Page<ConversationDTO> getConversationsByStatus(String status, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching conversations by status: {}", status);

			if (status == null || status.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E535_INVALID_STATUS);
			}

			// Use findByStatusPaged for paginated status filtering
			ConversationStatus statusEnum;
			try {
				statusEnum = ConversationStatus.valueOf(status.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new DetailException(ChatConstant.E535_INVALID_STATUS);
			}
			Page<Conversation> conversationsPage = conversationRepository.findByStatusPaged(statusEnum, pageable);

			log.info("Fetched {} conversations with status {} - took: {}ms", conversationsPage.getContent().size(),
					status,
					System.currentTimeMillis() - start);
			return conversationsPage.map(this::convertToDTO);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching conversations by status {}", status, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	// Interface-required method with enum parameter
	@Override
	public Page<ConversationDTO> getConversationsByStatus(ConversationStatus status, Pageable pageable)
			throws DetailException {
		try {
			if (status == null) {
				throw new DetailException(ChatConstant.E535_INVALID_STATUS);
			}
			return getConversationsByStatus(status.name(), pageable);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching conversations by status enum {}", status, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public ConversationDTO assignConversationToAdmin(Long conversationId, Long adminId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Assigning conversation {} to admin {}", conversationId, adminId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (adminId == null || adminId <= 0) {
				throw new DetailException(ChatConstant.E536_INVALID_ADMIN_ID);
			}

			// Find conversation by id
			Conversation conversation = conversationRepository.findById(conversationId)
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));

			// Verify admin exists
			if (!userRepository.findById(adminId).isPresent()) {
				throw new DetailException(ChatConstant.E522_ADMIN_NOT_FOUND);
			}

			conversation.setAdminId(adminId);
			conversation.setStatus("ASSIGNED");
			conversation.setUpdatedAt(new Date());

			Conversation updatedConversation = conversationRepository.update(conversation);
			log.info("Conversation {} assigned to admin {} - took: {}ms", conversationId, adminId,
					System.currentTimeMillis() - start);

			return convertToDTO(updatedConversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error assigning conversation {} to admin {}", conversationId, adminId, e);
			throw new DetailException(ChatConstant.E505_CONVERSATION_ASSIGNMENT_FAILED);
		}
	}

	// String version for internal use
	public ConversationDTO updateConversationStatus(Long conversationId, String status) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Updating conversation {} status to: {}", conversationId, status);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (status == null || status.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E535_INVALID_STATUS);
			}

			// Find conversation by id
			Conversation conversation = conversationRepository.findById(conversationId)
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));

			conversation.setStatus(status.trim());
			conversation.setUpdatedAt(new Date());

			Conversation updatedConversation = conversationRepository.update(conversation);
			log.info("Conversation {} status updated to: {} - took: {}ms", conversationId, status,
					System.currentTimeMillis() - start);

			return convertToDTO(updatedConversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating conversation {} status to {}", conversationId, status, e);
			throw new DetailException(ChatConstant.E504_CONVERSATION_STATUS_UPDATE_FAILED);
		}
	}

	// Interface-required method with enum parameter
	@Override
	public ConversationDTO updateConversationStatus(Long conversationId, ConversationStatus status)
			throws DetailException {
		try {
			if (status == null) {
				throw new DetailException(ChatConstant.E535_INVALID_STATUS);
			}
			return updateConversationStatus(conversationId, status.name());
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating conversation {} status with enum {}", conversationId, status, e);
			throw new DetailException(ChatConstant.E504_CONVERSATION_STATUS_UPDATE_FAILED);
		}
	}

	@Override
	public List<ConversationDTO> getUnassignedConversations() throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching unassigned conversations");

			// Use findUnassignedConversations from repository
			List<Conversation> unassignedConversations = conversationRepository.findUnassignedConversations(
					org.springframework.data.domain.PageRequest.of(0, 1000));

			log.info("Fetched {} unassigned conversations - took: {}ms", unassignedConversations.size(),
					System.currentTimeMillis() - start);
			return unassignedConversations.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error fetching unassigned conversations", e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	// Additional methods for controller compatibility

	@Override
	public Page<ConversationDTO> findByUsername(String username, Pageable pageable) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Finding conversations by username: {}", username);

			if (username == null || username.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E533_INVALID_USERNAME);
			}

			Optional<User> userOpt = userRepository.findByUsername(username.trim());
			if (userOpt.isEmpty()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			Page<ConversationDTO> result = getConversationsByUserId(userOpt.get().getId(), pageable);
			log.info("Found conversations for username {} - took: {}ms", username,
					System.currentTimeMillis() - start);
			return result;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error finding conversations by username {}", username, e);
			throw new DetailException(ChatConstant.E501_CONVERSATION_FETCH_FAILED);
		}
	}

	@Override
	public ConversationDTO findById(Long conversationId) throws DetailException {
		return getConversationById(conversationId);
	}

	@Override
	public ConversationDTO createConversation(String username, String subject) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Creating conversation for username {} with subject: {}", username, subject);

			if (username == null || username.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E533_INVALID_USERNAME);
			}

			if (subject == null || subject.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E531_INVALID_SUBJECT);
			}

			Optional<User> userOpt = userRepository.findByUsername(username.trim());
			if (userOpt.isEmpty()) {
				throw new DetailException(ChatConstant.E521_USER_NOT_FOUND);
			}

			ConversationDTO result = createConversation(userOpt.get().getId(), subject.trim());
			log.info("Created conversation for username {} - took: {}ms", username,
					System.currentTimeMillis() - start);
			return result;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating conversation for username {}", username, e);
			throw new DetailException(ChatConstant.E503_CONVERSATION_CREATION_FAILED);
		}
	}

	@Override
	public Page<ConversationDTO> findAllConversationsAdmin(Pageable pageable) throws DetailException {
		return getAllConversations(pageable);
	}

	@Override
	public ConversationDTO closeConversation(Long conversationId) throws DetailException {
		return updateConversationStatus(conversationId, "CLOSED");
	}

	@Override
	public ConversationDTO reopenConversation(Long conversationId) throws DetailException {
		return updateConversationStatus(conversationId, "OPEN");
	}

	@Override
	public boolean isUserOwnerOfConversation(String username, Long conversationId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Checking if user {} owns conversation {}", username, conversationId);

			if (username == null || username.trim().isEmpty()) {
				throw new DetailException(ChatConstant.E533_INVALID_USERNAME);
			}

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			Optional<User> userOpt = userRepository.findByUsername(username.trim());
			if (userOpt.isEmpty()) {
				return false;
			}

			Optional<Conversation> conversationOpt = conversationRepository.findByIdAndUserId(conversationId,
					userOpt.get().getId());
			log.debug("Ownership check result: {} - took: {}ms", conversationOpt.isPresent(),
					System.currentTimeMillis() - start);
			return conversationOpt.isPresent();
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error checking conversation ownership for user {} and conversation {}", username, conversationId,
					e);
			throw new DetailException(ChatConstant.E537_OWNERSHIP_CHECK_FAILED);
		}
	}

	@Override
	public boolean canUserAccessConversation(Long userId, Long conversationId, boolean isAdmin) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Checking if user {} can access conversation {}", userId, conversationId);

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (isAdmin) {
				log.debug("Admin access granted - took: {}ms", System.currentTimeMillis() - start);
				return true; // Admins can access all conversations
			}

			Optional<Conversation> conversationOpt = conversationRepository.findByIdAndUserId(conversationId, userId);
			log.debug("Access check result: {} - took: {}ms", conversationOpt.isPresent(),
					System.currentTimeMillis() - start);
			return conversationOpt.isPresent();
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error checking conversation access for user {} and conversation {}", userId, conversationId, e);
			throw new DetailException(ChatConstant.E537_OWNERSHIP_CHECK_FAILED);
		}
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
		dto.setAiEnabled(conversation.isAiEnabled());
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

	@Override
	public ConversationDTO toggleAiForConversation(Long conversationId, boolean aiEnabled) throws DetailException {
		try {
			Conversation conversation = conversationRepository.findById(conversationId)
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));
			if (aiEnabled) {
				conversation.enableAi();
			} else {
				conversation.disableAi();
			}
			conversation.setUpdatedAt(new Date());
			conversationRepository.save(conversation);
			return convertToDTO(conversation);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error toggling AI for conversation {}: {}", conversationId, e.getMessage(), e);
			throw new DetailException(ChatConstant.E545_CHAT_OPERATION_FAILED);
		}
	}

	// Override method that accepts ConversationStatus enum (but we'll use String)
	public Page<ConversationDTO> getConversationsByStatus(Object status, Pageable pageable) throws DetailException {
		String statusString = status != null ? status.toString() : "OPEN";
		return getConversationsByStatus(statusString, pageable);
	}
}
