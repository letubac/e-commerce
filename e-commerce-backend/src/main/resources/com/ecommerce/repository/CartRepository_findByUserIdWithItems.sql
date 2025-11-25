SELECT c.* FROM carts c 
LEFT JOIN cart_items ci ON c.id = ci.cart_id 
LEFT JOIN products p ON ci.product_id = p.id 
WHERE c.user_id = /*userId*/