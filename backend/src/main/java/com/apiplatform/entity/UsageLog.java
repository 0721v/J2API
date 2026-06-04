package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API使用日志实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("usage_logs")
public class UsageLog {

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 请求ID（用于关联响应） */
    private String requestId;

    /** 用户ID */
    private Long userId;

    /** 令牌ID */
    private Long tokenId;

    /** 令牌分组ID */
    private Long groupId;

    /** 模型ID */
    private Long modelId;

    /** 模型名称 */
    private String modelName;

    /** 渠道ID */
    private Long channelId;

    /** 渠道名称 */
    private String channelName;

    /** API类型：chat/completion/embedding/rerank/tts */
    private String apiType;

    /** 请求Token数（输入） */
    @Builder.Default
    private Integer requestTokens = 0;

    /** 响应Token数（输出） */
    @Builder.Default
    private Integer responseTokens = 0;

    /** 是否缓存命中 */
    @Builder.Default
    private Boolean cacheHit = false;

    /** 计费Token数（考虑缓存） */
    @Builder.Default
    private Integer billedTokens = 0;

    /** 计费金额（单位：分） */
    @Builder.Default
    private Long billedAmount = 0L;

    /** 视频/音频时长（秒）- 用于按秒计费 */
    private Double duration;

    /** 阶梯计费累计用量 - 用于阶梯计费 */
    private Long tieredUsage;

    /** 响应时间（毫秒） */
    @Builder.Default
    private Long responseTime = 0L;

    /** HTTP状态码 */
    private Integer httpStatus;

    /** 请求状态：success/error/timeout */
    @Builder.Default
    private String status = "success";

    /** 错误消息 */
    private String errorMessage;

    /** 错误码 */
    private String errorCode;

    /** IP地址 */
    private String ipAddress;

    /** 用户代理 */
    private String userAgent;

    /** 请求路径 */
    private String requestPath;

    /** 请求方法 */
    private String requestMethod;

    /** 请求体摘要（MD5） */
    private String requestHash;

    /** 响应体摘要（MD5） */
    private String responseHash;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

    /**
     * 检查是否计费
     */
    public boolean shouldBill() {
        return isSuccess() && billedAmount > 0;
    }

    /**
     * 获取总Token数
     */
    public Integer getTotalTokens() {
        return requestTokens + responseTokens;
    }
}
