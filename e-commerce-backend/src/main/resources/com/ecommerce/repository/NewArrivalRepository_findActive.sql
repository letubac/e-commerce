SELECT na.*
FROM new_arrivals na
WHERE na.is_active = true
ORDER BY na.display_order ASC, na.created_at DESC;