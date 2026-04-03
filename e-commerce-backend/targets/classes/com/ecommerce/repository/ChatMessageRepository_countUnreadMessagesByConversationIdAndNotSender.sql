SELECT COUNT(*) FROM chat_messages 
WHERE conversation_id = /*conversationId*/ AND is_read = false AND sender_id != /*userId*/