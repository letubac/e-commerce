SELECT fsp.*
FROM flash_sale_products fsp
WHERE fsp.flash_sale_id = /*flashSaleId*/0
  AND fsp.is_active = true
  AND fsp.stock_sold >= fsp.stock_limit
