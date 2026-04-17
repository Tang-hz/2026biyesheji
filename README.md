# 🛒 智能电商系统

> 基于 Spring Boot 与大模型向量检索的下一代电商平台

## ✨ 为什么选择这个项目？

- 🤖 **AI 智能客服** — 基于 LangChain4j + 阿里云百炼，对话记忆连贯
- 🚀 **现代化架构** — Spring Boot 3 + Vue 3 + Vite，体验丝滑
- 📊 **完整业务闭环** — 商品、订单、用户、统计，开箱即用
- 💡 **RAG 知识库** — 智能推荐与搜索，懂你所需

## 🧱 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.0.2 + MyBatis-Plus + LangChain4j |
| 前端 | Vue 3 + Vite + Ant Design Vue |
| 数据库 | MySQL 5.7 + Redis |
| AI | 阿里云百炼 (DashScope) + 文本向量 |

## 🚀 快速启动

### 后端

```bash
cd server
# 配置 application.yml（数据库、Redis）
mvn spring-boot:run
```

### 前端

```bash
cd web
npm install
npm run dev
```

访问 http://localhost:3000

## 📁 项目结构

```
.
├── server/           # Spring Boot 后端
├── web/              # Vue 3 前端
├── java_shop.sql     # 数据库初始化脚本
└── README.md
```

## 🎯 在线体验

- 演示地址: http://shop.gitapp.cn
- 管理员账号: `admin123` / `admin123`

## 许可证

MIT License
