package com.apiplatform.controller;

import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Message;
import com.apiplatform.service.MessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 站内消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取消息列表
     */
    @GetMapping
    public Result<PageResult<Message>> getList(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(messageService.getList(userId, type, page, pageSize));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(messageService.getUnreadCount(userId));
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        messageService.markAsRead(id, userId);
        return Result.success("已标记已读", null);
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        messageService.markAllAsRead(userId);
        return Result.success("全部已标记已读", null);
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        messageService.delete(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 管理端：发送消息
     */
    @PostMapping("/admin/send")
    public Result<Void> send(
            @Validated @RequestBody SendMessageRequest request) {
        if (request.getUserId() != null) {
            messageService.send(request.getUserId(), request.getType(), request.getTitle(),
                    request.getContent(), request.getPriority(), request.getData());
        } else if (request.getUserIds() != null && request.getUserIds().length > 0) {
            messageService.sendBatch(request.getUserIds(), request.getType(), request.getTitle(),
                    request.getContent(), request.getPriority());
        }
        return Result.success("发送成功", null);
    }

    // ==================== 请求DTO ====================

    @Data
    public static class SendMessageRequest {
        private Long userId;
        private Long[] userIds;
        private String type;
        private String title;
        private String content;
        private String priority;
        private Map<String, Object> data;
    }
}
