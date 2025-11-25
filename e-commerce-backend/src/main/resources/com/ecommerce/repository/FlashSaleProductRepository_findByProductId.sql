SELECT fsp.*
FROM flash_sale_products fsp
WHERE fsp.product_id = /*productId*/0
ORDER BY fsp.created_at DESC;