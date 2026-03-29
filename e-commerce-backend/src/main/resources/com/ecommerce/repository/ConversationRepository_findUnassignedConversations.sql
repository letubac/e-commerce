SELECT 
    id,
    user_id,
    admin_id,
    subject,
    status,
    priority,
    unread_count,
    last_message_at,
    created_at,
    updated_at
FROM conversations 
WHERE status = 'OPEN' 
ORDER BY updated_at DESC