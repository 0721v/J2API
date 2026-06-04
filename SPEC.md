# API聚合平台 - 技术规范文档

## 1. 项目概述

### 1.1 项目名称
**API Platform** - 智能API聚合与计费管理平台

### 1.2 项目目标
构建一个功能完善的API聚合平台，支持多渠道管理、智能路由、在线充值、灵活的套餐计费，并提供完善的数据统计和用户管理功能。

### 1.3 核心技术栈
- **后端**: Java 17 + Spring Boot 3.x + MyBatis-Plus + Redis + MySQL
- **前端**: Vue 3 + Element Plus + Pinia + Vue Router
- **特性**: 多语言(i18n) + 多主题(Theme)
- **部署**: Docker + Docker Compose

## 2. 功能模块

### 2.1 用户管理模块
- [x] 用户注册与登录
- [x] 邮箱验证
- [x] 密码重置
- [x] 用户信息管理
- [x] 多语言支持 (中文、English、其他)
- [x] 多主题支持 (亮色、暗色、自定义)

### 2.2 授权登录模块
- [x] 邮箱密码登录
- [x] LinuxDO OAuth2.0 登录
- [x] Telegram Bot 登录
- [x] OIDC 通用登录
- [x] 第三方登录绑定

### 2.3 令牌(Token)管理模块
- [x] API Key 生成与查看
- [x] 令牌分组管理
- [x] 令牌权限设置
- [x] 模型访问限制
- [x] 渠道访问限制
- [x] 分钟请求频率限制
- [x] 日请求次数限制
- [x] 令牌状态管理（启用/禁用）

### 2.4 渠道(Channel)管理模块
- [x] 渠道配置管理
- [x] 渠道类型支持:
  - OpenAI 官方
  - Azure OpenAI
  - Claude (Anthropic)
  - Cohere Rerank
  - Jina Rerank
  - 自定义 API 端点
- [x] 渠道权重设置
- [x] 渠道状态管理
- [x] 渠道成本配置
- [x] 渠道用量统计

### 2.5 模型(Model)管理模块
- [x] 模型配置管理
- [x] 模型类型:
  - Chat 模型 (GPT-4, Claude, etc.)
  - Embedding 模型
  - Rerank 模型
  - Realtime API 模型
- [x] 模型按次计费
- [x] 模型分组管理
- [x] 模型限流配置

### 2.6 套餐(Package)管理模块
- [x] 计次套餐
  - 按调用次数计费
  - 可设置有效期
- [x] 包月套餐
  - 固定时间内无限次使用
  - 可限制每日用量
- [x] 套餐组合管理
- [x] 套餐价格配置

### 2.7 充值与支付模块
- [x] 余额充值
- [x] 支付方式:
  - 支付宝
  - 微信支付
  - Stripe 信用卡
  - USDT TRC20
- [x] 订单管理
- [x] 交易记录查询
- [x] 充值优惠活动

### 2.8 计费与账单模块
- [x] 实时计费
- [x] 缓存计费（按命中率比例计费）
- [x] 账单生成
- [x] 消费明细查询
- [x] 用量统计报表

### 2.9 路由(Routing)模块
- [x] 加权随机路由
- [x] 令牌分组路由
- [x] 模型特定路由
- [x] 渠道自动切换
- [x] 故障转移

### 2.10 数据看板模块
- [x] 实时用量监控
- [x] 用户消费统计
- [x] 渠道调用统计
- [x] 收入统计
- [x] 活跃用户统计
- [x] API 调用趋势图
- [x] 自定义时间范围查询

### 2.11 API 网关模块
- [x] OpenAI 兼容 API
- [x] Claude API 兼容
- [x] Azure OpenAI 兼容
- [x] Realtime API 支持
- [x] 请求日志
- [x] 响应缓存
- [x] 限流控制

### 2.12 管理后台模块
- [x] 系统配置
- [x] 用户管理
- [x] 渠道管理
- [x] 模型管理
- [x] 套餐管理
- [x] 订单管理
- [x] 数据统计
- [x] 操作日志

### 2.13 API 文档模块
- [x] 自动生成 API 文档
- [x] Swagger/OpenAPI 支持
- [x] 在线测试
- [x] 代码示例
- [x] 多语言文档

## 3. 数据库设计

### 3.1 核心表结构
```
users              - 用户表
tokens             - API令牌表
token_groups        - 令牌分组表
channels           - 渠道配置表
models             - 模型配置表
packages           - 套餐表
package_items      - 套餐明细表
orders             - 订单表
transactions       - 交易记录表
bills              - 账单表
bill_items         - 账单明细表
usage_logs         - 使用日志表
rate_limits         - 限流配置表
system_settings    - 系统配置表
oauth_providers     - OAuth提供商配置表
oauth_bindings     - OAuth绑定表
```

