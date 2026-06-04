package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理商提现记录实体
 */
@Data
@TableName("agent_withdrawals")
public class AgentWithdrawal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提现单号
     */
    private String withdrawalNo;

    /**
     * 代理商ID
     */
    private Long agentId;

    /**
     * 提现金额（分）
     */
    private Long amount;

    /**
     * 手续费（分）
     */
    private Long fee;

    /**
     * 实际到账金额（分）
     */
    private Long actualAmount;

    /**
     * 提现方式: bank/alipay/wechat
     */
    private String method;

    /**
     * 状态: pending/processing/completed/rejected
     */
    private String status;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 支付宝账号
     */
    private String alipayAccount;

    /**
     * 微信账户
     */
    private String wechatAccount;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 处理人ID
     */
    private Long processedBy;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 关联对象 ====================
    @TableField(exist = false)
    private Agent agent;

    // ==================== 辅助方法 ====================

    /**
     * 获取提现金额（元）
     */
    public BigDecimal getAmountYuan() {
        return amount != null ? 
            new BigDecimal(amount).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }

    /**
     * 获取手续费（元）
     */
    public BigDecimal getFeeYuan() {
        return fee != null ? 
            new BigDecimal(fee).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }

    /**
     * 获取实际到账金额（元）
     */
    public BigDecimal getActualAmountYuan() {
        return actualAmount != null ? 
            new BigDecimal(actualAmount).divide(new BigDecimal(100)) : 
            BigDecimal.ZERO;
    }

    /**
     * 获取提现方式文本
     */
    public String getMethodText() {
        return switch (method) {
            case "bank" -> "银行卡";
            case "alipay" -> "支付宝";
            case "wechat" -> "微信";
            default -> method;
        };
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return switch (status) {
            case "pending" -> "待处理";
            case "processing" -> "处理中";
            case "completed" -> "已完成";
            case "rejected" -> "已拒绝";
            default -> status;
        };
    }

    /**
     * 是否可以取消
     */
    public boolean canCancel() {
        return "pending".equals(status);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
}
