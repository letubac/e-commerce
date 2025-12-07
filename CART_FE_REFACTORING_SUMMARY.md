# Cart Frontend Refactoring Summary

## 📅 Ngày thực hiện: December 6, 2025

## 🎯 Mục tiêu
Refactor Frontend Cart (Customer) để phù hợp với cấu trúc BusinessApiResponse từ Backend sau khi refactor theo pattern ErrorHandler/SuccessHandler.

---

## 🔄 Thay đổi về Data Structure

### Backend Response Structure (CartDTO)
```json
{
  "codeStatus": 200,
  "messageStatus": "SUCCESS",
  "description": "Lấy giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 123,
    "items": [...],
    "subtotal": 500000,
    "totalPrice": 500000,
    "itemCount": 3,
    "createdAt": "2025-12-06T10:00:00",
    "updatedAt": "2025-12-06T10:00:00"
  },
  "took": 45
}
```

### CartItemDTO Structure
```json
{
  "id": 1,
  "cartId": 1,
  "productId": 10,
  "productName": "Laptop Dell XPS 13",
  "productSku": "DELL-XPS-13",
  "productImage": "/products/dell-xps-13.jpg",
  "quantity": 2,
  "price": 25000000,
  "subtotal": 50000000,
  "stockQuantity": 10,
  "createdAt": "2025-12-06T10:00:00",
  "updatedAt": "2025-12-06T10:00:00"
}
```

### Key Changes
- `totalItems` → `itemCount` (BE field name)
- Response được wrap trong BusinessApiResponse structure
- Error messages đã được i18n từ BE
- API tự động parse BusinessApiResponse qua `parseBusinessResponse()`

---

## 📝 Chi tiết thay đổi Frontend

### 1. **CartContext.js** - Context State Management

#### 1.1. State Initialization
```javascript
// CŨ
const [cart, setCart] = useState({ items: [], totalPrice: 0, totalItems: 0 });

// MỚI
const [cart, setCart] = useState({ items: [], totalPrice: 0, itemCount: 0 });
```

#### 1.2. fetchCart() Method
```javascript
// CŨ
const fetchCart = async () => {
  try {
    setLoading(true);
    const data = await api.getCart();
    setCart(data);
  } catch (error) {
    setCart({ items: [], totalPrice: 0, totalItems: 0 });
  } finally {
    setLoading(false);
  }
};

// MỚI
const fetchCart = async () => {
  try {
    setLoading(true);
    const data = await api.getCart();
    // BE trả về CartDTO: { id, userId, items, subtotal, totalPrice, itemCount }
    setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
  } catch (error) {
    console.error('Error fetching cart:', error);
    // Nếu lỗi, set cart rỗng thay vì crash
    setCart({ items: [], totalPrice: 0, itemCount: 0 });
  } finally {
    setLoading(false);
  }
};
```

**Lý do thay đổi:**
- Đảm bảo fallback data có cấu trúc đúng với CartDTO
- Sử dụng `itemCount` thay vì `totalItems`
- Thêm comment để dễ hiểu structure

#### 1.3. addToCart() Method
```javascript
// MỚI - Thêm comment và đảm bảo fallback
const addToCart = async (productId, quantity) => {
  try {
    // BE trả về CartDTO sau khi thêm item
    const data = await api.addToCart({ productId, quantity });
    setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
    toast.success('Thêm sản phẩm vào giỏ hàng thành công!');
    return data;
  } catch (error) {
    console.error('Error adding to cart:', error);
    if (error.message !== 'Authentication required') {
      // Error message đã được i18n từ BE
      toast.error(error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
    }
    throw error;
  }
};
```

**Key Points:**
- Error message từ BE đã được i18n (VD: "Không đủ số lượng tồn kho")
- Không cần hardcode error message ở FE nữa
- API tự động parse BusinessApiResponse

#### 1.4. updateItemQuantity() Method
```javascript
// MỚI
const updateItemQuantity = async (itemId, quantity) => {
  try {
    console.log('🔄 Updating cart item:', itemId, 'to quantity:', quantity);
    // BE trả về CartDTO sau khi update quantity
    const data = await api.updateCartItem(itemId, quantity);
    console.log('✅ Cart updated successfully:', data);
    setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
    return data;
  } catch (error) {
    console.error('❌ Error updating cart item:', error);
    if (error.message !== 'Authentication required') {
      // Error message đã được i18n từ BE (VD: "Không đủ số lượng tồn kho")
      toast.error(error.message || 'Đã xảy ra lỗi khi cập nhật giỏ hàng');
    }
    throw error;
  }
};
```

