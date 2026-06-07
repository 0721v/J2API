package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.ChannelService;
import com.apiplatform.service.ModelService;
import com.apiplatform.service.OrderService;
import com.apiplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据看板控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final OrderService orderService;
    private final ChannelService channelService;
    private final ModelService modelService;

    /**
     * 获取仪表盘概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        
        // 用户统计数据
        Map<String, Object> userStats = userService.getUserStats();
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userStats.get("totalUsers"));
        data.put("activeUsers", userStats.get("activeUsers"));
        data.put("totalBalance", userStats.get("totalBalance"));
        data.put("newUsersToday", userStats.get("newUsersToday"));
        
        return Result.success(data);
    }

    /**
     * 获取销售统计
     */
    @GetMapping("/sales")
    public Result<Map<String, Object>> getSalesStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        // 获取订单统计
        Map<String, Object> orderStats = orderService.getOrderStats(null, startTime, endTime);
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", orderStats.get("totalOrders"));
        data.put("totalAmount", orderStats.get("totalAmount"));
        data.put("paidOrders", orderStats.get("paidOrders"));
        data.put("dailyStats", orderStats.get("dailyStats"));
        
        return Result.success(data);
    }

    /**
     * 获取使用统计
     */
    @GetMapping("/usage")
    public Result<Map<String, Object>> getUsageStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        // 获取渠道使用统计
        var channelStats = channelService.getChannelUsageStats(
                java.time.ZonedDateTime.of(startTime.toLocalDate(), java.time.LocalTime.MIN, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                java.time.ZonedDateTime.of(endTime.toLocalDate(), java.time.LocalTime.MIN, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("channelStats", channelStats);
        data.put("period", Map.of("start", startTime, "end", endTime));
        
        return Result.success(data);
    }

    /**
     * 获取渠道分布
     */
    @GetMapping("/channel-distribution")
    public Result<Map<String, Object>> getChannelDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        var channelStats = channelService.countByType();
        
        Map<String, Object> data = new HashMap<>();
        data.put("distribution", channelStats);
        
        return Result.success(data);
    }

    /**
     * 获取模型分布
     */
    @GetMapping("/model-distribution")
    public Result<Map<String, Object>> getModelDistribution() {
        var modelStats = modelService.list().stream()
                .map(m -> Map.of(
                        "modelId", m.getModelId(),
                        "name", m.getName(),
                        "type", m.getType(),
                        "enabled", m.getEnabled()
                ))
                .toList();
        
        Map<String, Object> data = new HashMap<>();
        data.put("models", modelStats);
        
        return Result.success(data);
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam String type, // users, orders, usage, revenue
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "daily") String granularity) { // hourly, daily, weekly, monthly
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        // TODO: 根据type和granularity聚合数据
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("granularity", granularity);
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        data.put("trend", java.util.List.of()); // TODO: 返回实际趋势数据
        
        return Result.success(data);
    }

    /**
     * 获取实时数据
     */
    @GetMapping("/realtime")
    public Result<Map<String, Object>> getRealtimeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("activeConnections", 0); // TODO: 从Redis获取
        data.put("requestsPerMinute", 0);
        data.put("avgResponseTime", 0);
        data.put("errorRate", 0.0);
        data.put("timestamp", System.currentTimeMillis());
        
        return Result.success(data);
    }

    /**
     * 获取Top排名
     */
    @GetMapping("/top")
    public Result<Map<String, Object>> getTopRanking(
            @RequestParam(defaultValue = "users") String type, // users, models, channels
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (startTime == null) startTime = LocalDateTime.now().minusDays(30);
        if (endTime == null) endTime = LocalDateTime.now();
        
        Map<String, Object> data = new HashMap<>();
        
        switch (type) {
            case "users":
                // TODO: 获取消费Top用户
                data.put("ranking", java.util.List.of());
                break;
            case "models":
                // TODO: 获取使用Top模型
                data.put("ranking", java.util.List.of());
                break;
            case "channels":
                // TODO: 获取流量Top渠道
                data.put("ranking", java.util.List.of());
                break;
            default:
                data.put("ranking", java.util.List.of());
        }
        
        return Result.success(data);
    }
}
