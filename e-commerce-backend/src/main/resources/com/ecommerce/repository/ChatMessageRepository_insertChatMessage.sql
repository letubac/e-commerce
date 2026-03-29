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
    created_at,
    updated_at
) VALUES (
    NEXTVAL('seq_chat_messages'),
    /*conversationId*/,
    /*senderId*/,
    /*senderType*/,
    /*content*/,
    /*messageType*/,
    /*attachmentUrl*/,
    /*attachmentName*/,
    /*isRead*/,
    /*createdAt*/,
    /*updatedAt*/
) RETURNING *