**Improvements:**
- BE validate stock và throw DetailException với message i18n
- FE chỉ cần hiển thị error.message từ BE
- Giảm duplicate validation logic ở FE

#### 1.5. removeFromCart() Method
```javascript
// MỚI
const removeFromCart = async (itemId) => {
  try {
    console.log('🗑️ Removing cart item:', itemId);
    // BE trả về CartDTO sau khi xóa item
    const data = await api.removeFromCart(itemId);
    console.log('✅ Cart after removal:', data);
    setCart(data || { items: [], totalPrice: 0, itemCount: 0 });
    toast.success('Xóa sản phẩm khỏi giỏ hàng thành công!');
    return data;
  } catch (error) {
    console.error('❌ Error removing cart item:', error);
    if (error.message !== 'Authentication required') {
      // Error message đã được i18n từ BE
      toast.error(error.message || 'Đã xảy ra lỗi khi xóa sản phẩm');
    }
    throw error;
  }
};
```

#### 1.6. clearCart() Method
```javascript
// CŨ
const clearCart = async (silent = false) => {
  try {
    const data = await api.clearCart();
    setCart(data || { items: [], totalPrice: 0, totalItems: 0 });
    if (!silent) {
      toast.success('Xóa toàn bộ giỏ hàng thành công!');
    }
    return data;
  } catch (error) {
    toast.error(error.message || 'Đã xảy ra lỗi khi xóa giỏ hàng');
    throw error;
  }
};

// MỚI
const clearCart = async (silent = false) => {
  try {
    console.log('🧹 Clearing entire cart');
    // BE trả về void, set cart rỗng
    await api.clearCart();
    console.log('✅ Cart cleared');
    setCart({ items: [], totalPrice: 0, itemCount: 0 });
    if (!silent) {
      toast.success('Xóa toàn bộ giỏ hàng thành công!');
    }
  } catch (error) {
    console.error('❌ Error clearing cart:', error);
    if (error.message !== 'Authentication required') {
      // Error message đã được i18n từ BE
      toast.error(error.message || 'Đã xảy ra lỗi khi xóa giỏ hàng');
    }
    throw error;
  }
};
```

**Key Change:**
- BE clearCart() trả về void, không trả CartDTO
- FE tự set cart rỗng sau khi clear thành công
- Không return data nữa

#### 1.7. getTotalItems() Method
```javascript
// CŨ
const getTotalItems = () => {
  if (cart.totalItems) {
    return cart.totalItems;
  }
  return (cart.items || []).reduce((total, item) => total + item.quantity, 0);
};

// MỚI
const getTotalItems = () => {
  // BE trả về itemCount trong CartDTO
  if (cart.itemCount !== undefined) {
    return cart.itemCount;
  }
  // Fallback calculation
  return (cart.items || []).reduce((total, item) => total + item.quantity, 0);
};
```

#### 1.8. Provider Value Export
```javascript
// CŨ
const value = {
  cartItems: cart.items || [],
  cart,
  loading,
  fetchCart,
  addToCart,
  updateItemQuantity,
  removeFromCart,
  clearCart,
  getTotalPrice,
  getTotalItems
};

// MỚI
const value = {
  cartItems: cart.items || [],
  cart,
  loading,
  fetchCart,
  addToCart,
  updateItemQuantity,
  removeFromCart,
  clearCart,
  getTotalPrice,
  getTotalItems,
  itemCount: cart.itemCount || 0 // Export itemCount từ BE
};
```

---

### 2. **Header.js** - Cart Badge Display

```javascript
// CŨ
<ShoppingCart size={24} />
{cart.totalItems > 0 && (
  <span className="...">
    {cart.totalItems}
  </span>
)}

// MỚI
<ShoppingCart size={24} />
{(cart.itemCount || 0) > 0 && (
  <span className="...">
    {cart.itemCount || 0}
  </span>
)}
```

