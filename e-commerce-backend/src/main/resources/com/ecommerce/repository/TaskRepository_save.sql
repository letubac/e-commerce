INSERT INTO tasks (title, description, status, priority, assigned_to, assigned_role, created_by, due_date, created_at, updated_at)
VALUES (/*title*/'', /*description*/'', /*status*/'TODO', /*priority*/'MEDIUM', /*assignedTo*/null, /*assignedRole*/'', /*createdBy*/0, /*dueDate*/null, /*createdAt*/'', /*updatedAt*/'')
RETURNING id
