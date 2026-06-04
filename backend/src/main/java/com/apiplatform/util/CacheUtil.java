package com.apiplatform.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具接口
 *
 * @author API Platform Team
 */
public interface CacheUtil {

    // ==================== 基本操作 ====================

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    void setEx(String key, Object value, long seconds);

    <T> T get(String key);

    <T> T get(String key, T defaultValue);

    Boolean delete(String key);

    Long delete(Collection<String> keys);

    Boolean hasKey(String key);

    Boolean expire(String key, long timeout, TimeUnit unit);

    Long getExpire(String key);

    Boolean persist(String key);

    // ==================== 计数操作 ====================

    Long increment(String key);

    Long increment(String key, long delta);

    Long decrement(String key);

    Long decrement(String key, long delta);

    Double incrementFloat(String key, double delta);

    // ==================== Hash操作 ====================

    void hSet(String key, String field, Object value);

    void hMSet(String key, Map<String, Object> map);

    <T> T hGet(String key, String field);

    Map<Object, Object> hGetAll(String key);

    Long hDelete(String key, Object... fields);

    Boolean hExists(String key, String field);

    Long hSize(String key);

    Long hIncrement(String key, String field, long delta);

    // ==================== Set操作 ====================

    Long sAdd(String key, Object... members);

    Set<Object> sMembers(String key);

    Boolean sIsMember(String key, Object member);

    Long sSize(String key);

    Long sRemove(String key, Object... members);

    // ==================== 限流操作 ====================

    boolean slidingWindowRateLimit(String key, int maxRequests, long windowSeconds);

    boolean tokenBucketRateLimit(String key, int capacity, int refillRate);

    Long getRateLimitCount(String key);

    Long getRateLimitTTL(String key);

    // ==================== 缓存操作 ====================

    boolean cacheHit(String key);

    void cacheMiss(String key);

    boolean isCacheHit(String key);
}