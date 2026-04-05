SELECT fs.*
FROM flash_sales fs
WHERE fs.is_active = true
  AND fs.end_time < NOW()
ORDER BY fs.end_time ASC
