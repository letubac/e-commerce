SELECT * FROM notifications 
WHERE user_id = /*userId*/ 
  AND (LOWER(title) LIKE LOWER('%' || /*keyword*/ || '%') 
       OR LOWER(message) LIKE LOWER('%' || /*keyword*/ || '%'))
ORDER BY created_at DESC
