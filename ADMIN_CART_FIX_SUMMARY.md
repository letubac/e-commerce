# Fix Admin Login Gọi Cart API - Summary

## 🐛 Vấn đề:
Khi admin login vào hệ thống, CartContext tự động gọi API `GET /api/v1/cart` → Bị lỗi authentication/logic vì admin không có giỏ hàng.

## 🔍 Nguyên nhân:
1. **Frontend (CartContext.js)**: `useEffect` tự động fetch cart cho TẤT CẢ user khi có token, không phân biệt role
2. **Backend (CartController.java)**: Cart API không kiểm tra role, nhận request từ bất kỳ user nào có token
3. **Header.js**: Hiển thị cart icon cho cả admin

## ✅ Giải pháp đã triển khai:

### 1. **Frontend - CartContext.js**
```javascript
// TRƯỚC:
if (user) {
  fetchCart(); // ❌ Fetch cho TẤT CẢ user
}

// SAU:
if (user) {
  const userRole = user.role || (user.roles && user.roles[0]);
  
  if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
    console.log('🛒 User is admin, skipping cart fetch');
    setCart({ items: [], totalPrice: 0, totalItems: 0 });
    setLoading(false);
    return; // ✅ Không fetch cart cho admin
  }
  
  fetchCart(); // ✅ Chỉ fetch cho customer
}
```

**Kết quả:**
- ✅ Admin login → KHÔNG gọi cart API
- ✅ Customer login → Gọi cart API bình thường
- ✅ Guest user → Không gọi cart API

---

### 2. **Backend - CartController.java**
Thêm method `checkCustomerRole()` để kiểm tra role trước khi xử lý:

```java
private void checkCustomerRole(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    boolean isAdmin = userPrincipal.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                         auth.getAuthority().equals("ROLE_SUPER_ADMIN"));
    
    if (isAdmin) {
        throw new RuntimeException("Admin không có giỏ hàng");
    }
}
```

**Áp dụng cho TẤT CẢ cart endpoints:**
- ✅ `GET /api/v1/cart` - Xem giỏ hàng
- ✅ `POST /api/v1/cart/items` - Thêm sản phẩm
- ✅ `PUT /api/v1/cart/items/{itemId}` - Cập nhật số lượng
- ✅ `DELETE /api/v1/cart/items/{itemId}` - Xóa sản phẩm
- ✅ `DELETE /api/v1/cart` - Xóa toàn bộ giỏ hàng

**Kết quả:**
- ✅ Admin gọi cart API → HTTP 500 "Admin không có giỏ hàng"
- ✅ Customer gọi cart API → Hoạt động bình thường
- ⚠️ Nên đổi HTTP 500 thành 403 Forbidden (improvement)

---

### 3. **Frontend - Header.js**
Ẩn cart icon khi user là admin:

```javascript
// TRƯỚC:
<button onClick={() => navigate('/cart')}>
  <ShoppingCart size={24} />
  {cart.totalItems > 0 && <span>{cart.totalItems}</span>}
</button>

// SAU:
{/* Cart - chỉ hiển thị cho customer */}
{user && !isAdmin() && (
  <button onClick={() => navigate('/cart')}>
    <ShoppingCart size={24} />
    {cart.totalItems > 0 && <span>{cart.totalItems}</span>}
  </button>
)}

{/* Cart cho guest user */}
{!user && (
  <button onClick={() => navigate('/cart')}>
    <ShoppingCart size={24} />
  </button>
)}
```

**Kết quả:**
- ✅ Admin → Không thấy cart icon
- ✅ Customer → Thấy cart icon + badge số lượng
- ✅ Guest → Thấy cart icon (không có badge)

---

## 🧪 Test Scenarios:

### Scenario 1: Admin Login
1. Login với tài khoản admin
2. ✅ KHÔNG gọi `GET /api/v1/cart`
3. ✅ Header KHÔNG hiển thị cart icon
4. ✅ Console log: "🛒 User is admin, skipping cart fetch"

### Scenario 2: Customer Login
1. Login với tài khoản customer
2. ✅ Tự động gọi `GET /api/v1/cart`
3. ✅ Header hiển thị cart icon với badge
4. ✅ Console log: "🛒 User is customer, fetching cart..."

