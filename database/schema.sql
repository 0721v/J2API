-- =========================================
-- API Platform Database Schema
-- 版本: 1.0.0
-- 更新日期: 2026-06-03
-- =========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS api_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE api_platform;

-- =========================================
-- 1. 用户表
-- =========================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `display_name` VARCHAR(100) DEFAULT NULL COMMENT '显示名称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` BIGINT DEFAULT 0 COMMENT '余额（分）',
    `total_consumption` BIGINT DEFAULT 0 COMMENT '累计消费（分）',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：user/admin',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/disabled/banned',
    `email_verified` TINYINT(1) DEFAULT 0 COMMENT '邮箱验证',
    `preferred_language` VARCHAR(10) DEFAULT 'zh-CN' COMMENT '首选语言',
    `preferred_theme` VARCHAR(20) DEFAULT 'light' COMMENT '首选主题',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `register_ip` VARCHAR(50) DEFAULT NULL COMMENT '注册IP',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =========================================
-- 2. 令牌分组表
-- =========================================
CREATE TABLE IF NOT EXISTS `token_groups` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分组描述',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `default_models` VARCHAR(500) DEFAULT NULL COMMENT '默认模型列表',
    `default_channels` VARCHAR(500) DEFAULT NULL COMMENT '默认渠道列表',
    `minute_limit` INT DEFAULT 0 COMMENT '每分钟限制',
    `day_limit` INT DEFAULT 0 COMMENT '每日限制',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='令牌分组表';

-- =========================================
-- 3. 令牌表
-- =========================================
CREATE TABLE IF NOT EXISTS `tokens` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '令牌名称',
    `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分组ID',
    `api_key` VARCHAR(100) NOT NULL COMMENT 'API Key',
    `api_secret` VARCHAR(255) DEFAULT NULL COMMENT 'API Secret',
    `allowed_models` VARCHAR(500) DEFAULT NULL COMMENT '允许的模型',
    `allowed_channels` VARCHAR(500) DEFAULT NULL COMMENT '允许的渠道',
    `minute_limit` INT DEFAULT 0 COMMENT '每分钟限制',
    `day_limit` INT DEFAULT 0 COMMENT '每日限制',
    `used_quota` BIGINT DEFAULT 0 COMMENT '已用额度（分）',
    `remaining_quota` BIGINT DEFAULT -1 COMMENT '剩余额度（分），-1表示无限制',
    `quota_limit` BIGINT DEFAULT 0 COMMENT '额度上限，0表示无限制',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `renew_days` INT DEFAULT 30 COMMENT '续期天数',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='令牌表';

-- =========================================
-- 4. 渠道表
-- =========================================
CREATE TABLE IF NOT EXISTS `channels` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '渠道ID',
    `name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
    `type` VARCHAR(50) NOT NULL COMMENT '渠道类型：openai/azure/claude/cohere/jina/custom',
    `sub_type` VARCHAR(50) DEFAULT 'chat' COMMENT '子类型：chat/completion/embedding/rerank/realtime',
    `endpoint` VARCHAR(500) NOT NULL COMMENT 'API端点',
    `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥',
    `auth_type` VARCHAR(20) DEFAULT 'bearer' COMMENT '认证类型',
    `custom_headers` TEXT DEFAULT NULL COMMENT '自定义请求头',
    `timeout` INT DEFAULT 60000 COMMENT '超时（毫秒）',
    `model_mapping` TEXT DEFAULT NULL COMMENT '模型映射',
    `default_model` VARCHAR(100) DEFAULT NULL COMMENT '默认模型',
    `weight` INT DEFAULT 100 COMMENT '权重',
    `cost_per_thousand` DECIMAL(10,2) DEFAULT 0 COMMENT '成本（每千token，分）',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `failure_count` INT DEFAULT 0 COMMENT '失败次数',
    `last_failure_time` DATETIME DEFAULT NULL COMMENT '最后失败时间',
    `max_retries` INT DEFAULT 3 COMMENT '最大重试次数',
    `backup_channel_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '备用渠道',
    `region` VARCHAR(50) DEFAULT NULL COMMENT '区域',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道表';

