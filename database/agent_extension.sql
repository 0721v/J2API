-- =========================================
-- 代理商消息通知表
-- =========================================

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

-- =========================================
-- 邀请关系追踪表
-- =========================================

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

-- =========================================
-- 代理商业绩统计表（按月统计）
-- =========================================

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
