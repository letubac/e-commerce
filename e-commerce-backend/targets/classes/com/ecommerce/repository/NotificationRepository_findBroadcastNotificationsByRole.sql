SELECT * FROM notifications 
WHERE target_role = /*role*/ 
  AND user_id IS NULL 
ORDER BY created_at DESC
