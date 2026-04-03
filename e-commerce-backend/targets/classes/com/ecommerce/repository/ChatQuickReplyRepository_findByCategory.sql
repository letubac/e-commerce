SELECT cqr.*
FROM chat_quick_replies cqr
WHERE cqr.is_active = true 
AND cqr.category = /*category*/''
ORDER BY cqr.title ASC;