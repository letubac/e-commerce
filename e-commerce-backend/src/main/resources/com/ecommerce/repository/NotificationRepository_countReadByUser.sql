SELECT COUNT(*) FROM notifications 
WHERE user_id = /*userId*/ 
  AND is_read = true
