# Cart Module Refactoring Summary

## Mục tiêu
Refactor toàn bộ Cart module (Backend + Frontend) theo pattern **ErrorHandler/SuccessHandler + BusinessApiResponse** giống như Brand module.

---

## 🔧 Backend Changes

### 1. CartConstant.java (MỚI)
**Location:** `src/main/java/com/ecommerce/constant/CartConstant.java`

**Error Codes (E300-E349):**
- **E300-E309**: Cart errors
  - `E300_CART_NOT_FOUND`: Không tìm thấy giỏ hàng
  - `E301_CART_CREATE_ERROR`: Lỗi tạo giỏ hàng
  - `E302_CART_UPDATE_ERROR`: Lỗi cập nhật
  - `E303_CART_DELETE_ERROR`: Lỗi xóa
  - `E304_CART_GET_ERROR`: Lỗi lấy thông tin
  - `E305_CART_CLEAR_ERROR`: Lỗi xóa toàn bộ
  - `E306_ADMIN_NO_CART`: Admin không có giỏ hàng

- **E310-E319**: Cart item errors
  - `E310_CART_ITEM_NOT_FOUND`: Không tìm thấy item
  - `E311_CART_ITEM_ADD_ERROR`: Lỗi thêm item
  - `E312_CART_ITEM_UPDATE_ERROR`: Lỗi cập nhật item
  - `E313_CART_ITEM_DELETE_ERROR`: Lỗi xóa item
  - `E314_CART_ITEM_QUANTITY_INVALID`: Số lượng không hợp lệ

- **E320-E329**: Product/Stock validation
  - `E320_PRODUCT_NOT_FOUND`: Không tìm thấy sản phẩm
  - `E321_PRODUCT_OUT_OF_STOCK`: Hết hàng
  - `E322_INSUFFICIENT_STOCK`: Không đủ tồn kho
  - `E323_PRODUCT_INACTIVE`: Sản phẩm ngừng bán
  - `E324_PRODUCT_PRICE_CHANGED`: Giá đã thay đổi

- **E330-E339**: User validation
  - `E330_USER_NOT_FOUND`: Không tìm thấy user
  - `E331_USER_INACTIVE`: Tài khoản vô hiệu hóa
  - `E332_USER_NOT_CUSTOMER`: Không phải khách hàng

- **E340-E349**: Business logic
  - `E340_CART_EMPTY`: Giỏ hàng trống
  - `E341_CART_TOTAL_CALCULATION_ERROR`: Lỗi tính tổng
  - `E342_CART_MERGE_ERROR`: Lỗi gộp giỏ hàng
  - `E343_DUPLICATE_CART_ITEM`: Item trùng

**Success Codes (S300-S329):**
- `S300_CART_RETRIEVED`: Lấy giỏ hàng thành công
- `S301_CART_CREATED`: Tạo thành công
- `S302_CART_UPDATED`: Cập nhật thành công
- `S303_CART_CLEARED`: Xóa toàn bộ thành công
- `S310_ITEM_ADDED`: Thêm item thành công
- `S311_ITEM_UPDATED`: Cập nhật item thành công
- `S312_ITEM_REMOVED`: Xóa item thành công
- `S313_ITEM_QUANTITY_UPDATED`: Cập nhật số lượng thành công
- `S320_TOTAL_CALCULATED`: Tính tổng thành công

### 2. messages_vi.properties & messages_en.properties
**Location:** `src/main/resources/messages/`

Đã thêm tất cả Cart error codes và success codes vào cả 2 file properties với translations đầy đủ.

**Ví dụ:**
```properties
# Vietnamese
300_CART_NOT_FOUND=Không tìm thấy giỏ hàng
322_INSUFFICIENT_STOCK=Không đủ số lượng tồn kho
S310_ITEM_ADDED=Thêm sản phẩm vào giỏ hàng thành công

# English
300_CART_NOT_FOUND=Cart not found
322_INSUFFICIENT_STOCK=Insufficient stock available
S310_ITEM_ADDED=Item added to cart successfully
```

### 3. CartServiceImpl.java
**Location:** `src/main/java/com/ecommerce/service/impl/CartServiceImpl.java`

**Thay đổi:**
- ✅ Loại bỏ `ResourceNotFoundException`, `BadRequestException`
- ✅ Chỉ sử dụng `DetailException` với CartConstant error codes
- ✅ Thêm logging với SLF4J
- ✅ Proper try-catch với error handling

**Pattern:**
```java
try {
    // Validate user
    if (!userRepository.existsById(userId)) {
        throw new DetailException(CartConstant.E330_USER_NOT_FOUND);
    }
    
    // Business logic
    // ...
    
    log.info("Cart operation successful");
    return result;
} catch (DetailException e) {
    throw e;
} catch (Exception e) {
    log.error("Error in cart operation", e);
    throw new DetailException(CartConstant.E3XX_SPECIFIC_ERROR);
}
```

**Methods refactored:**
- `getCartByUserId()`
- `addToCart()`
- `updateCartItemQuantity()`
- `removeFromCart()`
- `clearCart()`
- `calculateCartTotal()`

### 4. CartController.java
**Location:** `src/main/java/com/ecommerce/controller/CartController.java`

**Thay đổi:**
- ✅ Import `ErrorHandler`, `SuccessHandler`, `BusinessApiResponse`
- ✅ Thêm SLF4J Logger
- ✅ Inject ErrorHandler và SuccessHandler via @Autowired
- ✅ Return type: `ResponseEntity<CartDTO>` → `ResponseEntity<BusinessApiResponse>`
- ✅ Thay `RuntimeException` → `DetailException` trong checkCustomerRole()

