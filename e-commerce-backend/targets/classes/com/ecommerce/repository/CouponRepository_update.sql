UPDATE coupons 
SET 
    code = /*code*/'',
    name = /*name*/'',
    description = /*description*/'',
    discount_type = /*discountType*/'',
    discount_value = /*discountValue*/0,
    min_order_amount = /*minOrderAmount*/0,
    max_discount_amount = /*maxDiscountAmount*/0,
    usage_limit = /*usageLimit*/0,
    usage_limit_per_user = /*usageLimitPerUser*/0,
    is_active = /*isActive*/true,
    start_date = /*startDate*/'',
    end_date = /*endDate*/'',
    updated_at = /*updatedAt*/''
WHERE id = /*id*/0
