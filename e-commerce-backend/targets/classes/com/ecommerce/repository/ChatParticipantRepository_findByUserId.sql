SELECT cp.*
FROM chat_participants cp
WHERE cp.user_id = /*userId*/0
ORDER BY cp.joined_at DESC;