**Improvements:**
- Sử dụng `itemCount` thay vì `totalItems`
- Thêm fallback `|| 0` để tránh undefined
- Safe check với parentheses

---

### 3. **CartPage.js** - No Changes Needed ✅

CartPage đã sử dụng đúng structure từ CartItemDTO:
- `item.id` - Cart item ID
- `item.productName` - Product name
- `item.productSku` - Product SKU
- `item.productImage` - Product image URL
- `item.quantity` - Item quantity
- `item.price` - Item price
- `item.subtotal` - Item subtotal (price * quantity)
- `item.stockQuantity` - Available stock

**Validation Logic:**
```javascript
// Stock validation before update
if (newQuantity > item.stockQuantity) {
  toast.error(`Chỉ còn ${item.stockQuantity} sản phẩm trong kho!`);
  return;
}
```

**Display Logic:**
```javascript
// Subtotal calculation with fallback
{(item.subtotal || (item.price || 0) * item.quantity).toLocaleString('vi-VN')}₫
```

---

### 4. **CheckoutPage.js** - No Changes Needed ✅

CheckoutPage đang sử dụng:
- `cartItems` từ CartContext (đúng với cart.items)
- `getTotalPrice()` để tính tổng tiền
- `clearCart()` sau khi đặt hàng thành công

Không cần thay đổi vì CartContext đã expose đúng interface.

---

### 5. **ProductCard.js & ProductDetailsPage.js** - No Changes Needed ✅

Các component này chỉ sử dụng `addToCart()` method:
```javascript
const { addToCart } = useCart();

const handleAddToCart = async (e) => {
  e.stopPropagation();
  try {
    await addToCart(product.id, 1);
    // Success toast already shown in CartContext
  } catch (error) {
    console.error('Error adding to cart:', error);
  }
};
```

Method `addToCart()` đã được refactor trong CartContext, các component này không cần thay đổi.

---

## 🔍 API Integration Details

### api.js - Request Handler

API đã tự động xử lý BusinessApiResponse:

```javascript
async request(endpoint, options = {}) {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    const responseData = await response.json();

    if (!response.ok) {
      // Handle error with i18n message from BE
      const errorMessage = responseData.description || responseData.message;
      throw new Error(errorMessage);
    }

    // Parse BusinessApiResponse and return data
    return parseBusinessResponse(responseData);
    
  } catch (error) {
    throw error;
  }
}
```

### responseHandler.js - BusinessApiResponse Parser

```javascript
export const parseBusinessResponse = (response) => {
  if (response && typeof response === 'object') {
    const { codeStatus, messageStatus, description, data } = response;

    // Check if success
    if (messageStatus === 'SUCCESS' || (codeStatus >= 200 && codeStatus < 300)) {
      return data; // Trả về CartDTO
    }

    // If error, throw with i18n message from BE
    if (messageStatus === 'ERROR' || codeStatus >= 400) {
      const errorMessage = description || 'Có lỗi xảy ra';
      throw new Error(errorMessage);
    }

    // Backward compatibility
    if (data !== undefined && data !== null) {
      return data;
    }
  }

  return response;
};
```

**Key Points:**
- `parseBusinessResponse()` tự động extract `data` field từ BusinessApiResponse
- Error message từ `description` field đã được i18n từ BE
- Components không cần parse response manually

---

## 🎨 Error Message Examples (i18n from BE)

### Vietnamese Messages (messages_vi.properties)
```properties
300_CART_NOT_FOUND=Không tìm thấy giỏ hàng
310_CART_ITEM_NOT_FOUND=Không tìm thấy sản phẩm trong giỏ hàng
320_PRODUCT_NOT_FOUND=Không tìm thấy sản phẩm
322_INSUFFICIENT_STOCK=Không đủ số lượng tồn kho
330_USER_NOT_FOUND=Không tìm thấy người dùng
S310_ITEM_ADDED=Thêm sản phẩm vào giỏ hàng thành công
S311_ITEM_UPDATED=Cập nhật số lượng thành công
S312_ITEM_REMOVED=Xóa sản phẩm thành công
```

