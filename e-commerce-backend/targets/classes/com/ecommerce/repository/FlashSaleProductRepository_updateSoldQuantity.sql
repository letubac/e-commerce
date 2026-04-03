UPDATE flash_sale_products 
SET sold_quantity = /*soldQuantity*/0,
    updated_at = NOW()
WHERE id = /*flashSaleProductId*/0;