### Scenario 3: Admin Cố Gắng Access Cart API
1. Admin đã login
2. Manually gọi `GET /api/v1/cart` (qua Postman/DevTools)
3. ✅ Backend trả về error "Admin không có giỏ hàng"
4. ✅ HTTP Status: 500 (nên đổi thành 403)

### Scenario 4: Guest User
1. Chưa login
2. ✅ KHÔNG gọi cart API
3. ✅ Header hiển thị cart icon (không có badge)
4. ✅ Click cart icon → Redirect về login page

---

## 📊 Flow Diagram:

```
┌─────────────────────────────────────────────────────────────┐
│                      USER LOGIN                              │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
         ┌────────────────────┐
         │ AuthContext        │
         │ setUser(userData)  │
         └────────┬───────────┘
                  │
                  ▼
         ┌────────────────────┐
         │ CartContext        │
         │ useEffect([user])  │
         └────────┬───────────┘
                  │
        ┌─────────┴──────────┐
        │                    │
        ▼                    ▼
    [ADMIN?]            [CUSTOMER?]
        │                    │
        ▼                    ▼
  ❌ Skip Cart         ✅ Fetch Cart
  setCart({empty})     api.getCart()
        │                    │
        ▼                    ▼
  Admin Dashboard      Cart Loaded
  (No cart icon)       (Show cart badge)
```

---

## 🔧 Improvements Cần Làm:

### 1. HTTP Status Code
```java
// Hiện tại:
throw new RuntimeException("Admin không có giỏ hàng"); // → 500

// Nên đổi thành:
throw new ForbiddenException("Admin không có giỏ hàng"); // → 403
```

### 2. Exception Handler
Tạo custom exception:
```java
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
```

### 3. Route Protection
Thêm route guard cho `/cart`:
```javascript
// CartPage.js
useEffect(() => {
  if (isAdmin()) {
    navigate('/admin');
    toast.info('Admin không có quyền truy cập giỏ hàng');
  }
}, []);
```

---

## 📝 Files Changed:

### Frontend:
1. ✅ `e-commerce-fe/src/context/CartContext.js`
   - Thêm role check trong useEffect
   - Skip cart fetch cho admin
   
2. ✅ `e-commerce-fe/src/components/Header.js`
   - Ẩn cart icon cho admin
   - Hiển thị cart icon cho customer và guest

### Backend:
3. ✅ `e-commerce-backend/src/main/java/com/ecommerce/controller/CartController.java`
   - Thêm `checkCustomerRole()` method
   - Apply check cho tất cả cart endpoints

---

## 🎯 Expected Behavior:

| User Type | Cart API Called? | Cart Icon Visible? | Can Access /cart? |
|-----------|------------------|--------------------|--------------------|
| **Admin** | ❌ No | ❌ No | ❌ Should redirect |
| **Customer** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Guest** | ❌ No | ✅ Yes (no badge) | ⚠️ Redirect to login |

---

## 🚀 Testing Commands:

### Test với Admin:
```bash
# Login admin
POST /api/v1/auth/login
{
  "usernameOrEmail": "admin@ecommerce.com",
  "password": "123456"
}

# Kiểm tra console log
→ Nên thấy: "🛒 User is admin, skipping cart fetch"
→ KHÔNG thấy: "GET /api/v1/cart"

# Thử gọi cart API manually
GET /api/v1/cart
→ Response: 500 "Admin không có giỏ hàng"
```

### Test với Customer:
```bash
# Login customer
POST /api/v1/auth/login
{
  "usernameOrEmail": "customer@ecommerce.com",
  "password": "123456"
}

# Kiểm tra console log
→ Nên thấy: "🛒 User is customer, fetching cart..."
→ Nên thấy: "GET /api/v1/cart"

# Cart icon hiển thị
→ Nên thấy: ShoppingCart icon với badge số lượng
```

---

## ✨ Summary:

✅ **Frontend**: CartContext chỉ fetch cart cho CUSTOMER, không fetch cho ADMIN
✅ **Backend**: Cart API reject requests từ ADMIN với error message rõ ràng
✅ **UI**: Header ẩn cart icon cho ADMIN, hiển thị bình thường cho CUSTOMER/GUEST
✅ **Console Logs**: Thêm debug logs để dễ troubleshoot

**Problem Solved!** 🎉
