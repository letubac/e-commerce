SELECT c.*
FROM conversation c
WHERE c.status = /*status*/''
ORDER BY c.created_at DESC