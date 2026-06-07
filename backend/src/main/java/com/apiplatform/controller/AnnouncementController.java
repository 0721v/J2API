package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Announcement;
import com.apiplatform.service.AnnouncementService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ==================== 用户端接口 ====================

    /**
     * 获取公告列表
     */
    @GetMapping
    public Result<PageResult<Announcement>> getList(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (userId == null) {
            userId = 0L; // 未登录用户
        }
        return Result.success(announcementService.getPublishedList(userId, page, pageSize));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getDetail(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        Announcement announcement = announcementService.getById(id);
        if (userId != null) {
            announcementService.markAsRead(id, userId);
        }
        return Result.success(announcement);
    }

    /**
     * 获取未读公告数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.success(0);
        }
        return Result.success(announcementService.getUnreadCount(userId));
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id,
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        announcementService.markAsRead(id, userId);
        return Result.success("已标记已读", null);
    }

    // ==================== 管理端接口 ====================

    /**
     * 管理端：获取公告列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Announcement>> getAdminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(announcementService.getAdminList(page, pageSize));
    }

    /**
     * 管理端：获取历史公告
     */
    @GetMapping("/admin/history")
    public Result<PageResult<Announcement>> getHistoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(announcementService.getHistoryList(page, pageSize));
    }

    /**
     * 管理端：创建公告
     */
    @PostMapping("/admin")
    public Result<Announcement> create(
            @Validated @RequestBody AnnouncementRequest request,
            @RequestAttribute(value = "userId") Long adminId) {
        if (adminId == null) {
            return Result.unauthorized("请先登录");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        announcement.setTargetType(request.getTargetType() != null ? request.getTargetType() : "all");

        return Result.success(announcementService.create(announcement, adminId));
    }

    /**
     * 管理端：更新公告
     */
    @PutMapping("/admin/{id}")
    public Result<Announcement> update(
            @PathVariable Long id,
            @Validated @RequestBody AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setPriority(request.getPriority());
        announcement.setTargetType(request.getTargetType());

        return Result.success(announcementService.update(announcement));
    }

    /**
     * 管理端：删除公告
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 管理端：发布公告
     */
    @PostMapping("/admin/{id}/publish")
    public Result<Announcement> publish(@PathVariable Long id) {
        return Result.success(announcementService.publish(id));
    }

    /**
     * 管理端：归档公告
     */
    @PostMapping("/admin/{id}/archive")
    public Result<Announcement> archive(@PathVariable Long id) {
        return Result.success(announcementService.archive(id));
    }

    // ==================== 请求DTO ====================

    @Data
    public static class AnnouncementRequest {
        private String title;
        private String content;
        private String type;
        private Integer priority;
        private String targetType;
    }
}
