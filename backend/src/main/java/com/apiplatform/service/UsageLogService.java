package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.UsageLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 使用日志服务接口
 *
 * @author API Platform Team
 */
public interface UsageLogService extends IService<UsageLog> {

    /**
     * 记录使用日志
     */
    UsageLog recordUsage(Long userId, Long tokenId, Long modelId, String modelName,
                         Long channelId, String channelName, String apiType,
                         Integer requestTokens, Integer responseTokens,
                         Boolean cacheHit, Long billedAmount,
                         Long responseTime, Integer httpStatus,
                         String status, String errorMessage,
                         String ipAddress, String requestPath);

    /**
     * 获取用户使用统计
     */
    Map<String, Object> getUserUsageStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取Token使用统计
     */
    Map<String, Object> getTokenUsageStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取模型使用排名
     */
    List<Map<String, Object>> getModelUsageRanking(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取渠道使用排名
     */
    List<Map<String, Object>> getChannelUsageRanking(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取日使用趋势
     */
    List<Map<String, Object>> getDailyUsageTrend(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取缓存命中率
     */
    Map<String, Object> getCacheHitStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询使用日志
     */
    PageResult<UsageLog> pageUsageLogs(Long userId, int page, int size,
                                        Long modelId, Long channelId,
                                        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取今日使用量
     */
    Long getTodayUsageCount(Long userId);

    /**
     * 清理过期日志
     */
    void cleanExpiredLogs(int days);

    /**
     * 获取最近调用记录
     */
    List<UsageLog> getRecentUsage(Long userId, int limit);
}
