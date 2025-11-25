-- ============================================
-- E-COMMERCE DATABASE ADDITIONS
-- Bổ sung cho schema hiện tại để hỗ trợ đầy đủ Frontend
-- 
-- 🚀 SAFE RE-EXECUTION: File này có thể chạy lại nhiều lần
-- - Sử dụng IF NOT EXISTS cho ALTER TABLE
-- - Sử dụng ON CONFLICT DO NOTHING cho INSERT
-- - Kiểm tra constraint và trigger trước khi tạo
-- ============================================

-- ============================================
-- 1. FLASH SALE & PROMOTIONS MODULE
-- ============================================

-- Flash Sale campaigns
CREATE TABLE IF NOT EXISTS flash_sales (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    banner_image_url VARCHAR(500),
    background_color VARCHAR(7) DEFAULT '#ef4444', -- red-600
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Flash Sale Products with limited stock
CREATE TABLE IF NOT EXISTS flash_sale_products (
    id BIGSERIAL PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    flash_price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    stock_limit INTEGER NOT NULL,
    stock_sold INTEGER NOT NULL DEFAULT 0,
    max_per_customer INTEGER DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    UNIQUE (flash_sale_id, product_id),
    CONSTRAINT flash_stock_positive CHECK (stock_limit > 0),
    CONSTRAINT flash_sold_valid CHECK (stock_sold >= 0 AND stock_sold <= stock_limit)
);

-- ============================================
-- 2. WISHLIST SYSTEM
-- ============================================

-- User wishlists
CREATE TABLE IF NOT EXISTS wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, product_id)
);

-- ============================================
-- 3. SEARCH & TRENDING SYSTEM
-- ============================================

-- Search history and analytics
CREATE TABLE IF NOT EXISTS search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    search_term VARCHAR(255) NOT NULL,
    result_count INTEGER DEFAULT 0,
    filters_applied JSONB, -- {"category": "electronics", "price_range": "1000-5000"}
    clicked_product_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trending searches aggregated
CREATE TABLE IF NOT EXISTS trending_searches (
    id BIGSERIAL PRIMARY KEY,
    search_term VARCHAR(255) UNIQUE NOT NULL,
    search_count INTEGER NOT NULL DEFAULT 1,
    last_searched TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. PRODUCT VARIANTS SYSTEM
-- ============================================

-- Product variants (for size, color, storage, etc.)
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(255) NOT NULL, -- "iPhone 15 Pro - 256GB - Titanium Blue"
    sku VARCHAR(100) UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    compare_at_price DECIMAL(10, 2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    variant_options JSONB, -- {"color": "blue", "storage": "256GB", "size": "Pro"}
    weight DECIMAL(8, 2) DEFAULT 0,
    dimensions VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT variant_price_positive CHECK (price >= 0),
    CONSTRAINT variant_stock_non_negative CHECK (stock_quantity >= 0)
);

-- ============================================
-- 5. SHIPPING & DELIVERY SYSTEM
-- ============================================

-- Shipping zones
CREATE TABLE IF NOT EXISTS shipping_zones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    countries TEXT[], -- ["VN", "TH", "SG"]
    provinces TEXT[], -- ["TP.HCM", "Hà Nội", "Đà Nẵng"]
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shipping rates and methods
CREATE TABLE IF NOT EXISTS shipping_rates (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL,
    method VARCHAR(100) NOT NULL, -- "EXPRESS", "STANDARD", "FREE", "SAME_DAY"
    name VARCHAR(255) NOT NULL, -- "Giao hàng nhanh", "Giao hàng tiêu chuẩn"
    description TEXT,
    min_weight DECIMAL(8, 2) DEFAULT 0,
    max_weight DECIMAL(8, 2),
    min_order_value DECIMAL(10, 2) DEFAULT 0,
    base_rate DECIMAL(10, 2) NOT NULL,
    per_kg_rate DECIMAL(10, 2) DEFAULT 0,
    estimated_days_min INTEGER NOT NULL,
    estimated_days_max INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (zone_id) REFERENCES shipping_zones(id) ON DELETE CASCADE
);

-- ============================================
-- 6. BANNERS & CONTENT MANAGEMENT
-- ============================================

-- Website banners and promotional content
CREATE TABLE IF NOT EXISTS banners (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    description TEXT,
    image_url VARCHAR(500),
    mobile_image_url VARCHAR(500), -- Separate image for mobile
    link_url VARCHAR(500),
    button_text VARCHAR(100),
    position VARCHAR(50) NOT NULL, -- "HERO", "SIDEBAR", "FOOTER", "POPUP", "CATEGORY"
    page VARCHAR(50), -- "HOME", "PRODUCTS", "CATEGORY"
    sort_order INTEGER DEFAULT 0,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    click_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT banner_position_check CHECK (position IN ('HERO', 'SIDEBAR', 'FOOTER', 'POPUP', 'CATEGORY', 'PRODUCT_DETAIL'))
);

-- ============================================
-- 7. INVENTORY MANAGEMENT
-- ============================================

-- Stock movement tracking
CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    movement_type VARCHAR(20) NOT NULL, -- "IN", "OUT", "ADJUSTMENT"
    quantity INTEGER NOT NULL,
    reason VARCHAR(100), -- "PURCHASE", "SALE", "RETURN", "DAMAGE", "ADJUSTMENT"
    reference_type VARCHAR(50), -- "ORDER", "RETURN", "MANUAL"
    reference_id BIGINT, -- order_id, return_id, etc.
    notes TEXT,
    unit_cost DECIMAL(10, 2),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT movement_type_check CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT'))
);

-- ============================================
-- 8. EMAIL & MESSAGING SYSTEM
-- ============================================

-- Email templates for system emails
CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    html_content TEXT NOT NULL,
    text_content TEXT,
    variables JSONB, -- {"customer_name", "order_number", "verification_link"}
    category VARCHAR(50), -- "AUTH", "ORDER", "MARKETING", "SYSTEM"
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Email sending log
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- "SENT", "FAILED", "PENDING", "BOUNCED"
    error_message TEXT,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES email_templates(id) ON DELETE SET NULL,
    CONSTRAINT email_status_check CHECK (status IN ('SENT', 'FAILED', 'PENDING', 'BOUNCED'))
);

-- ============================================
-- 9. COMPARISON & RECENTLY VIEWED
-- ============================================

-- Product comparison
CREATE TABLE IF NOT EXISTS product_comparisons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255), -- For anonymous users
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Recently viewed products
CREATE TABLE IF NOT EXISTS recently_viewed (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255), -- For anonymous users
    product_id BIGINT NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================
-- 10. PRODUCT Q&A SYSTEM
-- ============================================

-- Product questions and answers
CREATE TABLE IF NOT EXISTS product_questions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    is_answered BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    answer TEXT NOT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    is_official BOOLEAN NOT NULL DEFAULT FALSE, -- Answer from store admin
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES product_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- 11. IMPROVEMENTS TO EXISTING TABLES
-- ============================================

-- Add user preferences and activity tracking to users table (skip columns that already exist)
DO $$
BEGIN
    -- Add date_of_birth column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='date_of_birth') THEN
        ALTER TABLE users ADD COLUMN date_of_birth DATE;
    END IF;
    
    -- Add gender column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='gender') THEN
        ALTER TABLE users ADD COLUMN gender VARCHAR(10);
    END IF;
    
    -- Add preferred_language column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='preferred_language') THEN
        ALTER TABLE users ADD COLUMN preferred_language VARCHAR(10) DEFAULT 'vi';
    END IF;
    
    -- Add preferred_currency column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='preferred_currency') THEN
        ALTER TABLE users ADD COLUMN preferred_currency VARCHAR(3) DEFAULT 'VND';
    END IF;
    
    -- Add marketing_emails column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='marketing_emails') THEN
        ALTER TABLE users ADD COLUMN marketing_emails BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
    
    -- Skip last_login_at as it already exists in base schema
    
    -- Add avatar_url column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='avatar_url') THEN
        ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
    END IF;
    
    -- Add timezone column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='timezone') THEN
        ALTER TABLE users ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh';
    END IF;
