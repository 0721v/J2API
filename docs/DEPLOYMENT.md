# API聚合平台 - 部署指南

## 目录
- [环境要求](#环境要求)
- [快速部署](#快速部署)
- [详细配置](#详细配置)
- [数据库初始化](#数据库初始化)
- [Docker部署](#docker部署)
- [生产环境优化](#生产环境优化)
- [常见问题](#常见问题)

---

## 环境要求

### 基础环境

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 推荐使用 Eclipse Temurin 或 Amazon Corretto |
| MySQL | 8.0+ | 支持 JSON 类型 |
| Redis | 6.0+ | 用于缓存和限流 |
| Node.js | 18+ | 用于构建前端 |
| Nginx | 1.20+ | 用于前端静态资源服务 |

### 硬件配置

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 2核 | 4GB | 20GB |
| 生产环境 | 4核+ | 8GB+ | 100GB+ |

---

## 快速部署

### 方式一：一键部署（JAR包）

```bash
# 1. 下载最新版本
wget https://your-domain.com/api-platform.jar

# 2. 创建配置文件
mkdir -p /opt/api-platform
mv api-platform.jar /opt/api-platform/
cd /opt/api-platform

# 3. 创建 application.yml
cat > application.yml << 'EOF'
spring:
  application:
    name: api-platform
  datasource:
    url: jdbc:mysql://localhost:3306/api_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

server:
  port: 8080

app:
  jwt:
    secret: your-jwt-secret-key-min-32-chars
    access-expire: 3600
    refresh-expire: 604800
  upload:
    path: /opt/api-platform/uploads
  cors:
    allowed-origins: http://localhost:3000,https://your-domain.com
EOF

# 4. 运行
java -jar api-platform.jar
```

### 方式二：从源码构建

```bash
# 1. 克隆项目
git clone https://your-git-repo.com/api-platform.git
cd api-platform

# 2. 构建后端
cd backend
./mvnw clean package -DskipTests

# 3. 构建前端（自动打包进JAR）
cd ../frontend
npm install
npm run build

# 4. 运行
cd ../backend/target
java -jar api-platform.jar
```

---

## 详细配置

### application.yml 完整配置

```yaml
spring:
  application:
    name: api-platform

  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/api_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

  # 文件上传
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 20MB

  # Jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

# 服务配置
server:
  port: 8080
  servlet:
    context-path: /
  tomcat:
    threads:
      max: 200
      min-spare: 10
    accept-count: 100

# 应用配置
app:
  # JWT配置
  jwt:
    secret: your-jwt-secret-key-must-be-at-least-32-characters
    access-expire: 3600        # 访问令牌过期时间(秒)
    refresh-expire: 604800     # 刷新令牌过期时间(秒)

  # 上传配置
  upload:
    path: /opt/api-platform/uploads
    allowed-extensions: jpg,jpeg,png,gif,svg,pdf

  # CORS配置
  cors:
    allowed-origins:
      - http://localhost:3000
      - https://your-domain.com
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: '*'
    allow-credentials: true

  # API配置
  api:
    rate-limit:
      enabled: true
      default-minute-limit: 60      # 默认每分钟限制
      default-day-limit: 10000      # 默认每日限制

  # 支付配置
  payment:
    alipay:
      enabled: false
      app-id: your-alipay-app-id
      private-key: your-private-key
      alipay-public-key: alipay-public-key
      notify-url: https://your-domain.com/api/pay/alipay/notify
    wechat:
      enabled: false
      app-id: your-wechat-app-id
      mch-id: your-mch-id
      api-key: your-api-key
      notify-url: https://your-domain.com/api/pay/wechat/notify
    stripe:
      enabled: false
      api-key: sk_test_xxx
      webhook-secret: whsec_xxx
    okx:
      enabled: false
      api-key: xxx
      secret-key: xxx
      passphrase: xxx
    creem:
      enabled: false
      api-key: xxx
      webhook-secret: xxx

# 日志配置
logging:
  level:
    root: INFO
    com.apiplatform: DEBUG
  file:
    name: /var/log/api-platform/application.log
    max-size: 100MB
    max-history: 30
```

### 环境变量覆盖

支持通过环境变量覆盖配置：

```bash
# 数据库
export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/api_platform
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=secret

# Redis
export SPRING_REDIS_HOST=redis
export SPRING_REDIS_PASSWORD=secret

# JWT
export APP_JWT_SECRET=your-secret-key

java -jar api-platform.jar
```

---

## 数据库初始化

### 1. 创建数据库

```sql
CREATE DATABASE api_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行数据库迁移

```bash
# 进入数据库目录
cd database

# 按顺序执行SQL文件
mysql -u root -p api_platform < schema.sql
mysql -u root -p api_platform < agent_extension.sql
mysql -u root -p api_platform < migration_invite.sql
mysql -u root -p api_platform < migration_announcement_report.sql
```

### 3. 数据库表说明

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| tokens | API密钥表 |
| user_groups | 用户分组表 |
| models | AI模型配置表 |
| channels | 渠道配置表 |
| orders | 订单表 |
| packages | 套餐表 |
| transactions | 交易记录表 |
| usage_logs | 使用日志表 |
| agent_levels | 代理等级表 |
| agents | 代理信息表 |
| agent_commissions | 代理佣金表 |
| agent_withdrawals | 代理提现表 |
| invitations | 邀请记录表 |
| invite_rewards | 邀请奖励配置表 |
| announcements | 系统公告表 |
| messages | 站内消息表 |
| consumption_records | 消费记录表 |
| daily_user_stats | 每日统计表 |

---

## Docker部署

### docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: api-platform-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: api_platform
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - api-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: api-platform-redis
    restart: always
    command: redis-server --requirepass redis_password --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - api-network

  # 后端应用
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: api-platform-backend
    restart: always
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/api_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redis_password
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - api-network

  # Nginx前端
  nginx:
    image: nginx:alpine
    container_name: api-platform-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - backend
    networks:
      - api-network

volumes:
  mysql_data:
  redis_data:

networks:
  api-network:
    driver: bridge
```

### nginx.conf

```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    upstream backend {
        server backend:8080;
    }

    server {
        listen 80;
        server_name your-domain.com;

        # 前端静态资源
        location / {
            root /usr/share/nginx/html;
            try_files $uri $uri/ /index.html;
            index index.html;
        }

        # API代理
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # WebSocket支持
        location /ws/ {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }
    }

    # HTTPS配置（可选）
    server {
        listen 443 ssl;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;

        # 同上配置...
    }
}
```

### 启动命令

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

---

## 生产环境优化

### 1. JVM参数调优

```bash
java -Xms4g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/api-platform/heapdump.hprof \
     -jar api-platform.jar
```

### 2. 数据库连接池优化

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: ApiPlatformHikariCP
```

### 3. Redis安全配置

```bash
# 启用SSL
redis-server --tls-port 6379 --port 0 \
  --tls-cert-file /path/to/redis.crt \
  --tls-key-file /path/to/redis.key \
  --tls-ca-cert-file /path/to/ca.crt

# 设置密码
redis-server --requirepass your-strong-password
```

### 4. Nginx优化

```nginx
# Gzip压缩
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml;

# 静态资源缓存
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 30d;
    add_header Cache-Control "public, immutable";
}

# 连接数限制
limit_conn_zone $binary_remote_addr zone=conn:10m;
limit_conn conn 100;
```

### 5. 系统参数优化

```bash
# 打开文件数限制
echo "* soft nofile 65535" >> /etc/security/limits.conf
echo "* hard nofile 65535" >> /etc/security/limits.conf

# 网络参数优化
sysctl -w net.core.somaxconn=1024
sysctl -w net.ipv4.tcp_tw_reuse=1
sysctl -w net.ipv4.tcp_fin_timeout=30
```

---

## 常见问题

### Q1: 启动报错 "Connection refused"
```
检查MySQL和Redis是否正常运行：
- mysql -u root -p -e "SELECT 1"
- redis-cli ping
```

### Q2: 前端无法访问API
```
检查CORS配置是否包含前端域名：
app.cors.allowed-origins: http://your-frontend.com
```

### Q3: 支付回调失败
```
确认回调地址可公网访问，且使用HTTPS
检查防火墙是否开放80/443端口
```

### Q4: 内存溢出 (OOM)
```
增加JVM堆内存：
java -Xms2g -Xmx4g -jar api-platform.jar
```

### Q5: 数据库连接超时
```
检查网络连通性：
telnet mysql-host 3306
检查MySQL最大连接数配置
```

---

## 技术支持

- 文档版本：v1.0.0
- 更新日期：2024年
- 技术支持邮箱：support@your-domain.com
