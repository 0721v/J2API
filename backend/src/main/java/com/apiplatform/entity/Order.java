package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order {

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单类型：recharge/package/giftcard */
    private String type;

    /** 关联ID（充值订单关联交易ID，套餐订单关联套餐ID） */
    private Long relatedId;

    /** 订单金额（单位：分） */
    private BigDecimal amount;

    /** 实际支付金额（单位：分） */
    private BigDecimal paidAmount;

    /** 支付方式：alipay/wechat/stripe/trc20 */
    private String paymentMethod;

    /** 支付状态：pending/paid/cancelled/refunded/expired */
    @Builder.Default
    private String status = "pending";

    /** 第三方支付订单号 */
    private String transactionId;

    /** 支付时间 */
    private LocalDateTime paidTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 退款金额（单位：分） */
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    /** 退款原因 */
    private String refundReason;

    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 客户端IP */
    private String clientIp;

    /** 订单备注 */
    private String remark;

    /** 扩展数据（JSON） */
    private String extraData;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否可支付
     */
    public boolean canPay() {
        return "pending".equals(status) && !isExpired();
    }

    /**
     * 检查是否可退款
     */
    public boolean canRefund() {
        return "paid".equals(status) && refundAmount.compareTo(amount) < 0;
    }

    /**
     * 获取退款比例
     */
    public BigDecimal getRefundRate() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return refundAmount.divide(amount, 4, BigDecimal.ROUND_HALF_UP);
    }
}
