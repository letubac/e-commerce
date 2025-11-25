# E-Commerce Backend - SQL Files Approach

## Tổng Quan

Project này đã được refactor để sử dụng **external SQL files** thay vì inline SQL trong Java code. Điều này mang lại nhiều lợi ích:

- **Tách biệt rõ ràng** giữa logic business và SQL queries
- **Dễ maintain** và version control cho SQL
- **Performance tốt hơn** với SQL query caching
- **Hỗ trợ tốt** cho Database Administrators
- **IDE support** cho SQL syntax highlighting

## Cấu Trúc Thư Mục SQL

```
src/main/resources/sql/
├── user/                    # User domain queries
│   ├── insert-user.sql
│   ├── find-by-id.sql
│   ├── find-by-username.sql
│   ├── find-by-email.sql
│   ├── find-all.sql
│   ├── find-by-role.sql
│   ├── find-active.sql
│   ├── find-paginated.sql
│   ├── update-user.sql
│   ├── update-password.sql
│   ├── update-email-verified.sql
│   ├── update-last-login.sql
│   ├── update-status.sql
│   ├── delete-by-id.sql
│   ├── soft-delete.sql
│   ├── exists-by-username.sql
│   ├── exists-by-email.sql
│   ├── count-all.sql
│   └── count-by-role.sql
├── product/                 # Product domain queries
│   ├── insert-product.sql
│   ├── find-by-id.sql
│   ├── find-by-sku.sql
│   ├── find-by-slug.sql
│   ├── find-all.sql
│   ├── find-active.sql
│   ├── find-featured.sql
│   ├── find-by-category.sql
│   ├── find-by-brand.sql
│   ├── find-by-price-range.sql
│   ├── find-low-stock.sql
│   ├── search-by-name.sql
│   ├── find-paginated.sql
│   ├── find-active-paginated.sql
│   ├── find-category-paginated.sql
│   ├── update-product.sql
│   ├── update-stock.sql
│   ├── update-price.sql
│   ├── update-sale-price.sql
│   ├── delete-by-id.sql
│   ├── soft-delete.sql
│   ├── exists-by-sku.sql
│   ├── exists-by-slug.sql
│   ├── count-all.sql
│   ├── count-by-category.sql
│   ├── count-by-brand.sql
│   └── count-active.sql
├── category/               # Category domain queries
│   ├── insert-category.sql
│   ├── find-by-id.sql
│   ├── find-all.sql
│   ├── find-root-categories.sql
│   └── find-by-parent.sql
├── brand/                  # Brand domain queries
│   ├── insert-brand.sql
│   ├── find-by-id.sql
│   └── find-all.sql
├── order/                  # Order domain queries
│   ├── insert-order.sql
│   ├── find-by-id.sql
│   ├── find-by-user.sql
│   ├── insert-order-item.sql
│   ├── find-order-items.sql
│   ├── insert-cart-item.sql
│   └── find-cart-items.sql
└── chat/                   # Chat domain queries
    ├── insert-chat-room.sql
    ├── find-room-by-id.sql
    ├── insert-message.sql
    └── find-messages-by-room.sql
```

## Cách Thức Hoạt Động

### 1. SqlLoader Utility

`SqlLoader` component đọc và cache SQL queries từ files:

```java
@Component
public class SqlLoader {
    public String loadQuery(String queryPath) {
        // Loads SQL from classpath and caches it
    }
}
```

### 2. Repository Implementation

Repositories sử dụng `NamedParameterJdbcTemplate` với external SQL:

```java
@Repository("userRepositoryNewImpl")
public class UserRepositoryNewImpl implements UserRepository {
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    @Autowired 
    private SqlLoader sqlLoader;
    
    @Override
    public Optional<User> findById(Long id) {
        String sql = sqlLoader.loadQuery("sql/user/find-by-id.sql");
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
                
        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                sql, params, new BeanPropertyRowMapper<>(User.class));
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
```

### 3. Named Parameters Trong SQL Files

Tất cả SQL files sử dụng named parameters với syntax `:paramName`:

```sql
-- sql/user/find-by-username.sql
SELECT 
    id, username, email, first_name, last_name, phone, address,
    role, is_active, email_verified, created_at, updated_at
FROM users 
WHERE username = :username AND is_active = true
```

### 4. Service Layer Usage

Services có thể inject và sử dụng các repository mới:

```java
@Service("userServiceWithSqlFiles")
public class UserServiceWithSqlFilesImpl implements UserService {
    
    @Autowired
    private UserRepositoryNewImpl userRepositoryNewImpl;
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        // Sử dụng sql/user/find-by-username.sql
        return userRepositoryNewImpl.findByUsername(username);
    }
}
```

## Lợi Ích

### 1. Separation of Concerns
- SQL logic tách biệt khỏi Java code
- Dễ review và maintain SQL queries
- Database và Application teams có thể work independently

### 2. Performance
- SQL queries được cache trong memory
- Không cần recompile khi thay đổi SQL
- Optimized query execution plans

### 3. Version Control
- SQL changes có thể tracked riêng biệt  
- Code reviews tập trung vào SQL logic
- Rollback dễ dàng cho SQL changes

### 4. IDE Support
- Syntax highlighting cho SQL files
- SQL formatting và validation
- Database connection và testing

### 5. Database Migration
- SQL files có thể reuse cho migrations
- Consistent query patterns
- Easy database dialect changes

## Migration Pattern

Để migrate từ approach cũ sang approach mới:

1. **Tạo SQL files** trong `resources/sql/domain/`
2. **Implement repository mới** sử dụng `SqlLoader`
3. **Update services** để sử dụng repository mới  
4. **Test thoroughly** với existing data
5. **Remove old repository** implementations

## Best Practices

1. **Naming Convention**: `{operation}-{entity}.sql` (e.g., `find-by-username.sql`)
2. **Parameter Names**: Use descriptive names matching entity properties
3. **SQL Formatting**: Consistent indentation và line breaks
4. **Comments**: Document complex queries in SQL files
5. **Testing**: Unit test SQL files với test data
6. **Performance**: Monitor query performance và add indexes

## Example Usage

```java
// Inject the new repository
@Autowired
private UserRepositoryNewImpl userRepository;

// Create user - uses sql/user/insert-user.sql
User newUser = new User("john_doe", "john@example.com", "password123", 
                       "John", "Doe", UserRole.CUSTOMER);
User savedUser = userRepository.save(newUser);

// Find user - uses sql/user/find-by-username.sql  
Optional<User> foundUser = userRepository.findByUsername("john_doe");

// Update password - uses sql/user/update-password.sql
if (foundUser.isPresent()) {
    userRepository.updatePassword(foundUser.get().getId(), "newPassword123");
}

// Check existence - uses sql/user/exists-by-email.sql
boolean exists = userRepository.existsByEmail("john@example.com");
```

Approach này cung cấp một foundation mạnh mẽ và scalable cho database operations trong Spring Boot application với PostgreSQL backend.