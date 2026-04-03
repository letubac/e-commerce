SELECT na.*
FROM new_arrivals na
WHERE na.product_id = /*productId*/0
AND na.is_active = true
ORDER BY na.created_at DESC;