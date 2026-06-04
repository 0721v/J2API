package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("transactions")
public class Transaction {

    /** 交易ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 交易号 */
    private String transactionNo;

    /** 用户ID */
    private Long userId;

    /** 交易类型：recharge/consume/refund/bonus/adjust */
    private String type;

    /** 变动金额（正数增加，负数减少，单位：分） */
    private Long amount;

    /** 变动前余额（单位：分） */
    private Long balanceBefore;

    /** 变动后余额（单位：分） */
    private Long balanceAfter;

    /** 关联订单ID */
    private Long orderId;

    /** 关联令牌ID */
    private Long tokenId;

    /** 关联模型ID */
    private Long modelId;

    /** 关联渠道ID */
    private Long channelId;

    /** 交易描述 */
    private String description;

    /** 交易详情（JSON） */
    private String details;

    /** 状态：completed/cancelled/failed */
    @Builder.Default
    private String status = "completed";

    /** 操作者ID（管理员操作时） */
    private Long operatorId;

    /** 操作者名称 */
    private String operatorName;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    /**
     * 检查是否为收入
     */
    public boolean isIncome() {
        return amount != null && amount > 0;
    }

    /**
     * 检查是否为支出
     */
    public boolean isExpense() {
        return amount != null && amount < 0;
    }

    /**
     * 获取绝对金额
     */
    public Long getAbsoluteAmount() {
        return amount != null ? Math.abs(amount) : 0L;
    }
}
