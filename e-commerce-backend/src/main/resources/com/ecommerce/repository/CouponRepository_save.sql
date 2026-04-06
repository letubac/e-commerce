INSERT INTO coupons (
    code, 
    name, 
    description, 
    discount_type, 
    discount_value, 
    min_order_amount, 
    max_discount_amount, 
    usage_limit, 
    usage_limit_per_user, 
    used_count, 
    is_active, 
    start_date, 
    end_date, 
    created_at, 
    updated_at
) VALUES (
    /*code*/'',
    /*name*/'',
    /*description*/'',
    /*discountType*/'',
    /*discountValue*/0,
    /*minOrderAmount*/0,
    /*maxDiscountAmount*/0,
    /*usageLimit*/0,
    /*usageLimitPerUser*/0,
    /*usedCount*/0,
    /*isActive*/true,
    /*startDate*/'',
    /*endDate*/'',
    /*createdAt*/'',
    /*updatedAt*/''
)
RETURNING id
