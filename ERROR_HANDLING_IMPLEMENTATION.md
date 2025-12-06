# Chuẩn Hóa Error Handling - Hướng Dẫn Triển Khai

## ✅ Đã hoàn thành

### 1. **Constants & Messages**
- ✅ `ChatMessageConstant.java` - Chat error/success codes
- ✅ `messages_vi.properties` - Thêm 40+ messages tiếng Việt
- ✅ `messages_en.properties` - Thêm 40+ messages tiếng Anh

### 2. **Cấu trúc Error Codes**

**Conversation Errors (500-519)**
```
500_CONVERSATION_NOT_FOUND
501_CONVERSATION_CREATE_ERROR
502_CONVERSATION_ACCESS_DENIED
503_CONVERSATION_LIST_ERROR
504_CONVERSATION_UPDATE_ERROR
505_CONVERSATION_ASSIGN_ERROR
```

**Message Errors (520-539)**
```
520_MESSAGE_NOT_FOUND
521_MESSAGE_SEND_ERROR
522_MESSAGE_LIST_ERROR
523_MESSAGE_MARK_READ_ERROR
524_MESSAGE_INVALID_TYPE
525_MESSAGE_FILE_UPLOAD_ERROR
526_MESSAGE_FILE_SIZE_EXCEEDED
527_MESSAGE_FILE_TYPE_INVALID
```

**WebSocket Errors (540-549)**
```
540_WEBSOCKET_CONNECTION_ERROR
541_WEBSOCKET_AUTH_ERROR
542_WEBSOCKET_SEND_ERROR
```

**Success Codes (S500-S523)**
```
S500_CONVERSATION_CREATED
S501_CONVERSATION_UPDATED
...
S520_MESSAGE_SENT
S521_MESSAGE_LIST_SUCCESS
...
```

## 📋 Cần triển khai tiếp

### 3. **GlobalExceptionHandler** (Cần tạo)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage(
            ex.getMessageCode(), null, ex.getMessage(), locale);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, message));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, Locale locale) {
        String message = messageSource.getMessage(
            ex.getMessageCode(), null, ex.getMessage(), locale);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception ex, Locale locale) {
        log.error("Unexpected error", ex);
        String message = messageSource.getMessage(
            "app.internal.error", null, "Internal server error", locale);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
            .badRequest()
            .body(new ApiResponse<>(false, "Validation failed", errors));
    }
}
```

### 4. **MessageSourceConfig** (Cần tạo)

```java
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("vi"));
        localeResolver.setSupportedLocales(Arrays.asList(
            new Locale("vi"),
            new Locale("en")
        ));
        return localeResolver;
    }
}
```

### 5. **Custom Exceptions** (Cần cập nhật)

```java
public class BusinessException extends RuntimeException {
    private final String messageCode;
    private final HttpStatus status;

