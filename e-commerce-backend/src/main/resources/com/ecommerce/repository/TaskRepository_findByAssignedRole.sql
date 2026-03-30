SELECT t.*, u1.username as assigned_username, u2.username as created_by_username
FROM tasks t
LEFT JOIN users u1 ON t.assigned_to = u1.id
LEFT JOIN users u2 ON t.created_by = u2.id
WHERE t.assigned_role = /*assignedRole*/''
ORDER BY t.created_at DESC
