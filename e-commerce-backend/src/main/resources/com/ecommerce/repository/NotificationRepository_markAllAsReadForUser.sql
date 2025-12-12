UPDATE notifications 
SET is_read = true, 
    read_at = /*readAt*/, 
    updated_at = /*readAt*/
WHERE user_id = /*userId*/ 
  AND is_read = false
