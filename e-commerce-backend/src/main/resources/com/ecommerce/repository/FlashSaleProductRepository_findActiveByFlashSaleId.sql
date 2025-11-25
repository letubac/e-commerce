SELECT fsp.*
FROM flash_sale_products fsp
JOIN flash_sales fs ON fsp.flash_sale_id = fs.id
WHERE fsp.flash_sale_id = /*flashSaleId*/0
AND fsp.is_active = true
AND fs.is_active = true
ORDER BY fsp.display_order ASC;