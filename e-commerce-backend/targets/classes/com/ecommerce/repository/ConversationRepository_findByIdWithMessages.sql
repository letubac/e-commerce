SELECT c.* FROM conversations c 
LEFT JOIN chat_messages cm ON c.id = cm.conversation_id 
WHERE c.id = /*id*/