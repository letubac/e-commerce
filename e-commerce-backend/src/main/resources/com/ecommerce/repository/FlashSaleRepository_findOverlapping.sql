SELECT fs.*
FROM flash_sales fs
WHERE (fs.id != /*excludeId*/0)
  AND fs.start_time < /*endTime*/NOW()
  AND fs.end_time > /*startTime*/NOW()
ORDER BY fs.start_time ASC
