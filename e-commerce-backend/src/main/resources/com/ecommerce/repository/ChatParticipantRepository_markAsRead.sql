UPDATE chat_participants 
SET last_read_at = CURRENT_TIMESTAMP
WHERE conversation_id = /*conversationId*/0 
AND user_id = /*userId*/0;