## 4. API 接口设计

### 4.1 认证相关
```
POST   /api/auth/register     - 用户注册
POST   /api/auth/login        - 用户登录
POST   /api/auth/logout       - 用户登出
POST   /api/auth/refresh      - 刷新令牌
GET    /api/auth/me           - 获取当前用户
```

### 4.2 用户相关
```
GET    /api/user/profile      - 获取用户信息
PUT    /api/user/profile      - 更新用户信息
POST   /api/user/password     - 修改密码
GET    /api/user/balance      - 获取余额信息
```

### 4.3 令牌相关
```
GET    /api/tokens            - 获取令牌列表
POST   /api/tokens            - 创建令牌
GET    /api/tokens/{id}       - 获取令牌详情
PUT    /api/tokens/{id}       - 更新令牌
DELETE /api/tokens/{id}       - 删除令牌
GET    /api/tokens/{id}/usage - 获取令牌使用统计
```

### 4.4 套餐相关
```
GET    /api/packages          - 获取套餐列表
GET    /api/packages/{id}    - 获取套餐详情
POST   /api/packages/purchase - 购买套餐
GET    /api/packages/my       - 获取我的套餐
```

### 4.5 充值相关
```
POST   /api/recharge/create   - 创建充值订单
GET    /api/recharge/status/{id} - 查询充值状态
POST   /api/recharge/callback - 支付回调
GET    /api/recharge/history - 充值历史
```

### 4.6 AI API (网关)
```
POST   /v1/chat/completions   - Chat API
POST   /v1/completions        - Completion API
POST   /v1/embeddings        - Embedding API
POST   /v1/rerank             - Rerank API
POST   /v1/audio/speech       - TTS API
POST   /v1/images/generations - 图像生成 API
```

### 4.7 管理后台
```
/api/admin/dashboard/*       - 数据看板
/api/admin/users/*           - 用户管理
/api/admin/channels/*        - 渠道管理
/api/admin/models/*          - 模型管理
/api/admin/packages/*        - 套餐管理
/api/admin/orders/*          - 订单管理
/api/admin/settings/*        - 系统设置
```

## 5. 技术架构

### 5.1 系统架构
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
┌──────▼──────┐
│  Vue Frontend │
└──────┬──────┘
       │ HTTP/REST
┌──────▼──────┐
│ Spring Boot │
│  Gateway    │
├─────────────┤
│ Auth Module │
│ Token Module│
│ Billing Mod │
│ Proxy Mod   │
└──────┬──────┘
       │
┌──────▼──────┐
│   Services  │
└──────┬──────┘
       │
┌──────▼──────┐
│ Redis Cache │
└─────────────┘
       │
┌──────▼──────┐
│ MySQL DB    │
└─────────────┘
```

### 5.2 核心流程
```
用户请求 → API Gateway → Token验证 → 限流检查 
        → 路由选择 → 渠道代理 → 计费记录 → 响应返回
```

## 6. 部署要求

### 6.1 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Docker 20.10+

### 6.2 Docker 部署
提供 docker-compose.yml 一键部署

## 7. 国际化

### 7.1 支持语言
- 简体中文 (zh-CN)
- 英文 (en-US)
- 日文 (ja-JP)

### 7.2 实现
- 前端: Vue i18n
- 后端: Spring MessageSource

## 8. 主题支持

### 8.1 内置主题
- Light (亮色)
- Dark (暗色)
- 自定义主题

### 8.2 实现
- CSS Variables
- Element Plus 主题变量覆盖

## 9. 安全考虑

- JWT Token 认证
- API Key 加密存储
- 请求频率限制
- SQL 注入防护
- XSS 防护
- CSRF 防护
- 敏感数据加密

## 10. 开发进度

### Phase 1: 基础框架 (完成)
- [x] 项目结构搭建
- [x] Spring Boot 基础配置
- [x] MySQL + Redis 配置
- [x] 通用返回类
- [x] 异常处理

### Phase 2: 核心功能 (进行中)
- [x] 用户模块
- [x] 令牌模块
- [ ] 套餐模块
- [ ] 充值模块
- [ ] 计费模块

### Phase 3: API网关
- [ ] 代理功能
- [ ] 路由策略
- [ ] 限流功能

### Phase 4: 前端开发
- [ ] 用户界面
- [ ] 管理后台
- [ ] API文档页面

### Phase 5: 测试与部署
- [ ] 单元测试
- [ ] 集成测试
- [ ] Docker 部署
- [ ] 文档完善

## 11. 参考资料

- [NewAPI 项目](https://github.com/chatanywhere/NewAPI)
- [OneAPI 项目](https://github.com/songquanpeng/one-api)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Vue 3 文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)

---

**最后更新**: 2026-06-03  
**版本**: 1.0.0  
**状态**: 开发中
