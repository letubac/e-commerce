UPDATE tasks
SET title = /*title*/'',
    description = /*description*/'',
    status = /*status*/'TODO',
    priority = /*priority*/'MEDIUM',
    assigned_to = /*assignedTo*/null,
    assigned_role = /*assignedRole*/'',
    due_date = /*dueDate*/null,
    completed_at = /*completedAt*/null,
    updated_at = /*updatedAt*/''
WHERE id = /*id*/0
