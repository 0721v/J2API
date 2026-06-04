-- =====================================================
-- API聚合平台 - 完整数据库初始化脚本
-- 版本: v1.0.0
-- 更新日期: 2024年
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `api_platform` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `api_platform`;

-- =====================================================
-- 第一部分：基础表结构 (schema.sql)
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `balance` BIGINT NOT NULL DEFAULT 0 COMMENT '余额（分）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/disabled',
    `user_group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户组ID',
    `oauth_provider` VARCHAR(50) DEFAULT NULL COMMENT 'OAuth提供商',
    `oauth_id` VARCHAR(100) DEFAULT NULL COMMENT 'OAuth用户ID',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `email_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否验证',
    `email_verified_at` DATETIME DEFAULT NULL COMMENT '邮箱验证时间',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `login_attempts` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `locked_until` DATETIME DEFAULT NULL COMMENT '账户锁定截止时间',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_oauth` (`oauth_provider`, `oauth_id`),
    KEY `idx_user_group` (`user_group_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户组表
CREATE TABLE IF NOT EXISTS `user_groups` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户组ID',
    `name` VARCHAR(50) NOT NULL COMMENT '用户组名称',
    `code` VARCHAR(50) NOT NULL COMMENT '用户组代码',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `discount` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '折扣率(%)',
    `monthly_limit` BIGINT DEFAULT NULL COMMENT '月度额度限制（分）',
    `daily_limit` BIGINT DEFAULT NULL COMMENT '日度额度限制（分）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认组',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户组表';

