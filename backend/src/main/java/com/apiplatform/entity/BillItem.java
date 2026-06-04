package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单明细实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bill_items")
public class BillItem {

    /** 明细ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账单ID */
    private Long billId;

    /** 用户ID */
    private Long userId;

    /** 令牌ID */
    private Long tokenId;

    /** 模型ID */
    private Long modelId;

    /** 渠道ID */
    private Long channelId;

    /** 调用次数 */
    @Builder.Default
    private Long calls = 0L;

    /** 输入Token数 */
    @Builder.Default
    private Long inputTokens = 0L;

    /** 输出Token数 */
    @Builder.Default
    private Long outputTokens = 0L;

    /** 消费金额（单位：分） */
    @Builder.Default
    private Long amount = 0L;

    /** 是否缓存命中 */
    @Builder.Default
    private Boolean cacheHit = false;

    /** 缓存计费比例 */
    @Builder.Default
    private BigDecimal cacheRate = BigDecimal.ZERO;

    /** 缓存计费金额（单位：分） */
    @Builder.Default
    private Long cacheAmount = 0L;

    /** 调用日期 */
    private LocalDateTime callDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;
}
