DELETE FROM notifications 
WHERE expires_at IS NOT NULL 
  AND expires_at < /*now*/
