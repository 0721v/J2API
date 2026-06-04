package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Message;
import com.apiplatform.mapper.MessageMapper;
import com.apiplatform.service.MessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 站内消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<Message> getList(Long userId, String type, int page, int pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsDeleted, false)
                .and(w -> w.isNull(Message::getExpiresAt)
                        .or()
                        .gt(Message::getExpiresAt, LocalDateTime.now()));

        if (type != null && !type.isEmpty()) {
            wrapper.eq(Message::getType, type);
        }

        wrapper.orderByDesc(Message::getCreatedAt);

        IPage<Message> result = messageMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        int affected = messageMapper.markAsRead(messageId, userId);
        if (affected == 0) {
            throw BizException.notFound("消息不存在或无权操作");
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        messageMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void delete(Long messageId, Long userId) {
        Message message = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getId, messageId)
                .eq(Message::getUserId, userId));

        if (message == null) {
            throw BizException.notFound("消息不存在");
        }

        message.setIsDeleted(true);
        messageMapper.updateById(message);
    }

    @Override
    public Message send(Long userId, String type, String title, String content, String priority) {
        return send(userId, type, title, content, priority, null);
    }

    @Override
    @Transactional
    public Message send(Long userId, String type, String title, String content, String priority, Map<String, Object> data) {
        Message message = new Message();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setPriority(priority != null ? priority : "normal");
        message.setIsRead(false);
        message.setIsDeleted(false);

        if (data != null) {
            try {
                message.setData(objectMapper.writeValueAsString(data));
            } catch (Exception e) {
                log.error("序列化消息数据失败", e);
            }
        }

        messageMapper.insert(message);
        return message;
    }

    @Override
    @Transactional
    public void sendBatch(Long[] userIds, String type, String title, String content, String priority) {
        for (Long userId : userIds) {
            send(userId, type, title, content, priority);
        }
    }
}
