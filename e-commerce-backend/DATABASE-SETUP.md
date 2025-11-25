# Database Setup Guide

## Overview
Dự án E-Commerce sử dụng PostgreSQL với 3 file SQL chính để tạo database schema:

**⚠️ Lưu ý quan trọng:** Từ phiên bản này, tất cả các câu lệnh ALTER TABLE và CREATE TRIGGER đã được cập nhật để tránh lỗi duplicate khi chạy lại file. Sử dụng cú pháp `IF NOT EXISTS` để đảm bảo an toàn.

## Setup Order (Thứ tự chạy)

### 1. **database-base-schema.sql** (CHẠY TRƯỚC)
- Tạo tất cả các table cơ bản cần thiết cho hệ thống
- Bao gồm: users, products, orders, carts, payments, reviews, conversations, chat_messages, etc.
- Tạo indexes cho performance
- Tạo sample data cơ bản

### 2. **database-additions.sql** (CHẠY TIẾP THEO)
- Bổ sung các features mở rộng
- Thêm columns mới vào các table đã tồn tại
- Thêm các table cho flash sale, wishlist, search history, etc.

### 3. **database-chat-schema.sql** (CHẠY CUỐI)
- Bổ sung các table mở rộng cho chat system
- Tạo views và functions hỗ trợ
- Tạo triggers và stored procedures

## Commands

```bash
# Connect to PostgreSQL
psql -U your_username -d your_database

# Run in order:
\i database-base-schema.sql
\i database-additions.sql  
\i database-chat-schema.sql
```

## Complete Table List

### Core Tables (database-base-schema.sql)
1. **roles** - Vai trò người dùng
2. **users** - Thông tin người dùng
3. **user_roles** - Phân quyền người dùng
4. **categories** - Danh mục sản phẩm
5. **brands** - Thương hiệu
6. **products** - Sản phẩm
7. **product_images** - Hình ảnh sản phẩm
8. **addresses** - Địa chỉ giao hàng
9. **carts** - Giỏ hàng
10. **cart_items** - Sản phẩm trong giỏ hàng
11. **orders** - Đơn hàng
12. **order_items** - Sản phẩm trong đơn hàng
13. **payments** - Thanh toán
14. **coupons** - Mã giảm giá
15. **conversations** - Cuộc trò chuyện chat
16. **chat_messages** - Tin nhắn chat
17. **reviews** - Đánh giá sản phẩm
18. **notifications** - Thông báo

### Extended Tables (database-additions.sql)
- flash_sales
- flash_sale_products
- wishlists
- wishlist_items
- search_history
- user_search_history
- product_variants
- variant_options
- variant_values
- shipping_zones
- shipping_methods
- banners
- inventory_transactions

### Chat Extended Tables (database-chat-schema.sql)
- chat_participants
- chat_quick_replies
- chat_settings

## Entity Mapping

| Java Entity | Database Table | File |
|-------------|----------------|------|
| User | users | base |
| Product | products | base |
| Order | orders | base |
| OrderItem | order_items | base |
| Cart | carts | base |
| CartItem | cart_items | base |
| Review | reviews | base |
| Coupon | coupons | base |
| Conversation | conversations | base |
| ChatMessage | chat_messages | base |

## Notes

- Tất cả tables sử dụng BIGSERIAL cho primary key
- Sử dụng TIMESTAMP WITH TIME ZONE cho datetime fields  
- Foreign key constraints được thiết lập đúng
- Indexes được tạo cho performance optimization
- PostgreSQL syntax được sử dụng (||, boolean true/false, etc.)
- **✅ Safe re-execution**: Tất cả ALTER TABLE và CREATE TRIGGER sử dụng IF NOT EXISTS để tránh lỗi duplicate
- **✅ Column conflict prevention**: Bỏ qua các columns đã tồn tại trong base schema (như meta_title, meta_description, last_login_at)

## Common Issues & Solutions

### 1. Column Already Exists Error
```
ERROR: column "meta_title" of relation "products" already exists
```
**Solution:** Đã được fix! File database-additions.sql hiện sử dụng IF NOT EXISTS để kiểm tra column trước khi thêm.

### 2. Trigger Already Exists Error  
```
ERROR: relation "trigger_name" already exists
```
**Solution:** Đã được fix! Tất cả triggers kiểm tra existence trước khi tạo.

### 3. Safe Re-execution
Bây giờ bạn có thể chạy lại `database-additions.sql` nhiều lần mà không gặp lỗi duplicate.

## Verification

Sau khi chạy xong, kiểm tra bằng các lệnh:

```sql
-- Check all tables
\dt

-- Check table structure
\d table_name

-- Verify foreign keys
SELECT * FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY';
```