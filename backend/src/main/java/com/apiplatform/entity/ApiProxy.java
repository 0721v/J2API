package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API代理配置实体
 * 支持自定义API端点转发和配置
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_proxies")
public class ApiProxy {

    /** 代理ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 代理名称 */
    private String name;

    /** 代理类型：custom_endpoint/proxy_forward/path_rewrite
     custom_endpoint - 自定义端点
     proxy_forward - 代理转发
     path_rewrite - 路径重写
     */
    private String type;

    /** 目标URL */
    private String targetUrl;

    /** 源路径（匹配规则） */
    private String sourcePath;

    /** 目标路径（重写规则） */
    private String targetPath;

    /** 请求方法：GET/POST/PUT/DELETE/PATCH/* */
    @Builder.Default
    private String methods = "*";

    /** 请求头转换规则（JSON格式） */
    private String headerTransforms;

    /** 请求体转换规则（JSON格式） */
    private String requestTransforms;

    /** 响应转换规则（JSON格式） */
    private String responseTransforms;

    /** 关联的模型ID列表 */
    private String modelIds;

    /** 关联的渠道ID列表 */
    private String channelIds;

    /** 允许的分组ID列表（空表示全部） */
    private String allowedGroups;

    /** 优先级（数字越大优先级越高） */
    @Builder.Default
    private Integer priority = 0;

    /** 超时时间（毫秒） */
    @Builder.Default
    private Integer timeout = 30000;

    /** 重试次数 */
    @Builder.Default
    private Integer retryCount = 0;

    /** 是否启用请求日志 */
    @Builder.Default
    private Boolean logRequests = true;

    /** 是否启用响应日志 */
    @Builder.Default
    private Boolean logResponses = false;

    /** 是否缓存响应 */
    @Builder.Default
    private Boolean cacheEnabled = false;

    /** 缓存TTL（秒） */
    @Builder.Default
    private Integer cacheTtl = 3600;

    /** API密钥要求：required/optional/none */
    @Builder.Default
    private String authRequired = "required";

    /** 限流配置 */
    private String rateLimit;

    /** 是否启用 */
    @Builder.Default
    private Boolean enabled = true;

    /** 描述 */
    private String description;

    /** 请求计数（仅展示） */
    @Builder.Default
    private Long requestCount = 0L;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;

    /** 逻辑删除 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    // ==================== 辅助方法 ====================

    /**
     * 检查请求方法是否匹配
     */
    public boolean matchesMethod(String method) {
        if ("*".equals(methods)) {
            return true;
        }
        String[] allowedMethods = methods.split(",");
        for (String m : allowedMethods) {
            if (m.trim().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查路径是否匹配
     */
    public boolean matchesPath(String path) {
        if (sourcePath == null || sourcePath.isEmpty()) {
            return false;
        }

        // 支持通配符匹配
        if (sourcePath.contains("*")) {
            String regex = sourcePath.replace("*", ".*");
            return path.matches(regex);
        }

        // 支持路径前缀匹配
        if (sourcePath.endsWith("/")) {
            return path.startsWith(sourcePath);
        }

        return path.equals(sourcePath) || path.startsWith(sourcePath + "/");
    }

    /**
     * 获取目标URL（应用路径重写）
     */
    public String getRewrittenTargetUrl(String requestPath) {
        String target = targetUrl;
        if (targetPath != null && !targetPath.isEmpty()) {
            // 应用路径重写
            String rewrittenPath = requestPath;
            if (sourcePath != null && requestPath.startsWith(sourcePath)) {
                rewrittenPath = requestPath.replaceFirst(sourcePath, targetPath);
            }
            target = targetUrl + rewrittenPath;
        }
        return target;
    }

    /**
     * 是否需要认证
     */
    public boolean isAuthRequired() {
        return !"none".equals(authRequired);
    }

    /**
     * 获取代理类型描述
     */
    public String getTypeName() {
        return switch (type) {
            case "custom_endpoint" -> "自定义端点";
            case "proxy_forward" -> "代理转发";
            case "path_rewrite" -> "路径重写";
            default -> type;
        };
    }
}
