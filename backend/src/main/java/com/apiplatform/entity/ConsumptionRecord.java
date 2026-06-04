package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录实体
 */
@Data
@Accessors(chain = true)
@TableName("consumption_records")
public class ConsumptionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 消费类型: api_call/recharge/refund/reward/adjustment */
    private String type;

    /** 消费类别: chat/completion/embedding/video/audio */
    private String category;

    /** 消费/充值金额 */
    private BigDecimal amount;

    /** 变动前余额 */
    private BigDecimal balanceBefore;

    /** 变动后余额 */
    private BigDecimal balanceAfter;

    /** 描述 */
    private String description;

    /** 关联ID（如订单ID、用量日志ID） */
    private Long relatedId;

    /** 关联类型（如 order, usage_log） */
    private String relatedType;

    /** 附加元数据，JSON格式 */
    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 获取类型标签
     */
    public String getTypeLabel() {
        return switch (type) {
            case "api_call" -> "API调用";
            case "recharge" -> "充值";
            case "refund" -> "退款";
            case "reward" -> "奖励";
            case "adjustment" -> "调整";
            default -> "其他";
        };
    }

    /**
     * 获取类型图标
     */
    public String getTypeIcon() {
        return switch (type) {
            case "api_call" -> "Api";
            case "recharge" -> "Wallet";
            case "refund" -> "RefreshLeft";
            case "reward" -> "Gift";
            case "adjustment" -> "Edit";
            default -> "Document";
        };
    }

    /**
     * 是否为收入（充值、奖励、退款）
     */
    public boolean isIncome() {
        return "recharge".equals(type) || "reward".equals(type) || "refund".equals(type);
    }
}
