package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请奖励配置实体
 */
@Data
@TableName("invite_rewards")
public class InviteReward {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 奖励名称
     */
    private String name;

    /**
     * 类型: register/recharge/consumption
     */
    private String type;

    /**
     * 条件类型: once/every/nth
     */
    private String conditionType;

    /**
     * 条件值
     */
    private Integer conditionValue;

    /**
     * 邀请人奖励类型: fixed/percentage
     */
    private String inviterRewardType;

    /**
     * 邀请人奖励值（分或百分比*100）
     */
    private Long inviterRewardValue;

    /**
     * 被邀请人奖励类型
     */
    private String inviteeRewardType;

    /**
     * 被邀请人奖励值
     */
    private Long inviteeRewardValue;

    /**
     * 最低充值金额（分）
     */
    private Long minRechargeAmount;

    /**
     * 单次最高奖励（分）
     */
    private Long maxRewardAmount;

    /**
     * 总奖励次数限制
     */
    private Integer totalLimit;

    /**
     * 每日奖励次数限制
     */
    private Integer dailyLimit;

    /**
     * 生效开始时间
     */
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 奖励说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 辅助方法 ====================

    /**
     * 是否有效
     */
    public boolean isEffective() {
        if (!"active".equals(status)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    /**
     * 计算邀请人奖励
     */
    public long calculateInviterReward(Long baseAmount) {
        if ("fixed".equals(inviterRewardType)) {
            return inviterRewardValue != null ? inviterRewardValue : 0L;
        } else if ("percentage".equals(inviterRewardType)) {
            // percentage: 值是百分比*100，如10%存为1000
            if (baseAmount == null || baseAmount <= 0) return 0L;
            long reward = baseAmount * inviterRewardValue / 10000;
            if (maxRewardAmount != null && reward > maxRewardAmount) {
                reward = maxRewardAmount;
            }
            return reward;
        }
        return 0L;
    }

    /**
     * 计算被邀请人奖励
     */
    public long calculateInviteeReward(Long baseAmount) {
        if ("fixed".equals(inviteeRewardType)) {
            return inviteeRewardValue != null ? inviteeRewardValue : 0L;
        } else if ("percentage".equals(inviteeRewardType)) {
            if (baseAmount == null || baseAmount <= 0) return 0L;
            long reward = baseAmount * inviteeRewardValue / 10000;
            if (maxRewardAmount != null && reward > maxRewardAmount) {
                reward = maxRewardAmount;
            }
            return reward;
        }
        return 0L;
    }
}
