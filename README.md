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
│       ├── db/migration/   # SQL migrations
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
