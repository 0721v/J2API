package com.apiplatform.service.impl;

import com.apiplatform.entity.Token;
import com.apiplatform.service.RateLimitService;
import com.apiplatform.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final CacheUtil cacheUtil;

    @Value("${system.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${system.rate-limit.default-minute-limit:60}")
    private int defaultMinuteLimit;

    @Value("${system.rate-limit.default-day-limit:10000}")
    private int defaultDayLimit;

    private static final String MINUTE_KEY_PREFIX = "ratelimit:minute:";
    private static final String DAY_KEY_PREFIX = "ratelimit:day:";
    private static final String GLOBAL_KEY = "ratelimit:global";

    @Override
    public boolean checkMinuteLimit(Token token) {
        if (!rateLimitEnabled || token.getMinuteLimit() == null || token.getMinuteLimit() <= 0) {
            return true;
        }

        String key = MINUTE_KEY_PREFIX + token.getId();
        Long current = cacheUtil.get(key);
        
        if (current == null) {
            return true;
        }

        return current < token.getMinuteLimit();
    }

    @Override
    public boolean checkDayLimit(Token token) {
        if (!rateLimitEnabled || token.getDayLimit() == null || token.getDayLimit() <= 0) {
            return true;
        }

        String key = DAY_KEY_PREFIX + token.getId();
        Long current = cacheUtil.get(key);
        
        if (current == null) {
            return true;
        }

        return current < token.getDayLimit();
    }

    @Override
    public boolean checkGlobalLimit() {
        if (!rateLimitEnabled) {
            return true;
        }

        String key = GLOBAL_KEY + ":minute";
        Long current = cacheUtil.get(key);
        
        // 全局限流默认10000次/分钟
        if (current == null) {
            return true;
        }

        return current < 10000;
    }

    @Override
    public boolean checkUserLimit(Long userId) {
        if (!rateLimitEnabled) {
            return true;
        }

        // 用户级别限流：默认1000次/分钟
        String key = "ratelimit:user:" + userId + ":minute";
        Long current = cacheUtil.get(key);
        
        if (current == null) {
            return true;
        }

        return current < 1000;
    }

    @Override
    public boolean checkIpLimit(String ipAddress) {
        if (!rateLimitEnabled || ipAddress == null) {
            return true;
        }

        // IP级别限流：默认500次/分钟
        String key = "ratelimit:ip:" + ipAddress + ":minute";
        Long current = cacheUtil.get(key);
        
        if (current == null) {
            return true;
        }

        return current < 500;
    }

    @Override
    public void incrementRequestCount(Token token) {
        if (!rateLimitEnabled) {
            return;
        }

        // 分钟计数
        String minuteKey = MINUTE_KEY_PREFIX + token.getId();
        Long minuteCount = cacheUtil.increment(minuteKey);
        if (minuteCount != null && minuteCount == 1) {
            cacheUtil.expire(minuteKey, 60, TimeUnit.SECONDS);
        }

        // 日计数
        String dayKey = DAY_KEY_PREFIX + token.getId();
        Long dayCount = cacheUtil.increment(dayKey);
        if (dayCount != null && dayCount == 1) {
            // 设置过期时间为明天凌晨
            long secondsUntilMidnight = getSecondsUntilMidnight();
            cacheUtil.expire(dayKey, secondsUntilMidnight, TimeUnit.SECONDS);
        }

        // 全局限流计数
        String globalMinuteKey = GLOBAL_KEY + ":minute";
        cacheUtil.increment(globalMinuteKey);
        cacheUtil.expire(globalMinuteKey, 60, TimeUnit.SECONDS);

        // 用户计数
        String userMinuteKey = "ratelimit:user:" + token.getUserId() + ":minute";
        cacheUtil.increment(userMinuteKey);
        cacheUtil.expire(userMinuteKey, 60, TimeUnit.SECONDS);

        log.debug("增加请求计数: tokenId={}, minuteCount={}, dayCount={}", 
                token.getId(), minuteCount, dayCount);
    }

    @Override
    public Long getMinuteRequestCount(Token token) {
        String key = MINUTE_KEY_PREFIX + token.getId();
        Long count = cacheUtil.get(key);
        return count != null ? count : 0L;
    }

    @Override
    public Long getDayRequestCount(Token token) {
        String key = DAY_KEY_PREFIX + token.getId();
        Long count = cacheUtil.get(key);
        return count != null ? count : 0L;
    }

    @Override
    public Long getUserMinuteRequestCount(Long userId) {
        String key = "ratelimit:user:" + userId + ":minute";
        Long count = cacheUtil.get(key);
        return count != null ? count : 0L;
    }

    @Override
    public Long getUserDayRequestCount(Long userId) {
        String key = "ratelimit:user:" + userId + ":day:" + LocalDate.now();
        Long count = cacheUtil.get(key);
        return count != null ? count : 0L;
    }

    @Override
    public Map<String, Object> getRateLimitConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", rateLimitEnabled);
        config.put("defaultMinuteLimit", defaultMinuteLimit);
        config.put("defaultDayLimit", defaultDayLimit);
        return config;
    }

    @Override
    public void resetTokenCounts(Long tokenId) {
        String minuteKey = MINUTE_KEY_PREFIX + tokenId;
        String dayKey = DAY_KEY_PREFIX + tokenId;
        cacheUtil.delete(minuteKey);
        cacheUtil.delete(dayKey);
        log.info("重置信令计数: tokenId={}", tokenId);
    }

    @Override
    public void cleanExpiredCounts() {
        // Redis会自动清理过期键，这里可以做额外的清理逻辑
        log.info("清理过期限流计数");
    }

    /**
     * 获取到当天午夜剩余秒数
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight).getSeconds();
    }
}
