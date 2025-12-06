-- ============================================
-- ALTER TABLE cho hệ thống chat
-- Chỉ thêm các columns mới cần thiết
-- ============================================

-- 1. Thêm columns cho Conversations
ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'NORMAL';

ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS unread_count INTEGER DEFAULT 0;

COMMENT ON COLUMN conversations.priority IS 'Mức độ ưu tiên: LOW, NORMAL, HIGH, URGENT';
COMMENT ON COLUMN conversations.unread_count IS 'Số tin nhắn chưa đọc';

-- 2. Thêm columns cho Chat Messages
ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS message_type VARCHAR(20) DEFAULT 'TEXT';

ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS attachment_url VARCHAR(500);

ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS attachment_name VARCHAR(255);

ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;

ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS read_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN chat_messages.message_type IS 'Loại tin nhắn: TEXT, IMAGE, FILE';
COMMENT ON COLUMN chat_messages.attachment_url IS 'URL file đính kèm';
COMMENT ON COLUMN chat_messages.attachment_name IS 'Tên file đính kèm';
COMMENT ON COLUMN chat_messages.is_read IS 'Đã đọc chưa';
COMMENT ON COLUMN chat_messages.read_at IS 'Thời gian đọc';

-- 3. Tạo indexes cho performance
CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_created 
ON chat_messages(conversation_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chat_messages_is_read 
ON chat_messages(is_read) WHERE is_read = FALSE;

CREATE INDEX IF NOT EXISTS idx_conversations_priority 
ON conversations(priority);

-- 4. Trigger tự động update unread_count
CREATE OR REPLACE FUNCTION update_conversation_unread_count()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_read = FALSE AND NEW.sender_type = 'USER' THEN
        UPDATE conversations 
        SET unread_count = COALESCE(unread_count, 0) + 1
        WHERE id = NEW.conversation_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_unread_count ON chat_messages;
CREATE TRIGGER trigger_update_unread_count
    AFTER INSERT ON chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_unread_count();

-- 5. Trigger reset unread_count khi đánh dấu đã đọc
CREATE OR REPLACE FUNCTION reset_conversation_unread_count()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.is_read = FALSE AND NEW.is_read = TRUE THEN
        UPDATE conversations 
        SET unread_count = GREATEST(0, COALESCE(unread_count, 1) - 1)
        WHERE id = NEW.conversation_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_reset_unread_count ON chat_messages;
CREATE TRIGGER trigger_reset_unread_count
    AFTER UPDATE OF is_read ON chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION reset_conversation_unread_count();

-- 6. Update existing data
UPDATE conversations SET unread_count = 0 WHERE unread_count IS NULL;
UPDATE conversations SET priority = 'NORMAL' WHERE priority IS NULL;
UPDATE chat_messages SET message_type = 'TEXT' WHERE message_type IS NULL;
UPDATE chat_messages SET is_read = FALSE WHERE is_read IS NULL;
