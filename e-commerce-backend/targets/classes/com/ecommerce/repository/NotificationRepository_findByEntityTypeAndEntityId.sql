SELECT * FROM notifications 
WHERE entity_type = /*entityType*/ 
  AND entity_id = /*entityId*/ 
ORDER BY created_at DESC
