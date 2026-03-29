# E-Commerce Backend

A comprehensive Spring Boot REST API backend for e-commerce platform with PostgreSQL, Redis caching, Kafka messaging, and WebSocket chat functionality.

## Features

- ✅ **Spring Boot 3.2** with Java 17
- ✅ **PostgreSQL Database** with raw SQL queries (no JPA)
- ✅ **Redis Caching** and session management
- ✅ **JWT Authentication** and Spring Security
- ✅ **WebSocket** for real-time chat
- ✅ **Apache Kafka** for message queue
- ✅ **Mirage Sql**
- ✅ **Internationalization** (vi/en)
- ✅ **Modular Architecture** (monolithic with modules)
- ✅ **REST API** with comprehensive endpoints

## Architecture

```
src/
├── main/
│   ├── java/com/ecommerce/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Entity models
│   │   ├── repository/     # Data access layer with raw SQL
│   │   ├── service/        # Business logic layer
│   │   ├── security/       # Authentication & authorization
│   │   ├── websocket/      # WebSocket handlers
│   │   └── ECommerceApplication.java
│   └── resources/
│       ├── application.yml  # Application configuration
│       ├── db/migration/   # Flyway SQL migrations
│       └── messages/       # i18n message files
└── test/                   # Test classes
```

## Database Schema

### Core Tables
- `users` - User management with roles (ADMIN, CUSTOMER, SUPPORT)
- `categories` - Hierarchical product categories
- `brands` - Product brands
- `products` - Product catalog with inventory
- `product_images` - Product image management
- `orders` & `order_items` - Order management

### Chat System
- `conversations` - Chat conversations
- `messages` - Chat messages with file support
- `participants` - Conversation participants
- `message_reactions` - Message reactions/emojis
- `quick_replies` - Predefined quick responses
- `chat_settings` - User chat preferences

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 13+**
- **Redis 6+**
- **Apache Kafka 2.8+**

## Quick Start

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE ecommerce_db;

-- Create user (optional)
CREATE USER ecommerce_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
```

### 2. Redis Setup

```bash
# Install and start Redis (Windows with Chocolatey)
choco install redis-64
redis-server

# Or with Docker
docker run -d -p 6379:6379 redis:alpine
```

### 3. Kafka Setup

```bash
# With Docker Compose
docker-compose up -d zookeeper kafka

# Or download and run locally
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 4. Application Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: postgres
    password: postgres
  
  data:
    redis:
      host: localhost
      port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092
```

### 5. Run the Application

```bash
# Install dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start the application
mvn spring-boot:run

# Or run with profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

Application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh JWT token

### Products
- `GET /api/products` - List products with pagination
- `GET /api/products/{id}` - Get product by ID
- `POST /api/admin/products` - Create product (Admin)
- `PUT /api/admin/products/{id}` - Update product (Admin)
- `DELETE /api/admin/products/{id}` - Delete product (Admin)

### Categories & Brands
- `GET /api/categories` - List all categories
- `GET /api/brands` - List all brands
- `POST /api/admin/categories` - Create category (Admin)
- `POST /api/admin/brands` - Create brand (Admin)

### Orders
- `GET /api/orders` - List user orders
- `POST /api/orders` - Create order
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/cancel` - Cancel order

### Chat
- `GET /api/chat/conversations` - List user conversations
- `POST /api/chat/conversations` - Create conversation
- `GET /api/chat/conversations/{id}/messages` - Get messages
- `POST /api/chat/conversations/{id}/messages` - Send message
- `WebSocket /ws/chat` - Real-time chat

### Admin
- `GET /api/admin/users` - List all users
- `GET /api/admin/orders` - List all orders
- `GET /api/admin/dashboard/stats` - Dashboard statistics

## WebSocket Chat

```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

// Subscribe to conversation
stompClient.subscribe('/topic/conversation/123', (message) => {
    const chatMessage = JSON.parse(message.body);
    console.log('New message:', chatMessage);
});

// Send message
stompClient.send('/app/chat/send', {}, JSON.stringify({
    conversationId: 123,
    content: 'Hello!',
    messageType: 'TEXT'
}));
```

## Internationalization

The application supports multiple languages:

```java
// In controller
@Autowired
private MessageSource messageSource;

// Get localized message
String message = messageSource.getMessage("user.created", null, locale);
```

```http
# Request with language header
GET /api/products
Accept-Language: vi-VN
```

## Caching Strategy

- **Redis Sessions** - User session management
- **Product Caching** - Frequently accessed products
- **Category Caching** - Category hierarchy
- **User Profile Caching** - Authenticated user data

## Message Queue Events

- **order-events** - Order lifecycle events
- **chat-events** - Chat message notifications
- **notification-events** - Push notifications
- **email-events** - Email sending queue

## Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn test -Dtest=**/*IntegrationTest

# Run with test profile
mvn test -Dspring.profiles.active=test
```

## Docker Support

```dockerfile
# Build image
docker build -t ecommerce-backend .

# Run container
docker run -p 8080:8080 ecommerce-backend

# With environment variables
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_REDIS_HOST=redis-host \
  ecommerce-backend
```

## Development

### Code Style
- Java 17 features
- Spring Boot conventions
- RESTful API design
- Clean Architecture principles
- Comprehensive error handling

### Database Migrations

```bash
# Create new migration
# File: src/main/resources/db/migration/V4__Add_new_table.sql

# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info
```

## Monitoring & Health

- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Database Health**: `GET /actuator/health/db`
- **Redis Health**: `GET /actuator/health/redis`

## Security

- **JWT Authentication** with refresh tokens
- **Role-based Authorization** (ADMIN, CUSTOMER, SUPPORT)
- **CORS Configuration** for frontend integration
- **Session Management** with Redis
- **Password Encryption** with BCrypt
- **SQL Injection Prevention** with parameterized queries

## Deployment

### Production Configuration

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  data:
    redis:
      host: ${REDIS_HOST}
      password: ${REDIS_PASSWORD}
  
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS}

logging:
  level:
    com.ecommerce: INFO
```

### Environment Variables

```bash
export DB_URL=jdbc:postgresql://prod-db:5432/ecommerce
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=secure_password
export REDIS_HOST=prod-redis
export KAFKA_SERVERS=kafka1:9092,kafka2:9092
export JWT_SECRET=your-secret-key
```

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.# e-commerce
