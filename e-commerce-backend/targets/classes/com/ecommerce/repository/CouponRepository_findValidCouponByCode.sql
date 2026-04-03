SELECT * FROM coupons 
WHERE code = /*code*/ AND is_active = true AND /*now*/ BETWEEN start_date AND end_date