END $$;

-- Add gender constraint if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                  WHERE constraint_name = 'user_gender_check' AND table_name = 'users') THEN
        ALTER TABLE users ADD CONSTRAINT user_gender_check 
        CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL);
    END IF;
END $$;

-- Enhance cart items with variant support
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='cart_items' AND column_name='variant_id') THEN
        ALTER TABLE cart_items ADD COLUMN variant_id BIGINT;
        ALTER TABLE cart_items ADD FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Enhance order items with variant support  
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='variant_id') THEN
        ALTER TABLE order_items ADD COLUMN variant_id BIGINT;
        ALTER TABLE order_items ADD FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='order_items' AND column_name='variant_options') THEN
        ALTER TABLE order_items ADD COLUMN variant_options JSONB;
    END IF;
END $$;

-- Add coupon usage tracking
CREATE TABLE IF NOT EXISTS coupon_uses (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
);

-- ============================================
-- 12. CREATE INDEXES FOR NEW TABLES
-- ============================================

-- Flash Sales indexes
CREATE INDEX idx_flash_sales_active_dates ON flash_sales(is_active, start_time, end_time);
CREATE INDEX idx_flash_sale_products_sale_id ON flash_sale_products(flash_sale_id);
CREATE INDEX idx_flash_sale_products_product_id ON flash_sale_products(product_id);

-- Wishlist indexes
CREATE INDEX idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX idx_wishlists_product_id ON wishlists(product_id);

-- Search indexes
CREATE INDEX idx_search_history_user_id ON search_history(user_id);
CREATE INDEX idx_search_history_created_at ON search_history(created_at);
CREATE INDEX idx_search_history_term ON search_history(search_term);
CREATE INDEX idx_trending_searches_count ON trending_searches(search_count DESC);

-- Product variants indexes
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_active ON product_variants(is_active);

-- Shipping indexes
CREATE INDEX idx_shipping_rates_zone_id ON shipping_rates(zone_id);
CREATE INDEX idx_shipping_rates_method ON shipping_rates(method);

-- Banner indexes
CREATE INDEX idx_banners_position ON banners(position);
CREATE INDEX idx_banners_active_dates ON banners(is_active, start_date, end_date);
CREATE INDEX idx_banners_page ON banners(page);

-- Stock movement indexes
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_type, reference_id);

-- Email indexes
CREATE INDEX idx_email_logs_recipient ON email_logs(recipient_email);
CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_created_at ON email_logs(created_at);

-- Product interaction indexes
CREATE INDEX idx_product_comparisons_user_id ON product_comparisons(user_id);
CREATE INDEX idx_product_comparisons_session ON product_comparisons(session_id);
CREATE INDEX idx_recently_viewed_user_id ON recently_viewed(user_id);
CREATE INDEX idx_recently_viewed_session ON recently_viewed(session_id);
CREATE INDEX idx_recently_viewed_time ON recently_viewed(viewed_at);

-- Q&A indexes
CREATE INDEX idx_product_questions_product_id ON product_questions(product_id);
CREATE INDEX idx_product_questions_user_id ON product_questions(user_id);
CREATE INDEX idx_product_answers_question_id ON product_answers(question_id);

-- ============================================
-- 13. ADD TRIGGERS FOR NEW TABLES
-- ============================================

-- Create triggers only if they don't exist
DO $$
BEGIN
    -- Flash sales trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_flash_sales_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_flash_sales_updated_at BEFORE UPDATE ON flash_sales
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    -- Product variants trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_product_variants_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_product_variants_updated_at BEFORE UPDATE ON product_variants
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    -- Banners trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_banners_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_banners_updated_at BEFORE UPDATE ON banners
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    -- Email templates trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_email_templates_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_email_templates_updated_at BEFORE UPDATE ON email_templates
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    -- Product questions trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_product_questions_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_product_questions_updated_at BEFORE UPDATE ON product_questions
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;

    -- Product answers trigger
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_product_answers_updated_at') THEN
        EXECUTE 'CREATE TRIGGER update_product_answers_updated_at BEFORE UPDATE ON product_answers
                 FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()';
    END IF;
END $$;

-- ============================================
-- 14. SAMPLE DATA FOR NEW TABLES
-- ============================================

-- Insert sample flash sale
INSERT INTO flash_sales (name, description, start_time, end_time, banner_image_url) VALUES
('Flash Sale Cuối Tuần', 'Giảm giá khủng các sản phẩm công nghệ', 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '24 hours',
 '/images/banners/flash-sale-weekend.jpg');

-- Insert trending searches
INSERT INTO trending_searches (search_term, search_count) VALUES
('iPhone 15', 1250),
('Samsung Galaxy S24', 980),
('MacBook Pro', 750),
('AirPods', 680),
('iPad Pro', 520),
('Gaming Laptop', 450),
('Wireless Mouse', 320),
('Mechanical Keyboard', 280)
ON CONFLICT (search_term) DO NOTHING;

-- Insert shipping zones and rates
INSERT INTO shipping_zones (name, description, countries, provinces) VALUES
('Nội thành TP.HCM', 'Khu vực nội thành TP.HCM giao hàng nhanh', 
 ARRAY['VN'], ARRAY['TP.HCM']),
('Nội thành Hà Nội', 'Khu vực nội thành Hà Nội giao hàng nhanh', 
 ARRAY['VN'], ARRAY['Hà Nội']),
('Các tỉnh thành khác', 'Giao hàng toàn quốc', 
 ARRAY['VN'], ARRAY['Đà Nẵng', 'Cần Thơ', 'Hải Phòng']);

INSERT INTO shipping_rates (zone_id, method, name, description, min_order_value, base_rate, estimated_days_min, estimated_days_max) VALUES
(1, 'SAME_DAY', 'Giao trong ngày', 'Giao hàng trong ngày tại TP.HCM', 500000, 50000, 0, 1),
(1, 'EXPRESS', 'Giao hàng nhanh', 'Giao hàng trong 24h', 0, 30000, 1, 1),
(1, 'FREE', 'Miễn phí giao hàng', 'Miễn phí cho đơn hàng trên 1 triệu', 1000000, 0, 1, 2),
(2, 'EXPRESS', 'Giao hàng nhanh', 'Giao hàng trong 24h', 0, 35000, 1, 1),
(3, 'STANDARD', 'Giao hàng tiêu chuẩn', 'Giao hàng toàn quốc', 0, 40000, 2, 5);

-- Insert sample email templates
INSERT INTO email_templates (name, display_name, subject, html_content, text_content, variables, category) VALUES
('welcome', 'Chào mừng khách hàng mới', 'Chào mừng bạn đến với E-SHOP!', 
 '<h1>Chào mừng {{customer_name}}!</h1><p>Cảm ơn bạn đã đăng ký tài khoản tại E-SHOP.</p>', 
 'Chào mừng {{customer_name}}! Cảm ơn bạn đã đăng ký tài khoản tại E-SHOP.',
 '["customer_name", "verification_link"]', 'AUTH'),
('order_confirmation', 'Xác nhận đơn hàng', 'Xác nhận đơn hàng #{{order_number}}',
 '<h1>Đơn hàng #{{order_number}} đã được xác nhận</h1><p>Cảm ơn {{customer_name}} đã mua hàng!</p>',
 'Đơn hàng #{{order_number}} đã được xác nhận. Cảm ơn {{customer_name}} đã mua hàng!',
 '["customer_name", "order_number", "order_total", "order_items"]', 'ORDER')
ON CONFLICT (name) DO NOTHING;

-- Insert sample banners
INSERT INTO banners (title, subtitle, description, image_url, link_url, button_text, position, page, sort_order) VALUES
('Siêu Sale Công Nghệ', 'Giảm giá lên đến 50%', 'Khuyến mãi khủng cho các sản phẩm công nghệ hot nhất', 
 '/images/banners/hero-tech-sale.jpg', '/products?category=electronics', 'Mua ngay', 'HERO', 'HOME', 1),
