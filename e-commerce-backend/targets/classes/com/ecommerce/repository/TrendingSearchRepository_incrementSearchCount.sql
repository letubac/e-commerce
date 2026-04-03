UPDATE trending_searches 
SET search_count = search_count + 1,
    updated_at = CURRENT_TIMESTAMP
WHERE keyword = /*keyword*/'';