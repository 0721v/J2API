package com.apiplatform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.apiplatform.common.*;
import com.apiplatform.entity.UsageLog;
import com.apiplatform.mapper.UsageLogMapper;
import com.apiplatform.service.UsageLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用日志服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageLogServiceImpl extends ServiceImpl<UsageLogMapper, UsageLog> implements UsageLogService {

    private final UsageLogMapper usageLogMapper;

    @Override
    @Async
    @Transactional
    public UsageLog recordUsage(Long userId, Long tokenId, Long modelId, String modelName,
                                Long channelId, String channelName, String apiType,
                                Integer requestTokens, Integer responseTokens,
                                Boolean cacheHit, Long billedAmount,
                                Long responseTime, Integer httpStatus,
                                String status, String errorMessage,
                                String ipAddress, String requestPath) {
        
        String requestId = IdUtil.fastUUID().toString(true);

        UsageLog usageLog = UsageLog.builder()
                .requestId(requestId)
                .userId(userId)
                .tokenId(tokenId)
                .modelId(modelId)
                .modelName(modelName)
                .channelId(channelId)
                .channelName(channelName)
                .apiType(apiType)
                .requestTokens(requestTokens != null ? requestTokens : 0)
                .responseTokens(responseTokens != null ? responseTokens : 0)
                .cacheHit(cacheHit != null ? cacheHit : false)
                .billedTokens(calculateBilledTokens(requestTokens, responseTokens, cacheHit))
                .billedAmount(billedAmount != null ? billedAmount : 0L)
                .responseTime(responseTime != null ? responseTime : 0L)
                .httpStatus(httpStatus)
                .status(status != null ? status : "success")
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .requestPath(requestPath)
                .build();

        usageLogMapper.insert(usageLog);
        
        if (log.isDebugEnabled()) {
            log.debug("记录使用日志: requestId={}, userId={}, model={}, cost={}", 
                    requestId, userId, modelName, billedAmount);
        }
        
        return usageLog;
    }

    @Override
    public Map<String, Object> getUserUsageStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        // 总调用次数
        LambdaQueryWrapper<UsageLog> wrapper = buildQueryWrapper(userId, null, null, startTime, endTime);
        wrapper.eq(UsageLog::getStatus, "success");
        stats.put("totalCalls", count(wrapper));

        // 总消费金额
        wrapper.select("COALESCE(SUM(billed_amount), 0) as total");
        Object amountObj = getBaseMapper().selectObjs(wrapper).stream().findFirst().orElse(0L);
        stats.put("totalAmount", amountObj);

        // 输入/输出Token
        wrapper = buildQueryWrapper(userId, null, null, startTime, endTime);
        wrapper.eq(UsageLog::getStatus, "success")
                .select("COALESCE(SUM(request_tokens), 0) as input", 
                        "COALESCE(SUM(response_tokens), 0) as output",
                        "COALESCE(SUM(CASE WHEN cache_hit = true THEN 1 ELSE 0 END), 0) as cacheHits");
        
        List<Object> result = getBaseMapper().selectObjs(wrapper);
        if (!result.isEmpty() && result.get(0) instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result.get(0);
            stats.put("totalInputTokens", map.get("input"));
            stats.put("totalOutputTokens", map.get("output"));
            stats.put("cacheHitCalls", map.get("cacheHits"));
        }

        return stats;
    }

    @Override
    public Map<String, Object> getTokenUsageStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取用户下所有令牌的统计数据
        LambdaQueryWrapper<UsageLog> wrapper = buildQueryWrapper(userId, null, null, startTime, endTime);
        wrapper.eq(UsageLog::getStatus, "success")
                .select("token_id",
                        "COUNT(*) as calls",
                        "COALESCE(SUM(billed_amount), 0) as amount")
                .groupBy(UsageLog::getTokenId);

        List<UsageLog> logs = list(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("tokenStats", logs);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getModelUsageRanking(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return usageLogMapper.selectByModel(userId, startTime);
    }

    @Override
    public List<Map<String, Object>> getChannelUsageRanking(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return usageLogMapper.selectByChannel(userId, startTime);
    }

    @Override
    public List<Map<String, Object>> getDailyUsageTrend(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return usageLogMapper.selectTokenStats(userId, startTime);
    }

    @Override
    public Map<String, Object> getCacheHitStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = usageLogMapper.selectCacheStats(userId, startTime);
        
        if (stats != null) {
            long total = ((Number) stats.getOrDefault("total", 0L)).longValue();
            long hits = ((Number) stats.getOrDefault("hits", 0L)).longValue();
            
            double rate = total > 0 ? (double) hits / total * 100 : 0;
            stats.put("hitRate", rate);
        }
        
        return stats;
    }

    @Override
    public PageResult<UsageLog> pageUsageLogs(Long userId, int page, int size,
                                             Long modelId, Long channelId,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        Page<UsageLog> pageParam = new Page<>(page, size);
        IPage<UsageLog> pageResult = usageLogMapper.selectByUserId(
                pageParam, userId, modelId, channelId, startTime, endTime);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public Long getTodayUsageCount(Long userId) {
        return usageLogMapper.countTodayByUserId(userId);
    }

    @Override
    @Transactional
    public void cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<UsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(UsageLog::getCreatedAt, expireTime);
        
        // 只删除超过一定时间的旧日志，保留最近30天
        int deleted = getBaseMapper().delete(wrapper);
        log.info("清理过期日志: days={}, deleted={}", days, deleted);
    }

    @Override
    public List<UsageLog> getRecentUsage(Long userId, int limit) {
        return usageLogMapper.selectRecentByUserId(userId, limit);
    }

    /**
     * 计算计费Token数
     */
    private Integer calculateBilledTokens(Integer requestTokens, Integer responseTokens, Boolean cacheHit) {
        if (Boolean.TRUE.equals(cacheHit)) {
            // 缓存命中时，只计算部分
            return (requestTokens != null ? requestTokens : 0) / 2;
        }
        int total = (requestTokens != null ? requestTokens : 0) 
                   + (responseTokens != null ? responseTokens : 0);
        return total;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<UsageLog> buildQueryWrapper(Long userId, Long modelId, Long channelId,
                                                            LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<UsageLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(UsageLog::getUserId, userId);
        }
        if (modelId != null) {
            wrapper.eq(UsageLog::getModelId, modelId);
        }
        if (channelId != null) {
            wrapper.eq(UsageLog::getChannelId, channelId);
        }
        if (startTime != null) {
            wrapper.ge(UsageLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(UsageLog::getCreatedAt, endTime);
        }
        
        return wrapper;
    }
}
