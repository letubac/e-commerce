-- Migration: Add is_anonymous column to reviews table
-- Date: 2025-12-08
-- Description: Allow users to post anonymous reviews

-- Add is_anonymous column
ALTER TABLE reviews 
ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment
COMMENT ON COLUMN reviews.is_anonymous IS 'Whether the review is posted anonymously';

-- Optional: Update existing reviews to show as non-anonymous
UPDATE reviews SET is_anonymous = FALSE WHERE is_anonymous IS NULL;
