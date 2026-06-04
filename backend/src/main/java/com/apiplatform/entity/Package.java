package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("packages")
public class Package {

    /** 套餐ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐名称 */
    private String name;

    /** 套餐类型：count/month/永久 */
    private String type;

    /** 套餐描述 */
    private String description;

    /** 包含次数（计次套餐） */
    @Builder.Default
    private Integer totalCalls = 0;

    /** 有效期天数（包月套餐） */
    @Builder.Default
    private Integer validDays = 30;

    /** 每日限制次数（0表示无限制） */
    @Builder.Default
    private Integer dailyLimit = 0;

    /** 价格（单位：分） */
    private BigDecimal price;

    /** 原价（用于显示折扣） */
    private BigDecimal originalPrice;

    /** 折扣比例 */
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ONE;

    /** 包含的模型列表（逗号分隔，NULL表示全部） */
    private String includedModels;

    /** 排除的模型列表 */
    private String excludedModels;

    /** 额度（可用于支付API调用，单位：分） */
    @Builder.Default
    private Long quota = 0L;

    /** 是否允许续期 */
    @Builder.Default
    private Boolean allowRenewal = true;

    /** 续期价格（单位：分） */
    private BigDecimal renewalPrice;

    /** 续期天数 */
    @Builder.Default
    private Integer renewalDays = 30;

    /** 排序权重 */
    @Builder.Default
    private Integer sortOrder = 0;

    /** 是否推荐 */
    @Builder.Default
    private Boolean recommended = false;

    /** 是否热门 */
    @Builder.Default
    private Boolean hot = false;

    /** 是否显示在前台 */
    @Builder.Default
    private Boolean visible = true;

    /** 购买次数限制（0表示无限制） */
    @Builder.Default
    private Integer purchaseLimit = 0;

    /** 套餐配置（JSON） */
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

    /**
     * 是否为计次套餐
     */
    public boolean isCountPackage() {
        return "count".equals(type);
    }

    /**
     * 是否为包月套餐
     */
    public boolean isMonthPackage() {
        return "month".equals(type);
    }

    /**
     * 是否为永久套餐
     */
    public boolean isPermanent() {
        return "permanent".equals(type);
    }

    /**
     * 计算实际价格
     */
    public BigDecimal getActualPrice() {
        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0 && discountRate.compareTo(BigDecimal.ONE) < 0) {
            return price.multiply(discountRate);
        }
        return price;
    }
}
