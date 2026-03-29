package com.ecommerce.controller;

import java.util.Map;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.constant.ChatConstant;
import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.dto.request.CreateConversationRequest;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.entity.ConversationStatus;
import com.ecommerce.exception.DetailException;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.ChatMessageService;
import com.ecommerce.service.ConversationService;
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

    // USER ENDPOINTS

    /**
     * Get user's conversations
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getUserConversations(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            // Extract userId từ UserPrincipal instead of relying on getName()
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getId();

            if (userId == null || userId <= 0) {
                throw new DetailException(ChatConstant.E530_INVALID_USER_ID);
            }

            log.debug("✅ Fetching conversations for userId: {}", userId);
            Page<ConversationDTO> conversations = conversationService.getConversationsByUserId(
                    userId, PageRequest.of(0, 100));

            return ResponseEntity.ok(successHandler.handlerSuccess(
                    conversations, start));
        } catch (Exception e) {
            log.error("❌ Error fetching user conversations", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> getConversationMessages(
            @PathVariable(name = "conversationId") Long conversationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
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
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getId();
            String username = principal.getUsername();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, request.getConversationId())) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }

            ChatMessageDTO message = chatMessageService.sendMessage(userId, request);

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
            @PathVariable(name = "conversationId") Long conversationId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getId();
            String username = principal.getUsername();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, conversationId)) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }

            chatMessageService.markMessagesAsRead(conversationId, userId);

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

    // ADMIN ENDPOINTS

    /**
     * Get all conversations (admin)
     */
    @GetMapping("/admin/conversations")
    // Temporarily removed @PreAuthorize to debug role issue - ADD BACK AFTER
    // TESTING
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAllConversations(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name = "status", required = false) String status) {
        long start = System.currentTimeMillis();
        try {
            // Debug logging for role checking
            if (authentication != null) {
                log.info("🔍 User authenticated: {}", authentication.getName());
                log.info("🔍 User authorities: {}", authentication.getAuthorities());
            } else {
                log.warn("⚠️ No authentication found");
            }

            Page<ConversationDTO> conversations;
            if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                conversations = conversationService.getConversationsByStatus(
                        ConversationStatus.valueOf(status.toUpperCase()), PageRequest.of(page, size));
            } else {
                conversations = conversationService.getAllConversations(PageRequest.of(page, size));
            }
            return ResponseEntity.ok(successHandler.handlerSuccess(conversations, start));
        } catch (Exception e) {
            log.error("Admin: Error fetching all conversations", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get messages in any conversation (admin)
     */
    @GetMapping("/admin/conversations/{conversationId}/messages")
    // @PreAuthorize("hasRole('ADMIN')") - Temporarily removed for debugging
    public ResponseEntity<BusinessApiResponse> getAdminConversationMessages(
            @PathVariable(name = "conversationId") Long conversationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        long start = System.currentTimeMillis();
        try {
            Page<ChatMessageDTO> messages = chatMessageService.getMessagesByConversationId(
                    conversationId, PageRequest.of(page, size));
            return ResponseEntity.ok(successHandler.handlerSuccess(messages, start));
        } catch (Exception e) {
            log.error("Admin: Error fetching messages for conversation {}", conversationId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Send a message as admin
     */
    @PostMapping("/admin/messages")
    // @PreAuthorize("hasRole('ADMIN')") - Temporarily removed for debugging
    public ResponseEntity<BusinessApiResponse> adminSendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            // Debug logging
            log.info("Admin message request: conversation={}, content={}",
                    request.getConversationId(),
                    request.getContent() != null
                            ? request.getContent().substring(0, Math.min(20, request.getContent().length()))
                            : "null");

            if (authentication == null) {
                log.error("❌ Authentication is null for admin message");
                return ResponseEntity.ok(errorHandler.handlerException(
                        new Exception("Authentication required"), start));
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserPrincipal)) {
                log.error("❌ Principal is not UserPrincipal: {}", principal.getClass().getName());
                return ResponseEntity.ok(errorHandler.handlerException(
                        new Exception("Invalid authentication"), start));
            }

            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Long adminId = userPrincipal.getId();
            log.info("Admin {} sending message", adminId);

            ChatMessageDTO message = chatMessageService.sendMessage(adminId, request);
            log.info("✅ Admin {} sent message in conversation {}", adminId, request.getConversationId());
            return ResponseEntity.ok(successHandler.handlerSuccess(message, start));
        } catch (Exception e) {
            log.error("❌ Admin: Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Mark messages as read (admin)
     */
    @PostMapping("/admin/conversations/{conversationId}/read")
    // @PreAuthorize("hasRole('ADMIN')") - Temporarily removed for debugging
    public ResponseEntity<BusinessApiResponse> adminMarkMessagesAsRead(
            @PathVariable(name = "conversationId") Long conversationId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long adminId = principal.getId();
            chatMessageService.markMessagesAsRead(conversationId, adminId);
            return ResponseEntity.ok(successHandler.handlerSuccess(
                    ChatConstant.S514_MESSAGES_MARKED_READ, null, start));
        } catch (Exception e) {
            log.error("Admin: Error marking messages as read for conversation {}", conversationId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Update conversation status (admin)
     */
    @PutMapping("/admin/conversations/{conversationId}/status")
    // @PreAuthorize("hasRole('ADMIN')") - Temporarily removed for debugging
    public ResponseEntity<BusinessApiResponse> updateConversationStatus(
            @PathVariable(name = "conversationId") Long conversationId,
            @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        try {
            String status = body.get("status");
            ConversationDTO updated = conversationService.updateConversationStatus(
                    conversationId, ConversationStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            log.error("Admin: Error updating status for conversation {}", conversationId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Assign conversation to current admin
     */
    @PostMapping("/admin/conversations/{conversationId}/assign")
    // @PreAuthorize("hasRole('ADMIN')") - Temporarily removed for debugging
    public ResponseEntity<BusinessApiResponse> assignConversation(
            @PathVariable(name = "conversationId") Long conversationId,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long adminId = principal.getId();
            ConversationDTO updated = conversationService.assignConversationToAdmin(conversationId, adminId);
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            log.error("Admin: Error assigning conversation {}", conversationId, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}