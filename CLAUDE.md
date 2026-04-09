# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a graduation project - an e-commerce platform (java_shop) with B/S architecture.
- **Backend**: Spring Boot 3.0.2 + MyBatis-Plus + LangChain4j (AI features)
- **Frontend**: Vue 3 + Vite + Ant Design Vue
- **Database**: MySQL 5.7+ and Redis (for AI chat memory, optional RAG vector store)

## Quick Start

### Backend
1. Open `server` directory in IntelliJ IDEA
2. Configure `application.yml`:
   - `DB_NAME`: Database name (default: `java_shop`)
   - `BASE_LOCATION`: Base directory for file uploads
   - Database credentials (default: root/123456)
   - Redis credentials (default: 123456)
3. Create MySQL database: `CREATE DATABASE IF NOT EXISTS java_shop DEFAULT CHARSET utf8 COLLATE utf8_general_ci`
4. Restore SQL data from `java_shop.sql`
5. Run: `java -jar` via IDEA run button or `mvn spring-boot:run`

### Frontend
1. Install Node.js 16+
2. Navigate to `web` directory
3. Install dependencies: `npm install`
4. Run dev server: `npm run dev`

### Access URLs
- Frontend (customer): `http://localhost:3000/#/index`
- Frontend (admin): `http://localhost:3000/#/admin`
- API docs: `http://localhost:9100/api/swagger-ui/index.html`
- Druid monitoring: `http://localhost:9100/api/druid/index.html`

### Admin Login
- Username: 汤弘正
- Password: Thz20031001

## Build & Deploy

```bash
# Backend
mvn clean package
java -jar -Xms64m -Xmx128m target/springdemo-0.0.1-SNAPSHOT.jar

# Frontend
npm run build
```

## Architecture

### Backend Structure (`server/src/main/java/com/gk/study`)
```
├── controller/       # REST API endpoints (one per domain: Thing, Order, User, etc.)
├── service/          # Business logic interfaces
├── service/impl/     # Service implementations
├── mapper/           # MyBatis-Plus mappers (extends BaseMapper<T>)
├── entity/           # JPA entities with @TableName annotations
├── common/           # APIResponse wrapper, ResponseCode enums
├── interceptor/      # Access control interceptor
├── permission/       # @Access annotation and AccessLevel enum
├── config/           # Spring configurations (Redis, CORS, MyBatis)
├── ai/               # LangChain4j AI features
│   ├── config/       # AI model and chat memory configuration
│   ├── rag/          # RAG knowledge base retrieval
│   └── tool/         # AI tool integrations
└── utils/            # Utility classes (IpUtils, JsonUtils, etc.)
```

### Frontend Structure (`web/src`)
```
├── api/              # Axios API client definitions
├── router/           # Vue Router configurations
├── store/            # Pinia state management
├── utils/            # Utilities (http client, constants)
├── views/            # Page components
│   ├── admin/        # Admin management pages
│   └── ...           # Customer-facing pages
└── App.vue, main.js  # Entry points
```

### Key Patterns
- **CRUD Standard Flow**: entity -> mapper -> service -> controller -> frontend API -> Vue component
- **Permission Control**: Use `@Access(level = AccessLevel.ADMIN/LOGIN/DEMO)` on controller methods
- **Response Format**: All APIs return `APIResponse` wrapper with code/data/message
- **File Upload**: Multipart uploads handled via `imageFile` field in entities

## Configuration Files
- `server/src/main/resources/application.yml`: Main Spring Boot config
- `server/src/main/resources/logback-spring.xml`: Logging configuration
- `web/vite.config.js`: Vite build config
- `web/src/store/constants.js`: Frontend API base URL

## AI Features
- Customer service chat with Redis-backed memory
- RAG (Retrieval-Augmented Generation) using LangChain4j
- Configured for Alibaba Cloud Bailian (DashScope) API
- Embedding model: text-embedding-v3, Chat model: qwen3.5-flash

## Common Issues
- Run `mvn clean` if `application.yml` changes aren't picked up
- Use MySQL 5.7+ (required for UTF8MB4)
- Set `npm config set registry https://registry.npm.taobao.org` for China npm mirror
