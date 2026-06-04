package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理商实体
 */
@Data
@TableName("agents")
public class Agent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 代理商代码（邀请码）
     */
    private String agentCode;

    /**
     * 上级代理商ID
     */
    private Long parentId;

    /**
     * 等级ID
     */
    private Long levelId;

    /**
     * 状态: pending/active/suspended/rejected
     */
    private String status;

    /**
     * 下级用户数
     */
    private Integer totalUsers;

    /**
     * 累计充值金额（分）
     */
    private Long totalRecharge;

    /**
     * 累计获得佣金（分）
     */
    private Long totalCommission;

    /**
     * 可提现佣金（分）
     */
    private Long availableCommission;

    /**
     * 冻结佣金（分）
     */
    private Long frozenCommission;

    // 联系方式
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String idCard;
    private String businessLicense;

    // 收款信息
    private String bankName;
    private String bankAccount;
    private String bankBranch;
    private String alipayAccount;
    private String wechatAccount;

    // 审核信息
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;

    // ==================== 关联对象 ====================
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private AgentLevel level;

    @TableField(exist = false)
    private Agent parent;

    @TableField(exist = false)
    private Long subAgentCount;

    // ==================== 辅助方法 ====================

    /**
     * 获取佣金余额（分转为元）
     */
    public java.math.BigDecimal getAvailableCommissionYuan() {
        return availableCommission != null ? 
            new java.math.BigDecimal(availableCommission).divide(new java.math.BigDecimal(100)) : 
            java.math.BigDecimal.ZERO;
    }

    /**
     * 是否是有效代理
     */
    public boolean isActive() {
        return "active".equals(status);
    }

    /**
     * 是否可以发展下级代理
     */
    public boolean canSubAgent() {
        return level != null && Boolean.TRUE.equals(level.getSubAgentEnabled());
    }
}
