package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型配置实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("models")
public class Model {

    /** 模型ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型标识（API调用时使用） */
    private String modelId;

    /** 显示名称 */
    private String name;

    /** 模型类型：chat/embedding/rerank/tts/speech/video/image */
    @Builder.Default
    private String type = "chat";

    /** 计费类型：token/per_request/per_second/tiered */
    @Builder.Default
    private String billingType = "token";

    /** 所属渠道类型：openai/azure/claude/cohere/jina/custom */
    private String channelType;

    /** 支持的渠道ID列表（逗号分隔，NULL表示全部） */
    private String allowedChannels;

    // ==================== 按Token计费 ====================
    /** 输入价格（每1K tokens，单位：元） */
    @Builder.Default
    private BigDecimal inputPrice = BigDecimal.ZERO;

    /** 输出价格（每1K tokens，单位：元） */
    @Builder.Default
    private BigDecimal outputPrice = BigDecimal.ZERO;

    // ==================== 按次计费 ====================
    /** 每次请求价格（单位：元） */
    @Builder.Default
    private BigDecimal perRequestPrice = BigDecimal.ZERO;

    // ==================== 按秒计费（视频/音频） ====================
    /** 每秒价格（单位：元） */
    @Builder.Default
    private BigDecimal perSecondPrice = BigDecimal.ZERO;

    /** 媒体类型：video/audio/image */
    @Builder.Default
    private String mediaType = "video";

    /** 最小计费时长（秒） */
    @Builder.Default
    private Double minBillableSeconds = 1.0;

    /** 最大计费时长（秒） */
    @Builder.Default
    private Double maxBillableSeconds = 300.0;

    // ==================== 阶梯计费 ====================
    /** 阶梯计费配置（格式：0-100000:0.10,100000-1000000:0.08,1000000:+:0.06） */
    private String tieredConfig;

    // ==================== 兼容旧字段 ====================
    /** 每次调用价格（单位：分，0表示按token计费）- 兼容旧版 */
    @Builder.Default
    private BigDecimal pricePerCall = BigDecimal.ZERO;

    /** 默认输入上下文长度 */
    @Builder.Default
    private Integer defaultContextLength = 4096;

    /** 最大输入长度 */
    @Builder.Default
    private Integer maxInputLength = 128000;

    /** 最大输出长度 */
    @Builder.Default
    private Integer maxOutputLength = 4096;

    /** 是否支持流式响应 */
    @Builder.Default
    private Boolean supportsStreaming = true;

    /** 是否支持函数调用 */
    @Builder.Default
    private Boolean supportsFunctionCall = false;

    /** 是否支持Vision/图片输入 */
    @Builder.Default
    private Boolean supportsVision = false;

    /** 是否启用 */
    @Builder.Default
    private Boolean enabled = true;

    /** 是否推荐 */
    @Builder.Default
    private Boolean recommended = false;

    /** 模型描述 */
    private String description;

    /** 模型标签（JSON数组，用于分组） */
    private String tags;

    /** 模型配置（JSON，存储特殊配置） */
    private String config;

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

    // ==================== 辅助方法 ====================

    /**
     * 获取主单价（用于展示）
     */
    public BigDecimal getPrimaryPrice() {
        return switch (billingType) {
            case "token" -> inputPrice.compareTo(BigDecimal.ZERO) > 0 ? inputPrice : outputPrice;
            case "per_request" -> perRequestPrice;
            case "per_second" -> perSecondPrice;
            case "tiered" -> BigDecimal.ZERO; // 阶梯计费需要根据配置计算
            default -> inputPrice.compareTo(BigDecimal.ZERO) > 0 ? inputPrice : outputPrice;
        };
    }

    /**
     * 获取计费类型描述
     */
    public String getBillingTypeName() {
        return switch (billingType) {
            case "token" -> "按Token计费";
            case "per_request" -> "按次计费";
            case "per_second" -> mediaType != null ? "按" + mediaType + "秒计费" : "按时长计费";
            case "tiered" -> "阶梯计费";
            default -> "按Token计费";
        };
    }

    /**
     * 获取计费单位
     */
    public String getBillingUnit() {
        return switch (billingType) {
            case "token" -> "元/1K tokens";
            case "per_request" -> "元/次";
            case "per_second" -> "元/秒";
            case "tiered" -> "阶梯";
            default -> "元/1K tokens";
        };
    }

    /**
     * 是否为免费模型
     */
    public boolean isFree() {
        return inputPrice.compareTo(BigDecimal.ZERO) == 0
                && outputPrice.compareTo(BigDecimal.ZERO) == 0
                && perRequestPrice.compareTo(BigDecimal.ZERO) == 0
                && perSecondPrice.compareTo(BigDecimal.ZERO) == 0
                && (tieredConfig == null || tieredConfig.isEmpty());
    }

    /**
     * 是否按Token计费
     */
    public boolean isTokenBilling() {
        return "token".equals(billingType) || (billingType == null && inputPrice.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * 是否按次计费
     */
    public boolean isPerRequestBilling() {
        return "per_request".equals(billingType);
    }

    /**
     * 是否按时长计费（视频/音频）
     */
    public boolean isPerSecondBilling() {
        return "per_second".equals(billingType);
    }

    /**
     * 是否阶梯计费
     */
    public boolean isTieredBilling() {
        return "tiered".equals(billingType);
    }

    /**
     * 获取价格描述
     */
    public String getPriceDescription() {
        if (isFree()) {
            return "免费";
        }

        switch (billingType) {
            case "token":
                if (inputPrice.compareTo(BigDecimal.ZERO) > 0 && outputPrice.compareTo(BigDecimal.ZERO) > 0) {
                    return String.format("输入 ¥%.4f/1K | 输出 ¥%.4f/1K",
                            inputPrice, outputPrice);
                } else if (inputPrice.compareTo(BigDecimal.ZERO) > 0) {
                    return String.format("¥%.4f/1K tokens", inputPrice);
                } else if (outputPrice.compareTo(BigDecimal.ZERO) > 0) {
                    return String.format("¥%.4f/1K tokens", outputPrice);
                }
                return "未定价";

            case "per_request":
                return String.format("¥%.4f/次", perRequestPrice);

            case "per_second":
                String unit = mediaType != null ? mediaType : "秒";
                return String.format("¥%.4f/%s", perSecondPrice, unit);

            case "tiered":
                return "阶梯计费（用量越多越便宜）";

            default:
                return String.format("¥%.4f/1K tokens", inputPrice);
        }
    }
}
