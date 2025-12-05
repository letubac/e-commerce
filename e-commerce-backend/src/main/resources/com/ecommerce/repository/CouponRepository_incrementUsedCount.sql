UPDATE coupons 
SET used_count = used_count + 1, 
    updated_at = NOW()
WHERE id = /*id*/0
