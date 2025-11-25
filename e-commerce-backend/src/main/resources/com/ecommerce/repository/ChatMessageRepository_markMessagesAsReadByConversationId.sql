UPDATE chat_messages 
SET is_read = true, read_at = CURRENT_TIMESTAMP 
WHERE conversation_id = /*conversationId*/ AND sender_id != /*userId*/