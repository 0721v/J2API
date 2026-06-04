package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.UsageLogService;
import com.apiplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;
    private final UsageLogService usageLogService;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取当前用户的详细资料")
    public Result<Map<String, Object>> getProfile(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        var user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("balance", user.getBalance());
        data.put("totalConsumption", user.getTotalConsumption());
        data.put("preferredLanguage", user.getPreferredLanguage());
        data.put("preferredTheme", user.getPreferredTheme());
        data.put("createdAt", user.getCreatedAt());
        
        return Result.success(data);
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前用户的资料信息")
    public Result<Map<String, Object>> updateProfile(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        String displayName = request.get("displayName");
        String phone = request.get("phone");
        String avatar = request.get("avatar");
        
        var user = userService.updateProfile(userId, displayName, phone, avatar);
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("displayName", user.getDisplayName());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        
        return Result.success("资料更新成功", data);
    }

    /**
     * 获取用户余额
     */
    @GetMapping("/balance")
    @Operation(summary = "获取用户余额", description = "获取当前用户的账户余额")
    public Result<Map<String, Object>> getBalance(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        Long balance = userService.getBalance(userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("balance", balance);
        data.put("balanceStr", String.format("%.2f", balance / 100.0));
        
        return Result.success(data);
    }

    /**
     * 获取使用统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取使用统计", description = "获取当前用户的使用统计信息")
    public Result<Map<String, Object>> getStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        // 默认查询最近30天
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        Map<String, Object> stats = usageLogService.getUserUsageStats(userId, startTime, endTime);
        
        // 添加今日统计
        Long todayUsage = usageLogService.getTodayUsageCount(userId);
        stats.put("todayUsage", todayUsage);
        
        // 添加用户余额
        Long balance = userService.getBalance(userId);
        stats.put("balance", balance);
        
        return Result.success(stats);
    }

    /**
     * 更新语言偏好
     */
    @PutMapping("/language")
    @Operation(summary = "更新语言偏好", description = "更新用户的首选语言")
    public Result<Void> updateLanguage(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam String language) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        userService.updatePreferredLanguage(userId, language);
        return Result.success("语言设置成功", null);
    }

    /**
     * 更新主题偏好
     */
    @PutMapping("/theme")
    @Operation(summary = "更新主题偏好", description = "更新用户的首选主题")
    public Result<Void> updateTheme(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam String theme) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        userService.updatePreferredTheme(userId, theme);
        return Result.success("主题设置成功", null);
    }

    /**
     * 获取模型使用排名
     */
    @GetMapping("/model-ranking")
    @Operation(summary = "获取模型使用排名", description = "获取用户使用量最高的模型列表")
    public Result<Object> getModelRanking(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        var ranking = usageLogService.getModelUsageRanking(userId, startTime, endTime);
        return Result.success(ranking);
    }

    /**
     * 获取渠道使用排名
     */
    @GetMapping("/channel-ranking")
    @Operation(summary = "获取渠道使用排名", description = "获取用户使用量最高的渠道列表")
    public Result<Object> getChannelRanking(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        var ranking = usageLogService.getChannelUsageRanking(userId, startTime, endTime);
        return Result.success(ranking);
    }

    /**
     * 获取日使用趋势
     */
    @GetMapping("/daily-trend")
    @Operation(summary = "获取日使用趋势", description = "获取用户每日使用量的趋势数据")
    public Result<Object> getDailyTrend(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        var trend = usageLogService.getDailyUsageTrend(userId, startTime, endTime);
        return Result.success(trend);
    }
}
