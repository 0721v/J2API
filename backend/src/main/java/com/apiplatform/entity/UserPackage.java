package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户套餐实体（用户购买的套餐实例）
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_packages")
public class UserPackage {

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 套餐ID */
    private Long packageId;

    /** 剩余次数（计次套餐） */
    @Builder.Default
    private Integer remainingCalls = 0;

    /** 今日已用次数 */
    @Builder.Default
    private Integer todayUsedCalls = 0;

    /** 今日使用日期（用于重置计数） */
    private LocalDateTime todayDate;

    /** 额度余额（单位：分） */
    @Builder.Default
    private Long quotaBalance = 0L;

    /** 生效时间 */
    private LocalDateTime startTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 自动续期状态 */
    @Builder.Default
    private Boolean autoRenew = false;

    /** 续期状态：active/expired/cancelled */
    @Builder.Default
    private String status = "active";

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
     * 检查是否过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        return "active".equals(status) && !isExpired();
    }

    /**
     * 检查是否还有剩余次数
     */
    public boolean hasRemainingCalls() {
        if (remainingCalls == null || remainingCalls < 0) {
            return true; // 无限制
        }
        return remainingCalls > 0;
    }

    /**
     * 检查今日是否还有次数
     */
    public boolean hasTodayCalls() {
        if (dailyLimit == null || dailyLimit <= 0) {
            return true; // 无限制
        }
        return todayUsedCalls < dailyLimit;
    }

    /** 每日限制次数（从套餐配置中获取） */
    @TableField(exist = false)
    private Integer dailyLimit;

    /**
     * 消耗一次调用
     */
    public void consumeCall() {
        if (remainingCalls != null && remainingCalls > 0) {
            remainingCalls--;
        }
        todayUsedCalls++;
        todayDate = LocalDateTime.now();
    }

    /**
     * 消耗额度
     */
    public void consumeQuota(Long amount) {
        if (quotaBalance != null && quotaBalance > 0) {
            quotaBalance = Math.max(0, quotaBalance - amount);
        }
    }
}
