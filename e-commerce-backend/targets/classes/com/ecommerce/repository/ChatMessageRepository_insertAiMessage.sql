INSERT INTO chat_messages (
    id,
    conversation_id,
    sender_id,
    sender_type,
    content,
    message_type,
    attachment_url,
    attachment_name,
    is_read,
    is_ai_response,
    created_at,
    updated_at
) VALUES (
    NEXTVAL('seq_chat_messages'),
    /*conversationId*/,
    NULL,
    'AI',
    /*content*/,
    'TEXT',
    NULL,
    NULL,
    FALSE,
    TRUE,
    /*createdAt*/,
    /*updatedAt*/
) RETURNING *
