package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 令牌分组实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("token_groups")
public class TokenGroup {

    /** 分组ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 分组名称 */
    private String name;

    /** 分组描述 */
    private String description;

    /** 优先级（数字越大优先级越高） */
    @Builder.Default
    private Integer priority = 0;

    /** 默认模型列表 */
    private String defaultModels;

    /** 默认渠道列表 */
    private String defaultChannels;

    /** 每分钟请求限制（0表示不限制） */
    @Builder.Default
    private Integer minuteLimit = 0;

    /** 每日请求限制（0表示不限制） */
    @Builder.Default
    private Integer dayLimit = 0;

    /** 状态：active/disabled */
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
}
