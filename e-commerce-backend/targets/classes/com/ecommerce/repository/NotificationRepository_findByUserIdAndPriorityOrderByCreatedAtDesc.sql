SELECT * FROM notifications 
WHERE user_id = /*userId*/ 
  AND priority = /*priority*/ 
ORDER BY created_at DESC
