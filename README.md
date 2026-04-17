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

---

## 🚀 本地部署（详细步骤）

### 第一步：环境准备

| 工具 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 17+ | https://adoptium.net/ |
| MySQL | 5.7+ | https://dev.mysql.com/downloads/mysql/ |
| Node.js | 16+ | https://nodejs.org/ |
| IntelliJ IDEA | 2021+ | https://www.jetbrains.com/idea/ |
| Redis | 6.0+ | https://redis.io/download/ |

> 💡 **提示**：Redis 是可选的，不安装则 AI 客服的记忆功能不可用，但不影响核心功能。

---

### 第二步：创建数据库

1. 打开 MySQL 客户端（如 Navicat 或 MySQL Workbench）
2. 执行以下命令创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS java_shop DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

3. 导入初始化数据：

```sql
USE java_shop;
SOURCE /path/to/java_shop.sql;  -- 改成你实际的 sql 文件路径
```

---

### 第三步：配置后端

1. 用 IntelliJ IDEA 打开 `server` 文件夹
2. 找到 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    username: root          # 你的数据库用户名
    password: 123456        # 你的数据库密码
    url: jdbc:mysql://localhost:3306/java_shop?useSSL=false&serverTimezone=Asia/Shanghai

  redis:
    host: localhost         # Redis 地址（未安装可忽略）
    port: 6379
    password: 123456        # Redis 密码（未安装可忽略）

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

File:
  uploadPath: D:/upload      # 文件上传目录，自定义即可
```

3. AI 相关配置（可选，需要阿里云百炼 API Key）：

```yaml
ai:
  baidu:
    api-key: your_api_key_here
    secret-key: your_secret_key_here
```

---

### 第四步：启动后端

1. 在 IDEA 中打开 `SpringdemoApplication.java`（位于 `src/main/java`）
2. 右键点击文件，选择 **Run 'SpringdemoApplication'**
3. 控制台出现以下日志表示启动成功：

```
Started SpringdemoApplication in X.XXX seconds
```

4. 后端默认运行在 **http://localhost:9100**

---

### 第五步：配置并启动前端

1. 打开 `web` 文件夹
2. 安装依赖：

```bash
cd web
npm install
```

> 💡 **提示**：国内用户建议先设置淘宝镜像，速度更快：
> ```bash
> npm config set registry https://registry.npm.taobao.org
> npm install
> ```

3. 修改 API 地址（如果后端不在本地）：

打开 `src/store/constants.js`，修改：

```javascript
export const BASE_URL = 'http://localhost:9100'  // 改成你的后端地址
```

4. 启动开发服务器：

```bash
npm run dev
```

5. 打开浏览器访问：

- 顾客端：http://localhost:3000/#/index
- 管理端：http://localhost:3000/#/admin

---

### 第六步：登录后台

- 管理员账号：`admin123`
- 管理员密码：`admin123`

> ⚠️ **重要**：首次使用请及时修改密码！

---

## 📁 项目结构

```
.
├── server/                    # Spring Boot 后端项目
│   └── src/main/java/
│       └── com/gk/study/
│           ├── controller/    # 控制器层（API接口）
│           ├── service/       # 业务逻辑层
│           ├── mapper/        # 数据访问层
│           ├── entity/        # 实体类
│           ├── config/        # 配置类
│           ├── ai/            # AI相关（LangChain4j、RAG）
│           └── common/        # 通用工具
├── web/                       # Vue 3 前端项目
│   └── src/
│       ├── api/               # API请求封装
│       ├── router/            # 路由配置
│       ├── store/             # 状态管理
│       └── views/             # 页面组件
├── java_shop.sql              # 数据库初始化脚本
└── README.md
```

---

## 🔧 常见问题

### Q: npm install 失败怎么办？

```bash
# 切换到淘宝镜像
npm config set registry https://registry.npm.taobao.org
npm install
```

### Q: 数据库导入失败？

- 确保 MySQL 版本 ≥ 5.7
- 确保数据库名称为 `java_shop`，字符集为 `utf8mb4`

### Q: 前端页面空白？

检查浏览器控制台是否报错，通常是 API 地址配置不正确。

### Q: AI 客服无法使用？

1. 确保已安装并启动 Redis
2. 确保在 `application.yml` 中正确配置了阿里云百炼的 API Key

---

## 🎯 在线体验

- 演示地址：http://shop.gitapp.cn
- 管理员账号：`admin123` / `admin123`

---

## 📜 许可证

MIT License — 欢迎 Star ⭐ 和 Fork！
