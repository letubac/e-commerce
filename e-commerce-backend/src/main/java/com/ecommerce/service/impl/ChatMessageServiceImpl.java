package com.ecommerce.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.entity.ChatMessage;
import com.ecommerce.entity.Conversation;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
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
	public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
		log.debug("Sending message from sender {} to conversation {}", senderId, request.getConversationId());

		// Validate conversation exists
		Conversation conversation = StreamSupport.stream(conversationRepository.findAll().spliterator(), false)
				.filter(c -> c.getId().equals(request.getConversationId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(
						"Conversation not found with id: " + request.getConversationId()));

		// Validate sender exists
		User sender = userRepository.findById(senderId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + senderId));

		// Determine sender type
		String senderType = "ADMIN".equals(sender.getRole()) ? "ADMIN" : "USER";

		// Create chat message
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setConversationId(request.getConversationId());
		chatMessage.setSenderId(senderId);
		chatMessage.setSenderType(senderType);
		chatMessage.setContent(request.getContent());
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

		log.info("Message sent successfully with id: {}", savedMessage.getId());

		// Convert to DTO and broadcast via WebSocket
		ChatMessageDTO messageDTO = convertToDTO(savedMessage, sender.getUsername());
		try {
			webSocketChatService.broadcastMessage(request.getConversationId(), messageDTO);
		} catch (Exception e) {
			log.error("Error broadcasting message via WebSocket", e);
		}

		return messageDTO;
	}

	@Override
	public List<ChatMessageDTO> getMessagesByConversationId(Long conversationId) {
		log.debug("Fetching messages for conversation {}", conversationId);

		List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
		return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public Page<ChatMessageDTO> getMessagesByConversationId(Long conversationId, Pageable pageable) {
		log.debug("Fetching paginated messages for conversation {} with page {}", conversationId,
				pageable.getPageNumber());

		Page<ChatMessage> messagesPage = chatMessageRepository.findByConversationId(conversationId, pageable);
		return messagesPage.map(this::convertToDTO);
	}

	@Override
	public void markMessagesAsRead(Long conversationId, Long userId) {
		log.debug("Marking messages as read for conversation {} by user {}", conversationId, userId);

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

		log.info("Messages marked as read for conversation {} by user {}", conversationId, userId);
	}

	@Override
	public Long getUnreadMessageCount(Long conversationId, Long userId) {
		log.debug("Getting unread message count for conversation {} and user {}", conversationId, userId);

		Long count = chatMessageRepository.countUnreadMessagesByConversationIdAndNotSender(conversationId, userId);
		log.debug("Unread message count: {}", count);
		return count;
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