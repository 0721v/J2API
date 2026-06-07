package com.apiplatform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 当配置了spring.redis.host时启用
 *
 * @author API Platform Team
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisUtil implements CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 基本操作 ====================

    /**
     * 设置值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置值并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置值并指定过期时间（秒）
     */
    public void setEx(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取值（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T value = (T) redisTemplate.opsForValue().get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除键
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 移除过期时间（永久）
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    // ==================== 计数操作 ====================

    /**
     * 递增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 递减指定值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 递增（浮点数）
     */
    public Double incrementFloat(String key, double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ==================== Hash操作 ====================

    /**
     * 设置Hash字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 批量设置Hash字段
     */
    public void hMSet(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取Hash字段值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取所有Hash字段值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断Hash字段是否存在
     */
    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取Hash字段数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * Hash递增
     */
    public Long hIncrement(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    // ==================== Set操作 ====================

    /**
     * 添加Set成员
     */
    public Long sAdd(String key, Object... members) {
        return redisTemplate.opsForSet().add(key, members);
    }

    /**
     * 获取Set所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断是否为Set成员
     */
    public Boolean sIsMember(String key, Object member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    /**
     * 获取Set成员数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 移除Set成员
     */
    public Long sRemove(String key, Object... members) {
        return redisTemplate.opsForSet().remove(key, members);
    }

    // ==================== List操作 ====================

    /**
     * 左边添加
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 批量左边添加
     */
    public Long lLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 右边添加
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 获取List范围
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取List长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 获取单个元素
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 移除元素
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== 限流操作 ====================

    /**
     * 滑动窗口限流
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许请求
     */
    public boolean slidingWindowRateLimit(String key, int maxRequests, long windowSeconds) {
        String luaScript = """
            local key = KEYS[1]
            local max = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local expire = window
            
            -- 删除窗口外的旧数据
            redis.call('ZREMRANGEBYSCORE', key, 0, now - window * 1000)
            
            -- 获取当前请求数
            local current = redis.call('ZCARD', key)
            
            if current < max then
                -- 添加新请求
                redis.call('ZADD', key, now, now .. ':' .. math.random())
                redis.call('PEXPIRE', key, expire * 1000)
                return 1
            else
                return 0
            end
            """;
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script, 
                List.of(key), 
                maxRequests, 
                windowSeconds, 
                System.currentTimeMillis());
        return result != null && result == 1;
    }

    /**
     * 令牌桶限流
     * @param key 限流键
     * @param capacity 桶容量
     * @param refillRate 补充速率（个/秒）
     * @return 是否允许请求
     */
    public boolean tokenBucketRateLimit(String key, int capacity, int refillRate) {
        String luaScript = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillRate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = 1
            
            local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
            local tokens = tonumber(bucket[1])
            local lastRefill = tonumber(bucket[2])
            
            if tokens == nil then
                tokens = capacity
                lastRefill = now
            end
            
            -- 计算应该补充的令牌数
            local elapsed = (now - lastRefill) / 1000
            local tokensToAdd = elapsed * refillRate
            tokens = math.min(capacity, tokens + tokensToAdd)
            
            if tokens >= requested then
                tokens = tokens - requested
                redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now)
                redis.call('EXPIRE', key, 60)
                return 1
            else
                redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now)
                redis.call('EXPIRE', key, 60)
                return 0
            end
            """;
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script, 
                List.of(key), 
                capacity, 
                refillRate, 
                System.currentTimeMillis());
        return result != null && result == 1;
    }

    /**
     * 获取当前限流计数
     */
    public Long getRateLimitCount(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取限流剩余时间
     */
    public Long getRateLimitTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // ==================== 缓存操作 ====================

    /**
     * 缓存首次命中（用于缓存计费）
     */
    public boolean cacheHit(String key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key + ":hit", "1", 24, TimeUnit.HOURS));
    }

    /**
     * 标记缓存未命中
     */
    public void cacheMiss(String key) {
        redisTemplate.opsForValue().set(key + ":miss", "1", 24, TimeUnit.HOURS);
    }

    /**
     * 检查是否为缓存命中
     */
    public boolean isCacheHit(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key + ":hit"));
    }
}