    public BusinessException(String messageCode) {
        super(messageCode);
        this.messageCode = messageCode;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String messageCode, HttpStatus status) {
        super(messageCode);
        this.messageCode = messageCode;
        this.status = status;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String messageCode) {
        super(messageCode, HttpStatus.NOT_FOUND);
    }
}
```

### 6. **Cập nhật ChatController** (Ví dụ)

```java
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getUserConversations(
            Authentication authentication, Locale locale) {
        try {
            String username = authentication.getName();
            List<ConversationDTO> conversations = conversationService
                .findByUsername(username, PageRequest.of(0, 100))
                .getContent();

            String message = messageSource.getMessage(
                ChatMessageConstant.S504_CONVERSATION_LIST_SUCCESS,
                null, locale);

            return ResponseEntity.ok(
                new ApiResponse<>(true, message, conversations));
                
        } catch (ResourceNotFoundException ex) {
            throw ex; // Let GlobalExceptionHandler handle it
        } catch (Exception ex) {
            log.error("Error getting conversations", ex);
            throw new BusinessException(
                ChatMessageConstant.E503_CONVERSATION_LIST_ERROR);
        }
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication,
            Locale locale) {
        try {
            String username = authentication.getName();
            
            if (!conversationService.isUserOwnerOfConversation(
                    username, request.getConversationId())) {
                throw new BusinessException(
                    ChatMessageConstant.E502_CONVERSATION_ACCESS_DENIED,
                    HttpStatus.FORBIDDEN);
            }

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("user.not.found"));

            ChatMessageDTO message = chatMessageService
                .sendMessage(user.getId(), request);

            String successMsg = messageSource.getMessage(
                ChatMessageConstant.S520_MESSAGE_SENT, null, locale);

            return ResponseEntity.ok(
                new ApiResponse<>(true, successMsg, message));
                
        } catch (ResourceNotFoundException | BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error sending message", ex);
            throw new BusinessException(
                ChatMessageConstant.E521_MESSAGE_SEND_ERROR);
        }
    }
}
```

### 7. **Cập nhật ServiceImpl** (Ví dụ)

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final WebSocketChatService webSocketChatService;

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        log.debug("Sending message from sender {} to conversation {}", 
            senderId, request.getConversationId());

        // Validate conversation
        Conversation conversation = conversationRepository
            .findById(request.getConversationId())
            .orElseThrow(() -> new ResourceNotFoundException(
                ChatMessageConstant.E500_CONVERSATION_NOT_FOUND));

        // Validate sender
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("user.not.found"));

        // Validate message type
        if (request.getMessageType() != null && 
            !Arrays.asList("TEXT", "IMAGE", "FILE")
                .contains(request.getMessageType())) {
            throw new BusinessException(
                ChatMessageConstant.E524_MESSAGE_INVALID_TYPE);
        }

        try {
            // Create and save message
            ChatMessage chatMessage = createChatMessage(
                senderId, sender.getRole(), request);
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // Update conversation
            updateConversation(conversation, sender.getRole());

            // Broadcast via WebSocket
            ChatMessageDTO messageDTO = convertToDTO(savedMessage, sender.getUsername());
            webSocketChatService.broadcastMessage(
                request.getConversationId(), messageDTO);

            log.info("Message sent successfully with id: {}", savedMessage.getId());
            return messageDTO;
            
        } catch (Exception ex) {
            log.error("Error saving message", ex);
            throw new BusinessException(
                ChatMessageConstant.E521_MESSAGE_SEND_ERROR);
        }
    }

    // Helper methods...
}
```

## 🚀 Cách sử dụng

### Controller Pattern
```java
try {
    // Business logic
    return ResponseEntity.ok(
        new ApiResponse<>(true, getMessage(successCode), data));
} catch (BusinessException | ResourceNotFoundException ex) {
    throw ex; // GlobalExceptionHandler sẽ xử lý
} catch (Exception ex) {
    log.error("Error", ex);
    throw new BusinessException(errorCode);
}
```

### Service Pattern
```java
// Validate
if (!isValid) {
    throw new BusinessException(ERROR_CODE);
}

// Not found
entity.orElseThrow(() -> 
    new ResourceNotFoundException(ERROR_CODE));

// Try-catch for external calls
try {
    externalService.call();
} catch (Exception ex) {
    throw new BusinessException(ERROR_CODE);
}
```

## 📝 Checklist Triển Khai

- [x] Tạo ChatMessageConstant với error/success codes
- [x] Thêm messages vào messages_vi.properties
- [x] Thêm messages vào messages_en.properties
- [ ] Tạo GlobalExceptionHandler
- [ ] Tạo MessageSourceConfig
- [ ] Cập nhật BusinessException
- [ ] Tạo ResourceNotFoundException
- [ ] Cập nhật ChatController với try-catch
- [ ] Cập nhật ChatMessageServiceImpl với try-catch
- [ ] Cập nhật ConversationServiceImpl với try-catch
- [ ] Cập nhật tất cả Controllers khác
- [ ] Cập nhật tất cả ServiceImpls khác
- [ ] Test error handling
- [ ] Test i18n messages (vi/en)

## 💡 Lưu ý

1. **Luôn throw BusinessException hoặc ResourceNotFoundException** trong Service layer
2. **Controller chỉ catch và re-throw** cho GlobalExceptionHandler
3. **Log error trước khi throw** để debug
4. **Sử dụng messageCode** thay vì hardcode message
5. **FE sẽ nhận ApiResponse** với message đã được i18n

Bạn muốn tôi triển khai tiếp phần nào? GlobalExceptionHandler, Custom Exceptions, hay cập nhật các Controllers/Services?