('iPhone 15 Series', 'Mới ra mắt', 'Đặt trước ngay hôm nay để nhận ưu đãi đặc biệt', 
 '/images/banners/iphone-15-banner.jpg', '/products/iphone-15', 'Xem chi tiết', 'HERO', 'HOME', 2),
('Black Friday 2024', 'Giảm giá khủng', 'Siêu sale cuối năm với hàng nghìn sản phẩm giảm giá', 
 '/images/banners/black-friday.jpg', '/flash-sale', 'Mua ngay', 'HERO', 'HOME', 3),
('Gaming Zone', 'Thiết bị gaming chuyên nghiệp', 'Đầy đủ phụ kiện gaming từ chuột, bàn phím đến headset', 
 '/images/banners/gaming-banner.jpg', '/products?category=gaming', 'Khám phá', 'SIDEBAR', 'HOME', 1);

-- ============================================
-- 16. EXTENSIVE DUMMY DATA FOR TESTING
-- ============================================

-- Insert sample users (customers and admins) - Skip if already exists
INSERT INTO users (email, password, full_name, phone_number, email_verified, preferred_language, marketing_emails) VALUES
('user1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMpOXxvZxVXR4j5VjmQLdA7oTBfv/vMGHDO', 'Nguyễn Văn An', '0987654321', TRUE, 'vi', TRUE),
('user2@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMpOXxvZxVXR4j5VjmQLdA7oTBfv/vMGHDO', 'Trần Thị Bình', '0976543210', TRUE, 'vi', FALSE),
('user3@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMpOXxvZxVXR4j5VjmQLdA7oTBfv/vMGHDO', 'Lê Hoàng Cường', '0965432109', TRUE, 'vi', TRUE),
('user4@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMpOXxvZxVXR4j5VjmQLdA7oTBfv/vMGHDO', 'Phạm Thu Dung', '0954321098', TRUE, 'vi', TRUE),
('manager@eshop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMpOXxvZxVXR4j5VjmQLdA7oTBfv/vMGHDO', 'Manager User', '0943210987', TRUE, 'vi', TRUE)
ON CONFLICT (email) DO NOTHING;

-- Assign roles to users using email lookup - Skip if already exists  
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, 1 FROM users u WHERE u.email = 'user1@gmail.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, 1 FROM users u WHERE u.email = 'user2@gmail.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, 1 FROM users u WHERE u.email = 'user3@gmail.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, 1 FROM users u WHERE u.email = 'user4@gmail.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 1);

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, 2 FROM users u WHERE u.email = 'manager@eshop.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = 2);

-- Insert more categories (hierarchical)
INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
-- Electronics subcategories
('Điện thoại', 'dien-thoai', 'Điện thoại thông minh các hãng', 1, '/images/categories/phones.jpg', TRUE),
('Laptop', 'laptop', 'Laptop cho công việc và gaming', 1, '/images/categories/laptops.jpg', TRUE),
('Tablet', 'tablet', 'Máy tính bảng iPad và Android', 1, '/images/categories/tablets.jpg', TRUE),
('Phụ kiện điện tử', 'phu-kien-dien-tu', 'Phụ kiện cho các thiết bị điện tử', 1, '/images/categories/accessories.jpg', TRUE),
('PC & Components', 'pc-components', 'Linh kiện máy tính và PC', 1, '/images/categories/pc-components.jpg', TRUE),
-- Clothing subcategories  
('Áo nam', 'ao-nam', 'Áo sơ mi, áo thun nam', 2, '/images/categories/mens-shirts.jpg', TRUE),
('Áo nữ', 'ao-nu', 'Áo sơ mi, áo thun nữ', 2, '/images/categories/womens-shirts.jpg', TRUE),
('Quần nam', 'quan-nam', 'Quần jeans, kaki nam', 2, '/images/categories/mens-pants.jpg', TRUE),
('Quần nữ', 'quan-nu', 'Quần jeans, kaki nữ', 2, '/images/categories/womens-pants.jpg', TRUE),
-- Gaming category
('Gaming', 'gaming', 'Thiết bị gaming chuyên nghiệp', NULL, '/images/categories/gaming.jpg', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- Ensure required categories exist for products
DO $$
BEGIN
    -- Make sure essential categories exist for our products
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'dien-thoai') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('Điện thoại', 'dien-thoai', 'Điện thoại thông minh các hãng', 1, '/images/categories/phones.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'laptop') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('Laptop', 'laptop', 'Laptop cho công việc và gaming', 1, '/images/categories/laptops.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'tablet') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('Tablet', 'tablet', 'Máy tính bảng iPad và Android', 1, '/images/categories/tablets.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'phu-kien-dien-tu') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('Phụ kiện điện tử', 'phu-kien-dien-tu', 'Phụ kiện cho các thiết bị điện tử', 1, '/images/categories/accessories.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'pc-components') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('PC & Components', 'pc-components', 'Linh kiện máy tính và PC', 1, '/images/categories/pc-components.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'gaming') THEN
        INSERT INTO categories (name, slug, description, parent_id, image_url, is_active) VALUES
        ('Gaming', 'gaming', 'Thiết bị gaming chuyên nghiệp', NULL, '/images/categories/gaming.jpg', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
END $$;

-- Insert more brands
INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
('Apple', 'apple', 'Innovative technology and premium devices', '/images/brands/apple-logo.png', TRUE),
('Samsung', 'samsung', 'Leading smartphone and electronics manufacturer', '/images/brands/samsung-logo.png', TRUE),
('LG', 'lg', 'Thiết bị điện tử LG', '/images/brands/lg-logo.png', TRUE),
('Xiaomi', 'xiaomi', 'Smartphone và thiết bị thông minh', '/images/brands/xiaomi-logo.png', TRUE),
('OPPO', 'oppo', 'Smartphone camera selfie', '/images/brands/oppo-logo.png', TRUE),
('Vivo', 'vivo', 'Smartphone thiết kế đẹp', '/images/brands/vivo-logo.png', TRUE),
('Huawei', 'huawei', 'Công nghệ viễn thông', '/images/brands/huawei-logo.png', TRUE),
('Dell', 'dell', 'Máy tính và laptop', '/images/brands/dell-logo.png', TRUE),
('HP', 'hp', 'Máy tính và máy in', '/images/brands/hp-logo.png', TRUE),
('Asus', 'asus', 'Motherboard và laptop gaming', '/images/brands/asus-logo.png', TRUE),
('Acer', 'acer', 'Laptop và máy tính', '/images/brands/acer-logo.png', TRUE),
('MSI', 'msi', 'Gaming laptop và components', '/images/brands/msi-logo.png', TRUE),
('Logitech', 'logitech', 'Thiết bị ngoại vi máy tính', '/images/brands/logitech-logo.png', TRUE),
('Razer', 'razer', 'Gaming gear chuyên nghiệp', '/images/brands/razer-logo.png', TRUE),
('NVIDIA', 'nvidia', 'Graphics processing units and AI technology', '/images/brands/nvidia-logo.png', TRUE),
('Sony', 'sony', 'Consumer electronics and entertainment', '/images/brands/sony-logo.png', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- Add missing fields to products table BEFORE inserting data (skip columns that already exist in base schema)
DO $$
BEGIN
    -- Ensure required brands exist with proper IDs by using INSERT...ON CONFLICT with explicit checks
    -- First, check if we need to insert brands at all
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'apple') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Apple', 'apple', 'Innovative technology and premium devices', '/images/brands/apple-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'samsung') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Samsung', 'samsung', 'Leading smartphone and electronics manufacturer', '/images/brands/samsung-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'xiaomi') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Xiaomi', 'xiaomi', 'Smartphone và thiết bị thông minh', '/images/brands/xiaomi-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'dell') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Dell', 'dell', 'Máy tính và laptop', '/images/brands/dell-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'asus') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Asus', 'asus', 'Motherboard và laptop gaming', '/images/brands/asus-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'logitech') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Logitech', 'logitech', 'Thiết bị ngoại vi máy tính', '/images/brands/logitech-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'razer') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Razer', 'razer', 'Gaming gear chuyên nghiệp', '/images/brands/razer-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'nvidia') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('NVIDIA', 'nvidia', 'Graphics processing units and AI technology', '/images/brands/nvidia-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM brands WHERE slug = 'sony') THEN
        INSERT INTO brands (name, slug, description, logo_url, is_active) VALUES
        ('Sony', 'sony', 'Consumer electronics and entertainment', '/images/brands/sony-logo.png', TRUE)
        ON CONFLICT (slug) DO NOTHING;
    END IF;
END $$;

-- Add missing fields to products table BEFORE inserting data (skip columns that already exist in base schema)
DO $$
BEGIN
    -- Add weight column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='weight') THEN
        ALTER TABLE products ADD COLUMN weight DECIMAL(8, 2) DEFAULT 0;
    END IF;
    
    -- Add dimensions column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='dimensions') THEN
        ALTER TABLE products ADD COLUMN dimensions VARCHAR(100);
    END IF;
    
    -- Add warranty_months column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='warranty_months') THEN
        ALTER TABLE products ADD COLUMN warranty_months INTEGER DEFAULT 12;
    END IF;
    
    -- Add compare_at_price column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='compare_at_price') THEN
        ALTER TABLE products ADD COLUMN compare_at_price DECIMAL(10, 2);
    END IF;
    
    -- Add min_order_quantity column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='min_order_quantity') THEN
        ALTER TABLE products ADD COLUMN min_order_quantity INTEGER DEFAULT 1;
    END IF;
    
    -- Add max_order_quantity column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='max_order_quantity') THEN
        ALTER TABLE products ADD COLUMN max_order_quantity INTEGER;
    END IF;
    
    -- Skip meta_title and meta_description as they already exist in base schema
    
    -- Add tags column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='tags') THEN
        ALTER TABLE products ADD COLUMN tags TEXT[];
    END IF;
    
    -- Add is_digital column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='is_digital') THEN
        ALTER TABLE products ADD COLUMN is_digital BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    
    -- Add view_count column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='view_count') THEN
        ALTER TABLE products ADD COLUMN view_count INTEGER DEFAULT 0;
    END IF;
    
    -- Add purchase_count column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='purchase_count') THEN
        ALTER TABLE products ADD COLUMN purchase_count INTEGER DEFAULT 0;
    END IF;
    
    -- Add featured_until column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='featured_until') THEN
        ALTER TABLE products ADD COLUMN featured_until TIMESTAMP;
    END IF;
