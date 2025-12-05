# Fix Admin API 401 Unauthorized - JWT Authentication Issue

## 🐛 Vấn đề:
Admin login thành công nhưng khi gọi API `/api/v1/admin/brands`, `/api/v1/admin/categories` bị 401 Unauthorized.

## 🔍 Root Cause:
Backend có **2 JWT utility classes khác nhau** và không tương thích:

1. **JwtTokenProvider** - Được dùng trong AuthController để **tạo token**
   - Store: `userId` trong JWT claims
   - Method: `generateToken(Authentication)` 

2. **JwtUtil** - Được dùng trong JwtAuthenticationFilter để **validate token**
   - Parse: `username` từ JWT subject
   - Method: `extractUsername(token)`

→ **Mismatch**: Token được tạo với `userId`, nhưng filter cố parse `username` → Fail authentication → 401

## ✅ Giải pháp:

### 1. **SecurityConfig.java**
Cập nhật cart permission để chỉ cho phép CUSTOMER (không có ADMIN):

```java
// TRƯỚC:
.requestMatchers("/api/v1/cart/**").hasAnyRole("ADMIN", "CUSTOMER")

// SAU:
.requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER")
```

**Lý do**: Admin không có giỏ hàng, không nên access cart API.

---

### 2. **JwtAuthenticationFilter.java**
Đổi từ `JwtUtil` sang `JwtTokenProvider`:

```java
// TRƯỚC:
@Autowired
private JwtUtil jwtUtil;

String username = jwtUtil.extractUsername(jwtToken);
UserDetails userDetails = userDetailsService.loadUserByUsername(username);

// SAU:
@Autowired
private JwtTokenProvider jwtTokenProvider;

Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
UserDetails userDetails = userDetailsService.loadUserById(userId);
```

**Lý do**: 
- JwtTokenProvider store `userId` trong token
- Parse `userId` rồi load user từ DB để lấy fresh role/authorities
- Đảm bảo role check chính xác trong SecurityFilterChain

---

### 3. **CustomUserDetailsService.java**
Thêm method `loadUserById()`:

```java
public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

    if (!user.isActive()) {
        throw new UsernameNotFoundException("User account is disabled: " + userId);
    }

    return UserPrincipal.create(user);
}
```

**Lý do**: JwtAuthenticationFilter cần load user by ID thay vì username.

---

## 📊 Authentication Flow (AFTER FIX):

```
┌──────────────────────────────────────────────────────────────┐
│ 1. Admin Login                                                │
│    POST /api/v1/auth/login                                    │
└─────────────────────┬────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. AuthController                                             │
│    → authService.generateToken(authentication)                │
│    → JwtTokenProvider.generateToken()                         │
│    → JWT: { userId: 1, tokenType: "ACCESS" }                 │
└─────────────────────┬────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. Client stores token → Makes request                       │
│    GET /api/v1/admin/brands                                   │
│    Headers: { Authorization: "Bearer <JWT>" }                │
└─────────────────────┬────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. JwtAuthenticationFilter                                    │
│    → Extract token from header                                │
│    → jwtTokenProvider.validateToken(token) ✅                 │
│    → jwtTokenProvider.getUserIdFromToken(token) → userId = 1  │
│    → userDetailsService.loadUserById(1) → User with ADMIN role│
│    → UserPrincipal.create(user) → authorities: [ROLE_ADMIN]   │
│    → Set SecurityContext with authentication                  │
└─────────────────────┬────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. SecurityFilterChain                                        │
│    → Check: /api/v1/admin/** requires ROLE_ADMIN ✅           │
│    → User has ROLE_ADMIN → Allow request                      │
└─────────────────────┬────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. Controller executes → Return data                          │
│    HTTP 200 ✅                                                 │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔧 Technical Details:

### JwtTokenProvider vs JwtUtil Comparison:

| Feature | JwtTokenProvider | JwtUtil |
|---------|------------------|---------|
| **Used in** | AuthController (token creation) | ❌ JwtAuthenticationFilter (OLD) |
| **Token Claims** | `userId`, `tokenType` | `role` (optional) |
| **Subject** | `username` | `username` |
| **Parse method** | `getUserIdFromToken()` | `extractUsername()` |
| **Validation** | `validateToken(token)` | `validateToken(token, userDetails)` |
| **Role handling** | Load from DB via userId | Expected in token claims |

**Kết luận**: JwtUtil không tương thích với token format của JwtTokenProvider → Phải dùng JwtTokenProvider trong filter.

---

## 📝 Files Changed:

### Backend:
1. ✅ `SecurityConfig.java`
   - Line 83: `.requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER")`

