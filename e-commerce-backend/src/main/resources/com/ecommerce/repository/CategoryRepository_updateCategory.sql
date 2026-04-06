UPDATE categories SET
    name = /*name*/'',
    slug = /*slug*/'',
    description = /*description*/'',
    parent_id = /*parentId*/0,
    image_url = /*imageUrl*/'',
    sort_order = /*sortOrder*/0,
    is_active = /*isActive*/true,
    updated_at = /*updatedAt*/CURRENT_TIMESTAMP
WHERE id = /*id*/0