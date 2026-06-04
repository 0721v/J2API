# API Platform

智能API聚合与计费管理平台

## 功能特性

### 核心功能
- ✅ 用户管理 - 注册、登录、OAuth授权
- ✅ API密钥管理 - 创建、管理、限流配置
- ✅ 多渠道管理 - OpenAI、Claude、Azure、Cohere、Jina等
- ✅ 模型配置 - 按次计费、Token计费
- ✅ 套餐管理 - 计次套餐、包月套餐
- ✅ 在线充值 - 支付宝、微信、Stripe、USDT
- ✅ 用量统计 - 实时监控、消费分析
- ✅ 智能路由 - 加权随机、故障转移

### 授权登录
- ✅ 邮箱密码登录
- ✅ LinuxDO OAuth
- ✅ Telegram Bot登录
- ✅ OIDC通用登录

### AI模型支持
- ✅ OpenAI GPT-4/3.5
- ✅ Claude 3.5/3 Opus/Haiku
- ✅ Azure OpenAI
- ✅ Cohere Rerank
- ✅ Jina Rerank
- ✅ Embedding模型
- ✅ Realtime API

### 高级功能
- ✅ 模型限流 - 分钟/日限制
- ✅ 缓存计费 - 按命中率计费
- ✅ 多语言支持 - 中文、英文
- ✅ 多主题支持 - 亮色、暗色
- ✅ API限流保护

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2
- MyBatis-Plus
- Redis
- MySQL 8.0
- JWT

### 前端
- Vue 3
- Element Plus
- Pinia
- Vue Router
- ECharts

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
cd backend

# 修改 application.yml 中的数据库配置

# 构建项目
mvn clean package

# 运行项目
java -jar target/api-platform-backend-1.0.0.jar
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### Docker部署

```bash
# 构建镜像
docker build -t api-platform .

# 运行容器
docker run -d -p 8080:8080 -p 3000:3000 api-platform
```

或使用 docker-compose:

```bash
docker-compose up -d
```

## 项目结构

```
api-platform/
├── backend/                 # Spring Boot后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/apiplatform/
│   │       │   ├── common/     # 通用类
│   │       │   ├── config/      # 配置类
│   │       │   ├── controller/  # 控制器
│   │       │   ├── entity/      # 实体类
│   │       │   ├── mapper/      # Mapper接口
│   │       │   ├── service/     # 服务层
│   │       │   └── util/        # 工具类
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── frontend/                # Vue前端
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 组件
│   │   ├── layout/          # 布局
│   │   ├── locales/         # 国际化
│   │   ├── router/          # 路由
│   │   ├── stores/          # 状态管理
│   │   ├── utils/           # 工具
│   │   └── views/           # 页面
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── database/                 # 数据库脚本
│   └── schema.sql
├── docs/                     # 文档
└── README.md
```

## API文档

启动后访问: http://localhost:8080/api/swagger-ui.html

## 默认账户

- 管理员: admin@example.com / admin123

## 许可证

Apache License 2.0

## 联系方式

- 邮箱: support@example.com
- 网站: https://example.com
