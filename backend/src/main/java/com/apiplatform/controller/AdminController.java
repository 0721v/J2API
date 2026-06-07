package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final ChannelService channelService;
    private final ModelService modelService;
    private final TokenService tokenService;
    private final AnnouncementService announcementService;
    private final AgentService agentService;

    /**
     * 获取管理后台统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // 用户统计
            Map<String, Object> userStats = userService.getUserStats();
            data.put("totalUsers", userStats.getOrDefault("totalUsers", 0));
            data.put("activeUsers", userStats.getOrDefault("activeUsers", 0));
            data.put("newUsersToday", userStats.getOrDefault("newUsersToday", 0));
            data.put("totalBalance", userStats.getOrDefault("totalBalance", 0L));
            
            // 订单统计
            Map<String, Object> orderStats = orderService.getOrderStats(null, null, null);
            data.put("totalOrders", orderStats.getOrDefault("totalOrders", 0));
            data.put("totalAmount", orderStats.getOrDefault("totalAmount", 0L));
            
            // Token统计
            data.put("totalTokens", tokenService.count());
            
            // 渠道统计
            data.put("totalChannels", channelService.count());
            data.put("activeChannels", channelService.count());
            
            // 模型统计
            data.put("totalModels", modelService.count());
            data.put("activeModels", modelService.count());
            
            // 公告统计
            data.put("unreadAnnouncements", announcementService.getUnreadCount(0L));
            
            // 代理统计
            data.put("totalAgents", agentService.count());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getHealth() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("timestamp", System.currentTimeMillis());
        data.put("uptime", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        data.put("memory", Map.of(
                "total", Runtime.getRuntime().totalMemory(),
                "free", Runtime.getRuntime().freeMemory(),
                "used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        ));
        return Result.success(data);
    }
}