SELECT 
    c.id,
    c.user_id,
    c.admin_id,
    c.subject,
    c.status,
    c.priority,
    c.unread_count,
    c.last_message_at,
    c.created_at,
    c.updated_at
FROM conversations c
ORDER BY c.updated_at DESC