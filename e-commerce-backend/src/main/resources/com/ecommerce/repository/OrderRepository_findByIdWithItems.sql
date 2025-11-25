SELECT o.* FROM orders o 
LEFT JOIN order_items oi ON o.id = oi.order_id 
WHERE o.id = /*id*/