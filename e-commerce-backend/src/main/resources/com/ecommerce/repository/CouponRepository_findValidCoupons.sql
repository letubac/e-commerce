SELECT * FROM coupons 
WHERE is_active = true AND /*now*/ BETWEEN valid_from AND valid_until