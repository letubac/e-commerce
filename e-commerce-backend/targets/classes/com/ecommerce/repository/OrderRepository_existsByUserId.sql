SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
FROM orders o
WHERE o.user_id = /*userId*/0