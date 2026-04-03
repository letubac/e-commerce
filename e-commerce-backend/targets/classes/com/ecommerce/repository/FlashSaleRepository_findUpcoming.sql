SELECT fs.*
FROM flash_sales fs
WHERE fs.is_active = true
AND fs.start_time > NOW()
ORDER BY fs.start_time ASC;