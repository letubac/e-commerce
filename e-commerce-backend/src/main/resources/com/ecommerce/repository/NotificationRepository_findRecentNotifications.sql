SELECT * FROM notifications 
WHERE user_id = /*userId*/ 
  AND created_at >= /*sinceDate*/ 
ORDER BY created_at DESC
