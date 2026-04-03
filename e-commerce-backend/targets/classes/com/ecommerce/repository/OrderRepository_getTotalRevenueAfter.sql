SELECT COALESCE(SUM(total), 0) FROM orders 
WHERE status = 'DELIVERED' AND created_at > /*date*/