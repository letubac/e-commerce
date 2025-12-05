CREATE SEQUENCE IF NOT EXISTS seq_carts START 1;
SELECT setval('seq_carts', (SELECT COALESCE(MAX(id), 1) FROM carts));
ALTER TABLE carts
    ALTER COLUMN id SET DEFAULT nextval('seq_carts');
--
CREATE SEQUENCE IF NOT EXISTS seq_cart_items START 1;
SELECT setval('seq_cart_items', (SELECT COALESCE(MAX(id), 1) FROM cart_items));
ALTER TABLE carts
    ALTER COLUMN id SET DEFAULT nextval('seq_cart_items');
--
CREATE SEQUENCE IF NOT EXISTS seq_addresses START 1;
SELECT setval('seq_addresses', (SELECT COALESCE(MAX(id), 1) FROM addresses));
ALTER TABLE carts
    ALTER COLUMN id SET DEFAULT nextval('seq_addresses');
--
CREATE SEQUENCE IF NOT EXISTS seq_banners START 1;
SELECT setval('seq_banners', (SELECT COALESCE(MAX(id), 1) FROM banners));
ALTER TABLE carts
    ALTER COLUMN id SET DEFAULT nextval('seq_banners');
--
CREATE SEQUENCE IF NOT EXISTS seq_brands START 1;
SELECT setval('seq_brands', (SELECT COALESCE(MAX(id), 1) FROM brands));
ALTER TABLE carts
    ALTER COLUMN id SET DEFAULT nextval('seq_brands');
-- categories
CREATE SEQUENCE IF NOT EXISTS seq_categories START 1;
SELECT setval('seq_categories', (SELECT COALESCE(MAX(id), 1) FROM categories));
ALTER TABLE categories ALTER COLUMN id SET DEFAULT nextval('seq_categories');
-- chat_messages
CREATE SEQUENCE IF NOT EXISTS seq_chat_messages START 1;
SELECT setval('seq_chat_messages', (SELECT COALESCE(MAX(id), 1) FROM chat_messages));
ALTER TABLE chat_messages ALTER COLUMN id SET DEFAULT nextval('seq_chat_messages');

-- chat_participants
CREATE SEQUENCE IF NOT EXISTS seq_chat_participants START 1;
SELECT setval('seq_chat_participants', (SELECT COALESCE(MAX(id), 1) FROM chat_participants));
ALTER TABLE chat_participants ALTER COLUMN id SET DEFAULT nextval('seq_chat_participants');

-- chat_quick_replies
CREATE SEQUENCE IF NOT EXISTS seq_chat_quick_replies START 1;
SELECT setval('seq_chat_quick_replies', (SELECT COALESCE(MAX(id), 1) FROM chat_quick_replies));
ALTER TABLE chat_quick_replies ALTER COLUMN id SET DEFAULT nextval('seq_chat_quick_replies');

-- chat_settings
CREATE SEQUENCE IF NOT EXISTS seq_chat_settings START 1;
SELECT setval('seq_chat_settings', (SELECT COALESCE(MAX(id), 1) FROM chat_settings));
ALTER TABLE chat_settings ALTER COLUMN id SET DEFAULT nextval('seq_chat_settings');

-- conversations
CREATE SEQUENCE IF NOT EXISTS seq_conversations START 1;
SELECT setval('seq_conversations', (SELECT COALESCE(MAX(id), 1) FROM conversations));
ALTER TABLE conversations ALTER COLUMN id SET DEFAULT nextval('seq_conversations');

-- coupon_uses
CREATE SEQUENCE IF NOT EXISTS seq_coupon_uses START 1;
SELECT setval('seq_coupon_uses', (SELECT COALESCE(MAX(id), 1) FROM coupon_uses));
ALTER TABLE coupon_uses ALTER COLUMN id SET DEFAULT nextval('seq_coupon_uses');

