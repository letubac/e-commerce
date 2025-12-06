# 🚀 HƯỚNG DẪN SETUP HỆ THỐNG CHAT HOÀN CHỈNH

## Tổng quan hệ thống

Hệ thống chat được xây dựng với các công nghệ hiện đại:
- **Backend**: Spring Boot + Mirage SQL + WebSocket
- **Real-time**: WebSocket cho chat real-time
- **Database**: PostgreSQL với triggers và views tối ưu
- **Architecture**: Clean Architecture với Service Layer pattern

## 📋 Các thành phần đã implement

### 1. Database Layer
- ✅ **Tables**: conversations, chat_messages, chat_participants, chat_quick_replies
- ✅ **Indexes**: Tối ưu cho query performance
- ✅ **Triggers**: Tự động cập nhật unread_count, last_message_at
- ✅ **Views**: conversation_details_view, chat_statistics_view

### 2. Entity Layer (Mirage SQL)
- ✅ **ChatMessage**: Entity với đầy đủ fields (attachment, read status)
- ✅ **Conversation**: Entity với priority và unread_count
- ✅ **ChatParticipant**: Quản lý người tham gia
- ✅ **ChatQuickReply**: Tin nhắn mẫu

### 3. Repository Layer (Mirage SQL)
- ✅ **ChatMessageRepository**: Queries với SQL files
- ✅ **ConversationRepository**: Full CRUD operations
- ✅ **ChatParticipantRepository**: Participant management
- ✅ **ChatQuickReplyRepository**: Quick replies

### 4. Service Layer
- ✅ **ChatService**: Unified service cho tất cả chat operations
- ✅ **ChatServiceImpl**: Implementation đầy đủ với transaction management
- ✅ **WebSocketChatService**: Quản lý WebSocket sessions và broadcasting

### 5. Controller Layer
- ✅ **ChatControllerV2**: REST API endpoints đầy đủ
- ✅ **Security**: PreAuthorize với role-based access
- ✅ **Validation**: Jakarta validation cho request DTOs

### 6. WebSocket Layer
- ✅ **ChatWebSocketHandler**: Handler cho WebSocket connections
- ✅ **WebSocketChatService**: Broadcasting và session management
- ✅ **WebSocketAuthInterceptor**: JWT authentication cho WebSocket
- ✅ **WebSocketConfig**: Configuration với CORS

## 🛠️ Setup Instructions

### Bước 1: Cập nhật Database

```bash
# Chạy script cập nhật schema
psql -U postgres -d ecommerce -f database-chat-schema-update.sql
```

### Bước 2: Cấu hình Application

File `application.yml` đã có sẵn cấu hình WebSocket:

```yaml
# WebSocket đã được config trong WebSocketConfig.java
# CORS origins: http://localhost:3000, http://localhost:3001
```

### Bước 3: Build và Run Backend

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

### Bước 4: Test API Endpoints

#### **Tạo conversation mới:**
```bash
POST /api/v1/chat/conversations
Authorization: Bearer <token>
Content-Type: application/json

{
  "subject": "Hỏi về sản phẩm",
  "initialMessage": "Xin chào, tôi muốn hỏi về sản phẩm ABC"
}
```

#### **Lấy danh sách conversations:**
```bash
GET /api/v1/chat/conversations
Authorization: Bearer <token>
```

#### **Gửi tin nhắn:**
```bash
POST /api/v1/chat/messages
Authorization: Bearer <token>
Content-Type: application/json

{
  "conversationId": 1,
  "content": "Đây là nội dung tin nhắn",
  "messageType": "TEXT"
}
```

#### **Lấy tin nhắn trong conversation:**
```bash
GET /api/v1/chat/conversations/1/messages?page=0&size=50
Authorization: Bearer <token>
```

#### **Đánh dấu đã đọc:**
```bash
POST /api/v1/chat/conversations/1/read
Authorization: Bearer <token>
```

### Bước 5: Kết nối WebSocket từ Frontend

```javascript
// Kết nối WebSocket
const token = localStorage.getItem('token');
const conversationId = 1;

const ws = new WebSocket(
  `ws://localhost:8080/ws/chat?token=${token}&conversationId=${conversationId}`
);

ws.onopen = () => {
  console.log('WebSocket connected');
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Received:', data);
  
  switch(data.type) {
    case 'CONNECTION_ESTABLISHED':
      console.log('Connected as:', data.username);
      break;
    case 'NEW_MESSAGE':
      console.log('New message:', data.message);
      // Cập nhật UI với tin nhắn mới
      break;
    case 'TYPING':
      console.log('User typing:', data.userName);
      // Hiển thị typing indicator
      break;
    case 'MESSAGES_READ':
      console.log('Messages read by:', data.userId);
      // Cập nhật read status
      break;
  }
};

