SELECT cqr.*
FROM chat_quick_replies cqr
WHERE cqr.is_active = true
ORDER BY cqr.category ASC, cqr.title ASC;