**Pattern:**
```java
@GetMapping
public ResponseEntity<BusinessApiResponse> getCart(Authentication authentication) {
    long start = System.currentTimeMillis();
    try {
        checkCustomerRole(authentication);
        Long userId = getUserIdFromAuthentication(authentication);
        CartDTO cart = cartService.getCartByUserId(userId);
        
        return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
    } catch (Exception e) {
        log.error("Error getting cart", e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}
```

**Endpoints refactored:**
- `GET /api/v1/cart` - getCart
- `POST /api/v1/cart/items` - addToCart
- `PUT /api/v1/cart/items/{itemId}` - updateCartItem
- `DELETE /api/v1/cart/items/{itemId}` - removeCartItem
- `DELETE /api/v1/cart` - clearCart

---

## 🎨 Frontend Changes

### Response Handling
**No changes needed!** 🎉

Frontend đã được refactor trước đó với `responseHandler.js` và `parseBusinessResponse()`.

- ✅ `api.js` tự động parse BusinessApiResponse
- ✅ Cart APIs (`getCart`, `addToCart`, `updateCartItem`, `removeFromCart`, `clearCart`) đã hoạt động với response mới
- ✅ Error messages từ BE (đã i18n) tự động hiển thị qua toast

### CartContext.js
**Location:** `src/context/CartContext.js`

**Current state:** ✅ Hoạt động tốt
- API calls sử dụng `api.request()` đã được parse
- Error handling với toast messages
- Admin role check để không fetch cart

**Example:**
```javascript
const addToCart = async (productId, quantity) => {
  try {
    const data = await api.addToCart({ productId, quantity });
    setCart(data);
    toast.success('Thêm sản phẩm vào giỏ hàng thành công!');
    return data;
  } catch (error) {
    console.error('Error adding to cart:', error);
    if (error.message !== 'Authentication required') {
      toast.error(error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
    }
    throw error;
  }
};
```

### CartPage.js
**Location:** `src/pages/CartPage.js`

**Current state:** ✅ Hoạt động tốt
- Sử dụng CartContext hooks
- Stock validation trước khi update
- Toast messages cho user feedback
- Loading states
- Empty cart handling

---

## 📊 Response Structure

### Backend Response (BusinessApiResponse)
```json
{
  "codeStatus": 200,
  "messageStatus": "SUCCESS",
  "description": "Thêm sản phẩm vào giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 123,
    "items": [
      {
        "id": 1,
        "productId": 456,
        "productName": "Product Name",
        "quantity": 2,
        "price": 100000,
        "stockQuantity": 50
      }
    ],
    "totalPrice": 200000,
    "itemCount": 1
  },
  "took": 45
}
```

### Error Response
```json
{
  "codeStatus": 322,
  "messageStatus": "ERROR",
  "description": "Không đủ số lượng tồn kho",
  "took": 12
}
```

### Frontend Data (sau khi parse)
```javascript
// parseBusinessResponse() tự động extract data
const cart = {
  id: 1,
  userId: 123,
  items: [...],
  totalPrice: 200000,
  itemCount: 1
}
```

---

## ✅ Testing Checklist

### Backend
- [ ] GET /api/v1/cart - Lấy giỏ hàng
- [ ] POST /api/v1/cart/items - Thêm sản phẩm
  - [ ] Sản phẩm mới
  - [ ] Sản phẩm đã có (tăng quantity)
  - [ ] Không đủ stock → E322_INSUFFICIENT_STOCK
  - [ ] Sản phẩm không tồn tại → E320_PRODUCT_NOT_FOUND
- [ ] PUT /api/v1/cart/items/{id} - Cập nhật số lượng
  - [ ] Quantity > 0
  - [ ] Quantity = 0 (xóa item)
  - [ ] Quantity > stock → E322_INSUFFICIENT_STOCK
  - [ ] Item không tồn tại → E310_CART_ITEM_NOT_FOUND
- [ ] DELETE /api/v1/cart/items/{id} - Xóa item
- [ ] DELETE /api/v1/cart - Xóa toàn bộ giỏ hàng
- [ ] Admin access → E306_ADMIN_NO_CART

### Frontend
- [ ] Thêm sản phẩm từ ProductDetailsPage
- [ ] Hiển thị cart items trong CartPage
- [ ] Tăng/giảm quantity inline
- [ ] Xóa item với confirmation
- [ ] Xóa toàn bộ cart
- [ ] Stock validation (không cho nhập > stockQuantity)
- [ ] Error messages hiển thị đúng (từ BE, đã i18n)
- [ ] Success messages hiển thị
- [ ] Admin không thấy cart icon/page

---

## 🎯 Benefits

1. **Consistent Error Handling**: Tất cả errors đều có code và message i18n
2. **Centralized Messages**: Dễ maintain và update messages
3. **Type Safety**: Error codes là constants, không bị typo
4. **Better UX**: Error messages rõ ràng, đúng ngôn ngữ user
5. **Logging**: Có log đầy đủ cho debugging
6. **Response Time**: Tracking với `took` field
7. **Clean Code**: Loại bỏ duplicate error handling

---

## 📁 Files Changed

### Backend
- ✅ `CartConstant.java` (NEW)
- ✅ `messages_vi.properties` (UPDATED)
- ✅ `messages_en.properties` (UPDATED)
- ✅ `CartServiceImpl.java` (REFACTORED)
- ✅ `CartController.java` (REFACTORED)

### Frontend
- ✅ No changes needed (already uses responseHandler)

---

## 🚀 Next Steps

1. Test thoroughly với các scenarios trên
2. Apply pattern này cho **Category**, **Product**, **Order** modules
3. Document API responses cho team
4. Monitor error rates và response times
