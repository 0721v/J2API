package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户分组实体
 * 支持对用户进行分组，设置不同的权限、配额和价格倍率
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_groups")
public class UserGroup {

    /** 分组ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组名称 */
    private String name;

    /** 分组描述 */
    private String description;

    /** 优先级（数字越大优先级越高） */
    @Builder.Default
    private Integer priority = 0;

    /** 状态：enabled/disabled */
    @Builder.Default
    private String status = "enabled";

    // ==================== 配额设置 ====================
    /** 默认每分钟请求限制（0表示继承系统设置） */
    @Builder.Default
    private Integer defaultMinuteLimit = 0;

    /** 默认每日请求限制（0表示继承系统设置） */
    @Builder.Default
    private Integer defaultDailyLimit = 0;

    /** 默认每月请求限制（0表示无限制） */
    @Builder.Default
    private Integer defaultMonthlyLimit = 0;

    // ==================== 价格倍率 ====================
    /** 全局价格倍率（默认1.0，1.2表示加价20%） */
    @Builder.Default
    private BigDecimal globalRate = BigDecimal.ONE;

    /** 模型价格倍率配置（JSON格式：{"gpt-4": 1.2, "claude-3": 1.5}） */
    private String modelRates;

    /** 渠道价格倍率配置（JSON格式：{"openai": 1.0, "azure": 0.8}） */
    private String channelRates;

    // ==================== 功能权限 ====================
    /** 允许使用的模型列表（JSON数组，NULL表示全部） */
    private String allowedModels;

    /** 禁止使用的模型列表（JSON数组） */
    private String blockedModels;

    /** 允许使用的渠道列表（JSON数组，NULL表示全部） */
    private String allowedChannels;

    /** 是否允许使用充值功能 */
    @Builder.Default
    private Boolean allowRecharge = true;

    /** 是否允许使用套餐功能 */
    @Builder.Default
    private Boolean allowPackages = true;

    /** 是否允许查看用量统计 */
    @Builder.Default
    private Boolean allowUsageStats = true;

    // ==================== 注册设置 ====================
    /** 会员等级：free/basic/pro/enterprise */
    @Builder.Default
    private String level = "free";

    /** 有效天数（0表示永久） */
    @Builder.Default
    private Integer validDays = 0;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 用户数量（仅用于展示） */
    @Builder.Default
    private Integer userCount = 0;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    // ==================== 辅助方法 ====================

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "enabled".equals(status);
    }

    /**
     * 检查分组是否过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取指定模型的价格倍率
     */
    public BigDecimal getModelRate(String modelName) {
        if (modelRates == null || modelRates.isEmpty()) {
            return globalRate;
        }
        try {
            com.alibaba.fastjson2.JSONObject rates = com.alibaba.fastjson2.JSON.parseObject(modelRates);
            if (rates.containsKey(modelName)) {
                return rates.getBigDecimal(modelName);
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return globalRate;
    }

    /**
     * 获取指定渠道的价格倍率
     */
    public BigDecimal getChannelRate(String channelName) {
        if (channelRates == null || channelRates.isEmpty()) {
            return BigDecimal.ONE;
        }
        try {
            com.alibaba.fastjson2.JSONObject rates = com.alibaba.fastjson2.JSON.parseObject(channelRates);
            if (rates.containsKey(channelName)) {
                return rates.getBigDecimal(channelName);
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return BigDecimal.ONE;
    }

    /**
     * 计算最终价格（基础价格 × 模型倍率 × 渠道倍率）
     */
    public BigDecimal calculatePrice(BigDecimal basePrice, String modelName, String channelName) {
        BigDecimal modelRate = getModelRate(modelName);
        BigDecimal channelRate = getChannelRate(channelName);
        return basePrice.multiply(modelRate).multiply(channelRate);
    }

    /**
     * 检查是否允许使用指定模型
     */
    public boolean isModelAllowed(String modelName) {
        // 如果禁止列表包含该模型，则不允许
        if (blockedModels != null && !blockedModels.isEmpty()) {
            try {
                java.util.List<String> blocked = com.alibaba.fastjson2.JSON.parseArray(blockedModels, String.class);
                if (blocked.contains(modelName)) {
                    return false;
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        // 如果允许列表不为空，则必须在列表中
        if (allowedModels != null && !allowedModels.isEmpty()) {
            try {
                java.util.List<String> allowed = com.alibaba.fastjson2.JSON.parseArray(allowedModels, String.class);
                return allowed.contains(modelName);
            } catch (Exception e) {
                return true;
            }
        }

        return true;
    }

    /**
     * 获取会员等级名称
     */
    public String getLevelName() {
        return switch (level) {
            case "free" -> "免费用户";
            case "basic" -> "基础用户";
            case "pro" -> "专业用户";
            case "enterprise" -> "企业用户";
            default -> level;
        };
    }
}
