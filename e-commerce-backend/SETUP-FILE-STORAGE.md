# File Storage Setup Guide

## 1. Tạo cấu trúc thư mục

Tạo folder `uploads` trong root project của backend:

```bash
# Từ thư mục e-commerce-backend
mkdir uploads
mkdir uploads\products
mkdir uploads\categories
mkdir uploads\brands
mkdir uploads\users
```

## 2. Cấu trúc thư mục

```
e-commerce-backend/
├── uploads/
│   ├── products/       # Product images
│   ├── categories/     # Category images
│   ├── brands/         # Brand logos
│   └── users/          # User avatars
```

## 3. API Endpoints

### Upload File (Admin only)
```bash
POST /api/v1/admin/upload
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- category: String (optional, default: "products")

Response:
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileName": "product-image.jpg",
    "filePath": "/products/uuid-random.jpg",
    "fileUrl": "/api/v1/files/products/uuid-random.jpg",
    "fileSize": "123456",
    "contentType": "image/jpeg"
  }
}
```

### Upload Multiple Files (Admin only)
```bash
POST /api/v1/admin/upload/multiple
Content-Type: multipart/form-data

Parameters:
- files: MultipartFile[] (required)
- category: String (optional, default: "products")
```

### Serve File (Public)
```bash
GET /api/v1/files/products/uuid-random.jpg
```

### Delete File (Admin only)
```bash
DELETE /api/v1/admin/files?filePath=/products/uuid-random.jpg
```

## 4. Test Upload với Postman/cURL

### Upload single file
```bash
curl -X POST http://localhost:8080/api/v1/admin/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "category=products"
```

### Access file
```bash
# Direct URL
http://localhost:8080/api/v1/files/products/uuid-random.jpg
```

## 5. Update Product Images

Sau khi upload file, update database:

```sql
-- Update product image
UPDATE product_images 
SET image_url = '/products/uuid-from-upload.jpg'
WHERE id = 31;

-- Or insert new image
INSERT INTO product_images (product_id, image_url, alt_text, sort_order, is_primary, created_at)
VALUES (13, '/products/uuid-from-upload.jpg', 'Product image', 1, true, CURRENT_TIMESTAMP);
```

## 6. Frontend Integration

Frontend sẽ tự động thêm domain:

```javascript
// Trong ProductCard.js, NewArrivals.js, etc.
const fullImageUrl = imageUrl.startsWith('http') 
  ? imageUrl 
  : `http://localhost:8080/api/v1/files${imageUrl}`;
```

## 7. Sample Test

1. Start backend server
2. Login as admin để lấy token
3. Upload file:
```bash
curl -X POST http://localhost:8080/api/v1/admin/upload \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@product-image.jpg" \
  -F "category=products"
```
4. Copy `filePath` từ response
5. Update database với path đó
6. Refresh frontend - image sẽ hiển thị!

## 8. Production Considerations

Để production, nên:
- Sử dụng CDN (Cloudinary, AWS S3, Azure Blob Storage)
- Implement image optimization/resize
- Add image validation (type, size)
- Add virus scanning
- Implement caching headers
- Use signed URLs for private images

## 9. Tạo sample images cho testing

Download sample images và đặt trong `uploads/products/`:
```bash
# Or tạo placeholder images
cd uploads/products
# Tạo file test
echo "test" > test-image-1.jpg
```

## 10. Troubleshooting

### Issue: File not found
- Check folder `uploads/` exists
- Check file permissions
- Check filePath trong database có đúng format `/products/filename.jpg`

### Issue: Access denied
- Check SecurityConfig có cho phép `/api/v1/files/**`
- Check CORS settings

### Issue: File too large
- Check `spring.servlet.multipart.max-file-size` trong application.yml
- Default là 10MB
