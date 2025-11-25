SELECT ts.*
FROM trending_searches ts
WHERE ts.is_active = true 
AND ts.category = /*category*/''
ORDER BY ts.search_count DESC, ts.display_order ASC;