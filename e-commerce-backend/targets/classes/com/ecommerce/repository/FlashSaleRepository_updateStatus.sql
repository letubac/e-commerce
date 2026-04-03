UPDATE flash_sales 
SET is_active = /*isActive*/false,
    updated_at = NOW()
WHERE id = /*id*/0