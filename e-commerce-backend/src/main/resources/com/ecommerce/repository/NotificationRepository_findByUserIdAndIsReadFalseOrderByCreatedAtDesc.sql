SELECT * FROM notifications 
WHERE user_id = /*userId*/ 
  AND is_read = false 
ORDER BY created_at DESC
