package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理分成配置实体
 */
@Data
@TableName("agent_commission_rules")
public class AgentCommissionRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 类型: recharge/package/upgrade
     */
    private String type;

    /**
     * 条件类型: all/amount/user_count
     */
    private String conditionType;

    /**
     * 条件值JSON
     */
    private String conditionValue;

    /**
     * 佣金类型: rate/fixed
     */
    private String commissionType;

    /**
     * 佣金值（比例或固定金额）
     */
    private BigDecimal commissionValue;

    /**
     * 最低订单金额（分）
     */
    private Long minAmount;

    /**
     * 最高订单金额（分）
     */
    private Long maxAmount;

    /**
     * 代理商等级（NULL表示全部等级）
     */
    private Long levelId;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 状态
     */
    private String status;

    /**
     * 生效开始时间
     */
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 规则说明
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

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;

    // ==================== 关联对象 ====================
    @TableField(exist = false)
    private AgentLevel level;

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
     * 计算佣金金额（分）
     */
    public Long calculateCommission(Long orderAmount) {
        if (orderAmount == null || orderAmount <= 0) {
            return 0L;
        }
        if (minAmount != null && orderAmount < minAmount) {
            return 0L;
        }
        if (maxAmount != null && orderAmount > maxAmount) {
            orderAmount = maxAmount;
        }
        if ("rate".equals(commissionType)) {
            return new BigDecimal(orderAmount)
                    .multiply(commissionValue)
                    .setScale(0, java.math.RoundingMode.DOWN)
                    .longValue();
        } else {
            return commissionValue.multiply(new BigDecimal(100)).longValue();
        }
    }

    /**
     * 获取佣金类型文本
     */
    public String getTypeText() {
        return switch (type) {
            case "recharge" -> "充值";
            case "package" -> "套餐购买";
            case "upgrade" -> "套餐升级";
            default -> type;
        };
    }
}
