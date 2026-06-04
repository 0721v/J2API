package com.apiplatform.controller;

import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Message;
import com.apiplatform.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "站内消息", description = "站内消息相关接口")
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取消息列表
     */
    @GetMapping
    @Operation(summary = "获取消息列表")
    public Result<PageResult<Message>> getList(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
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
    @Operation(summary = "获取未读数量")
    public Result<Integer> getUnreadCount(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(messageService.getUnreadCount(userId));
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
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
    @Operation(summary = "标记全部已读")
    public Result<Void> markAllAsRead(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
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
    @Operation(summary = "删除消息")
    public Result<Void> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
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
    @Operation(summary = "管理端-发送消息")
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
