package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API令牌实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tokens")
public class Token {

    /** 令牌ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 令牌名称 */
    private String name;

    /** API Key */
    private String apiKey;

    /** API Secret（加密存储） */
    private String apiSecret;

    /** 令牌分组ID */
    private Long groupId;

    /** 允许访问的模型列表（逗号分隔，NULL表示全部） */
    private String allowedModels;

    /** 允许访问的渠道列表（逗号分隔，NULL表示全部） */
    private String allowedChannels;

    /** 每分钟请求限制（0表示不限制） */
    @Builder.Default
    private Integer minuteLimit = 0;

    /** 每日请求限制（0表示不限制） */
    @Builder.Default
    private Integer dayLimit = 0;

    /** 已用额度（单位：分） */
    @Builder.Default
    private Long usedQuota = 0L;

    /** 剩余额度（单位：分，-1表示无限制） */
    @Builder.Default
    private Long remainingQuota = -1L;

    /** 额度上限（单位：分，0表示无限制） */
    @Builder.Default
    private Long quotaLimit = 0L;

    /** 过期时间（NULL表示永不过期） */
    private LocalDateTime expiresAt;

    /** 续期天数 */
    @Builder.Default
    private Integer renewDays = 30;

    /** 状态：active/disabled/expired */
    @Builder.Default
    private String status = "active";

    /** 备注 */
    private String remark;

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
     * 检查令牌是否过期
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 检查令牌是否有效
     */
    public boolean isValid() {
        return "active".equals(status) && !isExpired();
    }

    /**
     * 检查是否有额度限制
     */
    public boolean hasQuotaLimit() {
        return remainingQuota != null && remainingQuota >= 0;
    }

    /**
     * 检查是否有可用额度
     */
    public boolean hasAvailableQuota() {
        if (remainingQuota == null || remainingQuota < 0) {
            return true; // 无限制
        }
        return remainingQuota > 0;
    }
}
