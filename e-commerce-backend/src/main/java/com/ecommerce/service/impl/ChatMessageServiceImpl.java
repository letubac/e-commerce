package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Override
	public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) throws DetailException {
		long start = System.currentTimeMillis();
		try {
			log.debug("Sending message from sender {} to conversation {}", senderId, request.getConversationId());

			// Validation
			if (senderId == null || senderId <= 0) {
				throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
			}

			if (request.getConversationId() == null || request.getConversationId() <= 0) {
				throw new DetailException(ChatConstant.E534_INVALID_CONVERSATION_ID);
			}

			if (request.getContent() == null || request.getContent().trim().isEmpty()) {
				throw new DetailException(ChatConstant.E532_INVALID_MESSAGE_CONTENT);
			}

			// Validate conversation exists
			Conversation conversation = StreamSupport.stream(conversationRepository.findAll().spliterator(), false)
					.filter(c -> c.getId().equals(request.getConversationId()))
					.findFirst()
					.orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));

			// Validate sender exists
			User sender = userRepository.findById(senderId)
					.orElseThrow(() -> new DetailException(ChatConstant.E521_USER_NOT_FOUND));

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

			ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

			// Update conversation last message time and unread count
			conversation.updateLastMessage();
			if ("USER".equals(senderType)) {
				conversation.incrementUnreadCount();
			}
			conversation.setUpdatedAt(new Date());
			conversationRepository.save(conversation);

			log.info("Message sent successfully with id: {} - took: {}ms", savedMessage.getId(),
					System.currentTimeMillis() - start);

			// Convert to DTO and broadcast via WebSocket
			ChatMessageDTO messageDTO = convertToDTO(savedMessage, sender.getUsername());
			try {
				webSocketChatService.broadcastMessage(request.getConversationId(), messageDTO);
			} catch (Exception e) {
				log.error("Error broadcasting message via WebSocket", e);
			}

			return messageDTO;
		} catch (DetailException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error sending message from sender {} to conversation {}", senderId,
					request.getConversationId(), e);
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

			chatMessageRepository.markMessagesAsReadByConversationId(conversationId, userId);

			// Reset unread count
			StreamSupport.stream(conversationRepository.findAll().spliterator(), false)
					.filter(c -> c.getId().equals(conversationId))
					.findFirst()
					.ifPresent(conversation -> {
						conversation.resetUnreadCount();
						conversationRepository.save(conversation);
					});

			// Notify via WebSocket
			try {
				webSocketChatService.notifyMessagesRead(conversationId, userId);
			} catch (Exception e) {
				log.error("Error notifying messages read via WebSocket", e);
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

		// Set sender name if not provided
		if (senderName == null && message.getSenderId() != null) {
			User sender = userRepository.findById(message.getSenderId()).orElse(null);
			dto.setSenderName(sender != null ? sender.getUsername() : "Unknown");
		} else {
			dto.setSenderName(senderName);
		}

		return dto;
	}
}