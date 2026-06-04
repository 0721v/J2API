# API Platform 部署指南

## 概述

本项目支持一键打包，前端和后端会合并到一个 JAR 文件中，部署时只需部署一个 JAR 包即可。

## 快速开始

### 方式一：一键打包（推荐）

```bash
# Windows
.\build.bat

# Linux/Mac
chmod +x build.sh
./build.sh
```

### 方式二：Maven 命令打包

```bash
# 在项目根目录执行
cd backend
mvn clean package -DskipTests

# 或跳过前端（如果已有前端构建产物）
mvn clean package -DskipTests -P backend-only
```

## 打包后运行

```bash
# 进入后端目录
cd backend

# 启动应用
java -jar target/api-platform-backend-1.0.0.jar

# 或指定配置文件启动
java -jar target/api-platform-backend-1.0.0.jar --spring.config.location=application-prod.yml
```

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

## 配置说明

### 数据库配置

修改 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/api_platform
    username: your_username
    password: your_password
```

### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 生产环境变量

可通过环境变量覆盖配置：

```bash
java -jar api-platform-backend-1.0.0.jar \
  --spring.datasource.password=your_password \
  --spring.data.redis.password=your_redis_password \
  --jwt.secret=your-secret-key
```

## 访问地址

- 前端页面: http://localhost:8080/api/
- API 文档: http://localhost:8080/api/swagger-ui.html
- 健康检查: http://localhost:8080/api/actuator/health

## 目录结构

```
api-platform/
├── backend/
│   ├── src/main/resources/
│   │   └── static/           # 前端构建产物目录
│   └── target/               # Maven 构建输出
│       └── *.jar             # 可执行 JAR 包
├── frontend/
│   ├── src/
│   ├── dist/                # 前端构建产物
│   └── package.json
├── database/
│   └── schema.sql           # 数据库 Schema
├── build.bat                # Windows 打包脚本
├── build.sh                 # Linux/Mac 打包脚本
└── README.md
```

## Docker 部署

如果使用 Docker 部署，可以参考以下 Dockerfile：

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 常见问题

### 1. 打包时 Node.js 下载失败

frontend-maven-plugin 会自动下载指定版本的 Node.js。如果网络较慢，可以：

1. 手动安装 Node.js (v20.11.0)
2. 使用 `-P backend-only` 跳过前端构建
3. 手动构建前端：`cd frontend && npm install && npm run build`

### 2. 前端 API 请求 404

确保后端配置了正确的 context-path。前端默认请求 `/api` 前缀。

### 3. 静态资源加载失败

检查 `backend/src/main/resources/static/` 目录是否存在构建产物。

## 开发说明

### 前后端分离开发

如果只需要开发后端，可以跳过前端构建：

```bash
mvn clean package -DskipTests -P backend-only
```

### 前端单独开发

```bash
cd frontend
npm install
npm run dev
```

前端开发模式下，API 请求会代理到 http://localhost:8080。

## 许可证

MIT License
