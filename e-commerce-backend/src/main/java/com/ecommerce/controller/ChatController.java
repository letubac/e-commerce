package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ChatMessageDTO;
import com.ecommerce.dto.ConversationDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.dto.request.CreateConversationRequest;
import com.ecommerce.dto.request.SendMessageRequest;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ConversationService;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing chat functionality.
 * Provides endpoints for conversations and messages.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;

    // USER ENDPOINTS

    /**
     * Get user's conversations
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getUserConversations(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ConversationDTO> conversations = conversationService.findByUsername(
                    username,
                    org.springframework.data.domain.PageRequest.of(0, 100)).getContent();

            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Lấy danh sách cuộc trò chuyện thành công", conversations));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách cuộc trò chuyện", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi lấy danh sách cuộc trò chuyện"));
        }
    }

    /**
     * Get messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ChatMessageDTO>>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, conversationId)) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "Bạn không có quyền truy cập cuộc trò chuyện này"));
            }

            Page<ChatMessageDTO> messages = chatMessageService.getMessagesByConversationId(
                    conversationId,
                    org.springframework.data.domain.PageRequest.of(page, size));

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy tin nhắn cuộc trò chuyện thành công", messages));
        } catch (SecurityException e) {
            log.warn("Không có quyền truy cập cuộc trò chuyện ID: {}", conversationId);
            return ResponseEntity.status(403)
                    .body(new ApiResponse<>(false, "Bạn không có quyền truy cập cuộc trò chuyện này"));
        } catch (RuntimeException e) {
            log.warn("Không tìm thấy cuộc trò chuyện ID: {}", conversationId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn cuộc trò chuyện ID: {}", conversationId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi lấy tin nhắn"));
        }
    }

    /**
     * Send a message
     */
    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, request.getConversationId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "Bạn không có quyền gửi tin nhắn"));
            }

            // Get userId from username
            Optional<UserDTO> user = userService.getUserByUsername(username);
			if (user.isEmpty()) {
				throw new ResourceNotFoundException("User not found");
			}
			// chuyển Optional<UserDTO> user sang User userEntity
			User userEntity = new User();
			userEntity.setId(user.get().getId());
			
			
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            ChatMessageDTO message = chatMessageService.sendMessage(user.getId(), request);

            log.info("Người dùng {} đã gửi tin nhắn trong cuộc trò chuyện ID: {}",
                    username, request.getConversationId());

            return ResponseEntity.ok(new ApiResponse<>(true, "Gửi tin nhắn thành công", message));
        } catch (SecurityException e) {
            log.warn("Không có quyền gửi tin nhắn: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(new ApiResponse<>(false, "Bạn không có quyền gửi tin nhắn"));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi gửi tin nhắn: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi gửi tin nhắn"));
        }
    }

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConversationDTO>> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            ConversationDTO conversation = conversationService.createConversation(
                    username,
                    request.getSubject());

            log.info("Người dùng {} đã tạo cuộc trò chuyện mới", username);

            return ResponseEntity.ok(new ApiResponse<>(true, "Tạo cuộc trò chuyện thành công", conversation));
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu không hợp lệ khi tạo cuộc trò chuyện: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tạo cuộc trò chuyện", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi tạo cuộc trò chuyện"));
        }
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> markMessagesAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            // Verify user has access to conversation
            if (!conversationService.isUserOwnerOfConversation(username, conversationId)) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "Bạn không có quyền thực hiện hành động này"));
            }

            // Get userId from username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            chatMessageService.markMessagesAsRead(conversationId, user.getId());

            return ResponseEntity.ok(new ApiResponse<>(true, "Đánh dấu đã đọc thành công"));
        } catch (SecurityException e) {
            log.warn("Không có quyền đánh dấu đã đọc cuộc trò chuyện ID: {}", conversationId);
            return ResponseEntity.status(403)
                    .body(new ApiResponse<>(false, "Bạn không có quyền thực hiện hành động này"));
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tin nhắn đã đọc", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi đánh dấu đã đọc"));
        }
    }

    /**
     * Upload file for chat
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadChatFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            Map<String, String> fileInfo = Map.of("url", "/uploads/" + file.getOriginalFilename());

            log.info("Người dùng {} đã tải lên file: {}", username, file.getOriginalFilename());

            return ResponseEntity.ok(new ApiResponse<>(true, "Tải lên file thành công", fileInfo));
        } catch (IllegalArgumentException e) {
            log.warn("File không hợp lệ: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "File không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi tải lên file", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi tải lên file"));
        }
    }

    /**
     * Get chat status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatStatus() {
        try {
            Map<String, Object> status = Map.of("online", true, "activeUsers", 100);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy trạng thái chat thành công", status));
        } catch (Exception e) {
            log.error("Lỗi khi lấy trạng thái chat", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi hệ thống khi lấy trạng thái chat"));
        }
    }
}