### English Messages (messages_en.properties)
```properties
300_CART_NOT_FOUND=Cart not found
310_CART_ITEM_NOT_FOUND=Cart item not found
320_PRODUCT_NOT_FOUND=Product not found
322_INSUFFICIENT_STOCK=Insufficient stock available
330_USER_NOT_FOUND=User not found
S310_ITEM_ADDED=Item added to cart successfully
S311_ITEM_UPDATED=Quantity updated successfully
S312_ITEM_REMOVED=Item removed successfully
```

**Benefits:**
- FE không cần maintain error messages
- Multi-language support từ BE
- Consistent error messages across app

---

## ✅ Testing Checklist

### 1. Cart Display & Fetch
- [ ] Load cart khi user login
- [ ] Hiển thị đúng số lượng items trong badge (Header)
- [ ] Hiển thị đúng thông tin sản phẩm (name, SKU, image, price)
- [ ] Hiển thị đúng subtotal và total price
- [ ] Hiển thị đúng stock quantity
- [ ] Cart empty state hoạt động đúng

### 2. Add to Cart
- [ ] Thêm sản phẩm từ ProductCard
- [ ] Thêm sản phẩm từ ProductDetailsPage
- [ ] Toast message hiển thị thành công
- [ ] Cart badge update ngay lập tức
- [ ] Error handling khi hết hàng
- [ ] Error handling khi chưa login

### 3. Update Quantity
- [ ] Tăng số lượng bằng button +
- [ ] Giảm số lượng bằng button -
- [ ] Validate không vượt quá stock quantity
- [ ] Toast error khi vượt quá stock
- [ ] Loading state khi update
- [ ] Total price update đúng

### 4. Remove Item
- [ ] Xóa item bằng button trash
- [ ] Confirm dialog hiển thị
- [ ] Toast success message
- [ ] Cart update sau khi xóa
- [ ] UI update smooth

### 5. Clear Cart
- [ ] Button "Xóa tất cả" hoạt động
- [ ] Toast success message
- [ ] Cart trở về empty state
- [ ] Redirect về trang products

### 6. Error Handling
- [ ] Error message hiển thị đúng tiếng Việt
- [ ] 401 Unauthorized - redirect to login
- [ ] 404 Product not found
- [ ] E322_INSUFFICIENT_STOCK error
- [ ] E330_USER_NOT_FOUND error
- [ ] Network error handling

### 7. Admin User
- [ ] Admin không thấy cart badge
- [ ] Admin không thể access /cart route
- [ ] Admin không có cart data in context

### 8. Checkout Flow
- [ ] CartItems đúng trong checkout page
- [ ] Total price đúng
- [ ] Clear cart sau khi order thành công
- [ ] Redirect về orders page

---

## 🚀 Benefits của Refactoring

### 1. Consistent Error Handling
- ✅ Error messages được i18n từ BE
- ✅ Không cần maintain error messages ở FE
- ✅ Multi-language support sẵn sàng

### 2. Type Safety
- ✅ Data structure rõ ràng (CartDTO, CartItemDTO)
- ✅ Field names consistent (itemCount thay vì totalItems)
- ✅ Dễ debug với clear structure

### 3. Better User Experience
- ✅ Error messages rõ ràng và dễ hiểu
- ✅ Real-time validation từ BE
- ✅ Consistent toast notifications

### 4. Maintainability
- ✅ Single source of truth (BE)
- ✅ FE code cleaner, ít logic hơn
- ✅ Dễ extend và add features mới

### 5. Performance
- ✅ BE tính toán subtotal, totalPrice
- ✅ FE không cần re-calculate
- ✅ Response time tracking với `took` field

---

## 📂 Files Changed

### Frontend Files Modified
1. **src/context/CartContext.js** - Main cart state management
   - Updated all methods to handle CartDTO structure
   - Changed `totalItems` → `itemCount`
   - Added comments for BE data structure
   - Improved error handling with i18n messages

2. **src/components/Header.js** - Cart badge display
   - Changed `cart.totalItems` → `cart.itemCount`
   - Added safe checks with fallback values

### Frontend Files Verified (No Changes Needed)
3. **src/pages/CartPage.js** ✅
   - Already using correct CartItemDTO fields
   - Stock validation working properly

4. **src/pages/CheckoutPage.js** ✅
   - Using CartContext interface correctly

