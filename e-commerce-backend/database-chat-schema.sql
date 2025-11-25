-- Chat System Extended Features for E-Commerce
-- Note: Basic tables (conversations, chat_messages) are already created in database-base-schema.sql
-- This file contains additional tables for extended chat functionality

-- Table: chat_participants
-- Theo dõi những người tham gia cuộc trò chuyện (for group support)
CREATE TABLE chat_participants (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'customer', 'admin', 'supervisor'
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(conversation_id, user_id)
);

CREATE INDEX idx_chat_participants_conversation_id ON chat_participants(conversation_id);
CREATE INDEX idx_chat_participants_user_id ON chat_participants(user_id);

-- Table: chat_quick_replies
-- Tin nhắn mẫu cho admin
CREATE TABLE chat_quick_replies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50), -- greeting, faq, closing, etc.
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table: chat_settings
-- Cài đặt chat cho system
CREATE TABLE chat_settings (
    id BIGSERIAL PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    description TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default quick replies
INSERT INTO chat_quick_replies (title, content, category, is_active) VALUES
('Chào mừng', 'Xin chào! Cảm ơn bạn đã liên hệ với E-SHOP. Tôi có thể giúp gì cho bạn?', 'greeting', TRUE),
('Hỏi thông tin đơn hàng', 'Bạn có thể cung cấp mã đơn hàng để tôi kiểm tra thông tin cho bạn không?', 'order', TRUE),
('Hỏi thông tin sản phẩm', 'Bạn đang quan tâm đến sản phẩm nào? Tôi sẽ tư vấn chi tiết cho bạn.', 'product', TRUE),
('Chính sách đổi trả', 'E-SHOP có chính sách đổi trả trong vòng 15 ngày với điều kiện sản phẩm còn nguyên seal và không có dấu hiệu sử dụng.', 'policy', TRUE),
('Cảm ơn và kết thúc', 'Cảm ơn bạn đã liên hệ với E-SHOP. Chúc bạn có trải nghiệm mua sắm tuyệt vời!', 'closing', TRUE);

-- Insert default settings
INSERT INTO chat_settings (key_name, value, description) VALUES
('chat_enabled', 'true', 'Enable/disable chat system'),
('max_file_size', '10485760', 'Maximum file upload size in bytes (10MB)'),
('allowed_file_types', 'jpg,jpeg,png,gif,pdf,doc,docx', 'Allowed file extensions'),
('auto_close_hours', '24', 'Auto close conversations after hours of inactivity'),
('welcome_message', 'Xin chào! Chúng tôi có thể hỗ trợ gì cho bạn?', 'Default welcome message');

-- View: conversation_summary
-- Tóm tắt cuộc trò chuyện với tin nhắn cuối
CREATE VIEW conversation_summary AS
SELECT 
    c.id,
    c.user_id,
    c.admin_id,
    c.subject,
    c.status,
    c.created_at,
    c.updated_at,
    u.full_name as user_name,
    u.email as user_email,
    a.full_name as admin_name,
    (SELECT content FROM chat_messages WHERE conversation_id = c.id ORDER BY created_at DESC LIMIT 1) as last_message,
    (SELECT created_at FROM chat_messages WHERE conversation_id = c.id ORDER BY created_at DESC LIMIT 1) as last_message_at,
    (SELECT COUNT(*) FROM chat_messages WHERE conversation_id = c.id AND sender_type = 'USER') as message_count
FROM conversations c
LEFT JOIN users u ON c.user_id = u.id
LEFT JOIN users a ON c.admin_id = a.id;

-- Function: update_conversation_timestamp
-- Tự động update updated_at khi có tin nhắn mới
CREATE OR REPLACE FUNCTION update_conversation_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversations 
    SET updated_at = CURRENT_TIMESTAMP,
        last_message_at = NEW.created_at
    WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: update conversation when new message
CREATE TRIGGER trigger_update_conversation_timestamp
    AFTER INSERT ON chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_timestamp();

-- Function: mark_messages_as_read
-- Đánh dấu tin nhắn đã đọc (simplified version for base schema)
CREATE OR REPLACE FUNCTION mark_messages_as_read(
    p_conversation_id BIGINT,
    p_user_id BIGINT
) RETURNS VOID AS $$
BEGIN
    -- Update last_read_at for participants if table exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'chat_participants') THEN
        UPDATE chat_participants 
        SET last_read_at = CURRENT_TIMESTAMP 
        WHERE conversation_id = p_conversation_id 
        AND user_id = p_user_id;
    END IF;
END;
$$ LANGUAGE plpgsql;