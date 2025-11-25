SELECT ci.*
FROM cart_items ci
WHERE ci.cart_id = /*cart.id*/1 
  AND ci.product_id = /*product.id*/1;