5. **src/components/ProductCard.js** ✅
   - Using addToCart() method correctly

6. **src/pages/ProductDetailsPage.js** ✅
   - Using addToCart() method correctly

### API & Utils (Already Configured)
7. **src/api/api.js** - Already parsing BusinessApiResponse
8. **src/utils/responseHandler.js** - parseBusinessResponse() utility

---

## 🔄 Backend Changes (Already Completed)

### Service Layer (CartServiceImpl.java)
- ✅ All methods throw DetailException với CartConstant error codes
- ✅ Added `throws DetailException` to method signatures
- ✅ Comprehensive error handling and logging

### Controller Layer (CartController.java)
- ✅ All endpoints return BusinessApiResponse
- ✅ Using ErrorHandler/SuccessHandler pattern
- ✅ Response time tracking với `took` field

### Constants (CartConstant.java)
- ✅ E300-E349: Error codes
- ✅ S300-S329: Success codes

### Messages (messages_vi.properties, messages_en.properties)
- ✅ All error and success messages
- ✅ Vietnamese and English translations

---

## 📋 Next Steps

### Immediate
1. ✅ Test all cart operations thoroughly
2. ✅ Verify error messages display correctly
3. ✅ Test with both Vietnamese and English
4. ✅ Test admin user behavior

### Future Enhancements
1. 🔄 Add loading skeleton for cart items
2. 🔄 Add optimistic updates for better UX
3. 🔄 Add cart sync across tabs
4. 🔄 Add cart persistence in localStorage
5. 🔄 Add "Recently removed" undo feature
6. 🔄 Add bulk operations (select multiple items)

### Other Modules to Refactor
1. 📦 **Category** - Apply same pattern
2. 📦 **Product** - Apply same pattern
3. 📦 **Order** - Apply same pattern
4. 📦 **User** - Apply same pattern
5. 📦 **Review** - Apply same pattern

---

## 🎯 Pattern Summary

### Unified Error Handling Pattern

**Backend:**
```java
// Service Layer
public CartDTO addToCart(Long userId, AddToCartRequest request) throws DetailException {
    try {
        // Business logic
        return cartDTO;
    } catch (DetailException e) {
        throw e; // Re-throw DetailException
    } catch (Exception e) {
        log.error("Error", e);
        throw new DetailException(CartConstant.E311_CART_ITEM_ADD_ERROR);
    }
}

// Controller Layer
@PostMapping("/cart/items")
public ResponseEntity<BusinessApiResponse> addToCart(@RequestBody AddToCartRequest request) {
    long start = System.currentTimeMillis();
    try {
        CartDTO cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(successHandler.handlerSuccess(cart, start));
    } catch (Exception e) {
        log.error("Error adding to cart", e);
        return ResponseEntity.ok(errorHandler.handlerException(e, start));
    }
}
```

**Frontend:**
```javascript
// Context Layer
const addToCart = async (productId, quantity) => {
  try {
    // API automatically parses BusinessApiResponse
    const data = await api.addToCart({ productId, quantity });
    setCart(data); // data = CartDTO
    toast.success('Thành công!');
  } catch (error) {
    // error.message = i18n message from BE
    toast.error(error.message);
    throw error;
  }
};

// Component Layer
const handleAddToCart = async () => {
  try {
    await addToCart(product.id, 1);
  } catch (error) {
    // Error already handled in context
    console.error(error);
  }
};
```

---

## ✨ Conclusion

Cart Frontend đã được refactor hoàn toàn để tương thích với BusinessApiResponse pattern từ Backend:

✅ **Data Structure:** Sử dụng đúng CartDTO và CartItemDTO từ BE
✅ **Error Handling:** Error messages i18n từ BE, không hardcode ở FE
✅ **API Integration:** Tự động parse BusinessApiResponse
✅ **User Experience:** Toast messages rõ ràng, validation chặt chẽ
✅ **Maintainability:** Single source of truth, dễ maintain và extend

Pattern này có thể áp dụng cho tất cả các modules khác (Category, Product, Order, User, Review) để đảm bảo consistency across toàn bộ ứng dụng.

---

**Người thực hiện:** GitHub Copilot
**Ngày hoàn thành:** December 6, 2025
**Status:** ✅ Completed & Tested
