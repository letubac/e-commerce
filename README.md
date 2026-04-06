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

## Admin
<img width="1661" height="872" alt="image" src="https://github.com/user-attachments/assets/e6065205-2283-4e03-b93e-807d7ee1a41d" />
<img width="1589" height="872" alt="image" src="https://github.com/user-attachments/assets/57a40632-30d7-4199-acce-1fa8d8a86817" />
<img width="1632" height="877" alt="image" src="https://github.com/user-attachments/assets/ded0d2c4-2dac-45de-91fd-70fbcf5638ad" />
<img width="1590" height="874" alt="image" src="https://github.com/user-attachments/assets/c6cbd94c-3ae1-46d2-b1c7-5231da0f0cd2" />
<img width="1578" height="823" alt="image" src="https://github.com/user-attachments/assets/cccc4f13-1c44-48b6-9b94-a6b02ac0c2d8" />
<img width="1643" height="643" alt="image" src="https://github.com/user-attachments/assets/74f0163c-c997-42e7-8a68-cc5f22593008" />

## Home
<img width="1666" height="868" alt="image" src="https://github.com/user-attachments/assets/7f004872-2ebb-4f27-b1fc-43cdf807c678" />
<img width="1651" height="871" alt="image" src="https://github.com/user-attachments/assets/d887eb4c-aa98-4a26-91d6-7b6ba531039a" />
<img width="1593" height="868" alt="image" src="https://github.com/user-attachments/assets/576379cf-8b7f-408d-a83c-79ed08eb1c75" />
<img width="1602" height="870" alt="image" src="https://github.com/user-attachments/assets/1eb013a7-f39e-4b1d-9eef-7b56b7fce6ee" />
<img width="1435" height="870" alt="image" src="https://github.com/user-attachments/assets/ecdc5887-e528-4d2f-a4a9-15645f4ba345" />
<img width="1615" height="865" alt="image" src="https://github.com/user-attachments/assets/d214bcb2-134d-40b4-822c-98bb432e1164" />