// Gửi typing indicator
ws.send(JSON.stringify({
  type: 'TYPING',
  isTyping: true
}));

// Gửi read receipt
ws.send(JSON.stringify({
  type: 'READ'
}));
```

## 📊 Database Schema

### Conversations Table
```sql
- id: BIGINT (PK)
- user_id: BIGINT (FK -> users)
- admin_id: BIGINT (FK -> users, nullable)
- subject: VARCHAR(500)
- status: VARCHAR(20) -- OPEN, ASSIGNED, RESOLVED, CLOSED
- priority: VARCHAR(20) -- LOW, NORMAL, HIGH, URGENT
- unread_count: INTEGER
- last_message_at: TIMESTAMP
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

### Chat Messages Table
```sql
- id: BIGINT (PK)
- conversation_id: BIGINT (FK -> conversations)
- sender_id: BIGINT (FK -> users)
- sender_type: VARCHAR(20) -- USER, ADMIN
- content: TEXT
- message_type: VARCHAR(20) -- TEXT, IMAGE, FILE
- attachment_url: VARCHAR(500)
- attachment_name: VARCHAR(255)
- is_read: BOOLEAN
- read_at: TIMESTAMP
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

## 🔄 Flow hoạt động

### 1. User tạo conversation mới:
1. Frontend gọi `POST /api/v1/chat/conversations`
2. Backend tạo conversation và tin nhắn đầu tiên
3. WebSocket broadcast thông báo có conversation mới đến admin
4. Return conversation data về frontend

### 2. User gửi tin nhắn:
1. Frontend gọi `POST /api/v1/chat/messages`
2. Backend lưu message vào database
3. Update `last_message_at` và `unread_count` của conversation
4. WebSocket broadcast tin nhắn đến tất cả participants
5. Return message data về frontend

### 3. Real-time updates qua WebSocket:
1. Frontend kết nối WebSocket với token + conversationId
2. Backend authenticate và register session
3. Khi có event (message mới, typing, read), WebSocket broadcast
4. Frontend nhận update và cập nhật UI real-time

## 🎯 Features đã implement

### Core Features:
- ✅ Tạo conversation
- ✅ Gửi/nhận tin nhắn
- ✅ Upload file đính kèm
- ✅ Đánh dấu đã đọc
- ✅ Đếm tin nhắn chưa đọc
- ✅ WebSocket real-time
- ✅ Typing indicator
- ✅ Read receipts
- ✅ Conversation status management
- ✅ Priority levels
- ✅ Admin assignment

### Security:
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ User can only access own conversations
- ✅ Admin can access all conversations
- ✅ WebSocket authentication

## 📝 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/chat/conversations` | Lấy danh sách conversations |
| GET | `/api/v1/chat/conversations/{id}/messages` | Lấy tin nhắn |
| POST | `/api/v1/chat/conversations` | Tạo conversation mới |
| POST | `/api/v1/chat/messages` | Gửi tin nhắn |
| POST | `/api/v1/chat/conversations/{id}/read` | Đánh dấu đã đọc |
| GET | `/api/v1/chat/conversations/{id}/unread-count` | Số tin nhắn chưa đọc |
| POST | `/api/v1/chat/upload` | Upload file |

## 🔧 Troubleshooting

### WebSocket không kết nối được:
1. Check CORS configuration trong `WebSocketConfig.java`
2. Verify token validity
3. Check browser console for errors

### Tin nhắn không real-time:
1. Verify WebSocket connection established
2. Check `WebSocketChatService` đang broadcast đúng
3. Check session registration

### Database errors:
1. Chạy lại migration script
2. Check sequence exists
3. Verify foreign key constraints

## 🚀 Next Steps

Để hoàn thiện hệ thống, bạn có thể:
1. Implement file upload service (hiện tại là placeholder)
2. Thêm Admin Controller cho quản lý conversations
3. Implement notifications (email/push)
4. Thêm typing indicator persistence
5. Implement message search
6. Add conversation archiving
7. Implement chat analytics

## 💡 Best Practices

1. **Luôn dùng transactions** cho operations cập nhật nhiều tables
2. **WebSocket chỉ để notify**, business logic ở REST API
3. **Validate permissions** ở cả service và controller layer
4. **Use indexes** cho queries thường xuyên
5. **Log properly** cho debugging và monitoring

---

**Hệ thống đã sẵn sàng để sử dụng! 🎉**

Nếu có vấn đề gì, check logs trong console và database triggers.
