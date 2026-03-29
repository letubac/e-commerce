-- AI Support Agent Schema Migration
-- Run this script to enable AI messages in the chat system

-- 1. Allow AI as sender_type (drop old constraint, recreate with 'AI')
DO $$
BEGIN
    -- Drop the old check constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'message_sender_type_valid'
          AND table_name = 'chat_messages'
    ) THEN
        ALTER TABLE chat_messages DROP CONSTRAINT message_sender_type_valid;
    END IF;
END $$;

ALTER TABLE chat_messages
    ADD CONSTRAINT message_sender_type_valid
    CHECK (sender_type IN ('USER', 'ADMIN', 'AI'));

-- 2. Allow sender_id to be NULL (AI messages have no real user)
ALTER TABLE chat_messages ALTER COLUMN sender_id DROP NOT NULL;

-- 3. Add is_ai_response flag for easy filtering (optional index)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'is_ai_response'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN is_ai_response BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_chat_messages_is_ai ON chat_messages(is_ai_response) WHERE is_ai_response = TRUE;
