SELECT * FROM coupons 
WHERE usage_limit IS NOT NULL AND used_count >= usage_limit