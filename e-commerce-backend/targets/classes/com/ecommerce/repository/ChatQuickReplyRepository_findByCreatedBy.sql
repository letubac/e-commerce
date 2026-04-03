SELECT cqr.*
FROM chat_quick_replies cqr
WHERE cqr.created_by = /*createdBy*/''
ORDER BY cqr.title ASC;