package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.UserGroup;
import com.apiplatform.service.UserGroupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户分组控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/admin/user-groups")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    /**
     * 获取分组列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserGroup>> getGroupList() {
        List<UserGroup> groups = userGroupService.getAllGroups();
        return Result.success(groups);
    }

    /**
     * 分页查询分组
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserGroup>> getGroupPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<UserGroup> result = userGroupService.getGroupPage(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 获取分组详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserGroup> getGroupDetail(@PathVariable Long id) {
        UserGroup group = userGroupService.getGroupById(id);
        if (group == null) {
            return Result.notFound("分组不存在");
        }
        return Result.success(group);
    }

    /**
     * 创建分组
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserGroup> createGroup(@Validated @RequestBody UserGroupRequest request) {
        try {
            UserGroup group = UserGroup.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .priority(request.getPriority() != null ? request.getPriority() : 0)
                    .defaultMinuteLimit(request.getDefaultMinuteLimit())
                    .defaultDailyLimit(request.getDefaultDailyLimit())
                    .defaultMonthlyLimit(request.getDefaultMonthlyLimit())
                    .globalRate(request.getGlobalRate())
                    .modelRates(request.getModelRates())
                    .channelRates(request.getChannelRates())
                    .allowedModels(request.getAllowedModels())
                    .blockedModels(request.getBlockedModels())
                    .allowedChannels(request.getAllowedChannels())
                    .allowRecharge(request.getAllowRecharge())
                    .allowPackages(request.getAllowPackages())
                    .allowUsageStats(request.getAllowUsageStats())
                    .level(request.getLevel())
                    .validDays(request.getValidDays())
                    .remark(request.getRemark())
                    .build();

            group = userGroupService.createGroup(group);
            return Result.success("分组创建成功", group);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新分组
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserGroup> updateGroup(@PathVariable Long id, @RequestBody UserGroupRequest request) {
        try {
            UserGroup group = UserGroup.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .priority(request.getPriority())
                    .status(request.getStatus())
                    .defaultMinuteLimit(request.getDefaultMinuteLimit())
                    .defaultDailyLimit(request.getDefaultDailyLimit())
                    .defaultMonthlyLimit(request.getDefaultMonthlyLimit())
                    .globalRate(request.getGlobalRate())
                    .modelRates(request.getModelRates())
                    .channelRates(request.getChannelRates())
                    .allowedModels(request.getAllowedModels())
                    .blockedModels(request.getBlockedModels())
                    .allowedChannels(request.getAllowedChannels())
                    .allowRecharge(request.getAllowRecharge())
                    .allowPackages(request.getAllowPackages())
                    .allowUsageStats(request.getAllowUsageStats())
                    .level(request.getLevel())
                    .validDays(request.getValidDays())
                    .remark(request.getRemark())
                    .build();

            group = userGroupService.updateGroup(id, group);
            return Result.success("分组更新成功", group);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除分组
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        try {
            userGroupService.deleteGroup(id);
            return Result.success("分组删除成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 复制分组
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserGroup> copyGroup(@PathVariable Long id, @RequestParam String newName) {
        try {
            UserGroup group = userGroupService.copyGroup(id, newName);
            return Result.success("分组复制成功", group);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 批量分配用户到分�?     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignUsers(
            @PathVariable Long id,
            @RequestBody List<Long> userIds) {
        try {
            for (Long userId : userIds) {
                userGroupService.assignUserToGroup(userId, id);
            }
            return Result.success("用户分配成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 移除用户
     */
    @PostMapping("/remove-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> removeUser(@PathVariable Long userId) {
        try {
            userGroupService.removeUserFromGroup(userId);
            return Result.success("用户已从分组移除", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取模型倍率配置
     */
    @GetMapping("/model-rates")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getModelRates() {
        List<UserGroup> groups = userGroupService.getAllGroups();
        // 返回分组列表，供前端配置
        return Result.success(Map.of("groups", groups));
    }

    // ==================== 请求DTO ====================

    @Data
    public static class UserGroupRequest {
        private String name;
        private String description;
        private Integer priority;
        private String status;
        private Integer defaultMinuteLimit;
        private Integer defaultDailyLimit;
        private Integer defaultMonthlyLimit;
        private java.math.BigDecimal globalRate;
        private String modelRates;        // JSON: {"gpt-4": 1.2, "claude-3": 1.5}
        private String channelRates;     // JSON: {"openai": 1.0, "azure": 0.8}
        private String allowedModels;      // JSON数组
        private String blockedModels;    // JSON数组
        private String allowedChannels;   // JSON数组
        private Boolean allowRecharge;
        private Boolean allowPackages;
        private Boolean allowUsageStats;
        private String level;
        private Integer validDays;
        private String remark;
    }
}
