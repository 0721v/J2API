package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 渠道配置实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("channels")
public class Channel {

    /** 渠道ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道名称 */
    private String name;

    /** 渠道类型：openai/azure/claude/cohere/jina/custom */
    private String type;

    /** 渠道子类型：chat/completion/embedding/rerank/realtime */
    @Builder.Default
    private String subType = "chat";

    /** API端点URL */
    private String endpoint;

    /** API密钥 */
    private String apiKey;

    /** 认证类型：bearer/api-key/basic/custom */
    @Builder.Default
    private String authType = "bearer";

    /** 自定义请求头（JSON格式） */
    private String customHeaders;

    /** 请求超时（毫秒） */
    @Builder.Default
    private Integer timeout = 60000;

    /** 模型映射（JSON格式） */
    private String modelMapping;

    /** 默认模型 */
    private String defaultModel;

    /** 权重（用于加权随机） */
    @Builder.Default
    private Integer weight = 100;

    /** 成本价格（每千token/每请求，单位：分） */
    @Builder.Default
    private BigDecimal costPerThousand = BigDecimal.ZERO;

    /** 渠道状态：active/disabled/maintenance/error */
    @Builder.Default
    private String status = "active";

    /** 优先级（数字越大优先级越高） */
    @Builder.Default
    private Integer priority = 0;

    /** 失败次数 */
    @Builder.Default
    private Integer failureCount = 0;

    /** 最后失败时间 */
    private LocalDateTime lastFailureTime;

    /** 自动重试次数 */
    @Builder.Default
    private Integer maxRetries = 3;

    /** 备用渠道ID（故障转移用） */
    private Long backupChannelId;

    /** 区域/机房 */
    private String region;

    /** 描述/备注 */
    private String description;

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
     * 检查渠道是否可用
     */
    public boolean isAvailable() {
        return "active".equals(status) && failureCount < maxRetries;
    }

    /**
     * 检查是否为官方渠道
     */
    public boolean isOfficial() {
        return "openai".equals(type) || "azure".equals(type) || "claude".equals(type);
    }

    /**
     * 增加失败次数
     */
    public void incrementFailure() {
        this.failureCount++;
        this.lastFailureTime = LocalDateTime.now();
    }

    /**
     * 重置失败次数
     */
    public void resetFailure() {
        this.failureCount = 0;
        this.lastFailureTime = null;
    }
}
