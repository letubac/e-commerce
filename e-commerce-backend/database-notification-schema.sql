-- ========================================
-- NOTIFICATION SYSTEM SCHEMA UPDATES
-- Add new columns for enhanced notification features
-- ========================================

-- Add new columns to notifications table
ALTER TABLE notifications
ADD COLUMN IF NOT EXISTS target_role VARCHAR(20),
ADD COLUMN IF NOT EXISTS icon_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS entity_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS entity_id BIGINT,
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'NORMAL',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

-- Add comments for documentation
COMMENT ON COLUMN notifications.target_role IS 'Target role for broadcast: ADMIN, USER, or NULL for specific user';
COMMENT ON COLUMN notifications.icon_url IS 'Optional icon/image URL for notification';
COMMENT ON COLUMN notifications.entity_type IS 'Related entity type: Order, FlashSale, Coupon, Product, etc.';
COMMENT ON COLUMN notifications.entity_id IS 'Related entity ID';
COMMENT ON COLUMN notifications.priority IS 'Notification priority: LOW, NORMAL, HIGH, URGENT';
COMMENT ON COLUMN notifications.expires_at IS 'Expiration date for the notification';

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_target_role ON notifications(target_role);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_notifications_entity ON notifications(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- Update existing notification types
UPDATE notifications SET type = 'ORDER' WHERE type IS NULL OR type = '';

-- Set default priority for existing notifications
UPDATE notifications SET priority = 'NORMAL' WHERE priority IS NULL;

-- Add trigger to automatically update updated_at
CREATE OR REPLACE FUNCTION update_notification_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_notification_timestamp ON notifications;
CREATE TRIGGER trigger_update_notification_timestamp
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_notification_timestamp();

-- Sample notification types documentation
-- ORDER: Order placed, shipped, delivered, cancelled
-- FLASH_SALE: Flash sale started, ending soon, new flash sale
-- COUPON: New coupon available, coupon expiring soon
-- PROMOTION: New promotion started
-- PRODUCT: Product back in stock, price drop
-- SYSTEM: System maintenance, announcements
