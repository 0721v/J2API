package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 公告阅读记录实体
 */
@Data
@Accessors(chain = true)
@TableName("announcement_reads")
public class AnnouncementRead {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告ID */
    private Long announcementId;

    /** 用户ID */
    private Long userId;

    /** 阅读时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime readAt;
}
