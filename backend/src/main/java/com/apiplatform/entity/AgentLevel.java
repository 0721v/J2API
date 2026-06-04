package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理商等级实体
 */
@Data
@TableName("agent_levels")
public class AgentLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String name;

    /**
     * 等级代码
     */
    private String code;

    /**
     * 等级数值
     */
    private Integer level;

    /**
     * 最少下级用户数
     */
    private Integer minUsers;

    /**
     * 最少累计充值金额（分）
     */
    private Long minRecharge;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 推荐奖励比例
     */
    private BigDecimal bonusRate;

    /**
     * 自己购买折扣
     */
    private BigDecimal priceDiscount;

    /**
     * 是否允许发展下级代理
     */
    private Boolean subAgentEnabled;

    /**
     * 下级代理佣金比例
     */
    private BigDecimal subAgentRate;

    /**
     * 等级说明
     */
    private String description;

    /**
     * 等级权益JSON
     */
    private String benefits;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}
