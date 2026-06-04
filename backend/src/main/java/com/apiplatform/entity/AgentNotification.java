package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理商消息通知实体
 */
@Data
@TableName("agent_notifications")
public class AgentNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 代理商ID
     */
    private Long agentId;

    /**
     * 用户ID（可选，用于关联用户通知）
     */
    private Long userId;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 相关金额（分）
     */
    private Long amount;

    /**
     * 相关订单ID
     */
    private Long orderId;

    /**
     * 关联数据JSON
     */
    private String data;

    /**
     * 状态: unread/read
     */
    private String status;

    /**
     * 优先级: low/normal/high/urgent
     */
    private String priority;

    /**
     * 是否已推送: 0/1
     */
    private Boolean pushed;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}
