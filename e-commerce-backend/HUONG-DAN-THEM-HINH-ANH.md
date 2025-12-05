# Hướng dẫn thêm hình ảnh sản phẩm

## Cách 1: Thêm file thủ công (ĐƠN GIẢN NHẤT)

1. Mở thư mục: `e-commerce-backend\uploads\images\products\`

2. Copy các file hình ảnh của bạn vào đó với tên giống database:
   - `iphone-15-pro-max-titanium-1.jpg`
   - `samsung-s24-ultra-black-1.jpg`
   - `xiaomi-14-ultra-black-1.jpg`
   - v.v...

3. Restart backend server

4. Test URL: `http://localhost:8080/api/v1/files/images/products/[tên-file].jpg`

## Cách 2: Dùng Web UI Upload

1. Mở file: `e-commerce-backend\upload-images.html` trong trình duyệt

2. Login với tài khoản admin

3. Upload hình ảnh qua giao diện web

## Cách 3: Tạo file placeholder nhanh (Windows)

Chạy lệnh sau trong PowerShell (ở thư mục backend):

```powershell
cd uploads\images\products

# Tạo file text giả làm placeholder (chỉ để test)
"iPhone 15 Pro Max" | Out-File -FilePath iphone-15-pro-max-titanium-1.jpg -Encoding UTF8
"Samsung S24 Ultra" | Out-File -FilePath samsung-s24-ultra-black-1.jpg -Encoding UTF8
"Xiaomi 14 Ultra" | Out-File -FilePath xiaomi-14-ultra-black-1.jpg -Encoding UTF8
"MacBook Pro 14" | Out-File -FilePath macbook-pro-14-space-gray-1.jpg -Encoding UTF8
"Dell XPS 13" | Out-File -FilePath dell-xps-13-plus-platinum-1.jpg -Encoding UTF8
"ASUS ROG G16" | Out-File -FilePath asus-rog-g16-gray-1.jpg -Encoding UTF8
```

**Lưu ý:** Cách 3 chỉ tạo file text để test API, không phải hình ảnh thật

## Kiểm tra

Sau khi thêm file, kiểm tra:
- File tồn tại: `dir uploads\images\products`
- API hoạt động: Mở browser vào `http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg`
- Frontend hiển thị: Refresh trang product

## Tải hình ảnh placeholder từ internet

Nếu muốn tải hình placeholder thật, mở các link sau trong browser và Save As vào thư mục `uploads\images\products\`:

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
