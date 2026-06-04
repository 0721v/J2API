package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Message;

import java.util.Map;

/**
 * 站内消息服务接口
 */
public interface MessageService {

    /**
     * 获取消息列表（分页）
     */
    PageResult<Message> getList(Long userId, String type, int page, int pageSize);

    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void delete(Long messageId, Long userId);

    /**
     * 发送消息给用户
     */
    Message send(Long userId, String type, String title, String content, String priority);

    /**
     * 发送消息给用户（带数据）
     */
    Message send(Long userId, String type, String title, String content, String priority, Map<String, Object> data);

    /**
     * 批量发送消息
     */
    void sendBatch(Long[] userIds, String type, String title, String content, String priority);
}
