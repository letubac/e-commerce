package com.ecommerce.controller;

import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.constant.ChatConstant;
import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.dto.request.CreateConversationRequest;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.ChatMessageService;
import com.ecommerce.service.ConversationService;
import com.ecommerce.service.UserService;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.validation.Valid;

/**
 * REST controller for managing chat functionality.
 * Provides endpoints for conversations and messages.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    // USER ENDPOINTS

    /**
     * Get user's conversations
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getUserConversations(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();
            Page<ConversationDTO> conversations = conversationService.findByUsername(
                    username, PageRequest.of(0, 100));

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    conversations, start));
        } catch (Exception e) {
            log.error("Error fetching user conversations", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, conversationId)) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }

            Page<ChatMessageDTO> messages = chatMessageService.getMessagesByConversationId(
                    conversationId, PageRequest.of(page, size));

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    messages, start));
        } catch (Exception e) {
            log.error("Error fetching conversation messages for ID: {}", conversationId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Send a message
     */
    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, request.getConversationId())) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }

            // Get userId from username
            Optional<UserDTO> user = userService.getUserByUsername(username);
            User userEntity = modelMapper.map(user.get(), User.class);

            ChatMessageDTO message = chatMessageService.sendMessage(userEntity.getId(), request);

            log.info("User {} sent message in conversation ID: {}", username, request.getConversationId());

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    message, start));
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();

            ConversationDTO conversation = conversationService.createConversation(
                    username, request.getSubject());

            log.info("User {} created new conversation", username);

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    conversation, start));
        } catch (Exception e) {
            log.error("Error creating conversation", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> markMessagesAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, conversationId)) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }

            // Get userId from username
            Optional<UserDTO> user = userService.getUserByUsername(username);
            User userEntity = modelMapper.map(user.get(), User.class);

            chatMessageService.markMessagesAsRead(conversationId, userEntity.getId());

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    ChatConstant.S514_MESSAGES_MARKED_READ, null, start));
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Upload file for chat
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> uploadChatFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String username = authentication.getName();

            if (file.isEmpty()) {
                throw new DetailException(ChatConstant.E543_FILE_EMPTY);
            }

            Map<String, String> fileInfo = Map.of("url", "/uploads/" + file.getOriginalFilename());

            log.info("User {} uploaded file: {}", username, file.getOriginalFilename());

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    fileInfo, null, start));
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get chat status
     */
    @GetMapping("/status")
    public ResponseEntity<BusinessApiResponse> getChatStatus() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> status = Map.of("online", true, "activeUsers", 100);
            return ResponseEntity.ok(successHandler.handlerSuccess(
                    status, null, start));
        } catch (Exception e) {
            log.error("Error getting chat status", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}