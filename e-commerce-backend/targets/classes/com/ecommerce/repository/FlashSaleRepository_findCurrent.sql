SELECT fs.*
FROM flash_sales fs
WHERE fs.is_active = true
AND NOW() BETWEEN fs.start_time AND fs.end_time
ORDER BY fs.start_time ASC;