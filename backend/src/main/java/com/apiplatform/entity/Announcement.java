package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统公告实体
 */
@Data
@Accessors(chain = true)
@TableName("announcements")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 公告类型: info/notice/warning/important */
    private String type;

    /** 优先级，越大越靠前 */
    private Integer priority;

    /** 状态: draft/published/archived */
    private String status;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 过期时间，为空表示永不过期 */
    private LocalDateTime expiredAt;

    /** 发布对象: all/all_users/all_agents/specific_users */
    private String targetType;

    /** 特定用户ID列表，JSON格式 */
    private String targetUsers;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建人ID */
    private Long createdBy;

    /** 是否已读（非持久化字段） */
    @TableField(exist = false)
    private boolean read;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 获取类型标签
     */
    public String getTypeLabel() {
        return switch (type) {
            case "important" -> "重要";
            case "warning" -> "警告";
            case "notice" -> "通知";
            default -> "信息";
        };
    }

    /**
     * 获取状态标签
     */
    public String getStatusLabel() {
        return switch (status) {
            case "published" -> "已发布";
            case "archived" -> "已归档";
            default -> "草稿";
        };
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }
}