-- 模型表
CREATE TABLE IF NOT EXISTS `models` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_id` VARCHAR(100) NOT NULL COMMENT '模型标识符',
    `type` VARCHAR(50) NOT NULL COMMENT '模型类型: chat/completion/embedding/image/audio/video',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `input_price` BIGINT DEFAULT 0 COMMENT '输入价格（分/1K tokens）',
    `output_price` BIGINT DEFAULT 0 COMMENT '输出价格（分/1K tokens）',
    `input_price_usd` DECIMAL(10,6) DEFAULT 0 COMMENT '输入价格（美元/1K tokens）',
    `output_price_usd` DECIMAL(10,6) DEFAULT 0 COMMENT '输出价格（美元/1K tokens）',
    `max_tokens` INT DEFAULT NULL COMMENT '最大Token数',
    `supports_streaming` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否支持流式输出',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_id` (`model_id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型表';

-- 渠道表
CREATE TABLE IF NOT EXISTS `channels` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '渠道ID',
    `name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
    `type` VARCHAR(50) NOT NULL COMMENT '渠道类型: openai/azure/anthropic/gemini/custom',
    `base_url` VARCHAR(500) NOT NULL COMMENT 'API基础URL',
    `api_key` VARCHAR(500) DEFAULT NULL COMMENT 'API密钥',
    `api_secret` VARCHAR(500) DEFAULT NULL COMMENT 'API密钥（加密存储）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认渠道',
    `weight` INT NOT NULL DEFAULT 100 COMMENT '权重',
    `models` TEXT DEFAULT NULL COMMENT '支持的模型列表（JSON）',
    `timeout` INT NOT NULL DEFAULT 60000 COMMENT '超时时间（毫秒）',
    `retry_times` INT NOT NULL DEFAULT 3 COMMENT '重试次数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/error/disable',
    `error_count` INT NOT NULL DEFAULT 0 COMMENT '连续错误次数',
    `last_error` TEXT DEFAULT NULL COMMENT '最后错误信息',
    `last_test_at` DATETIME DEFAULT NULL COMMENT '最后测试时间',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `config` TEXT DEFAULT NULL COMMENT '额外配置（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_weight` (`weight`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API渠道表';

-- API密钥表
CREATE TABLE IF NOT EXISTS `tokens` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '密钥ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '密钥名称',
    `api_key` VARCHAR(64) NOT NULL COMMENT 'API密钥',
    `api_secret` VARCHAR(128) NOT NULL COMMENT 'API密钥哈希',
    `secret_key` VARCHAR(64) DEFAULT NULL COMMENT '用于请求签名的密钥',
    `type` VARCHAR(20) NOT NULL DEFAULT 'full' COMMENT '类型: full/readonly',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/disabled',
    `rate_limit` INT DEFAULT NULL COMMENT '每分钟请求限制',
    `daily_limit` BIGINT DEFAULT NULL COMMENT '每日额度限制（分）',
    `monthly_limit` BIGINT DEFAULT NULL COMMENT '每月额度限制（分）',
    `used_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用额度（分）',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `last_used_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后使用IP',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key` (`api_key`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '订单类型: recharge/package/subscription',
    `amount` BIGINT NOT NULL COMMENT '订单金额（分）',
    `actual_amount` BIGINT NOT NULL COMMENT '实付金额（分）',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '货币',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/paid/completed/cancelled/refunded',
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式: alipay/wechat/stripe/okx/creem',
    `payment_channel` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '支付流水号',
    `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
    `package_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '套餐ID',
    `description` TEXT DEFAULT NULL COMMENT '订单描述',
    `metadata` TEXT DEFAULT NULL COMMENT '额外数据（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_payment_method` (`payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 套餐表
CREATE TABLE IF NOT EXISTS `packages` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `description` TEXT DEFAULT NULL COMMENT '套餐描述',
    `type` VARCHAR(50) NOT NULL DEFAULT 'recharge' COMMENT '类型: recharge/package/subscription',
    `price` BIGINT NOT NULL COMMENT '价格（分）',
    `credits` BIGINT NOT NULL COMMENT '包含额度（分）',
    `bonus` BIGINT DEFAULT 0 COMMENT '赠送额度（分）',
    `validity_days` INT DEFAULT NULL COMMENT '有效期（天）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_recommended` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推荐',
    `metadata` TEXT DEFAULT NULL COMMENT '额外配置（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值套餐表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: recharge/consume/refund/reward/adjustment',
    `amount` BIGINT NOT NULL COMMENT '金额（分）',
    `balance_before` BIGINT NOT NULL COMMENT '变动前余额',
    `balance_after` BIGINT NOT NULL COMMENT '变动后余额',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
    `usage_log_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联使用记录ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 使用日志表
CREATE TABLE IF NOT EXISTS `usage_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `token_id` BIGINT UNSIGNED NOT NULL COMMENT '密钥ID',
    `channel_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '渠道ID',
    `model_id` BIGINT UNSIGNED NOT NULL COMMENT '模型ID',
    `model` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `input_tokens` INT DEFAULT 0 COMMENT '输入Token数',
    `output_tokens` INT DEFAULT 0 COMMENT '输出Token数',
    `input_cost` BIGINT DEFAULT 0 COMMENT '输入费用（分）',
    `output_cost` BIGINT DEFAULT 0 COMMENT '输出费用（分）',
    `total_cost` BIGINT NOT NULL DEFAULT 0 COMMENT '总费用（分）',
    `latency_ms` INT DEFAULT NULL COMMENT '响应延迟（毫秒）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '状态: success/error/partial',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT '请求IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_token_id` (`token_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_model_id` (`model_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API使用日志表';

-- 系统设置表
CREATE TABLE IF NOT EXISTS `settings` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: general/payment/security/ratelimit',
    `key` VARCHAR(100) NOT NULL COMMENT '键',
    `value` TEXT DEFAULT NULL COMMENT '值',
    `type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '类型: string/number/boolean/json',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_key` (`category`, `key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- 用户余额变动记录表（审计用）
CREATE TABLE IF NOT EXISTS `balance_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: recharge/consume/refund/reward/adjustment/invite_reward',
    `amount` BIGINT NOT NULL COMMENT '变动金额（分）',
    `balance_before` BIGINT NOT NULL COMMENT '变动前余额',
    `balance_after` BIGINT NOT NULL COMMENT '变动后余额',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（后台管理员）',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（如订单ID）',
    `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联类型',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT '操作IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额变动记录表';

-- =====================================================
-- 第二部分：代理商相关表 (agent_schema.sql)
-- =====================================================

-- 代理商等级表
CREATE TABLE IF NOT EXISTS `agent_levels` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
    `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
    `code` VARCHAR(50) NOT NULL COMMENT '等级代码',
    `level` INT NOT NULL COMMENT '等级数值，越大越高',
    `min_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '累计业绩要求（分）',
    `commission_rate` INT NOT NULL DEFAULT 500 COMMENT '佣金比例（基点，500=5%）',
    `max_commission` BIGINT DEFAULT NULL COMMENT '单月最高佣金（分）',
    `benefits` TEXT DEFAULT NULL COMMENT '等级权益（JSON）',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '等级图标',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '等级颜色',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商等级表';

-- 代理商表
CREATE TABLE IF NOT EXISTS `agents` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '代理商ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `agent_code` VARCHAR(20) NOT NULL COMMENT '代理商邀请码',
    `level_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前等级ID',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上级代理商ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/active/disabled',
    `total_users` INT NOT NULL DEFAULT 0 COMMENT '累计用户数',
    `active_users` INT NOT NULL DEFAULT 0 COMMENT '活跃用户数',
    `total_recharge` BIGINT NOT NULL DEFAULT 0 COMMENT '累计充值金额（分）',
    `total_commission` BIGINT NOT NULL DEFAULT 0 COMMENT '累计获得佣金（分）',
    `available_commission` BIGINT NOT NULL DEFAULT 0 COMMENT '可用佣金（分）',
    `pending_commission` BIGINT NOT NULL DEFAULT 0 COMMENT '待结算佣金（分）',
    `withdrawn_commission` BIGINT NOT NULL DEFAULT 0 COMMENT '已提现佣金（分）',
    `applied_at` DATETIME DEFAULT NULL COMMENT '申请时间',
    `approved_at` DATETIME DEFAULT NULL COMMENT '审核通过时间',
    `approved_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_agent_code` (`agent_code`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商表';

-- 代理商佣金记录表
CREATE TABLE IF NOT EXISTS `agent_commissions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联用户ID（被邀请用户）',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: recharge/comsumption/register',
    `order_amount` BIGINT DEFAULT 0 COMMENT '订单金额（分）',
    `commission_rate` INT NOT NULL COMMENT '佣金比例（基点）',
    `commission_amount` BIGINT NOT NULL COMMENT '佣金金额（分）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/settled/withdrawn/cancelled',
    `settled_at` DATETIME DEFAULT NULL COMMENT '结算时间',
    `settled_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '结算人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商佣金记录表';

-- 代理商提现记录表
CREATE TABLE IF NOT EXISTS `agent_withdrawals` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '提现ID',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `amount` BIGINT NOT NULL COMMENT '提现金额（分）',
    `fee` BIGINT NOT NULL DEFAULT 0 COMMENT '手续费（分）',
    `actual_amount` BIGINT NOT NULL COMMENT '实际到账金额（分）',
    `payment_method` VARCHAR(50) NOT NULL COMMENT '支付方式: alipay/bank/wechat',
    `payment_account` VARCHAR(200) NOT NULL COMMENT '收款账户',
    `payment_name` VARCHAR(100) DEFAULT NULL COMMENT '收款人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processed/approved/rejected/completed/failed',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `processed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '支付流水号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商提现记录表';

-- =====================================================
-- 第三部分：代理商扩展表 (agent_extension.sql)
-- =====================================================

-- 代理商消息通知表
CREATE TABLE IF NOT EXISTS `agent_notifications` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT DEFAULT NULL COMMENT '通知内容',
    `amount` BIGINT DEFAULT NULL COMMENT '相关金额（分）',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
    `data` TEXT DEFAULT NULL COMMENT '关联数据JSON',
    `status` VARCHAR(20) DEFAULT 'unread' COMMENT '状态: unread/read',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级',
    `pushed` TINYINT(1) DEFAULT 0 COMMENT '是否已推送',
    `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商消息通知表';

-- 邀请关系追踪表
CREATE TABLE IF NOT EXISTS `invite_records` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `inviter_id` BIGINT UNSIGNED NOT NULL COMMENT '邀请人ID',
    `invitee_id` BIGINT UNSIGNED NOT NULL COMMENT '被邀请人ID',
    `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '使用的邀请码',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源: link/qrcode/manual',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/cancelled',
    `rewards_credited` TINYINT(1) DEFAULT 0 COMMENT '奖励是否已发放',
    `reward_amount` BIGINT DEFAULT 0 COMMENT '奖励金额（分）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invitee_id` (`invitee_id`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请关系追踪表';

-- 代理商业绩统计表（按月统计）
CREATE TABLE IF NOT EXISTS `agent_monthly_stats` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `year` INT NOT NULL COMMENT '统计年份',
    `month` INT NOT NULL COMMENT '统计月份',
    `new_users` INT DEFAULT 0 COMMENT '新增用户数',
    `total_users` INT DEFAULT 0 COMMENT '累计用户数',
    `new_recharge` BIGINT DEFAULT 0 COMMENT '本月充值金额（分）',
    `total_recharge` BIGINT DEFAULT 0 COMMENT '累计充值金额（分）',
    `commission_earned` BIGINT DEFAULT 0 COMMENT '本月获得佣金（分）',
    `commission_withdrawn` BIGINT DEFAULT 0 COMMENT '本月提现金额（分）',
    `commission_balance` BIGINT DEFAULT 0 COMMENT '期末佣金余额（分）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_year_month` (`agent_id`, `year`, `month`),
    KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商业绩统计表';

-- =====================================================
-- 第四部分：邀请奖励功能 (migration_invite.sql)
-- =====================================================

-- 为 users 表添加邀请相关字段
ALTER TABLE `users` 
ADD COLUMN `inviter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '邀请人ID' AFTER `remark`,
ADD COLUMN `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '用户的邀请码' AFTER `inviter_id`,
ADD INDEX `idx_inviter_id` (`inviter_id`),
ADD INDEX `idx_invite_code` (`invite_code`);

-- 邀请奖励配置表（存储邀请奖励规则）
CREATE TABLE IF NOT EXISTS `invite_rewards` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `name` VARCHAR(100) NOT NULL COMMENT '奖励名称',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: register/recharge/consumption',
    `condition_type` VARCHAR(20) DEFAULT 'once' COMMENT '条件类型: once/every/nth',
    `condition_value` INT DEFAULT 1 COMMENT '条件值',
    `inviter_reward_type` VARCHAR(20) DEFAULT 'fixed' COMMENT '邀请人奖励类型: fixed/percentage',
    `inviter_reward_value` BIGINT DEFAULT 0 COMMENT '邀请人奖励值（分或百分比*100）',
    `invitee_reward_type` VARCHAR(20) DEFAULT 'fixed' COMMENT '被邀请人奖励类型',
    `invitee_reward_value` BIGINT DEFAULT 0 COMMENT '被邀请人奖励值',
    `min_recharge_amount` BIGINT DEFAULT 0 COMMENT '最低充值金额（分），用于充值奖励',
    `max_reward_amount` BIGINT DEFAULT NULL COMMENT '单次最高奖励（分）',
    `total_limit` INT DEFAULT NULL COMMENT '总奖励次数限制',
    `daily_limit` INT DEFAULT NULL COMMENT '每日奖励次数限制',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '奖励说明',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请奖励配置表';

-- 插入默认邀请奖励配置
INSERT INTO `invite_rewards` (`name`, `type`, `condition_type`, `condition_value`, `inviter_reward_type`, `inviter_reward_value`, `invitee_reward_type`, `invitee_reward_value`, `description`, `priority`, `status`) VALUES
-- 注册奖励：邀请人获得100积分，被邀请人获得50积分
('新用户注册奖励', 'register', 'once', 1, 'fixed', 100, 'fixed', 50, '新用户注册时，邀请人和被邀请人分别获得积分奖励', 1, 'active'),
-- 首充奖励：邀请人获得充值金额的10%，被邀请人获得5%
('首充奖励', 'recharge', 'once', 1, 'percentage', 1000, 'percentage', 500, '被邀请人首次充值时，邀请人获得10%，被邀请人获得5%奖励', 2, 'active');

-- 用户邀请奖励记录表
CREATE TABLE IF NOT EXISTS `invite_reward_records` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `invite_record_id` BIGINT UNSIGNED NOT NULL COMMENT '邀请记录ID',
    `inviter_id` BIGINT UNSIGNED NOT NULL COMMENT '邀请人ID',
    `invitee_id` BIGINT UNSIGNED NOT NULL COMMENT '被邀请人ID',
    `reward_type` VARCHAR(20) NOT NULL COMMENT '奖励类型: register/recharge/consumption',
    `reward_to` VARCHAR(20) NOT NULL COMMENT '奖励对象: inviter/invitee',
    `amount` BIGINT NOT NULL COMMENT '奖励金额（分）',
    `balance_before` BIGINT DEFAULT 0 COMMENT '变动前余额',
    `balance_after` BIGINT DEFAULT 0 COMMENT '变动后余额',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '奖励描述',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID（用于充值奖励）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_invite_record_id` (`invite_record_id`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_invitee_id` (`invitee_id`),
    KEY `idx_reward_type` (`reward_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户邀请奖励记录表';

-- =====================================================
-- 第五部分：公告和报表功能 (migration_announcement_report.sql)
-- =====================================================

-- 系统公告表
CREATE TABLE IF NOT EXISTS `announcements` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `type` VARCHAR(20) NOT NULL DEFAULT 'info' COMMENT '公告类型: info/notice/warning/important',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，越大越靠前',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published/archived',
    `published_at` DATETIME NULL COMMENT '发布时间',
    `expired_at` DATETIME NULL COMMENT '过期时间，为空表示永不过期',
    `target_type` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT '发布对象: all/all_users/all_agents/specific_users',
    `target_users` TEXT NULL COMMENT '特定用户ID列表，JSON格式',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status_published` (`status`, `published_at`),
    INDEX `idx_priority` (`priority` DESC),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 公告阅读记录表
CREATE TABLE IF NOT EXISTS `announcement_reads` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `read_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_read_at` (`read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告阅读记录表';

-- 站内消息表（支持多种消息类型）
CREATE TABLE IF NOT EXISTS `messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(30) NOT NULL COMMENT '消息类型: system/recharge/usage/balance/agent/security',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `data` JSON NULL COMMENT '附加数据，如订单ID、用量详情等',
    `priority` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '优先级: low/normal/high/urgent',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
    `read_at` DATETIME NULL COMMENT '阅读时间',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    `expires_at` DATETIME NULL COMMENT '过期时间，为空永不过期',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_unread` (`user_id`, `is_read`, `is_deleted`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息表';

-- 使用记录增强表（报表用）
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `date` DATE NULL COMMENT '使用日期，方便统计' AFTER `status`;
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `hour` TINYINT NULL COMMENT '使用小时 0-23' AFTER `date`;
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `request_id` VARCHAR(64) NULL COMMENT '请求唯一ID' AFTER `hour`;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_usage_user_date` ON `usage_logs` (`user_id`, `date`);
CREATE INDEX IF NOT EXISTS `idx_usage_date` ON `usage_logs` (`date`);

-- 消费记录表（独立报表表）
CREATE TABLE IF NOT EXISTS `consumption_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '消费类型: api_call/recharge/refund/reward/adjustment',
    `category` VARCHAR(50) NULL COMMENT '消费类别: chat/completion/embedding/video/audio',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '消费/充值金额（正数）',
    `balance_before` DECIMAL(10, 2) NOT NULL COMMENT '变动前余额',
    `balance_after` DECIMAL(10, 2) NOT NULL COMMENT '变动后余额',
    `description` VARCHAR(500) NULL COMMENT '描述',
    `related_id` BIGINT NULL COMMENT '关联ID（如订单ID、用量日志ID）',
    `related_type` VARCHAR(50) NULL COMMENT '关联类型（如 order, usage_log）',
    `metadata` JSON NULL COMMENT '附加元数据',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_type_date` (`user_id`, `type`, `created_at`),
    INDEX `idx_user_date` (`user_id`, `created_at`),
    INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- 每日用户统计表（预聚合，加速报表查询）
CREATE TABLE IF NOT EXISTS `daily_user_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `total_api_calls` INT NOT NULL DEFAULT 0 COMMENT 'API调用次数',
    `total_input_tokens` BIGINT NULL COMMENT '总输入Token数',
    `total_output_tokens` BIGINT NULL COMMENT '总输出Token数',
    `total_cost` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '总消费金额',
    `recharge_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '充值金额',
    `reward_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '奖励金额',
    `refund_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '退款金额',
    `balance_end` DECIMAL(10, 2) NULL COMMENT '日终余额',
    `active_tokens` INT NULL COMMENT '活跃的API Key数量',
    `model_usage` JSON NULL COMMENT '各模型使用量 JSON',
    `channel_usage` JSON NULL COMMENT '各渠道使用量 JSON',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
    INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日用户统计表';

-- 插入默认公告
INSERT INTO `announcements` (`title`, `content`, `type`, `priority`, `status`, `published_at`, `created_by`) VALUES
('欢迎使用API聚合平台', '欢迎来到API聚合平台！我们提供稳定的AI API服务，支持OpenAI兼容接口。祝您使用愉快！', 'important', 100, 'published', NOW(), 1),
('系统维护通知', '系统将于本周日凌晨2:00-4:00进行例行维护，届时服务可能暂时不可用。给您带来不便敬请谅解。', 'warning', 50, 'published', NOW(), 1);

-- =====================================================
-- 初始化默认数据
-- =====================================================

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO `users` (`username`, `email`, `password`, `balance`, `status`, `email_verified`, `remark`) VALUES
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.sSLQoQfQCLfBhQ9M8u', 0, 'active', 1, '系统管理员');

-- 插入默认用户组
INSERT INTO `user_groups` (`name`, `code`, `description`, `priority`, `discount`, `enabled`, `is_default`) VALUES
('免费用户', 'free', '默认用户组', 0, 100.00, 1, 1),
('VIP用户', 'vip', 'VIP用户组，享受9折优惠', 10, 90.00, 1, 0),
('企业用户', 'enterprise', '企业用户组，享受8折优惠', 20, 80.00, 1, 0);

-- 插入默认代理商等级
INSERT INTO `agent_levels` (`name`, `code`, `level`, `min_amount`, `commission_rate`, `max_commission`, `sort_order`, `enabled`) VALUES
('青铜代理', 'bronze', 1, 0, 500, 100000, 1, 1),
('白银代理', 'silver', 2, 1000000, 800, 500000, 2, 1),
('黄金代理', 'gold', 3, 5000000, 1200, 2000000, 3, 1),
('铂金代理', 'platinum', 4, 20000000, 1500, NULL, 4, 1);

-- 插入默认套餐
INSERT INTO `packages` (`name`, `description`, `type`, `price`, `credits`, `bonus`, `validity_days`, `sort_order`, `enabled`, `is_recommended`) VALUES
('入门套餐', '适合新手体验', 'package', 1000, 1000, 0, 30, 1, 1, 0),
('基础套餐', '日常使用推荐', 'package', 5000, 5000, 500, 90, 2, 1, 1),
('专业套餐', '高频使用必备', 'package', 10000, 10000, 1500, 180, 3, 1, 0),
('企业套餐', '企业用户首选', 'package', 50000, 50000, 10000, 365, 4, 1, 0);

-- =====================================================
-- 数据库初始化完成
-- =====================================================
