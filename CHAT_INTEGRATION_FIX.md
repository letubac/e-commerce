# Chat System Integration Fix

## 🐛 Lỗi đã sửa

### Backend
**Lỗi**: `ChatMessageDTO message = chatMessageService.sendMessage(request, username);`
- **Nguyên nhân**: Interface `ChatMessageService.sendMessage()` yêu cầu `(Long senderId, SendMessageRequest request)` nhưng controller truyền `(request, username)`
- **Giải pháp**: Get `userId` từ `username` trước khi gọi service

**File**: `ChatController.java`
```java
// Trước (SAI)
ChatMessageDTO message = chatMessageService.sendMessage(request, username);

// Sau (ĐÚNG)
User user = userRepository.findByUsername(username)
    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
ChatMessageDTO message = chatMessageService.sendMessage(user.getId(), request);
```

### Frontend
**Vấn đề**: Response structure không khớp giữa FE và BE

**File**: `api.js`
- ✅ Cập nhật các Chat APIs để handle `ApiResponse<T>` wrapper
- ✅ Extract `data` field từ response

```javascript
// Trước
getUserConversations: () => api.request('/chat/conversations'),

// Sau
getUserConversations: async () => {
  const response = await api.request('/chat/conversations');
  return response.data || response; // Handle ApiResponse wrapper
},
```

**File**: `ChatWidget.js`
- ✅ Sửa `messageType: 'text'` → `messageType: 'TEXT'` (theo enum của BE)
- ✅ Sửa `messageType: 'file'` → `messageType: 'FILE'`
- ✅ Handle Page response từ `getConversationMessages`

```javascript
// Handle Page object: data.content or data array
const messagesArray = data?.content || data || [];
setMessages(messagesArray);
```

## 📋 Files đã thay đổi

### Backend (1 file)
1. ✅ `ChatController.java` - Sửa lỗi truyền param trong sendMessage()

### Frontend (2 files)
1. ✅ `api.js` - Handle ApiResponse wrapper cho tất cả Chat APIs
2. ✅ `ChatWidget.js` - 
   - Sửa messageType constants (TEXT, FILE)
   - Handle Page response structure

## 🔧 Chi tiết thay đổi

### 1. ChatController.java
```java
@PostMapping("/messages")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
        @Valid @RequestBody SendMessageRequest request,
        Authentication authentication) {
    
    String username = authentication.getName();
    
    // Verify user has access to conversation
    if (!conversationService.isUserOwnerOfConversation(username, request.getConversationId())) {
        return ResponseEntity.status(403)
                .body(new ApiResponse<>(false, "Bạn không có quyền gửi tin nhắn"));
    }

    // Get userId from username ✅ THÊM
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Call service with correct params ✅ SỬA
    ChatMessageDTO message = chatMessageService.sendMessage(user.getId(), request);

    return ResponseEntity.ok(new ApiResponse<>(true, "Gửi tin nhắn thành công", message));
}
```

### 2. api.js - Chat APIs
```javascript
// Chat APIs
getUserConversations: async () => {
  const response = await api.request('/chat/conversations');
  return response.data || response; // ✅ Handle ApiResponse wrapper
},

getConversationMessages: async (conversationId, page = 0, size = 50) => {
  const response = await api.request(`/chat/conversations/${conversationId}/messages?page=${page}&size=${size}`);
  return response.data || response; // ✅ Handle ApiResponse wrapper + Page support
},

sendMessage: async (data) => {
  const response = await api.request('/chat/messages', { method: 'POST', body: JSON.stringify(data) });
  return response.data || response; // ✅ Handle ApiResponse wrapper
},

createConversation: async (data) => {
  const response = await api.request('/chat/conversations', { method: 'POST', body: JSON.stringify(data) });
  return response.data || response; // ✅ Handle ApiResponse wrapper
},
```

### 3. ChatWidget.js - Message Types
```javascript
// Send text message
const messageData = {
  conversationId: currentConversation.id,
  content: newMessage.trim(),
  messageType: 'TEXT' // ✅ Uppercase để khớp với enum BE
};

// Send file message
const fileMessage = {
  conversationId: conversation.id,
  content: `Đã gửi file: ${file.name}`,
  messageType: 'FILE', // ✅ Uppercase
  attachmentUrl: result.url,
  attachmentName: file.name
};
```

### 4. ChatWidget.js - Handle Page Response
```javascript
const fetchNewMessages = useCallback(async () => {
  if (!conversation?.id || loading) return;
  
  try {
    const data = await api.getConversationMessages(conversation.id);
    // ✅ Handle Page object: data.content or data array
    const newMessages = data?.content || data || [];
    
    setMessages(currentMessages => {
      if (newMessages.length > currentMessages.length) {
        if (!isOpen) {
          setUnreadCount(prev => prev + (newMessages.length - currentMessages.length));
        } else {
          markAsRead(conversation.id);
        }
        return newMessages;
      }
      return currentMessages;
    });
  } catch (error) {
    console.error('Error fetching messages:', error);
  }
}, [conversation?.id, isOpen, markAsRead, loading]);
```

## 🎯 API Request/Response Structure

### Request: Send Message
```json
POST /api/v1/chat/messages
Headers: {
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
Body: {
  "conversationId": 1,
  "content": "Hello",
  "messageType": "TEXT"
}
```

### Response: ApiResponse wrapper
```json
{
  "success": true,
  "message": "Gửi tin nhắn thành công",
  "data": {
    "id": 123,
    "conversationId": 1,
    "senderId": 5,
    "senderName": "user123",
    "senderType": "USER",
    "content": "Hello",
    "createdAt": "2025-12-06T10:30:00",
    "updatedAt": "2025-12-06T10:30:00"
  },
  "timestamp": 1733481000000
}
```

### Response: Get Messages (Page)
```json
{
  "success": true,
  "message": "Lấy tin nhắn cuộc trò chuyện thành công",
  "data": {
    "content": [
      {
        "id": 123,
        "conversationId": 1,
        "senderId": 5,
        "senderName": "user123",
        "senderType": "USER",
        "content": "Hello",
        "createdAt": "2025-12-06T10:30:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 50
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "first": true
  },
  "timestamp": 1733481000000
}
```

## ✅ Testing

### 1. Test Backend
```bash
# Build
cd e-commerce-backend
mvn clean install

# Run
mvn spring-boot:run
```

### 2. Test Frontend
```bash
# Install dependencies
cd e-commerce-fe
npm install

# Run
npm start
```

### 3. Test Flow
1. ✅ Đăng nhập vào hệ thống
2. ✅ Mở ChatWidget
3. ✅ Gửi tin nhắn text
4. ✅ Kiểm tra console log (không có lỗi)
5. ✅ Tin nhắn hiển thị trong chat
6. ✅ Upload file (nếu có)

## 🚀 Next Steps

1. ✅ Test thoroughly trên dev environment
2. ⏳ Implement WebSocket real-time messaging
3. ⏳ Add typing indicators
4. ⏳ Add read receipts display
5. ⏳ Add notification sound
6. ⏳ Add emoji picker
7. ⏳ Add image preview

## 📝 Notes

- Backend sử dụng `ApiResponse<T>` wrapper cho tất cả responses
- Frontend cần extract `response.data` để lấy actual data
- Message types phải UPPERCASE: `TEXT`, `FILE`, `IMAGE`
- `getConversationMessages` trả về `Page<ChatMessageDTO>`, cần extract `content` array
- UserRepository được inject vào ChatController để convert username → userId