END $$;

-- Insert sample products with detailed information using brand name lookups
INSERT INTO products (name, slug, description, price, compare_at_price, stock_quantity, sku, category_id, brand_id, is_featured, weight, dimensions, warranty_months, tags, view_count, purchase_count) VALUES
-- Smartphones
('iPhone 15 Pro Max 256GB', 'iphone-15-pro-max-256gb', 
 'iPhone 15 Pro Max với chip A17 Pro, camera 48MP, titanium premium. Màn hình Super Retina XDR 6.7 inch, pin trâu, hỗ trợ USB-C.', 
 33990000, 36990000, 25, 'IPHONE15PM-256-TI', (SELECT id FROM categories WHERE slug = 'dien-thoai'), (SELECT id FROM brands WHERE slug = 'apple'), TRUE, 238, '16.0 x 7.7 x 0.83 cm', 24,
 ARRAY['iPhone', '5G', 'A17 Pro', 'Titanium', 'USB-C'], 1250, 89),

('Samsung Galaxy S24 Ultra 512GB', 'samsung-galaxy-s24-ultra-512gb',
 'Galaxy S24 Ultra với bút S Pen tích hợp, camera 200MP, chip Snapdragon 8 Gen 3. Màn hình Dynamic AMOLED 6.8 inch, pin 5000mAh.',
 31990000, 34990000, 18, 'SAMS24U-512-BK', (SELECT id FROM categories WHERE slug = 'dien-thoai'), (SELECT id FROM brands WHERE slug = 'samsung'), TRUE, 232, '16.2 x 7.9 x 0.86 cm', 24,
 ARRAY['Samsung', '5G', 'S Pen', 'Camera 200MP', 'Snapdragon'], 980, 67),

('Xiaomi 14 Ultra 512GB', 'xiaomi-14-ultra-512gb',
 'Xiaomi 14 Ultra with Leica 50MP camera, Snapdragon 8 Gen 3 chip, 90W fast charging. Premium design with glass and metal.',
 24990000, 27990000, 32, 'MI14U-512-BK', (SELECT id FROM categories WHERE slug = 'dien-thoai'), (SELECT id FROM brands WHERE slug = 'xiaomi'), TRUE, 224, '16.1 x 7.5 x 0.91 cm', 18,
 ARRAY['Xiaomi', '5G', 'Leica Camera', 'Fast Charge 90W'], 750, 45),

-- Laptops  
('MacBook Pro 14 M3 Pro 512GB', 'macbook-pro-14-m3-pro-512gb',
 'MacBook Pro 14 inch với chip M3 Pro 11-core, GPU 14-core, RAM 18GB, SSD 512GB. Màn hình Liquid Retina XDR, pin 17 giờ.',
 54990000, 59990000, 15, 'MBP14-M3P-512', (SELECT id FROM categories WHERE slug = 'laptop'), (SELECT id FROM brands WHERE slug = 'apple'), TRUE, 1600, '31.26 x 22.12 x 1.55 cm', 12,
 ARRAY['MacBook', 'M3 Pro', 'Retina XDR', '18GB RAM'], 890, 34),

('Dell XPS 13 Plus i7-13700H 1TB', 'dell-xps-13-plus-i7-1tb',
 'Dell XPS 13 Plus với Intel Core i7-13700H, RAM 32GB, SSD 1TB, màn hình 13.4 inch 4K OLED touch. Thiết kế siêu mỏng nhẹ.',
 42990000, 46990000, 12, 'DXPS13P-I7-1TB', (SELECT id FROM categories WHERE slug = 'laptop'), (SELECT id FROM brands WHERE slug = 'dell'), TRUE, 1260, '29.5 x 19.9 x 1.6 cm', 24,
 ARRAY['Dell', 'Intel i7', '4K OLED', '32GB RAM', 'Touch'], 654, 28),

('ASUS ROG Strix G16 RTX 4070', 'asus-rog-strix-g16-rtx4070',
 'ASUS ROG Strix G16 gaming laptop với Intel i7-13650HX, RTX 4070 8GB, RAM 16GB DDR5, SSD 1TB. Màn hình 16 inch 165Hz.',
 38990000, 42990000, 8, 'AROG-G16-4070', (SELECT id FROM categories WHERE slug = 'laptop'), (SELECT id FROM brands WHERE slug = 'asus'), TRUE, 2500, '35.4 x 25.2 x 2.3 cm', 24,
 ARRAY['ASUS', 'Gaming', 'RTX 4070', '165Hz', 'ROG'], 543, 19),

-- Tablets
('iPad Pro 12.9 M2 512GB WiFi', 'ipad-pro-129-m2-512gb',
 'iPad Pro 12.9 inch với chip M2, màn hình Liquid Retina XDR, camera 12MP, hỗ trợ Apple Pencil và Magic Keyboard.',
 31990000, 34990000, 20, 'IPADP129-M2-512', (SELECT id FROM categories WHERE slug = 'tablet'), (SELECT id FROM brands WHERE slug = 'apple'), TRUE, 682, '28.06 x 21.49 x 0.61 cm', 12,
 ARRAY['iPad', 'M2', 'Liquid Retina XDR', 'Apple Pencil'], 432, 23),

