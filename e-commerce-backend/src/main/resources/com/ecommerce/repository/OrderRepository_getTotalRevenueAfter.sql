SELECT COALESCE(SUM(total_price), 0) FROM orders 
WHERE status = 'DELIVERED' AND created_at > /*date*/