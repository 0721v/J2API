package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.UsageLogService;
import com.apiplatform.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final UsageLogService usageLogService;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(
            @RequestAttribute(value = "userId", required = false) Long userId) {
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
    public Result<Map<String, Object>> updateProfile(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
    public Result<Map<String, Object>> getBalance(
            @RequestAttribute(value = "userId", required = false) Long userId) {
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
    public Result<Map<String, Object>> getStats(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
        
        // 转换字段名以匹配前端期望
        stats.put("monthlyCost", stats.getOrDefault("totalAmount", 0L));
        stats.put("tokenCount", ((Number)stats.getOrDefault("totalInputTokens", 0)).intValue() + 
                            ((Number)stats.getOrDefault("totalOutputTokens", 0)).intValue());
        
        // 获取趋势数据
        LocalDateTime trendStart = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> trend = usageLogService.getDailyUsageTrend(userId, trendStart, endTime);
        // 转换趋势数据格式
        List<Map<String, Object>> formattedTrend = trend.stream().map(item -> {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("date", item.get("date"));
            formatted.put("count", item.getOrDefault("calls", item.getOrDefault("count", 0)));
            return formatted;
        }).collect(java.util.stream.Collectors.toList());
        stats.put("trend", formattedTrend);
        
        // 获取模型排行
        List<Map<String, Object>> modelRankingData = usageLogService.getModelUsageRanking(userId, startTime, endTime);
        // 转换模型排行格式
        List<Map<String, Object>> formattedRanking = modelRankingData.stream().map(item -> {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("model", item.getOrDefault("modelName", item.getOrDefault("model", "Unknown")));
            formatted.put("count", item.getOrDefault("calls", item.getOrDefault("count", 0)));
            return formatted;
        }).collect(java.util.stream.Collectors.toList());
        stats.put("modelRanking", formattedRanking);
        
        return Result.success(stats);
    }

    /**
     * 更新语言偏好
     */
    @PutMapping("/language")
    public Result<Void> updateLanguage(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
    public Result<Void> updateTheme(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
    public Result<Object> getModelRanking(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
    public Result<Object> getChannelRanking(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
    public Result<Object> getDailyTrend(
            @RequestAttribute(value = "userId", required = false) Long userId,
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
