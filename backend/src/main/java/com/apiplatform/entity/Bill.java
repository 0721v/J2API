package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bills")
public class Bill {

    /** 账单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账单号 */
    private String billNo;

    /** 用户ID */
    private Long userId;

    /** 账单周期：daily/monthly */
    @Builder.Default
    private String period = "daily";

    /** 周期开始日期 */
    private LocalDateTime periodStart;

    /** 周期结束日期 */
    private LocalDateTime periodEnd;

    /** 总调用次数 */
    @Builder.Default
    private Long totalCalls = 0L;

    /** 总消费金额（单位：分） */
    @Builder.Default
    private Long totalAmount = 0L;

    /** 输入Token总数 */
    @Builder.Default
    private Long totalInputTokens = 0L;

    /** 输出Token总数 */
    @Builder.Default
    private Long totalOutputTokens = 0L;

    /** 缓存命中次数 */
    @Builder.Default
    private Long cacheHitCalls = 0L;

    /** 缓存计费金额（单位：分） */
    @Builder.Default
    private Long cacheBillingAmount = 0L;

    /** 账单状态：pending/paid */
    @Builder.Default
    private String status = "pending";

    /** 支付时间 */
    private LocalDateTime paidTime;

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
     * 获取缓存命中率
     */
    public BigDecimal getCacheHitRate() {
        if (totalCalls == null || totalCalls == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cacheHitCalls)
                .divide(BigDecimal.valueOf(totalCalls), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查是否已支付
     */
    public boolean isPaid() {
        return "paid".equals(status);
    }
}
