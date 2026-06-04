package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录实体
 */
@Data
@TableName("agent_commissions")
public class AgentCommission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 代理商ID
     */
    private Long agentId;

    /**
     * 消费用户ID
     */
    private Long userId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 类型: recharge/package/upgrade/bonus/withdraw/freeze
     */
    private String type;

    /**
     * 变动金额（分）
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
     * 佣金比例
     */
    private BigDecimal rate;

    /**
     * 原订单金额（分）
     */
    private Long orderAmount;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * 结算时间
     */
    private LocalDateTime settledAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 关联对象 ====================
    @TableField(exist = false)
    private Agent agent;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Order order;

    // ==================== 辅助方法 ====================

    /**
     * 获取金额（元）
     */
    public BigDecimal getAmountYuan() {
        return amount != null ? 
            new BigDecimal(amount).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }

    /**
     * 获取订单金额（元）
     */
    public BigDecimal getOrderAmountYuan() {
        return orderAmount != null ? 
            new BigDecimal(orderAmount).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }

    /**
     * 获取类型文本
     */
    public String getTypeText() {
        return switch (type) {
            case "recharge" -> "充值佣金";
            case "package" -> "套餐佣金";
            case "upgrade" -> "升级佣金";
            case "bonus" -> "推荐奖励";
            case "withdraw" -> "提现扣减";
            case "freeze" -> "冻结";
            default -> type;
        };
    }
}
