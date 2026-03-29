# Chat Module Refactoring Guide

## ✅ Hoàn thành:
1. ✅ **ChatConstant.java** - Created với E500-E549, S500-S529
2. ✅ **messages_vi.properties** - Added 60+ Chat messages
3. ✅ **messages_en.properties** - Added 60+ Chat messages  
4. ✅ **ConversationService.java** - Added `throws DetailException` to all methods
5. ✅ **ChatMessageService.java** - Added `throws DetailException` to all methods

---

## 🔄 Cần làm tiếp:

### **BACKEND:**

#### **1. ConversationServiceImpl.java**
Refactor tất cả methods theo pattern:

```java
@Override
public ConversationDTO createConversation(Long userId, String subject) throws DetailException {
    long start = System.currentTimeMillis();
    try {
        log.info("Creating conversation for user ID: {} with subject: {}", userId, subject);
        
        // Validation
        if (userId == null) {
            throw new DetailException(ChatConstant.E530_USER_NOT_FOUND);
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new DetailException(ChatConstant.E536_SUBJECT_REQUIRED);
        }
        
        // Business logic
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new DetailException(ChatConstant.E530_USER_NOT_FOUND));
            
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setSubject(subject);
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setCreatedAt(LocalDateTime.now());
        
        Conversation saved = conversationRepository.save(conversation);
        
        log.info("Successfully created conversation ID: {} for user ID: {} - took: {}ms", 
                 saved.getId(), userId, System.currentTimeMillis() - start);
        
        return modelMapper.map(saved, ConversationDTO.class);
        
    } catch (DetailException e) {
        throw e;
    } catch (Exception e) {
        log.error("Error creating conversation for user ID: {}", userId, e);
        throw new DetailException(ChatConstant.E501_CONVERSATION_CREATE_ERROR);
    }
}
```

**Áp dụng cho:**
- `createConversation()` x3 overloads
- `getConversationById()`
- `getConversationByIdAndUserId()`
- `getConversationsByUserId()` x2
- `getAllConversations()`
- `getConversationsByStatus()`
- `assignConversationToAdmin()`
- `updateConversationStatus()`
- `getUnassignedConversations()`
- `findByUsername()`
- `findById()`
- `findAllConversationsAdmin()`
- `closeConversation()`
- `reopenConversation()`
- `isUserOwnerOfConversation()`
- `canUserAccessConversation()`

---

#### **2. ChatMessageServiceImpl.java**
Refactor tất cả methods:

```java
@Override
public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) throws DetailException {
    long start = System.currentTimeMillis();
    try {
        log.info("Sending message from sender ID: {} to conversation ID: {}", 
                 senderId, request.getConversationId());
        
        // Validation
        if (senderId == null) {
            throw new DetailException(ChatConstant.E532_SENDER_NOT_FOUND);
        }
        if (request.getConversationId() == null) {
            throw new DetailException(ChatConstant.E535_CONVERSATION_ID_REQUIRED);
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new DetailException(ChatConstant.E515_MESSAGE_CONTENT_EMPTY);
        }
        if (request.getContent().length() > 5000) {
            throw new DetailException(ChatConstant.E516_MESSAGE_CONTENT_TOO_LONG);
        }
        
        // Business logic
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new DetailException(ChatConstant.E532_SENDER_NOT_FOUND));
            
        Conversation conversation = conversationRepository.findById(request.getConversationId())
            .orElseThrow(() -> new DetailException(ChatConstant.E500_CONVERSATION_NOT_FOUND));
        
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        
        ChatMessage saved = chatMessageRepository.save(message);
        
        log.info("Successfully sent message ID: {} - took: {}ms", 
                 saved.getId(), System.currentTimeMillis() - start);
        
        return modelMapper.map(saved, ChatMessageDTO.class);
        
    } catch (DetailException e) {
        throw e;
    } catch (Exception e) {
        log.error("Error sending message from sender ID: {}", senderId, e);
        throw new DetailException(ChatConstant.E511_MESSAGE_SEND_ERROR);
    }
}
```

**Áp dụng cho:**
- `sendMessage()`
- `getMessagesByConversationId()` x2
- `markMessagesAsRead()`
- `getUnreadMessageCount()`

---

#### **3. ChatController.java**
Refactor tất cả endpoints để dùng BusinessApiResponse:

```java
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private ErrorHandler errorHandler;
    
    @Autowired
    private SuccessHandler successHandler;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ChatMessageService chatMessageService;

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
                ChatConstant.S502_CONVERSATIONS_LISTED, conversations, start));
                
        } catch (Exception e) {
            log.error("Error fetching user conversations", e);
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
            
            // Verify access
            if (!conversationService.isUserOwnerOfConversation(username, request.getConversationId())) {
                throw new DetailException(ChatConstant.E520_USER_NOT_CONVERSATION_OWNER);
            }
            
            // Get user and send message
            Optional<UserDTO> user = userService.getUserByUsername(username);
            if (user.isEmpty()) {
                throw new DetailException(ChatConstant.E530_USER_NOT_FOUND);
            }
            
            User userEntity = modelMapper.map(user.get(), User.class);
            ChatMessageDTO message = chatMessageService.sendMessage(userEntity.getId(), request);
            
            return ResponseEntity.ok(successHandler.handlerSuccess(
                ChatConstant.S510_MESSAGE_SENT, message, start));
                
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}
```

**Refactor 9 endpoints:**
1. ✅ GET `/conversations` - getUserConversations()
2. ✅ GET `/conversations/{id}/messages` - getConversationMessages()
3. ✅ POST `/messages` - sendMessage()
4. ✅ POST `/conversations` - createConversation()
5. ✅ POST `/conversations/{id}/read` - markMessagesAsRead()
6. ✅ POST `/upload` - uploadChatFile()
7. ✅ GET `/admin/conversations` - getAllConversations()
8. ✅ PUT `/admin/conversations/{id}/assign` - assignConversation()
9. ✅ PUT `/admin/conversations/{id}/close` - closeConversation()

**Imports cần thêm:**
```java
import com.ecommerce.constant.ChatConstant;
import com.ecommerce.webapp.BusinessApiResponse;
import com.ecommerce.webapp.handler.ErrorHandler;
import com.ecommerce.webapp.handler.SuccessHandler;
```

---

### **FRONTEND:**

#### **Files cần refactor:**
1. **AdminChatManagement.js** - Admin chat management component
2. **ChatWidget.js** - Customer chat widget

#### **Pattern:**
- API calls đã dùng `parseBusinessResponse()` tự động
- Cập nhật error handling để hiển thị i18n messages từ BE
- Verify response structure compatibility

#### **Example:**
```javascript
const loadConversations = async () => {
  try {
    const data = await api.getUserConversations();
    setConversations(Array.isArray(data) ? data : data?.content || []);
  } catch (error) {
    console.error('Error loading conversations:', error);
    alert(error.message || 'Không thể tải danh sách cuộc trò chuyện');
  }
};
```

---

## 📝 **Error Code Mapping:**

| Code | Vietnamese | English |
|------|-----------|---------|
| E500 | Không tìm thấy cuộc trò chuyện | Conversation not found |
| E501 | Lỗi khi tạo cuộc trò chuyện | Error creating conversation |
| E510 | Không tìm thấy tin nhắn | Message not found |
| E511 | Lỗi khi gửi tin nhắn | Error sending message |
| E520 | Bạn không phải chủ sở hữu cuộc trò chuyện này | You are not the owner of this conversation |
| E530 | Không tìm thấy người dùng | User not found |
| S500 | Tạo cuộc trò chuyện thành công | Conversation created successfully |
| S510 | Gửi tin nhắn thành công | Message sent successfully |

---

## 🎯 **Testing Checklist:**

### **Backend:**
- [ ] Create conversation - validation (subject required)
- [ ] Create conversation - success
- [ ] Get conversations - user not found
- [ ] Get conversations - success
- [ ] Send message - content empty
- [ ] Send message - content too long
- [ ] Send message - conversation not found
- [ ] Send message - access denied
- [ ] Send message - success
- [ ] Mark as read - success
- [ ] Admin: Get all conversations
- [ ] Admin: Assign conversation
- [ ] Admin: Close conversation

### **Frontend:**
- [ ] Customer: View conversations list
- [ ] Customer: Send message in chat widget
- [ ] Customer: Receive i18n error messages
- [ ] Admin: View all conversations
- [ ] Admin: Assign conversation to admin
- [ ] Admin: Close/reopen conversation
- [ ] Error handling displays BE messages

---

## ✨ **Benefits:**
1. Consistent error handling across Chat module
2. I18n error messages (Vietnamese + English)
3. Better logging with response times
4. Cleaner code with pattern reuse
5. Frontend auto-parses BusinessApiResponse
6. Easier debugging with specific error codes
