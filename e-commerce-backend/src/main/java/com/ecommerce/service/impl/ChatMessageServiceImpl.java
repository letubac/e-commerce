package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.ai.AiSupportService;
import com.ecommerce.constant.ChatConstant;
import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.entity.ChatMessage;
import com.ecommerce.entity.Conversation;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.ChatMessageRepository;
import com.ecommerce.repository.ConversationRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ChatMessageService;
import com.ecommerce.service.ConversationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;
	private final com.ecommerce.websocket.WebSocketChatService webSocketChatService;
	private final AiSupportService aiSupportService;
	private final ConversationService conversationService;

	@Override
	public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Sending message from sender {} to conversation {}", senderId, request.getConversationId());

			// Validation
			if (senderId == null || senderId <= 0) {
				log.error("❌ Invalid sender ID: {}", senderId);
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			if (request.getConversationId() == null || request.getConversationId() <= 0) {
				log.error("❌ Invalid conversation ID: {}", request.getConversationId());
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (request.getContent() == null || request.getContent().trim().isEmpty()) {
				log.error("❌ Invalid message content: null or empty");
				throw new DetailException(ChatConstant.E532_INVALID_MESSAGE_CONTENT);
			}

			// Validate conversation exists
			log.debug("Checking conversation with ID: {}", request.getConversationId());
			Conversation conversation = conversationRepository.findById(request.getConversationId())
					.orElseThrow(() -> {
						log.error("❌ Conversation not found: {}", request.getConversationId());
						return new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND);
					});

			// Validate sender exists
			log.debug("Checking sender with ID: {}", senderId);
			User sender = userRepository.findById(senderId)
					.orElseThrow(() -> {
						log.error("❌ Sender not found: {}", senderId);
						return new DetailException(ChatConstant.E521_USER_NOT_FOUND);
					});

			log.info("✅ Validated sender: {}, conversation: {}", sender.getUsername(), conversation.getId());

			// Determine sender type
			String senderType = "ADMIN".equals(sender.getRole()) ? "ADMIN" : "USER";

			// Create chat message
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setConversationId(request.getConversationId());
			chatMessage.setSenderId(senderId);
			chatMessage.setSenderType(senderType);
			chatMessage.setContent(request.getContent().trim());
			chatMessage.setMessageType(request.getMessageType() != null ? request.getMessageType() : "TEXT");
			chatMessage.setAttachmentUrl(request.getAttachmentUrl());
			chatMessage.setAttachmentName(request.getAttachmentName());
			chatMessage.setIsRead(false);
			chatMessage.setCreatedAt(new Date());
			chatMessage.setUpdatedAt(new Date());

			// Use insertChatMessage to auto-generate ID with sequence
			log.debug("Inserting chat message...");
			ChatMessage savedMessage = chatMessageRepository.insertChatMessage(
					request.getConversationId(),
					senderId,
					senderType,
					request.getContent().trim(),
					request.getMessageType() != null ? request.getMessageType() : "TEXT",
					request.getAttachmentUrl(),
					request.getAttachmentName(),
					false,
					new Date(),
					new Date());

			log.info("✅ Message saved with ID: {}", savedMessage.getId());

			// Update conversation last message time and unread count
			conversation.updateLastMessage();
			if ("USER".equals(senderType)) {
				conversation.incrementUnreadCount();
			}
			conversation.setUpdatedAt(new Date());
			conversationRepository.save(conversation);
			log.info("✅ Conversation updated");

			log.info("Message sent successfully with id: {} - took: {}ms", savedMessage.getId(),
					System.currentTimeMillis() - start);

			// Convert to DTO and broadcast via WebSocket
			ChatMessageDTO messageDTO = convertToDTO(savedMessage, sender.getUsername());
			try {
				webSocketChatService.broadcastMessage(request.getConversationId(), messageDTO);
				log.info("✅ Message broadcasted via WebSocket");
			} catch (Exception e) {
				log.error("⚠️ Error broadcasting message via WebSocket", e);
			}

			// Trigger async AI auto-reply when user sends a message, AI is enabled globally
			// and conversation has AI enabled (not disabled by admin) and no assigned admin (status == OPEN)
			if (ChatConstant.SENDER_TYPE_USER.equals(senderType) && aiSupportService.isEnabled()
					&& conversation.isAiEnabled()
					&& ChatConstant.STATUS_OPEN.equals(conversation.getStatus())) {
				final Long convId = request.getConversationId();
				final String userText = request.getContent().trim();
				final Long userId = senderId;
				final String username = sender.getUsername();
				CompletableFuture.runAsync(() -> sendAiReply(convId, userText, userId, username));
			}

			return messageDTO;
		} catch (DetailException e) {
			log.error("❌ DetailException in sendMessage: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("❌ Unexpected error sending message from sender {} to conversation {}: {}", senderId,
					request.getConversationId(), e.getMessage(), e);
			throw new DetailException(ChatConstant.E511_MESSAGE_SEND_ERROR);
		}
	}

	@Override
	public List<ChatMessageDTO> getMessagesByConversationId(Long conversationId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching messages for conversation {}", conversationId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
			log.info("Fetched {} messages for conversation {} - took: {}ms", messages.size(), conversationId,
					System.currentTimeMillis() - start);
			return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching messages for conversation {}", conversationId, e);
			throw new DetailException(ChatConstant.E512_MESSAGE_FETCH_ERROR);
		}
	}

	@Override
	public Page<ChatMessageDTO> getMessagesByConversationId(Long conversationId, Pageable pageable)
			throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Fetching paginated messages for conversation {} with page {}", conversationId,
					pageable.getPageNumber());

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			Page<ChatMessage> messagesPage = chatMessageRepository.findByConversationId(conversationId, pageable);
			log.info("Fetched {} messages (page {}) for conversation {} - took: {}ms",
					messagesPage.getContent().size(), pageable.getPageNumber(), conversationId,
					System.currentTimeMillis() - start);
			return messagesPage.map(this::convertToDTO);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching paginated messages for conversation {}", conversationId, e);
			throw new DetailException(ChatConstant.E512_MESSAGE_FETCH_ERROR);
		}
	}

	@Override
	public void markMessagesAsRead(Long conversationId, Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Marking messages as read for conversation {} by user {}", conversationId, userId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			// Verify conversation exists
			Conversation conversation = conversationRepository.findById(conversationId)
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));

			try {
				// Mark all messages as read (marks messages NOT sent by user as read)
				chatMessageRepository.markMessagesAsReadByConversationId(conversationId, userId);
				log.debug("Messages marked as read in database for conversation {}", conversationId);
			} catch (Exception e) {
				log.error("Error executing markMessagesAsReadByConversationId for conversation {}", conversationId, e);
				throw new DetailException(ChatConstant.E514_MARK_READ_ERROR);
			}

			// Reset unread count
			try {
				conversation.resetUnreadCount();
				conversationRepository.save(conversation);
				log.debug("Conversation unread count reset for conversation {}", conversationId);
			} catch (Exception e) {
				log.error("Error resetting unread count for conversation {}", conversationId, e);
				// Don't throw, just log - this is not critical
			}

			// Notify via WebSocket
			try {
				webSocketChatService.notifyMessagesRead(conversationId, userId);
			} catch (Exception e) {
				log.error("Error notifying messages read via WebSocket for conversation {}", conversationId, e);
				// Don't throw, just log - WebSocket is not critical
			}

			log.info("Messages marked as read for conversation {} by user {} - took: {}ms",
					conversationId, userId, System.currentTimeMillis() - start);
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error marking messages as read for conversation {} by user {}", conversationId, userId, e);
			throw new DetailException(ChatConstant.E514_MARK_READ_ERROR);
		}
	}

	@Override
	public Long getUnreadMessageCount(Long conversationId, Long userId) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Getting unread message count for conversation {} and user {}", conversationId, userId);

			if (conversationId == null || conversationId <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (userId == null || userId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			Long count = chatMessageRepository.countUnreadMessagesByConversationIdAndNotSender(conversationId, userId);
			log.debug("Unread message count: {} - took: {}ms", count, System.currentTimeMillis() - start);
			return count;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error getting unread message count for conversation {} and user {}", conversationId, userId, e);
			throw new DetailException(ChatConstant.E515_UNREAD_COUNT_ERROR);
		}
	}

	private ChatMessageDTO convertToDTO(ChatMessage message) {
		return convertToDTO(message, null);
	}

	private ChatMessageDTO convertToDTO(ChatMessage message, String senderName) {
		ChatMessageDTO dto = new ChatMessageDTO();
		dto.setId(message.getId());
		dto.setConversationId(message.getConversationId());
		dto.setSenderId(message.getSenderId());
		dto.setSenderType(message.getSenderType());
		dto.setContent(message.getContent());
		dto.setCreatedAt(message.getCreatedAt());
		dto.setUpdatedAt(message.getUpdatedAt());

		// AI messages have no real senderId
		if (ChatConstant.SENDER_TYPE_AI.equals(message.getSenderType())) {
			dto.setSenderName("AI Assistant");
		} else if (senderName == null && message.getSenderId() != null) {
			User sender = userRepository.findById(message.getSenderId()).orElse(null);
			dto.setSenderName(sender != null ? sender.getUsername() : "Unknown");
		} else {
			dto.setSenderName(senderName);
		}

		return dto;
	}

	/**
	 * Sends an AI-generated reply to the conversation asynchronously.
	 * Runs outside the main transaction to avoid blocking the user request.
	 * Includes user context injection and smart handoff detection.
	 */
	private void sendAiReply(Long conversationId, String userMessage, Long userId, String username) {
		try {
			log.info("AI generating reply for conversation {} (user: {})", conversationId, username);

			// Smart handoff: if user wants to speak with a human, disable AI and notify admin
			if (aiSupportService.isHumanHandoffRequested(userMessage)) {
				log.info("🤝 Human handoff requested by user {} in conversation {}", username, conversationId);
				handleHumanHandoff(conversationId);
				return;
			}

			// Broadcast AI typing indicator before calling AI model
			webSocketChatService.notifyAiTyping(conversationId, true);

			// Use context-aware respond() so AI tools can access userId directly
			String aiResponse = aiSupportService.respond(conversationId, userMessage, userId, username);

			// Stop AI typing indicator
			webSocketChatService.notifyAiTyping(conversationId, false);

			if (aiResponse == null || aiResponse.isBlank()) {
				return;
			}

			// Save AI message using dedicated insertAiMessage (sets is_ai_response=true)
			Date now = new Date();
			ChatMessage aiMessage = chatMessageRepository.insertAiMessage(
					conversationId,
					aiResponse.trim(),
					now,
					now);

			log.info("✅ AI message saved with ID: {}", aiMessage.getId());

			// Broadcast AI message via WebSocket
			ChatMessageDTO aiDTO = convertToDTO(aiMessage, "AI Assistant");
			webSocketChatService.broadcastMessage(conversationId, aiDTO);
			log.info("✅ AI message broadcasted for conversation {}", conversationId);
		} catch (Exception e) {
			// Ensure typing indicator is stopped even on error
			try {
				webSocketChatService.notifyAiTyping(conversationId, false);
			} catch (Exception ignored) {
			}
			log.error("❌ Error sending AI reply for conversation {}: {}", conversationId, e.getMessage(), e);
		}
	}

	/**
	 * Performs smart admin handoff:
	 * 1. Disables AI for this conversation.
	 * 2. Saves a handoff message from AI.
	 * 3. Notifies admin via WebSocket.
	 */
	private void handleHumanHandoff(Long conversationId) {
		try {
			// Disable AI for this conversation
			conversationService.toggleAiForConversation(conversationId, false);
			log.info("✅ AI disabled for conversation {} (human handoff)", conversationId);

			// Save handoff notification message
			String handoffMsg = "Tôi đã hiểu bạn muốn được hỗ trợ bởi nhân viên. "
					+ "AI sẽ tạm dừng trong cuộc trò chuyện này. "
					+ "Vui lòng chờ, nhân viên hỗ trợ sẽ tiếp quản sớm nhất!";
			Date now = new Date();
			ChatMessage handoffMessage = chatMessageRepository.insertAiMessage(
					conversationId,
					handoffMsg,
					now,
					now);

			// Broadcast handoff message to user
			ChatMessageDTO handoffDTO = convertToDTO(handoffMessage, "AI Assistant");
			webSocketChatService.broadcastMessage(conversationId, handoffDTO);

			// Notify all admins that this conversation needs human attention
			webSocketChatService.notifyHumanHandoffRequested(conversationId);

			log.info("✅ Human handoff completed for conversation {}", conversationId);
		} catch (Exception e) {
			log.error("❌ Error during human handoff for conversation {}: {}", conversationId, e.getMessage(), e);
		}
	}
}