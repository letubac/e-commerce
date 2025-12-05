SELECT SUM(total) FROM orders 
WHERE status = 'DELIVERED' AND created_at BETWEEN /*startDate*/ AND /*endDate*/