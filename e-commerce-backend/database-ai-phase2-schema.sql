-- AI Support Agent - Phase 2 Schema Migration
-- Run this script AFTER database-ai-schema.sql (Phase 1)

-- 1. Add ai_enabled flag to conversations
--    When FALSE, AI auto-reply is disabled for that conversation (e.g. admin has taken over).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'conversations' AND column_name = 'ai_enabled'
    ) THEN
        ALTER TABLE conversations ADD COLUMN ai_enabled BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
END $$;

COMMENT ON COLUMN conversations.ai_enabled IS
    'When FALSE the AI will not auto-reply in this conversation (admin has taken over).';

-- 2. Index for quick lookup of AI-disabled conversations
CREATE INDEX IF NOT EXISTS idx_conversations_ai_enabled
    ON conversations(ai_enabled) WHERE ai_enabled = FALSE;
