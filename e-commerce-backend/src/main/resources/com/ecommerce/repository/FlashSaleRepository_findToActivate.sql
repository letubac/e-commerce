SELECT fs.*
FROM flash_sales fs
WHERE fs.is_active = false
  AND fs.start_time <= NOW()
  AND fs.end_time > NOW()
ORDER BY fs.start_time ASC
