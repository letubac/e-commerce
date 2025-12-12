UPDATE notifications SET
    user_id = /*notification.userId*/,
    title = /*notification.title*/,
    message = /*notification.message*/,
    type = /*notification.type*/,
    link = /*notification.link*/,
    is_read = /*notification.isRead*/,
    read_at = /*notification.readAt*/,
    target_role = /*notification.targetRole*/,
    icon_url = /*notification.iconUrl*/,
    entity_type = /*notification.entityType*/,
    entity_id = /*notification.entityId*/,
    priority = /*notification.priority*/,
    updated_at = /*notification.updatedAt*/,
    expires_at = /*notification.expiresAt*/
WHERE id = /*notification.id*/
