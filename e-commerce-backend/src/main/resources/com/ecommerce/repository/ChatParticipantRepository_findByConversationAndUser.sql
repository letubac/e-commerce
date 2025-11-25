SELECT cp.*
FROM chat_participants cp
WHERE cp.conversation_id = /*conversationId*/0 
AND cp.user_id = /*userId*/0
LIMIT 1;