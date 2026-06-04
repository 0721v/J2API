package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 邀请奖励记录实体
 */
@Data
@TableName("invite_reward_records")
public class InviteRewardRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请记录ID
     */
    private Long inviteRecordId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 奖励类型: register/recharge/consumption
     */
    private String rewardType;

    /**
     * 奖励对象: inviter/invitee
     */
    private String rewardTo;

    /**
     * 奖励金额（分）
     */
    private Long amount;

    /**
     * 变动前余额
     */
    private Long balanceBefore;

    /**
     * 变动后余额
     */
    private Long balanceAfter;

    /**
     * 奖励描述
     */
    private String description;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 辅助方法 ====================

    /**
     * 获取金额（元）
     */
    public BigDecimal getAmountYuan() {
        return amount != null ? 
            new BigDecimal(amount).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }
}