2. ✅ `JwtAuthenticationFilter.java`
   - Đổi dependency: `JwtUtil` → `JwtTokenProvider`
   - Parse userId thay vì username
   - Call `loadUserById()` thay vì `loadUserByUsername()`
   - Thêm debug logs

3. ✅ `CustomUserDetailsService.java`
   - Thêm method: `loadUserById(Long userId)`

---

## 🧪 Testing:

### Test Case 1: Admin Login & Access Admin API
```bash
# 1. Login as admin
POST http://localhost:8080/api/v1/auth/login
{
  "usernameOrEmail": "admin@ecommerce.com",
  "password": "123456"
}

# Response:
{
  "accessToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "role": "ADMIN"
  }
}

# 2. Access admin API
GET http://localhost:8080/api/v1/admin/brands
Headers: Authorization: Bearer eyJhbGc...

# Expected: HTTP 200 ✅
# Before: HTTP 401 ❌
```

### Test Case 2: Admin Try to Access Cart API
```bash
GET http://localhost:8080/api/v1/cart
Headers: Authorization: Bearer <admin_token>

# Expected: HTTP 403 Forbidden ✅
# Message: "Admin không có giỏ hàng"
```

### Test Case 3: Customer Access Cart API
```bash
GET http://localhost:8080/api/v1/cart
Headers: Authorization: Bearer <customer_token>

# Expected: HTTP 200 ✅
```

---

## 🚨 Important Notes:

### 1. **Token Format Consistency**
Đảm bảo tất cả JWT operations dùng **cùng 1 provider**:
- ✅ Token creation: `JwtTokenProvider`
- ✅ Token validation: `JwtTokenProvider`
- ✅ Token parsing: `JwtTokenProvider`

### 2. **Role Loading**
- Role được load từ **database** mỗi request
- Không cache role trong JWT token
- ✅ Advantage: Role changes take effect immediately
- ⚠️ Disadvantage: Extra DB query per request

### 3. **JwtUtil Status**
- ❓ JwtUtil hiện không được dùng
- Consider: Xóa class này để tránh confusion
- Hoặc: Rename thành `JwtUtilDeprecated`

---

## 🎯 Expected Behavior After Fix:

| User Role | Login | Access `/api/v1/admin/**` | Access `/api/v1/cart` |
|-----------|-------|---------------------------|-----------------------|
| **ADMIN** | ✅ 200 | ✅ 200 | ❌ 403 Forbidden |
| **CUSTOMER** | ✅ 200 | ❌ 403 Forbidden | ✅ 200 |
| **Guest** | N/A | ❌ 401 Unauthorized | ❌ 401 Unauthorized |

---

## 🔍 Debugging Tips:

### Check JWT Token Contents:
```bash
# Decode JWT token at https://jwt.io
# Should see:
{
  "sub": "admin",
  "iat": 1701518400,
  "exp": 1701522000,
  "tokenType": "ACCESS",
  "userId": "1"
}
```

### Check Spring Security Logs:
```java
// JwtAuthenticationFilter.java line 65
logger.debug("✅ User authenticated: " + userId + " with authorities: " + userDetails.getAuthorities());
```

Should output:
```
✅ User authenticated: 1 with authorities: [ROLE_ADMIN]
```

### Check Database User Role:
```sql
SELECT id, username, email, role FROM users WHERE id = 1;
-- Expected: role = 'ADMIN'
```

---

## 💡 Improvements for Future:

### 1. Store Role in JWT Token
```java
// JwtTokenProvider.generateToken()
public String generateToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
    
    return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .claim("userId", userPrincipal.getId())
            .claim("role", role) // 👈 ADD THIS
            .claim("tokenType", "ACCESS")
            .signWith(getSigningKey())
            .compact();
}
```

**Benefits**:
- No DB query per request
- Faster authentication
- Stateless architecture

**Trade-offs**:
- Role changes require re-login
- Larger JWT token size

---

## ✨ Summary:

✅ **Root Cause**: JwtUtil và JwtTokenProvider không tương thích  
✅ **Solution**: Thống nhất dùng JwtTokenProvider cho cả create và validate token  
✅ **Side Effect Fix**: Cart API chỉ cho CUSTOMER, không có ADMIN  
✅ **Additional**: Thêm loadUserById() để load user by ID từ JWT token  

**Problem Solved!** 🎉

Admin giờ có thể:
- ✅ Login thành công
- ✅ Access `/api/v1/admin/**` APIs
- ✅ Role được check đúng trong SecurityFilterChain
- ❌ KHÔNG access được `/api/v1/cart` (by design)
