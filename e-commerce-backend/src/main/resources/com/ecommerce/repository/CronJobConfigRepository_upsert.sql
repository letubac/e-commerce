INSERT INTO cron_job_configs (job_name, enabled, updated_at)
VALUES (/*jobName*/'', /*enabled*/true, /*updatedAt*/'')
ON CONFLICT (job_name)
DO UPDATE SET
    enabled    = EXCLUDED.enabled,
    updated_at = EXCLUDED.updated_at