-- =========================================
-- 5. 模型表
-- =========================================
CREATE TABLE IF NOT EXISTS `models` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `model_id` VARCHAR(100) NOT NULL COMMENT '模型标识',
    `name` VARCHAR(100) NOT NULL COMMENT '显示名称',
    `type` VARCHAR(50) DEFAULT 'chat' COMMENT '类型：chat/embedding/rerank/tts/speech/video/image',
    `billing_type` VARCHAR(50) DEFAULT 'token' COMMENT '计费类型：token/per_request/per_second/tiered',
    `channel_type` VARCHAR(50) DEFAULT NULL COMMENT '渠道类型',
    `allowed_channels` VARCHAR(500) DEFAULT NULL COMMENT '允许的渠道',
    -- 按Token计费
    `input_price` DECIMAL(10,4) DEFAULT 0 COMMENT '输入价格（每1K tokens，元）',
    `output_price` DECIMAL(10,4) DEFAULT 0 COMMENT '输出价格（每1K tokens，元）',
    -- 按次计费
    `per_request_price` DECIMAL(10,4) DEFAULT 0 COMMENT '每次请求价格（元）',
    -- 按秒计费（视频/音频）
    `per_second_price` DECIMAL(10,4) DEFAULT 0 COMMENT '每秒价格（元）',
    `media_type` VARCHAR(20) DEFAULT 'video' COMMENT '媒体类型：video/audio/image',
    `min_billable_seconds` DECIMAL(10,2) DEFAULT 1.0 COMMENT '最小计费时长（秒）',
    `max_billable_seconds` DECIMAL(10,2) DEFAULT 300.0 COMMENT '最大计费时长（秒）',
    -- 阶梯计费
    `tiered_config` TEXT DEFAULT NULL COMMENT '阶梯计费配置',
    -- 兼容旧字段
    `price_per_call` DECIMAL(10,2) DEFAULT 0 COMMENT '每次调用价格（分）',
    `default_context_length` INT DEFAULT 4096 COMMENT '默认上下文长度',
    `max_input_length` INT DEFAULT 128000 COMMENT '最大输入',
    `max_output_length` INT DEFAULT 4096 COMMENT '最大输出',
    `supports_streaming` TINYINT(1) DEFAULT 1 COMMENT '支持流式',
    `supports_function_call` TINYINT(1) DEFAULT 0 COMMENT '支持函数调用',
    `supports_vision` TINYINT(1) DEFAULT 0 COMMENT '支持Vision',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `config` TEXT DEFAULT NULL COMMENT '配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_id` (`model_id`),
    KEY `idx_type` (`type`),
    KEY `idx_billing_type` (`billing_type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型表';

-- =========================================
-- 6. 套餐表
-- =========================================
CREATE TABLE IF NOT EXISTS `packages` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `type` VARCHAR(50) NOT NULL COMMENT '类型：count/month/permanent',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `total_calls` INT DEFAULT 0 COMMENT '包含次数',
    `valid_days` INT DEFAULT 30 COMMENT '有效期天数',
    `daily_limit` INT DEFAULT 0 COMMENT '每日限制次数',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格（分）',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `discount_rate` DECIMAL(4,2) DEFAULT 1.00 COMMENT '折扣比例',
    `included_models` VARCHAR(500) DEFAULT NULL COMMENT '包含的模型',
    `excluded_models` VARCHAR(500) DEFAULT NULL COMMENT '排除的模型',
    `quota` BIGINT DEFAULT 0 COMMENT '额度（分）',
    `allow_renewal` TINYINT(1) DEFAULT 1 COMMENT '允许续期',
    `renewal_price` DECIMAL(10,2) DEFAULT NULL COMMENT '续期价格',
    `renewal_days` INT DEFAULT 30 COMMENT '续期天数',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门',
    `visible` TINYINT(1) DEFAULT 1 COMMENT '是否显示',
    `purchase_limit` INT DEFAULT 0 COMMENT '购买次数限制',
    `config` TEXT DEFAULT NULL COMMENT '配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_visible` (`visible`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- =========================================
-- 7. 用户套餐表
-- =========================================
CREATE TABLE IF NOT EXISTS `user_packages` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `package_id` BIGINT UNSIGNED NOT NULL COMMENT '套餐ID',
    `remaining_calls` INT DEFAULT 0 COMMENT '剩余次数',
    `today_used_calls` INT DEFAULT 0 COMMENT '今日已用',
    `today_date` DATE DEFAULT NULL COMMENT '今日日期',
    `quota_balance` BIGINT DEFAULT 0 COMMENT '额度余额',
    `start_time` DATETIME NOT NULL COMMENT '生效时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `auto_renew` TINYINT(1) DEFAULT 0 COMMENT '自动续期',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_package_id` (`package_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户套餐表';

-- =========================================
-- 8. 订单表
-- =========================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：recharge/package/giftcard',
    `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额（分）',
    `paid_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '实付金额（分）',
    `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `refund_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '退款金额',
    `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `extra_data` TEXT DEFAULT NULL COMMENT '扩展数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =========================================
-- 9. 交易记录表
-- =========================================
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    `transaction_no` VARCHAR(50) NOT NULL COMMENT '交易号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：recharge/consume/refund/bonus/adjust',
    `amount` BIGINT NOT NULL COMMENT '变动金额（分）',
    `balance_before` BIGINT DEFAULT 0 COMMENT '变动前余额',
    `balance_after` BIGINT DEFAULT 0 COMMENT '变动后余额',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
    `token_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联令牌ID',
    `model_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联模型ID',
    `channel_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联渠道ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `details` TEXT DEFAULT NULL COMMENT '详情JSON',
    `status` VARCHAR(20) DEFAULT 'completed' COMMENT '状态',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作者ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作者名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- =========================================
-- 10. 账单表
-- =========================================
CREATE TABLE IF NOT EXISTS `bills` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账单ID',
    `bill_no` VARCHAR(50) NOT NULL COMMENT '账单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `period` VARCHAR(20) DEFAULT 'daily' COMMENT '周期：daily/monthly',
    `period_start` DATETIME NOT NULL COMMENT '周期开始',
    `period_end` DATETIME NOT NULL COMMENT '周期结束',
    `total_calls` BIGINT DEFAULT 0 COMMENT '总调用次数',
    `total_amount` BIGINT DEFAULT 0 COMMENT '总消费（分）',
    `total_input_tokens` BIGINT DEFAULT 0 COMMENT '输入Token数',
    `total_output_tokens` BIGINT DEFAULT 0 COMMENT '输出Token数',
    `cache_hit_calls` BIGINT DEFAULT 0 COMMENT '缓存命中次数',
    `cache_billing_amount` BIGINT DEFAULT 0 COMMENT '缓存计费金额',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_no` (`bill_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- =========================================
-- 11. 使用日志表
-- =========================================
CREATE TABLE IF NOT EXISTS `usage_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `request_id` VARCHAR(50) DEFAULT NULL COMMENT '请求ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `token_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '令牌ID',
    `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分组ID',
    `model_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '模型ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `channel_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '渠道ID',
    `channel_name` VARCHAR(100) DEFAULT NULL COMMENT '渠道名称',
    `api_type` VARCHAR(50) DEFAULT NULL COMMENT 'API类型',
    `request_tokens` INT DEFAULT 0 COMMENT '请求Token数',
    `response_tokens` INT DEFAULT 0 COMMENT '响应Token数',
    `cache_hit` TINYINT(1) DEFAULT 0 COMMENT '缓存命中',
    `billed_tokens` INT DEFAULT 0 COMMENT '计费Token数',
    `billed_amount` BIGINT DEFAULT 0 COMMENT '计费金额（分）',
    `duration` DECIMAL(10,2) DEFAULT NULL COMMENT '视频/音频时长（秒）',
    `tiered_usage` BIGINT DEFAULT NULL COMMENT '阶梯计费累计用量',
    `response_time` INT DEFAULT 0 COMMENT '响应时间（毫秒）',
    `http_status` INT DEFAULT 200 COMMENT 'HTTP状态',
    `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态',
    `error_message` TEXT DEFAULT NULL COMMENT '错误消息',
    `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `request_path` VARCHAR(200) DEFAULT NULL COMMENT '请求路径',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `request_hash` VARCHAR(64) DEFAULT NULL COMMENT '请求摘要',
    `response_hash` VARCHAR(64) DEFAULT NULL COMMENT '响应摘要',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_token_id` (`token_id`),
    KEY `idx_model_name` (`model_name`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用日志表';

-- =========================================
-- 12. 系统设置表
-- =========================================
CREATE TABLE IF NOT EXISTS `system_settings` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `category` VARCHAR(50) NOT NULL COMMENT '分组',
    `setting_key` VARCHAR(100) NOT NULL COMMENT '键',
    `setting_value` TEXT DEFAULT NULL COMMENT '值',
    `value_type` VARCHAR(20) DEFAULT 'string' COMMENT '类型',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `editable` TINYINT(1) DEFAULT 1 COMMENT '是否可编辑',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_key` (`category`, `setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- =========================================
-- 13. OAuth提供商表
-- =========================================
CREATE TABLE IF NOT EXISTS `oauth_providers` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '提供商ID',
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '类型',
    `client_id` VARCHAR(255) NOT NULL COMMENT '客户端ID',
    `client_secret` VARCHAR(500) DEFAULT NULL COMMENT '客户端密钥',
    `authorization_uri` VARCHAR(500) DEFAULT NULL COMMENT '授权URL',
    `token_uri` VARCHAR(500) DEFAULT NULL COMMENT 'Token URL',
    `user_info_uri` VARCHAR(500) DEFAULT NULL COMMENT '用户信息URL',
    `scopes` VARCHAR(500) DEFAULT NULL COMMENT '权限',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标',
    `button_color` VARCHAR(20) DEFAULT NULL COMMENT '按钮颜色',
    `client_id_field` VARCHAR(50) DEFAULT 'client_id' COMMENT '客户端ID字段名',
    `client_secret_field` VARCHAR(50) DEFAULT 'client_secret' COMMENT '密钥字段名',
    `response_type` VARCHAR(20) DEFAULT 'code' COMMENT '响应类型',
    `extra_params` TEXT DEFAULT NULL COMMENT '额外参数',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `visible` TINYINT(1) DEFAULT 1 COMMENT '是否显示',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_provider` (`provider`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth提供商表';

-- =========================================
-- 14. OAuth绑定表
-- =========================================
CREATE TABLE IF NOT EXISTS `oauth_bindings` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `provider_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '提供商ID',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供商类型',
    `provider_user_id` VARCHAR(100) NOT NULL COMMENT '第三方用户ID',
    `provider_username` VARCHAR(100) DEFAULT NULL COMMENT '第三方用户名',
    `provider_email` VARCHAR(255) DEFAULT NULL COMMENT '第三方邮箱',
    `provider_avatar` VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    `access_token` TEXT DEFAULT NULL COMMENT '访问令牌',
    `refresh_token` TEXT DEFAULT NULL COMMENT '刷新令牌',
    `token_expires_at` DATETIME DEFAULT NULL COMMENT '令牌过期时间',
    `bound_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_user` (`provider`, `provider_user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth绑定表';

-- =========================================
-- 初始化数据
-- =========================================

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO `users` (`username`, `email`, `password`, `display_name`, `role`, `email_verified`, `status`)
VALUES ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin', 1, 'active');

-- 插入默认系统设置
INSERT INTO `system_settings` (`category`, `setting_key`, `setting_value`, `value_type`, `description`) VALUES
('general', 'site_name', 'API Platform', 'string', '网站名称'),
('general', 'site_url', 'https://api.example.com', 'string', '网站地址'),
('general', 'support_email', 'support@example.com', 'string', '支持邮箱'),
('billing', 'cache_billing_enabled', 'true', 'boolean', '是否启用缓存计费'),
('billing', 'cache_hit_rate', '0.5', 'number', '缓存命中率计费比例'),
('billing', 'default_rate_limit', '60', 'number', '默认每分钟限流'),
('security', 'jwt_secret', 'change-this-in-production', 'string', 'JWT密钥'),
('security', 'jwt_expiration', '86400000', 'number', 'JWT过期时间（毫秒）'),
('payment', 'alipay_enabled', 'false', 'boolean', '是否启用支付宝'),
('payment', 'wechat_enabled', 'false', 'boolean', '是否启用微信支付'),
('payment', 'stripe_enabled', 'false', 'boolean', '是否启用Stripe');

-- 插入默认模型配置
INSERT INTO `models` (`model_id`, `name`, `type`, `channel_type`, `input_price`, `output_price`, `max_input_length`, `max_output_length`, `supports_streaming`, `enabled`) VALUES
('gpt-4o', 'GPT-4o', 'chat', 'openai', 15.00, 60.00, 128000, 4096, 1, 1),
('gpt-4-turbo', 'GPT-4 Turbo', 'chat', 'openai', 30.00, 90.00, 128000, 4096, 1, 1),
('gpt-3.5-turbo', 'GPT-3.5 Turbo', 'chat', 'openai', 0.50, 1.50, 16385, 4096, 1, 1),
('claude-3-5-sonnet-20241022', 'Claude Sonnet 4.5', 'chat', 'claude', 12.00, 36.00, 200000, 8192, 1, 1),
('claude-3-opus', 'Claude 3 Opus', 'chat', 'claude', 75.00, 300.00, 200000, 4096, 1, 1),
('claude-3-haiku', 'Claude 3 Haiku', 'chat', 'claude', 1.25, 5.00, 200000, 4096, 1, 1),
('text-embedding-3-small', 'Embedding 3 Small', 'embedding', 'openai', 0.02, 0, 8191, 0, 0, 1),
('text-embedding-3-large', 'Embedding 3 Large', 'embedding', 'openai', 0.13, 0, 8191, 0, 0, 1),
('cohere-rerank', 'Cohere Rerank', 'rerank', 'cohere', 1.00, 0, 4000, 0, 0, 1),
('jina-reranker', 'Jina Reranker', 'rerank', 'jina', 0.50, 0, 4000, 0, 0, 1);

-- 插入默认套餐
INSERT INTO `packages` (`name`, `type`, `description`, `total_calls`, `valid_days`, `price`, `quota`, `recommended`, `visible`, `sort_order`) VALUES
('试用套餐', 'count', '适合首次体验', 100, 7, 0, 0, 0, 1, 1),
('基础套餐', 'count', '适合轻度使用', 0, 30, 1000, 10000, 1, 1, 2),
('标准套餐', 'month', '适合日常使用', 0, 30, 9900, 0, 1, 1, 3),
('高级套餐', 'month', '适合高频使用', 0, 30, 29900, 0, 0, 1, 4),
('专业套餐', 'month', '适合企业用户', 0, 30, 99900, 0, 0, 1, 5);

-- =========================================
-- 16. 用户分组表
-- =========================================
CREATE TABLE IF NOT EXISTS `user_groups` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分组描述',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` VARCHAR(20) DEFAULT 'enabled' COMMENT '状态',
    -- 配额设置
    `default_minute_limit` INT DEFAULT 0 COMMENT '默认每分钟限制（0继承）',
    `default_daily_limit` INT DEFAULT 0 COMMENT '默认每日限制（0继承）',
    `default_monthly_limit` INT DEFAULT 0 COMMENT '默认每月限制（0无限制）',
    -- 价格倍率
    `global_rate` DECIMAL(5,2) DEFAULT 1.00 COMMENT '全局价格倍率',
    `model_rates` TEXT DEFAULT NULL COMMENT '模型价格倍率JSON',
    `channel_rates` TEXT DEFAULT NULL COMMENT '渠道价格倍率JSON',
    -- 功能权限
    `allowed_models` TEXT DEFAULT NULL COMMENT '允许的模型JSON',
    `blocked_models` TEXT DEFAULT NULL COMMENT '禁止的模型JSON',
    `allowed_channels` TEXT DEFAULT NULL COMMENT '允许的渠道JSON',
    `allow_recharge` TINYINT(1) DEFAULT 1 COMMENT '允许充值',
    `allow_packages` TINYINT(1) DEFAULT 1 COMMENT '允许购买套餐',
    `allow_usage_stats` TINYINT(1) DEFAULT 1 COMMENT '允许查看统计',
    -- 会员设置
    `level` VARCHAR(20) DEFAULT 'free' COMMENT '会员等级',
    `valid_days` INT DEFAULT 0 COMMENT '有效天数（0永久）',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `user_count` INT DEFAULT 0 COMMENT '用户数量',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_priority` (`priority`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户分组表';

-- 插入默认用户分组
INSERT INTO `user_groups` (`name`, `description`, `priority`, `level`, `global_rate`) VALUES
('免费用户', '免费注册用户', 0, 'free', 1.00),
('基础用户', '基础会员', 1, 'basic', 0.95),
('专业用户', '专业会员', 2, 'pro', 0.90),
('企业用户', '企业级用户', 3, 'enterprise', 0.80);

-- =========================================
-- 17. API代理表
-- =========================================
CREATE TABLE IF NOT EXISTS `api_proxies` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '代理ID',
    `name` VARCHAR(100) NOT NULL COMMENT '代理名称',
    `type` VARCHAR(50) DEFAULT 'proxy_forward' COMMENT '类型',
    `target_url` VARCHAR(500) NOT NULL COMMENT '目标URL',
    `source_path` VARCHAR(200) DEFAULT NULL COMMENT '源路径',
    `target_path` VARCHAR(200) DEFAULT NULL COMMENT '目标路径',
    `methods` VARCHAR(50) DEFAULT '*' COMMENT '请求方法',
    -- 转换规则
    `header_transforms` TEXT DEFAULT NULL COMMENT '请求头转换',
    `request_transforms` TEXT DEFAULT NULL COMMENT '请求体转换',
    `response_transforms` TEXT DEFAULT NULL COMMENT '响应转换',
    -- 关联配置
    `model_ids` TEXT DEFAULT NULL COMMENT '关联模型JSON',
    `channel_ids` TEXT DEFAULT NULL COMMENT '关联渠道JSON',
    `allowed_groups` TEXT DEFAULT NULL COMMENT '允许分组JSON',
    -- 高级设置
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `timeout` INT DEFAULT 30000 COMMENT '超时时间ms',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `log_requests` TINYINT(1) DEFAULT 1 COMMENT '记录请求',
    `log_responses` TINYINT(1) DEFAULT 0 COMMENT '记录响应',
    `cache_enabled` TINYINT(1) DEFAULT 0 COMMENT '启用缓存',
    `cache_ttl` INT DEFAULT 3600 COMMENT '缓存TTL秒',
    `auth_required` VARCHAR(20) DEFAULT 'required' COMMENT '认证要求',
    `rate_limit` TEXT DEFAULT NULL COMMENT '限流配置',
    -- 状态
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `request_count` BIGINT DEFAULT 0 COMMENT '请求计数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_priority` (`priority`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API代理表';

-- =========================================
-- 18. 扩展系统设置表
-- =========================================
CREATE TABLE IF NOT EXISTS `system_settings` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `category` VARCHAR(50) NOT NULL COMMENT '分类',
    `setting_key` VARCHAR(100) NOT NULL COMMENT '键',
    `setting_value` TEXT DEFAULT NULL COMMENT '值',
    `value_type` VARCHAR(20) DEFAULT 'string' COMMENT '类型',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_key` (`category`, `setting_key`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- 插入扩展系统设置
INSERT INTO `system_settings` (`category`, `setting_key`, `setting_value`, `value_type`, `description`) VALUES
-- 自定义设置
('customization', 'site_logo', '/assets/logo.png', 'string', '网站Logo'),
('customization', 'site_favicon', '/assets/favicon.ico', 'string', '网站图标'),
('customization', 'footer_text', '© 2024 API Platform. All rights reserved.', 'string', '页脚文本'),
('customization', 'welcome_message', '欢迎使用API Platform', 'string', '欢迎语'),
-- SEO设置
('seo', 'meta_title', 'API Platform - AI API聚合平台', 'string', '网站标题'),
('seo', 'meta_description', '专业AI API聚合平台，支持OpenAI、Claude、国产大模型', 'string', '网站描述'),
('seo', 'meta_keywords', 'AI,API,OpenAI,Claude,ChatGPT,GPT-4', 'string', '关键词'),
('seo', 'google_analytics_id', '', 'string', 'GA追踪ID'),
-- 注册限制
('registration', 'allow_register', 'true', 'boolean', '是否允许注册'),
('registration', 'require_email_verify', 'false', 'boolean', '是否需要邮箱验证'),
('registration', 'require_admin_approve', 'false', 'boolean', '是否需要管理员审批'),
('registration', 'max_accounts_per_ip', '5', 'number', '每IP最大账户数'),
('registration', 'default_group_id', '1', 'number', '新用户默认分组'),
-- 额度限制
('quota', 'new_user_bonus', '0', 'number', '新用户赠送积分'),
('quota', 'daily_bonus_enabled', 'false', 'boolean', '是否启用每日签到'),
('quota', 'daily_bonus_amount', '10', 'number', '每日签到积分'),
-- 代理设置
('proxy', 'enable_proxy', 'false', 'boolean', '是否启用API代理'),
('proxy', 'proxy_timeout', '30000', 'number', '代理超时ms'),
('proxy', 'proxy_retry_count', '2', 'number', '代理重试次数');

-- 添加用户分组ID字段到users表
ALTER TABLE `users` ADD COLUMN IF NOT EXISTS `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户分组ID' AFTER `role`;
ALTER TABLE `users` ADD COLUMN IF NOT EXISTS `expire_time` DATETIME DEFAULT NULL COMMENT '账户过期时间' AFTER `group_id`;

-- 创建索引
CREATE INDEX idx_users_balance ON users(balance);
CREATE INDEX idx_tokens_api_key ON tokens(api_key);
CREATE INDEX idx_usage_logs_created ON usage_logs(created_at);
CREATE INDEX idx_orders_created ON orders(created_at);