-- coupons
CREATE SEQUENCE IF NOT EXISTS seq_coupons START 1;
SELECT setval('seq_coupons', (SELECT COALESCE(MAX(id), 1) FROM coupons));
ALTER TABLE coupons ALTER COLUMN id SET DEFAULT nextval('seq_coupons');

-- email_logs
CREATE SEQUENCE IF NOT EXISTS seq_email_logs START 1;
SELECT setval('seq_email_logs', (SELECT COALESCE(MAX(id), 1) FROM email_logs));
ALTER TABLE email_logs ALTER COLUMN id SET DEFAULT nextval('seq_email_logs');

-- email_templates
CREATE SEQUENCE IF NOT EXISTS seq_email_templates START 1;
SELECT setval('seq_email_templates', (SELECT COALESCE(MAX(id), 1) FROM email_templates));
ALTER TABLE email_templates ALTER COLUMN id SET DEFAULT nextval('seq_email_templates');

-- flash_sale_products
CREATE SEQUENCE IF NOT EXISTS seq_flash_sale_products START 1;
SELECT setval('seq_flash_sale_products', (SELECT COALESCE(MAX(id), 1) FROM flash_sale_products));
ALTER TABLE flash_sale_products ALTER COLUMN id SET DEFAULT nextval('seq_flash_sale_products');

-- flash_sales
CREATE SEQUENCE IF NOT EXISTS seq_flash_sales START 1;
SELECT setval('seq_flash_sales', (SELECT COALESCE(MAX(id), 1) FROM flash_sales));
ALTER TABLE flash_sales ALTER COLUMN id SET DEFAULT nextval('seq_flash_sales');

-- notifications
CREATE SEQUENCE IF NOT EXISTS seq_notifications START 1;
SELECT setval('seq_notifications', (SELECT COALESCE(MAX(id), 1) FROM notifications));
ALTER TABLE notifications ALTER COLUMN id SET DEFAULT nextval('seq_notifications');

-- order_items
CREATE SEQUENCE IF NOT EXISTS seq_order_items START 1;
SELECT setval('seq_order_items', (SELECT COALESCE(MAX(id), 1) FROM order_items));
ALTER TABLE order_items ALTER COLUMN id SET DEFAULT nextval('seq_order_items');

-- orders
CREATE SEQUENCE IF NOT EXISTS seq_orders START 1;
SELECT setval('seq_orders', (SELECT COALESCE(MAX(id), 1) FROM orders));
ALTER TABLE orders ALTER COLUMN id SET DEFAULT nextval('seq_orders');

-- payments
CREATE SEQUENCE IF NOT EXISTS seq_payments START 1;
SELECT setval('seq_payments', (SELECT COALESCE(MAX(id), 1) FROM payments));
ALTER TABLE payments ALTER COLUMN id SET DEFAULT nextval('seq_payments');

-- product_answers
CREATE SEQUENCE IF NOT EXISTS seq_product_answers START 1;
SELECT setval('seq_product_answers', (SELECT COALESCE(MAX(id), 1) FROM product_answers));
ALTER TABLE product_answers ALTER COLUMN id SET DEFAULT nextval('seq_product_answers');

-- product_comparisons
CREATE SEQUENCE IF NOT EXISTS seq_product_comparisons START 1;
SELECT setval('seq_product_comparisons', (SELECT COALESCE(MAX(id), 1) FROM product_comparisons));
ALTER TABLE product_comparisons ALTER COLUMN id SET DEFAULT nextval('seq_product_comparisons');

-- product_images
CREATE SEQUENCE IF NOT EXISTS seq_product_images START 1;
SELECT setval('seq_product_images', (SELECT COALESCE(MAX(id), 1) FROM product_images));
ALTER TABLE product_images ALTER COLUMN id SET DEFAULT nextval('seq_product_images');

-- product_questions
CREATE SEQUENCE IF NOT EXISTS seq_product_questions START 1;
SELECT setval('seq_product_questions', (SELECT COALESCE(MAX(id), 1) FROM product_questions));
ALTER TABLE product_questions ALTER COLUMN id SET DEFAULT nextval('seq_product_questions');