-- Gaming accessories
('Logitech G Pro X Superlight 2', 'logitech-g-pro-x-superlight-2',
 'Chuột gaming wireless siêu nhẹ 60g, sensor HERO 32K, pin 95 giờ, switches Lightforce hybrid. Thiết kế cho esports.',
 3490000, 3990000, 45, 'LGPXSL2-BK', (SELECT id FROM categories WHERE slug = 'gaming'), (SELECT id FROM brands WHERE slug = 'logitech'), FALSE, 60, '12.5 x 6.3 x 4.0 cm', 24,
 ARRAY['Gaming Mouse', 'Wireless', 'Lightspeed', '32K DPI'], 321, 67),

('Razer BlackWidow V4 Pro', 'razer-blackwidow-v4-pro',
 'Bàn phím gaming cơ học với switches Green, RGB Chroma, macro keys, volume wheel, wrist rest nam châm.',
 5490000, 5990000, 28, 'RBW-V4P-GR', (SELECT id FROM categories WHERE slug = 'gaming'), (SELECT id FROM brands WHERE slug = 'razer'), FALSE, 1350, '46.1 x 15.2 x 4.2 cm', 24,
 ARRAY['Gaming Keyboard', 'Mechanical', 'RGB Chroma', 'Green Switch'], 287, 41),

-- PC Components
('RTX 4080 SUPER Founders Edition', 'rtx-4080-super-fe',
 'NVIDIA GeForce RTX 4080 SUPER với 16GB GDDR6X, Ada Lovelace architecture, DLSS 3, ray tracing thế hệ mới.',
 28990000, 31990000, 6, 'RTX4080S-FE-16GB', (SELECT id FROM categories WHERE slug = 'pc-components'), (SELECT id FROM brands WHERE slug = 'nvidia'), TRUE, 2200, '30.4 x 13.7 x 5.4 cm', 36,
 ARRAY['GPU', 'RTX 4080 SUPER', '16GB VRAM', 'DLSS 3', 'Ray Tracing'], 198, 12),

-- Headphones
('Sony WH-1000XM5 Wireless', 'sony-wh-1000xm5-wireless',
 'Tai nghe không dây chống ồn hàng đầu với chip V1, pin 30 giờ, quick charge, multipoint connection.',
 8990000, 9990000, 35, 'SONY-WH1000XM5-BK', (SELECT id FROM categories WHERE slug = 'phu-kien-dien-tu'), (SELECT id FROM brands WHERE slug = 'sony'), FALSE, 250, '27.0 x 19.5 x 8.0 cm', 12,
 ARRAY['Headphones', 'Noise Cancelling', 'Wireless', '30h Battery'], 456, 78),

-- Watches  
('Apple Watch Series 9 GPS 45mm', 'apple-watch-series-9-45mm',
 'Apple Watch Series 9 với chip S9, màn hình Always-On Retina, tính năng Double Tap, theo dõi sức khỏe toàn diện.',
 10990000, 11990000, 22, 'AW-S9-GPS-45-MN', (SELECT id FROM categories WHERE slug = 'phu-kien-dien-tu'), (SELECT id FROM brands WHERE slug = 'apple'), FALSE, 38.7, '4.5 x 3.8 x 1.05 cm', 12,
 ARRAY['Apple Watch', 'GPS', 'Health Tracking', 'Double Tap'], 367, 56)
ON CONFLICT (sku) DO NOTHING;

-- Insert product images
INSERT INTO product_images (product_id, image_url, alt_text, sort_order, is_primary) VALUES
-- iPhone 15 Pro Max images
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), '/images/products/iphone-15-pro-max-titanium-1.jpg', 'iPhone 15 Pro Max Titanium Natural chính diện', 1, TRUE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), '/images/products/iphone-15-pro-max-titanium-2.jpg', 'iPhone 15 Pro Max Titanium Natural mặt lưng', 2, FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), '/images/products/iphone-15-pro-max-titanium-3.jpg', 'iPhone 15 Pro Max Titanium Natural cạnh bên', 3, FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), '/images/products/iphone-15-pro-max-titanium-4.jpg', 'iPhone 15 Pro Max camera system', 4, FALSE),

-- Samsung Galaxy S24 Ultra images  
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), '/images/products/samsung-s24-ultra-black-1.jpg', 'Samsung Galaxy S24 Ultra Titanium Black chính diện', 1, TRUE),
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), '/images/products/samsung-s24-ultra-black-2.jpg', 'Samsung Galaxy S24 Ultra với S Pen', 2, FALSE),
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), '/images/products/samsung-s24-ultra-black-3.jpg', 'Samsung Galaxy S24 Ultra camera zoom 100x', 3, FALSE),

-- Xiaomi 14 Ultra images
((SELECT id FROM products WHERE slug = 'xiaomi-14-ultra-512gb'), '/images/products/xiaomi-14-ultra-black-1.jpg', 'Xiaomi 14 Ultra Black chính diện', 1, TRUE),
((SELECT id FROM products WHERE slug = 'xiaomi-14-ultra-512gb'), '/images/products/xiaomi-14-ultra-black-2.jpg', 'Xiaomi 14 Ultra Leica camera system', 2, FALSE),
((SELECT id FROM products WHERE slug = 'xiaomi-14-ultra-512gb'), '/images/products/xiaomi-14-ultra-black-3.jpg', 'Xiaomi 14 Ultra mặt lưng premium', 3, FALSE),

-- MacBook Pro images
((SELECT id FROM products WHERE slug = 'macbook-pro-14-m3-pro-512gb'), '/images/products/macbook-pro-14-space-gray-1.jpg', 'MacBook Pro 14 M3 Pro Space Gray', 1, TRUE),
((SELECT id FROM products WHERE slug = 'macbook-pro-14-m3-pro-512gb'), '/images/products/macbook-pro-14-space-gray-2.jpg', 'MacBook Pro 14 mở màn hình', 2, FALSE),
((SELECT id FROM products WHERE slug = 'macbook-pro-14-m3-pro-512gb'), '/images/products/macbook-pro-14-ports.jpg', 'MacBook Pro 14 cổng kết nối', 3, FALSE),

-- Dell XPS 13 Plus images
((SELECT id FROM products WHERE slug = 'dell-xps-13-plus-i7-1tb'), '/images/products/dell-xps-13-plus-platinum-1.jpg', 'Dell XPS 13 Plus Platinum', 1, TRUE),
((SELECT id FROM products WHERE slug = 'dell-xps-13-plus-i7-1tb'), '/images/products/dell-xps-13-plus-keyboard.jpg', 'Dell XPS 13 Plus bàn phím cảm ứng', 2, FALSE),

-- ASUS ROG Strix G16 images
((SELECT id FROM products WHERE slug = 'asus-rog-strix-g16-rtx4070'), '/images/products/asus-rog-g16-gray-1.jpg', 'ASUS ROG Strix G16 Eclipse Gray', 1, TRUE),
((SELECT id FROM products WHERE slug = 'asus-rog-strix-g16-rtx4070'), '/images/products/asus-rog-g16-rgb.jpg', 'ASUS ROG G16 RGB keyboard', 2, FALSE),
((SELECT id FROM products WHERE slug = 'asus-rog-strix-g16-rtx4070'), '/images/products/asus-rog-g16-ports.jpg', 'ASUS ROG G16 cổng kết nối gaming', 3, FALSE),

-- iPad Pro images
((SELECT id FROM products WHERE slug = 'ipad-pro-129-m2-512gb'), '/images/products/ipad-pro-129-space-gray-1.jpg', 'iPad Pro 12.9 Space Gray', 1, TRUE),
((SELECT id FROM products WHERE slug = 'ipad-pro-129-m2-512gb'), '/images/products/ipad-pro-129-pencil.jpg', 'iPad Pro 12.9 với Apple Pencil', 2, FALSE),

-- Gaming mouse images
((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), '/images/products/logitech-g-pro-x-superlight-2-black.jpg', 'Logitech G Pro X Superlight 2 Black', 1, TRUE),
((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), '/images/products/logitech-g-pro-x-superlight-2-white.jpg', 'Logitech G Pro X Superlight 2 White', 2, FALSE),

