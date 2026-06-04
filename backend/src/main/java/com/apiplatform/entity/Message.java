package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 站内消息实体
 */
@Data
@Accessors(chain = true)
@TableName("messages")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 消息类型: system/recharge/usage/balance/agent/security */
    private String type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 附加数据，JSON格式 */
    private String data;

    /** 优先级: low/normal/high/urgent */
    private String priority;

    /** 是否已读 */
    private Boolean isRead;

    /** 阅读时间 */
    private LocalDateTime readAt;

    /** 是否删除 */
    private Boolean isDeleted;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 获取类型标签
     */
    public String getTypeLabel() {
        return switch (type) {
            case "recharge" -> "充值";
            case "usage" -> "用量";
            case "balance" -> "余额";
            case "agent" -> "代理";
            case "security" -> "安全";
            default -> "系统";
        };
    }

    /**
     * 获取类型图标
     */
    public String getTypeIcon() {
        return switch (type) {
            case "recharge" -> "Wallet";
            case "usage" -> "DataLine";
            case "balance" -> "Coin";
            case "agent" -> "User";
            case "security" -> "Lock";
            default -> "Bell";
        };
    }
}
