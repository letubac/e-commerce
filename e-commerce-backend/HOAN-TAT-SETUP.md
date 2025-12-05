# ✅ Hoàn tất setup File Storage System

## Đã làm xong:

✅ Tạo FileStorageService.java - Service lưu/xóa file
✅ Tạo FileUploadController.java - REST API upload/serve/delete
✅ Cập nhật SecurityConfig.java - Cho phép public access /api/v1/files/**
✅ Tạo thư mục uploads/images/{products,categories,brands,users}
✅ Tạo 6 file placeholder cho test (text files)
✅ Frontend đã cập nhật để hiển thị hình từ /api/v1/files

## Bước cuối cùng - Khởi động backend:

### Cách 1: Dùng Maven (Khuyên dùng)
```bash
cd e-commerce-backend
mvnw spring-boot:run
```

### Cách 2: Dùng IDE
- Mở file `ECommerceApplication.java`
- Click nút Run/Debug

### Cách 3: Build JAR rồi chạy
```bash
mvnw clean package
java -jar target/e-commerce-backend-0.0.1-SNAPSHOT.jar
```

## Kiểm tra sau khi start:

1. **Test API file serving:**
   ```
   http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg
   ```
   → Nên trả về content của file (text "iPhone 15 Pro Max")

2. **Test frontend product display:**
   ```
   http://localhost:3000
   ```
   → Products hiển thị với hình ảnh (placeholder)

3. **Test upload UI:**
   ```
   file:///F:/NhaSachTinHoc/CDTC/e-commerce/e-commerce-backend/upload-images.html
   ```
   → Login admin → Upload hình thật

## Thay thế placeholder bằng hình thật:

### Option 1: Copy file thủ công
1. Chuẩn bị file JPG thật
2. Copy vào `e-commerce-backend/uploads/images/products/`
3. Đặt tên giống database: `iphone-15-pro-max-titanium-1.jpg`

### Option 2: Tải từ internet
Mở các link sau trong browser và Save As vào `uploads/images/products/`:

1. https://via.placeholder.com/800x600/667eea/ffffff?text=iPhone+15+Pro+Max
   → Save as: `iphone-15-pro-max-titanium-1.jpg`

2. https://via.placeholder.com/800x600/764ba2/ffffff?text=Samsung+S24+Ultra
   → Save as: `samsung-s24-ultra-black-1.jpg`

3. https://via.placeholder.com/800x600/667eea/ffffff?text=Xiaomi+14+Ultra
   → Save as: `xiaomi-14-ultra-black-1.jpg`

4. https://via.placeholder.com/800x600/764ba2/ffffff?text=MacBook+Pro+14
   → Save as: `macbook-pro-14-space-gray-1.jpg`

5. https://via.placeholder.com/800x600/667eea/ffffff?text=Dell+XPS+13
   → Save as: `dell-xps-13-plus-platinum-1.jpg`

6. https://via.placeholder.com/800x600/764ba2/ffffff?text=ASUS+ROG+G16
   → Save as: `asus-rog-g16-gray-1.jpg`

### Option 3: Upload qua Web UI
1. Start backend server
2. Mở `upload-images.html`
3. Login với admin account
4. Select category: "products"
5. Choose file → Upload

## Database format:

```sql
-- product_images table lưu như sau:
image_url = '/images/products/iphone-15-pro-max-titanium-1.jpg'

-- API serve tại:
GET http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg

-- Physical file location:
uploads/images/products/iphone-15-pro-max-titanium-1.jpg
```

## Troubleshooting:

### Lỗi 401 Unauthorized khi access file
→ Backend chưa start hoặc SecurityConfig chưa apply
→ Solution: Restart backend

### Lỗi 404 Not Found
→ File chưa tồn tại trong uploads/images/products/
→ Solution: Check file name khớp với DB

### Frontend không hiển thị hình
→ Check console log browser (F12)
→ Verify image URL đúng format: http://localhost:8080/api/v1/files/images/products/...
→ Check backend logs xem có request đến không

### Upload fail
→ Check folder permissions
→ Check application.yml có file.upload-dir config
→ Check FileStorageService bean initialized

## Notes:

- File placeholder hiện tại chỉ là text file (34-46 bytes)
- Để hình hiển thị đúng, cần thay bằng JPG/PNG thật
- Backend phải restart sau khi thêm FileStorageService/FileUploadController
- Frontend tự động add prefix /api/v1/files vào image URLs