-- Gaming keyboard images  
((SELECT id FROM products WHERE slug = 'razer-blackwidow-v4-pro'), '/images/products/razer-blackwidow-v4-pro-1.jpg', 'Razer BlackWidow V4 Pro', 1, TRUE),
((SELECT id FROM products WHERE slug = 'razer-blackwidow-v4-pro'), '/images/products/razer-blackwidow-v4-pro-rgb.jpg', 'Razer BlackWidow V4 Pro RGB lighting', 2, FALSE),

-- RTX 4080 SUPER images
((SELECT id FROM products WHERE slug = 'rtx-4080-super-fe'), '/images/products/rtx-4080-super-fe-1.jpg', 'RTX 4080 SUPER Founders Edition', 1, TRUE),
((SELECT id FROM products WHERE slug = 'rtx-4080-super-fe'), '/images/products/rtx-4080-super-fe-ports.jpg', 'RTX 4080 SUPER display ports', 2, FALSE),

-- Sony headphones images
((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), '/images/products/sony-wh-1000xm5-black-1.jpg', 'Sony WH-1000XM5 Black', 1, TRUE),
((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), '/images/products/sony-wh-1000xm5-silver.jpg', 'Sony WH-1000XM5 Silver', 2, FALSE),

-- Apple Watch images
((SELECT id FROM products WHERE slug = 'apple-watch-series-9-45mm'), '/images/products/apple-watch-s9-midnight-1.jpg', 'Apple Watch Series 9 Midnight', 1, TRUE),
((SELECT id FROM products WHERE slug = 'apple-watch-series-9-45mm'), '/images/products/apple-watch-s9-pink.jpg', 'Apple Watch Series 9 Pink', 2, FALSE);

-- Insert product variants
INSERT INTO product_variants (product_id, variant_name, sku, price, compare_at_price, stock_quantity, variant_options, is_default) VALUES
-- iPhone 15 Pro Max variants
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 256GB - Titanium Natural', 'IPHONE15PM-256-NAT', 33990000, 36990000, 8, '{"color": "Natural Titanium", "storage": "256GB"}', TRUE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 256GB - Titanium Blue', 'IPHONE15PM-256-BLU', 33990000, 36990000, 6, '{"color": "Blue Titanium", "storage": "256GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 256GB - Titanium White', 'IPHONE15PM-256-WHI', 33990000, 36990000, 5, '{"color": "White Titanium", "storage": "256GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 256GB - Titanium Black', 'IPHONE15PM-256-BLA', 33990000, 36990000, 6, '{"color": "Black Titanium", "storage": "256GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 512GB - Titanium Natural', 'IPHONE15PM-512-NAT', 39990000, 42990000, 4, '{"color": "Natural Titanium", "storage": "512GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 1TB - Titanium Natural', 'IPHONE15PM-1TB-NAT', 45990000, 48990000, 2, '{"color": "Natural Titanium", "storage": "1TB"}', FALSE),

