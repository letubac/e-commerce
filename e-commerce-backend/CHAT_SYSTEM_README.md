# Hệ thống Chat Hỗ trợ Khách hàng

## Tổng quan

Hệ thống chat real-time cho phép khách hàng tương tác trực tiếp với đội ngũ hỗ trợ của cửa hàng. Hệ thống bao gồm:

- **ChatWidget**: Widget chat cho người dùng trên trang web
- **AdminChatManagement**: Giao diện quản lý chat cho admin
- **Database Schema**: Cấu trúc cơ sở dữ liệu hỗ trợ chat
- **API Integration**: Các endpoint API cho chat

## Tính năng

### Cho Người dùng (ChatWidget):
- ✅ Gửi và nhận tin nhắn real-time
- ✅ Upload file đính kèm (hình ảnh, tài liệu)
- ✅ Minimize/maximize chat window
- ✅ Hiển thị trạng thái tin nhắn chưa đọc
- ✅ Tự động scroll xuống tin nhắn mới
- ✅ Responsive design cho mobile

### Cho Admin (AdminChatManagement):
- ✅ Xem danh sách tất cả cuộc trò chuyện
- ✅ Tìm kiếm và lọc cuộc trò chuyện
- ✅ Quản lý trạng thái cuộc trò chuyện (mở, đang xử lý, đã giải quyết, đóng)
- ✅ Tin nhắn mẫu (Quick Replies)
- ✅ Phân công cuộc trò chuyện cho admin
- ✅ Hiển thị mức độ ưu tiên
- ✅ Upload và quản lý file đính kèm
- ✅ Thống kê cuộc trò chuyện

## Cấu trúc Database

### Bảng chính:
- `chat_conversations`: Quản lý cuộc trò chuyện
- `chat_messages`: Lưu trữ tin nhắn
- `chat_participants`: Người tham gia cuộc trò chuyện
- `chat_quick_replies`: Tin nhắn mẫu
- `chat_settings`: Cài đặt hệ thống

### Views:
- `conversation_summary_view`: Tóm tắt cuộc trò chuyện
- `admin_conversation_view`: View cho admin
- `user_conversation_view`: View cho người dùng

## API Endpoints

### User Endpoints:
```
GET    /api/v1/chat/conversations          # Lấy cuộc trò chuyện của user
GET    /api/v1/chat/conversations/:id/messages  # Lấy tin nhắn
POST   /api/v1/chat/conversations          # Tạo cuộc trò chuyện mới
POST   /api/v1/chat/messages               # Gửi tin nhắn
POST   /api/v1/chat/upload                 # Upload file
POST   /api/v1/chat/conversations/:id/read # Đánh dấu đã đọc
```

### Admin Endpoints:
```
GET    /api/v1/admin/chat/conversations    # Lấy tất cả cuộc trò chuyện
GET    /api/v1/admin/chat/conversations/:id/messages  # Lấy tin nhắn
POST   /api/v1/admin/chat/messages         # Gửi tin nhắn (admin)
PUT    /api/v1/admin/chat/conversations/:id/status    # Cập nhật trạng thái
POST   /api/v1/admin/chat/conversations/:id/assign    # Phân công
GET    /api/v1/admin/chat/quick-replies    # Lấy tin nhắn mẫu
POST   /api/v1/admin/chat/quick-replies    # Tạo tin nhắn mẫu
GET    /api/v1/admin/chat/statistics       # Thống kê
```

## Cài đặt và Chạy

### 1. Database Setup
```sql
-- Chạy file SQL để tạo schema
psql -d your_database -f database-chat-schema.sql
```

### 2. Frontend Setup
Components đã được tích hợp vào:
- `src/components/ChatWidget.js` - Widget chat cho user
- `src/components/AdminChatManagement.js` - Giao diện admin
- `src/pages/admin/AdminDashboard.js` - Tích hợp vào admin dashboard

### 3. API Integration
Các API calls đã được chuẩn bị trong:
- `src/api/api.js` - User side APIs
- `src/api/adminApi.js` - Admin side APIs

## Sử dụng

### Tích hợp ChatWidget:
```jsx
import ChatWidget from './components/ChatWidget';

function App() {
  return (
    <div>
      {/* Your app content */}
      <ChatWidget />
    </div>
  );
}
```

### Truy cập Admin Chat:
1. Đăng nhập với tài khoản admin
2. Vào Admin Dashboard
3. Click tab "Chat hỗ trợ"

## Tính năng nâng cao (Cần phát triển)

### WebSocket Integration:
- Real-time messaging với Socket.IO
- Thông báo online/offline
- Typing indicators

### Push Notifications:
- Thông báo tin nhắn mới cho admin
- Email notifications

### AI Chatbot:
- Auto-reply với AI
- Smart routing conversations
- Sentiment analysis

### Analytics:
- Response time tracking
- Customer satisfaction scoring
- Detailed reporting dashboard

## File Structure

```
src/
├── components/
│   ├── ChatWidget.js              # Widget chat cho user
│   └── AdminChatManagement.js     # Giao diện admin chat
├── pages/admin/
│   └── AdminDashboard.js          # Dashboard tích hợp chat
├── api/
│   ├── api.js                     # User APIs
│   └── adminApi.js                # Admin APIs
└── database-chat-schema.sql       # Database schema
```

## Troubleshooting

### Common Issues:
1. **Chat không hiển thị**: Kiểm tra user đã đăng nhập
2. **Tin nhắn không gửi được**: Kiểm tra API endpoints và authentication
3. **File upload fail**: Kiểm tra file size limit và format

### Debug Mode:
Mở Console để xem logs chi tiết:
```javascript
console.log('Chat conversation:', conversation);
console.log('Messages:', messages);
```

## Security Considerations

- ✅ Authentication required cho tất cả endpoints
- ✅ File upload validation (size, type)
- ✅ XSS protection trong message content
- ✅ Rate limiting cho message sending
- ⏳ Message encryption (future)
- ⏳ File virus scanning (future)

## Performance Optimization

- ✅ Lazy loading conversations
- ✅ Message pagination
- ✅ Optimistic UI updates
- ⏳ Message caching (future)
- ⏳ CDN for file attachments (future)

## Contributing

1. Fork repository
2. Create feature branch
3. Implement changes
4. Add tests
5. Submit pull request

## License

MIT License - See LICENSE file for details