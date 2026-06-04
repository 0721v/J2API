package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日用户统计实体（预聚合表）
 */
@Data
@Accessors(chain = true)
@TableName("daily_user_stats")
public class DailyUserStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 统计日期 */
    private LocalDate statDate;

    /** API调用次数 */
    private Integer totalApiCalls;

    /** 总输入Token数 */
    private Long totalInputTokens;

    /** 总输出Token数 */
    private Long totalOutputTokens;

    /** 总消费金额 */
    private BigDecimal totalCost;

    /** 充值金额 */
    private BigDecimal rechargeAmount;

    /** 奖励金额 */
    private BigDecimal rewardAmount;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 日终余额 */
    private BigDecimal balanceEnd;

    /** 活跃的API Key数量 */
    private Integer activeTokens;

    /** 各模型使用量 JSON */
    private String modelUsage;

    /** 各渠道使用量 JSON */
    private String channelUsage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
