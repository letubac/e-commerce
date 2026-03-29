# 📞 Hướng dẫn Hệ thống Chat Khách hàng - Admin

## 🎯 Tổng Quan Luồng Hoạt Động

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. KHÁCH HÀNG TẠO CUỘC TRỘI CHUYỆN                              │
│    - Truy cập trang sản phẩm                                    │
│    - Nhấp "Chat với bộ phận hỗ trợ"                            │
│    - Viết tin nhắn đặt câu hỏi                                 │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ▼
        [POST] /api/v1/chat/messages
        ├─ conversationId: 1
        ├─ content: "Tôi có thắc mắc..."
        ├─ messageType: TEXT
        └─ Tạo ChatMessage ID tự động từ seq_chat_messages
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. TIN NHẮN ĐƯỢC LƯU VÀO DATABASE                               │
│    - ChatMessage được insert với ID sequence                   │
│    - Conversation.unreadCount tăng                             │
│    - Conversation.lastMessageAt được update                    │
│    - WebSocket broadcast tin nhắn realtime                     │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. ADMIN QUẢN LÝ CUỘC TRỘI CHUYỆN                               │
│    - Vào trang Admin > Chat Hỗ trợ                            │
│    - Xem danh sách tất cả conversations                        │
│    - Hiển thị:                                                  │
│      • Tên khách hàng (userName)                              │
│      • Tiêu đề cuộc trò chuyện (subject)                     │
│      • Trạng thái (OPEN/ASSIGNED/RESOLVED/CLOSED)             │
│      • Số tin nhắn chưa đọc (unreadCount)                     │
│      • Thời gian cập nhật cuối (lastMessageAt)               │
└──────────────────┬──────────────────────────────────────────────┘
                   │
                   ▼
        [GET] /api/v1/chat/admin/conversations
              ?page=0&size=50&status=OPEN
        └─ Trả về Page<ConversationDTO> với userName
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. ADMIN CHỌN CUỘC TRỘI CHUYỆN ĐỂ TRẢ LỜI                      │
│    - Nhấp vào conversation từ danh sách                        │
│    - Quản lý:                                                   │
│      • Xem lịch sử tin nhắn                                   │
│      • Soạn tin nhắn trả lời                                │
│      • Đánh dấu đã đọc                                        │
│      • Thay đổi trạng thái (Assigned → Resolved → Closed)     │
└──────────────────┬──────────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┬──────────────┬──────────────┐
        │                     │              │              │
        ▼                     ▼              ▼              ▼
    [GET] Conv       [POST] Send Message  [POST] Assign  [PUT] Status
    Messages       /chat/admin/messages  /chat/admin/   /chat/admin/
                                          conversations  conversations
                                          /{id}/assign    /{id}/status
```

---

## 🔧 Backend Implementation

### 1. **Entities & Data Models**

#### Conversation (cuộc trò chuyện)
```sql
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,              -- Khách hàng
    admin_id BIGINT,                      -- Admin phụ trách (NULL = unassigned)
    subject VARCHAR(255),                 -- Tiêu đề
    status VARCHAR(20),                   -- OPEN, ASSIGNED, RESOLVED, CLOSED
    priority VARCHAR(20),                 -- LOW, NORMAL, HIGH, URGENT
    unread_count INT DEFAULT 0,           -- Số tin chưa đọc
    last_message_at TIMESTAMP,            -- Lần cuối có tin nhắn
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### ChatMessage (tin nhắn)
```sql
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_type VARCHAR(50),              -- ADMIN, USER
    content TEXT,
    message_type VARCHAR(50),             -- TEXT, FILE, SYSTEM
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### 2. **REST API Endpoints**

#### User Endpoints
```
GET    /api/v1/chat/conversations
       - Lấy danh sách conversations của user hiện tại
       - Response: Page<ConversationDTO> với messages

POST   /api/v1/chat/conversations
       - Tạo conversation mới
       - Body: { subject, initialMessage }

GET    /api/v1/chat/conversations/{id}/messages
       - Lấy tin nhắn của conversation
       - Query: page, size
       - Response: Page<ChatMessageDTO>

POST   /api/v1/chat/messages
       - Gửi tin nhắn
       - Body: { conversationId, content, messageType }
       - Auto broadcast qua WebSocket

POST   /api/v1/chat/conversations/{id}/read
       - Đánh dấu tin nhắn đã đọc
       - Body: { userId }
