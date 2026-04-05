UPDATE coupons
SET is_active  = false,
    updated_at = NOW()
WHERE is_active = true
  AND (
      (end_date IS NOT NULL AND end_date < /*now*/)
      OR (usage_limit IS NOT NULL AND used_count >= usage_limit)
  )