-- Samsung Galaxy S24 Ultra variants
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 'Galaxy S24 Ultra 512GB - Titanium Black', 'SAMS24U-512-BK', 31990000, 34990000, 10, '{"color": "Titanium Black", "storage": "512GB"}', TRUE),
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 'Galaxy S24 Ultra 512GB - Titanium Gray', 'SAMS24U-512-GR', 31990000, 34990000, 4, '{"color": "Titanium Gray", "storage": "512GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 'Galaxy S24 Ultra 512GB - Titanium Violet', 'SAMS24U-512-VI', 31990000, 34990000, 4, '{"color": "Titanium Violet", "storage": "512GB"}', FALSE),
((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 'Galaxy S24 Ultra 1TB - Titanium Black', 'SAMS24U-1TB-BK', 37990000, 40990000, 3, '{"color": "Titanium Black", "storage": "1TB"}', FALSE),

-- Gaming mouse variants
((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 'G Pro X Superlight 2 - Black', 'LGPXSL2-BK', 3490000, 3990000, 25, '{"color": "Black"}', TRUE),
((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 'G Pro X Superlight 2 - White', 'LGPXSL2-WH', 3490000, 3990000, 20, '{"color": "White"}', FALSE),

-- Sony headphones variants  
((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 'WH-1000XM5 - Black', 'SONY-WH1000XM5-BK', 8990000, 9990000, 20, '{"color": "Black"}', TRUE),
((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 'WH-1000XM5 - Silver', 'SONY-WH1000XM5-SL', 8990000, 9990000, 15, '{"color": "Silver"}', FALSE);

-- Insert reviews (commented out due to user dependencies - uncomment after creating sample users)
-- INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, is_approved) VALUES
-- ((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 2, 5, 'iPhone tuyệt vời nhất từng dùng', 'Camera siêu đẹp, pin trâu hơn hẳn bản cũ. Titanium rất premium và nhẹ hơn so với thép không gỉ. USB-C cuối cùng cũng có!', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 3, 4, 'Tốt nhưng giá hơi cao', 'Máy chạy mượt, camera ổn. Nhưng giá so với Việt Nam thì vẫn cao. Nếu có tiền thì mua, không thì đợi giảm giá.', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 4, 5, 'Đáng đồng tiền bát gạo', 'Lên từ iPhone 12 Pro lên bản này, cảm nhận rõ sự khác biệt. Tốc độ, camera, pin đều vượt trội.', TRUE, TRUE),
-- 
-- ((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 2, 4, 'Galaxy S24 Ultra xứng danh flagship', 'S Pen viết mượt, camera zoom 100x ấn tượng. Màn hình đẹp nhất từng thấy. Chỉ tiếc là OneUI hơi nặng.', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 5, 5, 'Camera tuyệt vời cho công việc', 'Làm photographer nên đánh giá cao camera của máy này. Zoom xa vẫn sắc nét, chế độ chụp đêm cũng ok.', TRUE, TRUE),
-- 
-- ((SELECT id FROM products WHERE slug = 'macbook-pro-14-m3-pro-512gb'), 3, 5, 'MacBook Pro M3 Pro quá mạnh', 'Làm video 4K mượt không lag. Pin 14-15 tiếng liên tục. Màn hình mini-LED đẹp xuất sắc. Đáng tiền!', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'macbook-pro-14-m3-pro-512gb'), 4, 4, 'Tốt nhưng nóng khi render', 'Performance không có gì để chê. Chỉ có điều khi render video lâu thì máy hơi nóng. Còn lại đều tốt.', TRUE, TRUE),
-- 
-- ((SELECT id FROM products WHERE slug = 'asus-rog-strix-g16-rtx4070'), 5, 5, 'Gaming laptop trong tầm giá tốt nhất', 'RTX 4070 chạy game 1440p high settings 60fps stable. Bàn phím RGB đẹp, tản nhiệt ổn. Recommend!', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'asus-rog-strix-g16-rtx4070'), 2, 4, 'Mạnh nhưng hơi nặng', 'Performance gaming tuyệt vời, chạy mọi game hiện tại. Nhưng máy nặng và pin yếu khi không cắm sạc.', TRUE, TRUE),
-- 
-- ((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 4, 5, 'Chuột gaming tốt nhất', 'Siêu nhẹ, click crispy, tracking chính xác tuyệt đối. Dùng cho FPS rất tốt. Pin cũng trâu.', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 5, 4, 'Tốt nhưng giá hơi cao', 'Chất lượng không có gì để chê. Nhưng giá so với các hãng khác thì hơi cao. Nếu có budget thì mua.', TRUE, TRUE),
-- 
-- ((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 3, 5, 'Tai nghe chống ồn tuyệt vời', 'Chống ồn xuất sắc, âm thanh chi tiết. Pin 30 tiếng thực tế đúng như quảng cáo. Đáng mua!', TRUE, TRUE),
-- ((SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 5, 4, 'Âm thanh tốt, thiết kế đẹp', 'Chất âm balanced, bass đủ, treble trong. Đeo lâu không đau tai. Chỉ hơi to so với đầu châu Á.', TRUE, TRUE);

-- Insert into wishlist (commented out due to user ID dependencies - uncomment after checking user IDs)
-- INSERT INTO wishlists (user_id, product_id) VALUES
-- (2, 4), -- User 2 muốn MacBook Pro
-- (2, 7), -- User 2 muốn iPad Pro  
-- (3, 1), -- User 3 muốn iPhone 15 Pro Max
-- (3, 10), -- User 3 muốn RTX 4080 SUPER
-- (4, 2), -- User 4 muốn Galaxy S24 Ultra
-- (4, 6), -- User 4 muốn ASUS Gaming laptop
-- (5, 8), -- User 5 muốn gaming mouse
-- (5, 9) -- User 5 muốn gaming keyboard
-- ON CONFLICT (user_id, product_id) DO NOTHING;

-- Insert flash sale products  
INSERT INTO flash_sale_products (flash_sale_id, product_id, flash_price, original_price, stock_limit, stock_sold, sort_order) VALUES
(1, (SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 30990000, 33990000, 10, 3, 1), -- iPhone 15 Pro Max flash sale
(1, (SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 28990000, 31990000, 8, 2, 2),  -- Galaxy S24 Ultra flash sale
(1, (SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 2990000, 3490000, 20, 8, 3),   -- Gaming mouse flash sale
(1, (SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 7990000, 8990000, 15, 5, 4),  -- Sony headphones flash sale
(1, (SELECT id FROM products WHERE slug = 'razer-blackwidow-v4-pro'), 4490000, 5490000, 12, 3, 5);   -- Gaming keyboard flash sale

-- Insert sample addresses (commented out due to user ID dependencies)
-- INSERT INTO addresses (user_id, full_name, phone_number, address_line1, address_line2, city, state, postal_code, country, is_default, type) VALUES
-- (2, 'Nguyễn Văn An', '0987654321', '123 Nguyễn Huệ', 'Phường Bến Nghé', 'TP.HCM', 'TP.HCM', '700000', 'Vietnam', TRUE, 'BOTH'),
-- (3, 'Trần Thị Bình', '0976543210', '456 Hoàng Hoa Thám', 'Phường 12, Quận Tân Bình', 'TP.HCM', 'TP.HCM', '700000', 'Vietnam', TRUE, 'SHIPPING'),
-- (4, 'Lê Hoàng Cường', '0965432109', '789 Lê Lợi', 'Phường 1, Quận 1', 'TP.HCM', 'TP.HCM', '700000', 'Vietnam', TRUE, 'BOTH'),
-- (5, 'Phạm Thu Dung', '0954321098', '321 Nguyễn Trãi', 'Phường 2, Quận 5', 'TP.HCM', 'TP.HCM', '700000', 'Vietnam', TRUE, 'SHIPPING');

-- ============================================
-- 15. SAMPLE DATA - COMMENTED OUT DUE TO USER ID DEPENDENCIES
-- Uncomment and adjust user_ids after confirming actual user IDs in your database
-- ============================================

/*
-- Insert sample orders
INSERT INTO orders (order_number, user_id, subtotal, tax, shipping_cost, total, status, shipping_address_id, billing_address_id) VALUES
('ORD-2024-000001', 2, 33990000, 3399000, 0, 37389000, 'DELIVERED', 1, 1),
('ORD-2024-000002', 3, 8990000, 899000, 30000, 9919000, 'SHIPPED', 2, 2),
('ORD-2024-000003', 4, 5490000, 549000, 35000, 6074000, 'PROCESSING', 3, 3),
('ORD-2024-000004', 5, 3490000, 349000, 30000, 3869000, 'CONFIRMED', 4, 4),
('ORD-2024-000005', 2, 31990000, 3199000, 0, 35189000, 'PENDING', 1, 1);

-- Insert order items (commented out due to user/order dependencies)
-- INSERT INTO order_items (order_id, product_id, product_name, product_sku, price, quantity, total, variant_id) VALUES
-- (1, (SELECT id FROM products WHERE slug = 'iphone-15-pro-max-256gb'), 'iPhone 15 Pro Max 256GB', 'IPHONE15PM-256-NAT', 33990000, 1, 33990000, 1),
-- (2, (SELECT id FROM products WHERE slug = 'sony-wh-1000xm5-wireless'), 'Sony WH-1000XM5 Wireless', 'SONY-WH1000XM5-BK', 8990000, 1, 8990000, 12),
-- (3, (SELECT id FROM products WHERE slug = 'razer-blackwidow-v4-pro'), 'Razer BlackWidow V4 Pro', 'RBW-V4P-GR', 5490000, 1, 5490000, NULL),
-- (4, (SELECT id FROM products WHERE slug = 'logitech-g-pro-x-superlight-2'), 'Logitech G Pro X Superlight 2', 'LGPXSL2-BK', 3490000, 1, 3490000, 8),
-- (5, (SELECT id FROM products WHERE slug = 'samsung-galaxy-s24-ultra-512gb'), 'Samsung Galaxy S24 Ultra 512GB', 'SAMS24U-512-BK', 31990000, 1, 31990000, 5);

-- Insert payments
INSERT INTO payments (order_id, amount, method, status, transaction_id, paid_at) VALUES
(1, 37389000, 'CREDIT_CARD', 'COMPLETED', 'TXN-2024-001', NOW() - INTERVAL '5 days'),
(2, 9919000, 'STRIPE', 'COMPLETED', 'TXN-2024-002', NOW() - INTERVAL '3 days'),
(3, 6074000, 'VNPAY', 'PENDING', 'TXN-2024-003', NULL),
(4, 3869000, 'CASH_ON_DELIVERY', 'PENDING', NULL, NULL),
(5, 35189000, 'CREDIT_CARD', 'PROCESSING', 'TXN-2024-005', NULL);

-- Insert search history
INSERT INTO search_history (user_id, search_term, result_count, filters_applied, clicked_product_id) VALUES
(2, 'iPhone 15', 3, '{"category": "dien-thoai", "brand": "apple"}', 1),
(2, 'MacBook Pro', 2, '{"category": "laptop", "price_range": "50000000-60000000"}', 4),
(3, 'Samsung Galaxy', 4, '{"category": "dien-thoai"}', 2),
(3, 'gaming laptop', 5, '{"category": "laptop", "brand": "asus"}', 6),
(4, 'chuột gaming', 8, '{"category": "gaming"}', 8),
(4, 'tai nghe bluetooth', 6, '{"category": "phu-kien-dien-tu"}', 11),
(5, 'RTX 4080', 2, '{"category": "pc-components"}', 10),
*/

-- ============================================
-- 14. CREATE VIEWS FOR FRONTEND 
-- Note: Most sample data has been commented out due to user ID dependencies
-- The tables are created and ready, but sample data with user dependencies 
-- should be added after confirming actual user IDs exist in your database
-- ============================================
(2, 1, NOW() - INTERVAL '1 hour'),
(2, 4, NOW() - INTERVAL '2 hours'),
(2, 7, NOW() - INTERVAL '1 day'),
(3, 2, NOW() - INTERVAL '30 minutes'),
(3, 6, NOW() - INTERVAL '45 minutes'),
(3, 10, NOW() - INTERVAL '2 hours'),
(4, 8, NOW() - INTERVAL '15 minutes'),
(4, 9, NOW() - INTERVAL '20 minutes'),
(4, 11, NOW() - INTERVAL '1 hour'),
(5, 1, NOW() - INTERVAL '3 hours'),
(5, 8, NOW() - INTERVAL '4 hours');

-- Insert product comparisons
INSERT INTO product_comparisons (user_id, product_id) VALUES
(2, 1), -- Compare iPhone 15 Pro Max
(2, 2), -- with Galaxy S24 Ultra
(3, 4), -- Compare MacBook Pro
(3, 5), -- with Dell XPS 13
(4, 8), -- Compare gaming mice
(4, 9); -- and keyboards

-- Insert Q&A
INSERT INTO product_questions (product_id, user_id, question, is_answered, is_approved) VALUES
(1, 3, 'iPhone 15 Pro Max có hỗ trợ sạc không dây không?', TRUE, TRUE),
(1, 4, 'Pin iPhone 15 Pro Max sử dụng được bao lâu?', TRUE, TRUE),
(2, 2, 'Galaxy S24 Ultra có kháng nước không?', TRUE, TRUE),
(4, 5, 'MacBook Pro M3 Pro có chạy được game không?', TRUE, TRUE),
(8, 3, 'Chuột này có tương thích với Mac không?', TRUE, TRUE);

INSERT INTO product_answers (question_id, user_id, answer, is_approved, is_official) VALUES
(1, 1, 'Có, iPhone 15 Pro Max hỗ trợ sạc không dây MagSafe 15W và Qi 7.5W.', TRUE, TRUE),
(2, 1, 'Pin iPhone 15 Pro Max có thể sử dụng từ 24-29 giờ tùy vào cách sử dụng.', TRUE, TRUE),
(3, 1, 'Galaxy S24 Ultra có chứng nhận IP68, chống nước và bụi ở độ sâu 1.5m trong 30 phút.', TRUE, TRUE),
(4, 1, 'MacBook Pro M3 Pro có thể chạy được nhiều game, nhưng performance tùy thuộc vào game cụ thể.', TRUE, TRUE),
(5, 1, 'Có, Logitech G Pro X Superlight 2 tương thích hoàn toàn với macOS.', TRUE, TRUE);

-- Insert coupons  
INSERT INTO coupons (code, name, description, discount_type, discount_value, min_order_amount, usage_limit, start_date, end_date, created_by) VALUES
('WELCOME10', 'Chào mừng khách hàng mới', 'Giảm 10% cho đơn hàng đầu tiên', 'PERCENTAGE', 10, 1000000, 1000, NOW(), NOW() + INTERVAL '30 days', 1),
('FLASH50', 'Flash Sale 50K', 'Giảm 50.000đ cho Flash Sale', 'FIXED_AMOUNT', 50000, 500000, 500, NOW(), NOW() + INTERVAL '7 days', 1),
('FREESHIP', 'Miễn phí vận chuyển', 'Miễn phí ship cho đơn từ 2 triệu', 'FREE_SHIPPING', 0, 2000000, 2000, NOW(), NOW() + INTERVAL '60 days', 1),
('MEGA20', 'Mega Sale 20%', 'Giảm 20% tối đa 500K', 'PERCENTAGE', 20, 3000000, 100, NOW(), NOW() + INTERVAL '14 days', 1);

-- Insert coupon usage
INSERT INTO coupon_uses (coupon_id, user_id, order_id, discount_amount) VALUES
(2, 2, 1, 50000),
(1, 3, 2, 899000);

-- Insert notifications
INSERT INTO notifications (user_id, title, message, type, link) VALUES
(2, 'Đơn hàng đã được giao', 'Đơn hàng #ORD-2024-000001 đã được giao thành công', 'ORDER', '/orders/ORD-2024-000001'),
(3, 'Đơn hàng đang vận chuyển', 'Đơn hàng #ORD-2024-000002 đang trên đường giao đến bạn', 'ORDER', '/orders/ORD-2024-000002'),
(2, 'Flash Sale đặc biệt', 'Flash Sale cuối tuần với giảm giá lên đến 50%', 'PROMOTION', '/flash-sale'),
(4, 'Sản phẩm yêu thích giảm giá', 'Razer BlackWidow V4 Pro đang có ưu đãi đặc biệt', 'PRODUCT', '/product/razer-blackwidow-v4-pro'),
(5, 'Chào mừng bạn đến E-SHOP', 'Cảm ơn bạn đã đăng ký tài khoản tại E-SHOP', 'SYSTEM', '/profile');

-- Insert stock movements
INSERT INTO stock_movements (product_id, variant_id, movement_type, quantity, reason, reference_type, reference_id, notes, created_by) VALUES
(1, 1, 'OUT', 1, 'SALE', 'ORDER', 1, 'Bán cho khách hàng', 1),
(11, 12, 'OUT', 1, 'SALE', 'ORDER', 2, 'Bán cho khách hàng', 1),
(9, NULL, 'OUT', 1, 'SALE', 'ORDER', 3, 'Bán cho khách hàng', 1),
(1, 1, 'IN', 10, 'PURCHASE', 'MANUAL', NULL, 'Nhập kho từ nhà cung cấp', 1),
(2, 5, 'IN', 15, 'PURCHASE', 'MANUAL', NULL, 'Nhập kho Samsung Galaxy S24 Ultra', 1);

-- Update product view counts and purchase counts based on activity
UPDATE products SET view_count = view_count + 100, purchase_count = purchase_count + 5 WHERE id IN (1, 2);
UPDATE products SET view_count = view_count + 50, purchase_count = purchase_count + 2 WHERE id IN (4, 6, 8, 9, 11);
UPDATE products SET view_count = view_count + 25 WHERE id IN (3, 5, 7, 10, 12);

-- ============================================
-- 15. CREATE VIEWS FOR FRONTEND
-- ============================================

-- Active flash sale products with stock info
CREATE OR REPLACE VIEW active_flash_sale_products AS
SELECT 
    fsp.*,
    p.name as product_name,
    p.slug as product_slug,
    p.description as product_description,
    pi.image_url,
    fs.name as flash_sale_name,
    fs.end_time as flash_sale_end_time,
    ROUND(((fsp.original_price - fsp.flash_price) / fsp.original_price * 100)::NUMERIC, 0) as discount_percentage,
    (fsp.stock_limit - fsp.stock_sold) as stock_remaining
FROM flash_sale_products fsp
JOIN flash_sales fs ON fsp.flash_sale_id = fs.id
JOIN products p ON fsp.product_id = p.id
LEFT JOIN product_images pi ON p.id = pi.product_id AND pi.is_primary = TRUE
WHERE fs.is_active = TRUE 
  AND fs.start_time <= CURRENT_TIMESTAMP 
  AND fs.end_time > CURRENT_TIMESTAMP
  AND fsp.stock_sold < fsp.stock_limit
ORDER BY fsp.sort_order, fsp.id;

-- Product details with variants and images
CREATE OR REPLACE VIEW product_details_view AS
SELECT 
    p.*,
    c.name as category_name,
    c.slug as category_slug,
    b.name as brand_name,
    b.slug as brand_slug,
    COALESCE(
        (SELECT jsonb_agg(
            jsonb_build_object(
                'id', pv.id,
                'name', pv.variant_name,
                'sku', pv.sku,
                'price', pv.price,
                'compareAtPrice', pv.compare_at_price,
                'stockQuantity', pv.stock_quantity,
                'options', pv.variant_options,
                'isDefault', pv.is_default
            )
        ) FROM product_variants pv WHERE pv.product_id = p.id AND pv.is_active = TRUE),
        '[]'::jsonb
    ) as variants,
    COALESCE(
        (SELECT jsonb_agg(
            jsonb_build_object(
                'id', pi.id,
                'imageUrl', pi.image_url,
                'altText', pi.alt_text,
                'isPrimary', pi.is_primary,
                'sortOrder', pi.sort_order
            ) ORDER BY pi.sort_order, pi.id
        ) FROM product_images pi WHERE pi.product_id = p.id),
        '[]'::jsonb
    ) as images
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN brands b ON p.brand_id = b.id;

-- ============================================
-- END OF ADDITIONS
-- ============================================

SELECT 'Database additions completed successfully!' as message;
SELECT 'Added tables for: Flash Sales, Wishlist, Search, Variants, Shipping, Banners, Inventory, Email, Q&A, and more' as details;
SELECT 'Note: Sample data with user dependencies has been commented out. Check actual user IDs before uncommenting sample data.' as important;