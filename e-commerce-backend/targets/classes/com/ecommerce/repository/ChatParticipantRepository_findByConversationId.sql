SELECT cp.*
FROM chat_participants cp
WHERE cp.conversation_id = /*conversationId*/0
ORDER BY cp.joined_at ASC;