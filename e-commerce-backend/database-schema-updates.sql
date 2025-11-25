-- ============================================
-- DATABASE SCHEMA UPDATES
-- Updates for entity refactoring compatibility
-- ============================================

-- Update products table to match Product entity
ALTER TABLE products ADD COLUMN IF NOT EXISTS cost_price DECIMAL(10, 2);
ALTER TABLE products ADD COLUMN IF NOT EXISTS weight DECIMAL(8, 3);
ALTER TABLE products ADD COLUMN IF NOT EXISTS dimensions VARCHAR(100);

-- Add constraints for new fields
ALTER TABLE products ADD CONSTRAINT product_cost_price_valid CHECK (cost_price IS NULL OR cost_price >= 0);
ALTER TABLE products ADD CONSTRAINT product_weight_valid CHECK (weight IS NULL OR weight >= 0);

-- ============================================
-- FLASH SALES SYSTEM
-- ============================================

-- Table: flash_sales
CREATE TABLE IF NOT EXISTS flash_sales (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT flash_sale_discount_valid CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    CONSTRAINT flash_sale_time_valid CHECK (end_time > start_time)
);

-- Table: flash_sale_products
CREATE TABLE IF NOT EXISTS flash_sale_products (
    id BIGSERIAL PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    stock_limit INTEGER,
    sold_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(flash_sale_id, product_id),
    CONSTRAINT flash_sale_product_price_valid CHECK (sale_price < original_price),
    CONSTRAINT flash_sale_product_sold_valid CHECK (sold_quantity >= 0)
);

-- ============================================
-- TRENDING SEARCHES
-- ============================================

-- Table: trending_searches
CREATE TABLE IF NOT EXISTS trending_searches (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL UNIQUE,
    search_count INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT trending_search_count_positive CHECK (search_count > 0)
);

-- ============================================
-- INDEXES FOR NEW TABLES
-- ============================================

-- Flash sales indexes
CREATE INDEX IF NOT EXISTS idx_flash_sales_active ON flash_sales(is_active);
CREATE INDEX IF NOT EXISTS idx_flash_sales_time ON flash_sales(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_flash_sale_products_flash_sale_id ON flash_sale_products(flash_sale_id);
CREATE INDEX IF NOT EXISTS idx_flash_sale_products_product_id ON flash_sale_products(product_id);

-- Trending searches indexes  
CREATE INDEX IF NOT EXISTS idx_trending_searches_count ON trending_searches(search_count DESC);
CREATE INDEX IF NOT EXISTS idx_trending_searches_active ON trending_searches(is_active);

-- ============================================
-- UPDATE TRIGGERS FOR NEW TABLES
-- ============================================

-- Flash sales triggers
CREATE TRIGGER IF NOT EXISTS update_flash_sales_updated_at BEFORE UPDATE ON flash_sales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_flash_sale_products_updated_at BEFORE UPDATE ON flash_sale_products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_trending_searches_updated_at BEFORE UPDATE ON trending_searches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- COMMENTS FOR NEW FIELDS
-- ============================================

COMMENT ON COLUMN products.cost_price IS 'Cost price for profit calculation';
COMMENT ON COLUMN products.weight IS 'Product weight in kg for shipping calculation';
COMMENT ON COLUMN products.dimensions IS 'Product dimensions (L x W x H) in cm';

COMMENT ON TABLE flash_sales IS 'Flash sale events with time-limited discounts';
COMMENT ON TABLE flash_sale_products IS 'Products participating in flash sales';
COMMENT ON TABLE trending_searches IS 'Popular search keywords for suggestions';