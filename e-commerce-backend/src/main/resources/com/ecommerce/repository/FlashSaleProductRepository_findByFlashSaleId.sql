SELECT fsp.*
FROM flash_sale_products fsp
WHERE fsp.flash_sale_id = /*flashSaleId*/0
ORDER BY fsp.sort_order ASC;