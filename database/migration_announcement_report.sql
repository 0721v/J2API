-- =====================================================
-- 系统公告与报表功能数据库迁移
-- =====================================================

-- =====================================================
-- 1. 系统公告表
-- =====================================================
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

-- =====================================================
-- 2. 公告阅读记录表
-- =====================================================
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

-- =====================================================
-- 3. 站内消息表（支持多种消息类型）
-- =====================================================
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

-- =====================================================
-- 4. 使用记录增强表（报表用）
-- =====================================================
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `date` DATE NULL COMMENT '使用日期，方便统计' AFTER `status`;
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `hour` TINYINT NULL COMMENT '使用小时 0-23' AFTER `date`;
ALTER TABLE `usage_logs` ADD COLUMN IF NOT EXISTS `request_id` VARCHAR(64) NULL COMMENT '请求唯一ID' AFTER `hour`;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_usage_user_date` ON `usage_logs` (`user_id`, `date`);
CREATE INDEX IF NOT EXISTS `idx_usage_date` ON `usage_logs` (`date`);

-- =====================================================
-- 5. 消费记录表（独立报表表）
-- =====================================================
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

-- =====================================================
-- 6. 每日用户统计表（预聚合，加速报表查询）
-- =====================================================
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

-- =====================================================
-- 插入默认公告
-- =====================================================
INSERT INTO `announcements` (`title`, `content`, `type`, `priority`, `status`, `published_at`, `created_by`) VALUES
('欢迎使用API聚合平台', '欢迎来到API聚合平台！我们提供稳定的AI API服务，支持OpenAI兼容接口。祝您使用愉快！', 'important', 100, 'published', NOW(), 1),
('系统维护通知', '系统将于本周日凌晨2:00-4:00进行例行维护，届时服务可能暂时不可用。给您带来不便敬请谅解。', 'warning', 50, 'published', NOW(), 1);
