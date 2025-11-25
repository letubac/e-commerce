SELECT COALESCE(AVG(total_price), 0) FROM orders 
WHERE status = 'DELIVERED'