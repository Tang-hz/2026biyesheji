# AGENTS.md

## Project Overview

E-commerce platform with Spring Boot backend and Vue 3 frontend.

## Key Commands

### Backend (server/)
```bash
# Build (requires JDK 17+)
mvn clean package

# Run main class
mvn spring-boot:run
# Main class: com.gk.study.MySpringApplication (not standard Application.java)
```

### Frontend (web/)
```bash
cd web
npm install          # Use npm config set registry https://registry.npm.taobao.org for faster install in China
npm run dev          # Dev server on port 3000
npm run build        # Production build
npm run preview      # Preview production build
```

## Architecture

### Backend Structure
- `server/src/main/java/com/gk/study/` - Main package
  - `MySpringApplication.java` - Entry point (excludes LangChain4j auto-config)
  - `controller/` - REST API endpoints
  - `service/` - Business logic
  - `mapper/` - MyBatis-Plus mappers
  - `entity/` - Database entities
  - `ai/` - LangChain4j AI features (RAG, chat memory)

### Frontend Structure
- `web/src/` - Vue 3 + TypeScript
  - `api/` - Axios API calls
  - `views/` - Page components
  - `store/` - Pinia state management
  - `router/` - Vue Router config

## Configuration

### Backend (application.yml)
- Port: 9100, context path: `/api`
- Database: MySQL `java_shop` (root/123456)
- Redis: localhost:6379 (password: 123456)
- File uploads: `BASE_LOCATION/server/upload`
- AI: Requires `BAILIAN_API_KEY` env var for Alibaba Cloud DashScope
- RAG: Redis Stack optional (disabled by default, uses in-memory vector store)

### Frontend
- API base URL: `src/store/constants.ts` (default: http://127.0.0.1:9100)
- Dev server port: 3000 (configured in `build/constant.ts`)
- CSS: Less preprocessor with base styles imported

## Database

- MySQL 5.7+ required
- Database name: `java_shop`
- Import script: `java_shop.sql`
- Auto-migrations on startup: admin credentials, ad table columns, cart table

## Admin Login

- Username: 汤弘正
- Password: Thz20031001

## Important Notes

1. **LangChain4j Compatibility**: Spring Boot 3.0.2 requires manual AI service wiring (auto-config excluded)
2. **Redis Stack**: Only needed for RAG vector storage; basic Redis works for chat memory
3. **File Uploads**: Path depends on `BASE_LOCATION` in application.yml
4. **No Tests**: Project has no test suite
5. **Druid Monitor**: Available at `/druid/*` (admin/123)

## Environment Variables

- `BAILIAN_API_KEY` - Alibaba Cloud DashScope API key for AI features
- `DB_NAME` - Database name (default: java_shop)
- `BASE_LOCATION` - Base path for file uploads