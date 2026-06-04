-- =========================================
-- 邀请码注册功能数据库迁移
-- =========================================

-- 1. 为 users 表添加邀请相关字段
ALTER TABLE `users` 
ADD COLUMN `inviter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '邀请人ID' AFTER `remark`,
ADD COLUMN `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '用户的邀请码' AFTER `inviter_id`,
ADD INDEX `idx_inviter_id` (`inviter_id`),
ADD INDEX `idx_invite_code` (`invite_code`);

-- 2. 邀请奖励配置表（存储邀请奖励规则）
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

-- 3. 插入默认邀请奖励配置
INSERT INTO `invite_rewards` (`name`, `type`, `condition_type`, `condition_value`, `inviter_reward_type`, `inviter_reward_value`, `invitee_reward_type`, `invitee_reward_value`, `description`, `priority`, `status`) VALUES
-- 注册奖励：邀请人获得100积分，被邀请人获得50积分
('新用户注册奖励', 'register', 'once', 1, 'fixed', 100, 'fixed', 50, '新用户注册时，邀请人和被邀请人分别获得积分奖励', 1, 'active'),
-- 首充奖励：邀请人获得充值金额的10%，被邀请人获得5%
('首充奖励', 'recharge', 'once', 1, 'percentage', 1000, 'percentage', 500, '被邀请人首次充值时，邀请人获得10%，被邀请人获得5%奖励', 2, 'active');

-- 4. 用户邀请奖励记录表
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
