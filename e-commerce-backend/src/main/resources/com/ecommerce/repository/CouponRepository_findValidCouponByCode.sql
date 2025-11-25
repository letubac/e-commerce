SELECT * FROM coupons 
WHERE code = /*code*/ AND is_active = true AND /*now*/ BETWEEN valid_from AND valid_until