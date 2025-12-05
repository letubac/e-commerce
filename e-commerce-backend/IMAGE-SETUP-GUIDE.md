# HƯỚNG DẪN SỬ DỤNG HỆ THỐNG FILE STORAGE

## ✅ Đã cập nhật

File storage đã được cập nhật để khớp với database format:
- **Database format**: `/images/products/filename.jpg`
- **Upload sẽ lưu vào**: `uploads/images/products/`
- **URL truy cập**: `http://localhost:8080/api/v1/files/images/products/filename.jpg`

## 🚀 QUICK START

### Bước 1: Tạo folder structure
```bash
cd f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend
create-upload-folders.bat
```

Folder structure:
```
e-commerce-backend/
├── uploads/
│   └── images/
│       ├── products/      ← Lưu hình sản phẩm
│       ├── categories/    ← Lưu hình danh mục  
│       ├── brands/        ← Lưu logo thương hiệu
│       └── users/         ← Lưu avatar người dùng
```

### Bước 2: Đặt file ảnh vào folder
Có 3 cách:

**Cách 1: Copy thủ công**
- Tải ảnh sản phẩm về
- Copy vào `uploads/images/products/`
- Đổi tên file khớp với database (ví dụ: `iphone-15-pro-max-titanium-1.jpg`)

**Cách 2: Upload qua Web UI**
1. Mở `upload-images.html` trong browser
2. Login admin
3. Upload files
4. Copy path từ kết quả và update DB

**Cách 3: Upload qua API**
```bash
curl -X POST http://localhost:8080/api/v1/admin/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@image.jpg" \
  -F "category=products"
```

### Bước 3: Test
Access image qua URL:
```
http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg
```

## 📝 DATABASE FORMAT

Database đang lưu path như sau:
```sql
SELECT id, product_id, image_url FROM product_images LIMIT 5;

-- Results:
31 | 13 | /images/products/iphone-15-pro-max-titanium-1.jpg
32 | 13 | /images/products/iphone-15-pro-max-titanium-2.jpg
35 | 14 | /images/products/samsung-s24-ultra-black-1.jpg
```

**Quy tắc:**
- Path KHÔNG có `api/v1/files` prefix trong DB
- Path BẮT ĐẦU bằng `/images/`
- Frontend tự động thêm: `http://localhost:8080/api/v1/files` + DB path

## 🎯 CÁCH ĐẶT TÊN FILE

Để dễ quản lý, đặt tên file theo format:
```
{product-slug}-{number}.jpg

Ví dụ:
- iphone-15-pro-max-titanium-1.jpg
- iphone-15-pro-max-titanium-2.jpg
- samsung-s24-ultra-black-1.jpg
- macbook-pro-14-space-gray-1.jpg
```

## 🔧 UPLOAD MỚI

Khi upload file mới qua API:
1. File sẽ được lưu với UUID random: `abc-123-xyz.jpg`
2. Response trả về path: `/images/products/abc-123-xyz.jpg`
3. Update DB với path này:

```sql
UPDATE product_images 
SET image_url = '/images/products/abc-123-xyz.jpg'
WHERE id = 31;
```

## 🖼️ CÁCH LẤY ẢNH MẪU

### Option 1: Download từ placeholder services
```bash
# Download và lưu vào folder
cd uploads/images/products
curl -o iphone-15-pro-max-titanium-1.jpg "https://via.placeholder.com/800x600/667eea/ffffff?text=iPhone+15+Pro+Max"
```

### Option 2: Tìm ảnh thật
1. Tìm ảnh sản phẩm trên Google Images
2. Download về
3. Resize về kích thước phù hợp (800x600 hoặc 1200x900)
4. Copy vào `uploads/images/products/`
5. Đặt tên khớp với DB

### Option 3: Dùng ảnh có sẵn
Nếu bạn có folder ảnh sẵn, copy hết vào `uploads/images/products/`

## ✨ FRONTEND ĐÃ TỰ ĐỘNG XỬ LÝ

Các component đã được update:
- ✅ ProductCard.js
- ✅ NewArrivals.js
- ✅ FlashSale.js
- ✅ ProductDetailsPage.js

Tất cả đều tự động thêm prefix: `http://localhost:8080/api/v1/files`

## 🐛 TROUBLESHOOTING

### Ảnh không hiển thị?

**Check 1: Folder tồn tại?**
```bash
dir uploads\images\products
```

**Check 2: File có đúng tên?**
```sql
-- Kiểm tra path trong DB
SELECT image_url FROM product_images WHERE product_id = 13;

-- Kết quả: /images/products/iphone-15-pro-max-titanium-1.jpg
```

**Check 3: File có trong folder?**
```bash
dir uploads\images\products\iphone-15-pro-max-titanium-1.jpg
```

**Check 4: Backend có serve file?**
Truy cập trực tiếp:
```
http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg
```

**Check 5: CORS?**
Kiểm tra console browser có lỗi CORS không

### URL Format Reference

```
Database:  /images/products/file.jpg
Physical:  uploads/images/products/file.jpg
API:       /api/v1/files/images/products/file.jpg
Full URL:  http://localhost:8080/api/v1/files/images/products/file.jpg
```

## 📦 SAMPLE SETUP SCRIPT

Tạo file `setup-images.bat`:
```batch
@echo off
echo Setting up sample images...

cd uploads\images\products

echo Downloading placeholder images...
curl -o iphone-15-pro-max-titanium-1.jpg "https://via.placeholder.com/800/667eea/ffffff?text=iPhone+15+Pro"
curl -o samsung-s24-ultra-black-1.jpg "https://via.placeholder.com/800/764ba2/ffffff?text=Samsung+S24"
curl -o macbook-pro-14-space-gray-1.jpg "https://via.placeholder.com/800/667eea/ffffff?text=MacBook+Pro"

echo Done! Images created in uploads\images\products\
pause
```

Run: `setup-images.bat`

## 🎉 KẾT QUẢ

Sau khi setup:
1. ✅ Folder structure đã tạo
2. ✅ Images có trong folder
3. ✅ Backend serve files OK
4. ✅ Frontend tự động load images
5. ✅ Database paths đã match

**Test ngay**: Refresh trang product list hoặc product details!
