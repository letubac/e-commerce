-- ============================================
-- E-COMMERCE DATABASE MIGRATION SCRIPT
-- Cập nhật database schema để đồng bộ với entities
-- ============================================

-- 1. Thêm column address vào bảng users nếu chưa có
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'address'
    ) THEN
        ALTER TABLE users ADD COLUMN address TEXT;
    END IF;
END $$;

-- 2. Cập nhật bảng products nếu cần
DO $$
BEGIN
    -- Đảm bảo column status có giá trị mặc định
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'products' 
        AND column_name = 'status' 
        AND column_default IS NOT NULL
    ) THEN
        ALTER TABLE products ALTER COLUMN status SET DEFAULT 'PUBLISHED';
        
        -- Cập nhật các records hiện tại nếu status là NULL
        UPDATE products SET status = 'PUBLISHED' WHERE status IS NULL;
        
        -- Set NOT NULL constraint
        ALTER TABLE products ALTER COLUMN status SET NOT NULL;
    END IF;
END $$;

-- 3. Thêm indexes cho performance
CREATE INDEX IF NOT EXISTS idx_users_address ON users(address) WHERE address IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_users_email_verified_at ON users(email_verified_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);

-- 4. Cập nhật triggers nếu cần
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 5. Validation
SELECT 'Migration completed successfully!' as status;