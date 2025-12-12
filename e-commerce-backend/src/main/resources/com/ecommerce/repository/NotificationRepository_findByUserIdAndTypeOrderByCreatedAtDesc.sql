SELECT * FROM notifications 
WHERE user_id = /*userId*/ 
  AND type = /*type*/ 
ORDER BY created_at DESC
