SELECT fs.*
FROM flash_sales fs
WHERE fs.is_active = true
ORDER BY fs.created_at DESC;