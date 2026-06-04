-- =========================================
-- 代理商系统数据库表
-- =========================================

-- 1. 代理商等级表
CREATE TABLE IF NOT EXISTS `agent_levels` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
    `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
    `code` VARCHAR(20) NOT NULL COMMENT '等级代码',
    `level` INT DEFAULT 1 COMMENT '等级数值',
    `min_users` INT DEFAULT 0 COMMENT '最少下级用户数',
    `min_recharge` BIGINT DEFAULT 0 COMMENT '最少累计充值金额（分）',
    `commission_rate` DECIMAL(5,2) DEFAULT 0.10 COMMENT '佣金比例',
    `bonus_rate` DECIMAL(5,2) DEFAULT 0.05 COMMENT '推荐奖励比例',
    `price_discount` DECIMAL(5,2) DEFAULT 1.00 COMMENT '自己购买折扣',
    `sub_agent_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否允许发展下级代理',
    `sub_agent_rate` DECIMAL(5,2) DEFAULT 0.30 COMMENT '下级代理佣金比例',
    `description` TEXT DEFAULT NULL COMMENT '等级说明',
    `benefits` TEXT DEFAULT NULL COMMENT '等级权益JSON',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商等级表';

-- 2. 代理商表
CREATE TABLE IF NOT EXISTS `agents` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '代理商ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `agent_code` VARCHAR(20) NOT NULL COMMENT '代理商代码（邀请码）',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上级代理商ID',
    `level_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '等级ID',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/active/suspended/rejected',
    `total_users` INT DEFAULT 0 COMMENT '下级用户数',
    `total_recharge` BIGINT DEFAULT 0 COMMENT '累计充值金额（分）',
    `total_commission` BIGINT DEFAULT 0 COMMENT '累计获得佣金（分）',
    `available_commission` BIGINT DEFAULT 0 COMMENT '可提现佣金（分）',
    `frozen_commission` BIGINT DEFAULT 0 COMMENT '冻结佣金（分）',
    -- 联系方式
    `company_name` VARCHAR(200) DEFAULT NULL COMMENT '公司名称',
    `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `business_license` VARCHAR(200) DEFAULT NULL COMMENT '营业执照',
    -- 收款信息
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `bank_branch` VARCHAR(200) DEFAULT NULL COMMENT '支行名称',
    `alipay_account` VARCHAR(100) DEFAULT NULL COMMENT '支付宝账号',
    `wechat_account` VARCHAR(100) DEFAULT NULL COMMENT '微信账户',
    -- 审核信息
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `reviewed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
    `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_agent_code` (`agent_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商表';

-- 3. 佣金记录表
CREATE TABLE IF NOT EXISTS `agent_commissions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '消费用户ID',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: recharge/package/upgrade/bonus/withdraw/freeze',
    `amount` BIGINT NOT NULL COMMENT '变动金额（分）',
    `balance_before` BIGINT DEFAULT 0 COMMENT '变动前余额',
    `balance_after` BIGINT DEFAULT 0 COMMENT '变动后余额',
    `rate` DECIMAL(5,4) DEFAULT NULL COMMENT '佣金比例',
    `order_amount` BIGINT DEFAULT NULL COMMENT '原订单金额（分）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status` VARCHAR(20) DEFAULT 'completed' COMMENT '状态',
    `settled_at` DATETIME DEFAULT NULL COMMENT '结算时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金记录表';

-- 4. 提现记录表
CREATE TABLE IF NOT EXISTS `agent_withdrawals` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '提现ID',
    `withdrawal_no` VARCHAR(50) NOT NULL COMMENT '提现单号',
    `agent_id` BIGINT UNSIGNED NOT NULL COMMENT '代理商ID',
    `amount` BIGINT NOT NULL COMMENT '提现金额（分）',
    `fee` BIGINT DEFAULT 0 COMMENT '手续费（分）',
    `actual_amount` BIGINT NOT NULL COMMENT '实际到账金额（分）',
    `method` VARCHAR(20) NOT NULL COMMENT '提现方式: bank/alipay/wechat',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/processing/completed/rejected',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `alipay_account` VARCHAR(100) DEFAULT NULL COMMENT '支付宝账号',
    `wechat_account` VARCHAR(100) DEFAULT NULL COMMENT '微信账户',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `processed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
    `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_withdrawal_no` (`withdrawal_no`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商提现记录表';

-- 5. 代理分成配置表
CREATE TABLE IF NOT EXISTS `agent_commission_rules` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: recharge/package/upgrade',
    `condition_type` VARCHAR(20) DEFAULT 'all' COMMENT '条件类型: all/amount/user_count',
    `condition_value` TEXT DEFAULT NULL COMMENT '条件值JSON',
    `commission_type` VARCHAR(20) DEFAULT 'rate' COMMENT '佣金类型: rate/fixed',
    `commission_value` DECIMAL(10,4) DEFAULT 0 COMMENT '佣金值（比例或固定金额）',
    `min_amount` BIGINT DEFAULT 0 COMMENT '最低订单金额（分）',
    `max_amount` BIGINT DEFAULT NULL COMMENT '最高订单金额（分）',
    `level_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '代理商等级（NULL表示全部等级）',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '规则说明',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理分成配置表';

-- 插入默认代理商等级
INSERT INTO `agent_levels` (`name`, `code`, `level`, `min_users`, `min_recharge`, `commission_rate`, `bonus_rate`, `price_discount`, `sub_agent_enabled`, `sub_agent_rate`, `description`, `benefits`, `sort_order`, `status`) VALUES
('青铜代理', 'bronze', 1, 0, 0, 0.05, 0.02, 0.95, 0, 0.00, '初级代理商，享受5%佣金比例', '{"support": ["在线客服"], "discount": ["9.5折"]}', 1, 'active'),
('白银代理', 'silver', 2, 10, 1000000, 0.08, 0.03, 0.90, 1, 0.20, '中级代理商，享受8%佣金比例', '{"support": ["专属客服", "优先处理"], "discount": ["9折"]}', 2, 'active'),
('黄金代理', 'gold', 3, 50, 10000000, 0.12, 0.05, 0.85, 1, 0.25, '高级代理商，享受12%佣金比例', '{"support": ["专属客服", "7x24支持", "优先处理"], "discount": ["8.5折"], "features": ["自定义定价"]}', 3, 'active'),
('钻石代理', 'diamond', 4, 200, 100000000, 0.18, 0.08, 0.80, 1, 0.30, '顶级代理商，享受18%佣金比例', '{"support": ["专属客服", "7x24支持", "VIP通道"], "discount": ["8折"], "features": ["自定义定价", "独立API"]}', 4, 'active');

-- 插入默认分成规则
INSERT INTO `agent_commission_rules` (`name`, `type`, `commission_type`, `commission_value`, `description`, `priority`, `status`) VALUES
('充值佣金', 'recharge', 'rate', 0.05, '用户充值时，代理商获得5%佣金', 1, 'active'),
('套餐购买佣金', 'package', 'rate', 0.08, '用户购买套餐时，代理商获得8%佣金', 2, 'active'),
('升级佣金', 'upgrade', 'rate', 0.10, '用户升级套餐时，代理商获得10%佣金', 3, 'active');

-- 添加字段到 users 表（如果有邀请人字段）
-- ALTER TABLE `users` ADD COLUMN `inviter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '邀请人ID' AFTER `group_id`;
-- ALTER TABLE `users` ADD COLUMN `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码' AFTER `inviter_id`;
