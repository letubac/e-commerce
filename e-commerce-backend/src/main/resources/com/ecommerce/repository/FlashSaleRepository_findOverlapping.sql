SELECT fs.*
FROM flash_sales fs
WHERE (fs.id != /*excludeId*/0)
  AND fs.start_time < /*endTime*/CURRENT_TIMESTAMP
  AND fs.end_time > /*startTime*/CURRENT_TIMESTAMP
ORDER BY fs.start_time ASC
