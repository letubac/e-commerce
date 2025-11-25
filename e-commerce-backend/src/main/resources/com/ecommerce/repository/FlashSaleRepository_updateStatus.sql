UPDATE flash_sales 
SET is_active = /*status*/false,
    updated_at = NOW()
WHERE id = /*flashSaleId*/0;