```

#### Admin Endpoints
```
GET    /api/v1/chat/admin/conversations
       - Lấy danh sách TẤT CẢ conversations
       - Query: page, size, status
       - Response: Page<ConversationDTO> với:
         {
           id, userId, userName,
           adminId, adminName,
           subject, status, priority,
           unreadCount, lastMessageAt,
           createdAt, updatedAt
         }

GET    /api/v1/chat/admin/conversations/{id}/messages
       - Lấy tin nhắn của conversation bất kỳ
       - Query: page, size
       - Response: Page<ChatMessageDTO>

POST   /api/v1/chat/admin/messages
       - Admin trả lời tin nhắn
       - Body: { conversationId, content, messageType }
       - senderType tự động = ADMIN

POST   /api/v1/chat/admin/conversations/{id}/assign
       - Admin nhận phụ trách conversation
       - Tự động cập nhật adminId và status = ASSIGNED

PUT    /api/v1/chat/admin/conversations/{id}/status
       - Thay đổi trạng thái conversation
       - Body: { status: "RESOLVED" | "CLOSED" }

POST   /api/v1/chat/admin/conversations/{id}/read
       - Admin đánh dấu tin nhắn đã đọc
       - Cập nhật conversation.unreadCount = 0
```

### 3. **Service Layer (ChatMessageServiceImpl)**

```java
public class ChatMessageServiceImpl implements ChatMessageService {
    
    // ✅ FIX: Sử dụng insertChatMessage() để trigger sequence
    public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        ChatMessage savedMessage = chatMessageRepository.insertChatMessage(
            conversationId,
            senderId,
            senderType,
            content,
            messageType,
            attachmentUrl,
            attachmentName,
            false,  // isRead
            new Date(),
            new Date()
        );
        
        // Update conversation: lastMessageAt, unreadCount, status
        conversation.updateLastMessage();
        if ("USER".equals(senderType)) {
            conversation.incrementUnreadCount();
        }
        conversationRepository.save(conversation);
        
        // Broadcast WebSocket
        webSocketChatService.broadcastMessage(conversationId, messageDTO);
        
        return convertToDTO(savedMessage);
    }
    
    // ✅ FIX: Proper error handling + validation
    public void markMessagesAsRead(Long conversationId, Long userId) {
        // 1. Verify conversation exists
        Conversation conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow(...);
        
        // 2. Mark messages as read in database
        chatMessageRepository.markMessagesAsReadByConversationId(
            conversationId, userId);
        
        // 3. Reset unread count
        conversation.resetUnreadCount();
        conversationRepository.save(conversation);
        
        // 4. Notify WebSocket (non-critical)
        try {
            webSocketChatService.notifyMessagesRead(conversationId, userId);
        } catch (Exception e) {
            log.error("WebSocket notification failed", e);
            // Don't throw - WebSocket is not critical
        }
    }
}
```

### 4. **SQL Queries (Mirage SQL)**

#### ChatMessageRepository_insertChatMessage.sql
```sql
INSERT INTO chat_messages (
    id,                    -- ✅ NEXTVAL để auto-generate
    conversation_id,
    sender_id,
    sender_type,
    content,
    message_type,
    attachment_url,
    attachment_name,
    is_read,
    created_at,
    updated_at
) VALUES (
    NEXTVAL('seq_chat_messages'),  -- ✅ Trigger sequence
    /*conversationId*/,
    /*senderId*/,
    /*senderType*/,
    /*content*/,
    /*messageType*/,
    /*attachmentUrl*/,
    /*attachmentName*/,
    /*isRead*/,
    /*createdAt*/,
    /*updatedAt*/
) RETURNING *
```

#### ConversationRepository_findAllPaged.sql
```sql
SELECT 
    c.id,
    c.user_id,
    c.admin_id,
    c.subject,
    c.status,
    c.priority,
    c.unread_count,
    c.last_message_at,
    c.created_at,
    c.updated_at
FROM conversations c
ORDER BY c.updated_at DESC
-- Mirage SQL automatically handles pagination via Pageable parameter
```

#### ConversationRepository_findByStatusPaged.sql
```sql
SELECT 
    id, user_id, admin_id, subject,
    status, priority, unread_count,
    last_message_at, created_at, updated_at
