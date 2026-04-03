SELECT r.* 
FROM reviews r 
WHERE r.product_id = /*productId*/ 
ORDER BY r.created_at DESC