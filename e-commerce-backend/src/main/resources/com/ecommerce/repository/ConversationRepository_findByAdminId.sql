SELECT c.*
FROM conversations c
WHERE c.admin_id = /*adminId*/1
ORDER BY c.updated_at DESC;