FROM conversations
WHERE status = /*status*/''
ORDER BY updated_at DESC
```

---

## 🎨 Frontend Implementation

### Admin Chat Management Component

```javascript
function AdminChatManagement() {
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  
  // 1. Load conversations from admin endpoint
  const loadConversations = async () => {
    const data = await adminApi.getChatConversations({
      status: statusFilter !== 'all' ? statusFilter : undefined
    });
    setConversations(data.content || []);
  };
  
  // 2. Display conversation list with customer names
  return (
    <div className="conversations-list">
      {conversations.map(conv => (
        <ConversationItem
          key={conv.id}
          conversation={conv}
          selected={selectedConversation?.id === conv.id}
          onClick={() => setSelectedConversation(conv)}
        >
          <CustomerName>{conv.userName}</CustomerName>
          <Subject>{conv.subject}</Subject>
          <Status>{conv.status}</Status>
          <UnreadBadge>{conv.unreadCount}</UnreadBadge>
        </ConversationItem>
      ))}
    </div>
  );
}

// 3. When conversation selected, load messages
useEffect(() => {
  if (selectedConversation) {
    loadMessages(selectedConversation.id);
    markAsRead(selectedConversation.id);
  }
}, [selectedConversation]);

// 4. Admin can send reply
const sendReply = async (content) => {
  await adminApi.sendChatMessage({
    conversationId: selectedConversation.id,
    content,
    messageType: 'TEXT'
  });
  loadMessages(selectedConversation.id);
  loadConversations();  // Refresh list
};

// 5. Admin can change status
const updateStatus = async (status) => {
  await adminApi.updateConversationStatus(
    selectedConversation.id,
    status
  );
};

// 6. Admin can assign to themselves
const assignToMe = async () => {
  await adminApi.assignConversation(selectedConversation.id);
};
```

---

## 📊 Data Flow Diagram

```
CUSTOMER SIDE                  API                    ADMIN SIDE
════════════════════════════════════════════════════════════════════

1. Product Page
   [Chat Support Button] ─┐
                          │
2. Chat Modal            │
   [Type Message]  ◄─────┴──► POST /chat/messages
                               (INSERT via sequence)
   [Send] ─────────────────►  ✅ ChatMessage created
                               │
                          ┌────┴──────┐
                          │           │
                    ▼ UPDATE        ▼ WebSocket
            Conversation         Broadcast
            • lastMessageAt   → Admin sees NEW
            • unreadCount++      NOTIFICATION
                          
3. WebSocket Listen  ◄────────── ws://.../chat
   ✅ See my message
      (from broadcast)

4. Wait for reply... ◄──────── Admin sends reply
   [New message]
   [Read indicator]
                          
================================================
ADMIN SIDE:

1. Admin Dashboard
   [Chat Management] ◄─────── GET /chat/admin/conversations
                               • Shows all conversations
                               • With customer names
                               • Unread count
                               • Status badges

2. Click Conversation ──────► GET /chat/admin/conversations/{id}/messages
   [See messages list]
   [Load chat history]

3. Type & Send reply ───────► POST /chat/admin/messages
   [senderType = ADMIN]
   • Message inserted
   • broadcast WebSocket
   • conversationUpdated

4. Actions available:
   • POST /chat/admin/conversations/{id}/read
     → Mark unread=0
   
   • POST /chat/admin/conversations/{id}/assign
     → Set adminId = currentAdmin
     → Status = ASSIGNED
   
   • PUT /chat/admin/conversations/{id}/status
     → Change: OPEN→RESOLVED→CLOSED

5. Real-time updates:
   • Poll every 5 sec: loadConversations()
   • Poll every 3 sec: loadMessages()
   • WebSocket for instant notifications
```

---

## 🚀 Testing Steps

### Step 1: Create Test Conversation
```bash
# Login as customer (user@example.com)
POST /api/v1/auth/login
Body: { "username": "user@example.com", "password": "password123" }
Response: { "token": "JWT_TOKEN" }

# Create conversation
POST /api/v1/chat/conversations
Headers: { "Authorization": "Bearer JWT_TOKEN" }
Body: { "subject": "Tôi muốn hỏi về sản phẩm" }

