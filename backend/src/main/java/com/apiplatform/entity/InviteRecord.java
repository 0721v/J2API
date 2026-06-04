package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请记录实体
 */
@Data
@TableName("invite_records")
public class InviteRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 使用的邀请码
     */
    private String inviteCode;

    /**
     * 来源: link/qrcode/manual
     */
    private String source;

    /**
     * 状态: active/cancelled
     */
    private String status;

    /**
     * 奖励是否已发放
     */
    private Boolean rewardsCredited;

    /**
     * 奖励金额（分）
     */
    private Long rewardAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 关联对象 ====================
    @TableField(exist = false)
    private User inviter;

    @TableField(exist = false)
    private User invitee;

    // ==================== 辅助方法 ====================

    /**
     * 是否有效
     */
    public boolean isActive() {
        return "active".equals(status);
    }
}