-- product_variants
CREATE SEQUENCE IF NOT EXISTS seq_product_variants START 1;
SELECT setval('seq_product_variants', (SELECT COALESCE(MAX(id), 1) FROM product_variants));
ALTER TABLE product_variants ALTER COLUMN id SET DEFAULT nextval('seq_product_variants');

-- products
CREATE SEQUENCE IF NOT EXISTS seq_products START 1;
SELECT setval('seq_products', (SELECT COALESCE(MAX(id), 1) FROM products));
ALTER TABLE products ALTER COLUMN id SET DEFAULT nextval('seq_products');

-- recently_viewed
CREATE SEQUENCE IF NOT EXISTS seq_recently_viewed START 1;
SELECT setval('seq_recently_viewed', (SELECT COALESCE(MAX(id), 1) FROM recently_viewed));
ALTER TABLE recently_viewed ALTER COLUMN id SET DEFAULT nextval('seq_recently_viewed');

-- reviews
CREATE SEQUENCE IF NOT EXISTS seq_reviews START 1;
SELECT setval('seq_reviews', (SELECT COALESCE(MAX(id), 1) FROM reviews));
ALTER TABLE reviews ALTER COLUMN id SET DEFAULT nextval('seq_reviews');

-- search_history
CREATE SEQUENCE IF NOT EXISTS seq_search_history START 1;
SELECT setval('seq_search_history', (SELECT COALESCE(MAX(id), 1) FROM search_history));
ALTER TABLE search_history ALTER COLUMN id SET DEFAULT nextval('seq_search_history');

-- shipping_rates
CREATE SEQUENCE IF NOT EXISTS seq_shipping_rates START 1;
SELECT setval('seq_shipping_rates', (SELECT COALESCE(MAX(id), 1) FROM shipping_rates));
ALTER TABLE shipping_rates ALTER COLUMN id SET DEFAULT nextval('seq_shipping_rates');

-- shipping_zones
CREATE SEQUENCE IF NOT EXISTS seq_shipping_zones START 1;
SELECT setval('seq_shipping_zones', (SELECT COALESCE(MAX(id), 1) FROM shipping_zones));
ALTER TABLE shipping_zones ALTER COLUMN id SET DEFAULT nextval('seq_shipping_zones');

-- stock_movements
CREATE SEQUENCE IF NOT EXISTS seq_stock_movements START 1;
SELECT setval('seq_stock_movements', (SELECT COALESCE(MAX(id), 1) FROM stock_movements));
ALTER TABLE stock_movements ALTER COLUMN id SET DEFAULT nextval('seq_stock_movements');

-- trending_searches
CREATE SEQUENCE IF NOT EXISTS seq_trending_searches START 1;
SELECT setval('seq_trending_searches', (SELECT COALESCE(MAX(id), 1) FROM trending_searches));
ALTER TABLE trending_searches ALTER COLUMN id SET DEFAULT nextval('seq_trending_searches');

-- user_roles
CREATE SEQUENCE IF NOT EXISTS seq_user_roles START 1;
SELECT setval('seq_user_roles', (SELECT COALESCE(MAX(id), 1) FROM user_roles));
ALTER TABLE user_roles ALTER COLUMN id SET DEFAULT nextval('seq_user_roles');

-- users
CREATE SEQUENCE IF NOT EXISTS seq_users START 1;
SELECT setval('seq_users', (SELECT COALESCE(MAX(id), 1) FROM users));
ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('seq_users');

-- wishlists
CREATE SEQUENCE IF NOT EXISTS seq_wishlists START 1;
SELECT setval('seq_wishlists', (SELECT COALESCE(MAX(id), 1) FROM wishlists));
ALTER TABLE wishlists ALTER COLUMN id SET DEFAULT nextval('seq_wishlists');