# Get conversation ID
GET /api/v1/chat/conversations
```

### Step 2: Send Message as Customer
```bash
POST /api/v1/chat/messages
Headers: { "Authorization": "Bearer CUSTOMER_TOKEN" }
Body: {
    "conversationId": 1,
    "content": "Sản phẩm này có size nào khác không?",
    "messageType": "TEXT"
}
✅ Response: ChatMessageDTO { id: 123, ... }
```

### Step 3: View as Admin
```bash
# Login as admin
POST /api/v1/auth/login
Body: { "username": "admin", "password": "admin123" }

# Get all conversations
GET /api/v1/chat/admin/conversations?page=0&size=10
✅ Response: {
    "content": [{
        "id": 1,
        "userId": 2,
        "userName": "user@example.com",  ✅ Customer name populated
        "subject": "Tôi muốn hỏi về sản phẩm",
        "status": "OPEN",
        "unreadCount": 1,
        "lastMessageAt": "2026-03-08T13:15:00Z"
    }]
}
```

### Step 4: Admin Replies
```bash
POST /api/v1/chat/admin/messages
Headers: { "Authorization": "Bearer ADMIN_TOKEN" }
Body: {
    "conversationId": 1,
    "content": "Có, chúng tôi có size khác. Bạn muốn size bao nhiêu?",
    "messageType": "TEXT"
}
✅ CustomerWebSocket receives message realtime
```

### Step 5: Assign & Close
```bash
# Admin assigns to themselves
POST /api/v1/chat/admin/conversations/1/assign
✅ Status changes to ASSIGNED

# Admin resolves
PUT /api/v1/chat/admin/conversations/1/status
Body: { "status": "RESOLVED" }
✅ Conversation moves to RESOLVED state
```

---

## 🔍 Troubleshooting

### Issue: Admin sees "Không có cuộc trò chuyện nào"
**Root Causes:**
1. ❌ No conversations created yet
   - Create a test conversation via API

2. ❌ SQL query not returning customer name
   - ✅ FIXED: Updated ConversationRepository_findAllPaged.sql
   - ✅ Service calls `convertToDTO()` which fetches userName

3. ❌ Authentication failed
   - Verify JWT token is valid and not expired
   - Check ROLE_ADMIN permission

### Issue: sendMessage returns "id must not be null"
**Root Cause:** Mirage SQL `save()` method doesn't trigger sequence
**Solution:** ✅ FIXED - Use `insertChatMessage()` which calls NEXTVAL()

### Issue: markMessagesAsRead throws DetailException
**Root Cause:** Conversation validation missing + WebSocket failure cascades
**Solution:** ✅ FIXED - Added validation + make WebSocket non-critical

### Issue: WebSocket not connecting
- Check authToken in URL query parameter
- Verify WebSocket endpoint: `ws://localhost:8280/ws/chat?token=JWT`
- Check browser console for errors

---

## 📋 Checklist - System Complete

- ✅ Backend Server Running (Port 8280)
- ✅ Chat SQL Schema Created
- ✅ ChatMessageRepository.insertChatMessage() with NEXTVAL
- ✅ ConversationService.convertToDTO() fetches userName
- ✅ ConversationRepository SQL queries updated
- ✅ ChatController with all admin endpoints
- ✅ ChatMessageServiceImpl with proper error handling
- ✅ Frontend AdminChatManagement component
- ✅ WebSocket integration for realtime updates
- ✅ Admin can list conversations
- ✅ Admin can view messages
- ✅ Admin can send replies
- ✅ Admin can assign/close conversations

---

## Next Steps to Verify

1. **Refresh Admin Dashboard**
   - Navigate to http://localhost:3000/admin
   - Check Chat Management tab
   - Verify conversations display with customer names

2. **Test Full Flow**
   - Create conversation as customer
   - Send message
   - Admin sees in dashboard
   - Admin replies
   - Customer receives reply

3. **Monitor Database**
   - Check conversations table for records
   - Verify chat_messages inserting with proper IDs
   - Monitor unread_count updates

---

## Database Verification Queries

```sql
-- Check conversations
SELECT id, user_id, subject, status, unread_count, 
       last_message_at, created_at
FROM conversations
ORDER BY created_at DESC
LIMIT 10;

-- Check messages  
SELECT id, conversation_id, sender_id, sender_type,
       content, is_read, created_at
FROM chat_messages
WHERE conversation_id = 1
ORDER BY created_at DESC
LIMIT 20;

-- Check user for populate customer name
SELECT id, username, email
FROM users
WHERE id = (SELECT user_id FROM conversations LIMIT 1);
```

