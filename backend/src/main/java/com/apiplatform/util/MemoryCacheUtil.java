package com.apiplatform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存缓存工具类（当Redis不可用时使用）
 * 当未配置spring.redis.host时启用
 *
 * @author API Platform Team
 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "redisUtil")
public class MemoryCacheUtil implements CacheUtil {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Object>> hashCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Object>> setCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Object>> listCache = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        Object value;
        long expireTime;

        CacheEntry(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
    }

    // ==================== 基本操作 ====================

    public void set(String key, Object value) {
        cache.put(key, new CacheEntry(value, 0));
    }

    public void set(String key, Object value, long timeout, java.util.concurrent.TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(timeout);
        cache.put(key, new CacheEntry(value, expireTime));
    }

    public void setEx(String key, Object value, long seconds) {
        set(key, value, seconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cache.remove(key);
            }
            return null;
        }
        return (T) entry.value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    public Boolean delete(String key) {
        CacheEntry removed = cache.remove(key);
        hashCache.remove(key);
        setCache.remove(key);
        listCache.remove(key);
        return removed != null;
    }

    public Long delete(Collection<String> keys) {
        long count = 0;
        for (String key : keys) {
            if (delete(key)) {
                count++;
            }
        }
        return count;
    }

    public Boolean hasKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cache.remove(key);
            }
            return false;
        }
        return true;
    }

    public Boolean expire(String key, long timeout, java.util.concurrent.TimeUnit unit) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        entry.expireTime = System.currentTimeMillis() + unit.toMillis(timeout);
        return true;
    }

    public Long getExpire(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                cache.remove(key);
            }
            return -1L;
        }
        if (entry.expireTime == 0) {
            return -1L;
        }
        return Math.max(0, (entry.expireTime - System.currentTimeMillis()) / 1000);
    }

    public Boolean persist(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        entry.expireTime = 0;
        return true;
    }

    // ==================== 计数操作 ====================

    public Long increment(String key) {
        return increment(key, 1);
    }

    public Long increment(String key, long delta) {
        CacheEntry entry = cache.get(key);
        Long current = 0L;
        if (entry != null && !entry.isExpired()) {
            Object value = entry.value;
            if (value instanceof Number) {
                current = ((Number) value).longValue();
            }
        }
        current += delta;
        set(key, current);
        return current;
    }

    public Long decrement(String key) {
        return increment(key, -1);
    }

    public Long decrement(String key, long delta) {
        return increment(key, -delta);
    }

    public Double incrementFloat(String key, double delta) {
        CacheEntry entry = cache.get(key);
        Double current = 0.0;
        if (entry != null && !entry.isExpired()) {
            Object value = entry.value;
            if (value instanceof Number) {
                current = ((Number) value).doubleValue();
            }
        }
        current += delta;
        set(key, current);
        return current;
    }

    // ==================== Hash操作 ====================

    public void hSet(String key, String field, Object value) {
        hashCache.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
    }

    public void hMSet(String key, Map<String, Object> map) {
        hashCache.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).putAll(map);
    }

    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        Map<String, Object> map = hashCache.get(key);
        return map != null ? (T) map.get(field) : null;
    }

    public Map<Object, Object> hGetAll(String key) {
        Map<String, Object> map = hashCache.get(key);
        if (map == null) {
            return new HashMap<>();
        }
        Map<Object, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Long hDelete(String key, Object... fields) {
        Map<String, Object> map = hashCache.get(key);
        if (map == null) {
            return 0L;
        }
        long count = 0;
        for (Object field : fields) {
            if (map.remove(field) != null) {
                count++;
            }
        }
        return count;
    }

    public Boolean hExists(String key, String field) {
        Map<String, Object> map = hashCache.get(key);
        return map != null && map.containsKey(field);
    }

    public Long hSize(String key) {
        Map<String, Object> map = hashCache.get(key);
        return map != null ? (long) map.size() : 0L;
    }

    public Long hIncrement(String key, String field, long delta) {
        Map<String, Object> map = hashCache.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        Object value = map.get(field);
        Long current = value instanceof Number ? ((Number) value).longValue() : 0L;
        current += delta;
        map.put(field, current);
        return current;
    }

    // ==================== Set操作 ====================

    public Long sAdd(String key, Object... members) {
        Set<Object> set = setCache.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long count = 0;
        for (Object member : members) {
            if (set.add(member)) {
                count++;
            }
        }
        return count;
    }

    public Set<Object> sMembers(String key) {
        Set<Object> set = setCache.get(key);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    public Boolean sIsMember(String key, Object member) {
        Set<Object> set = setCache.get(key);
        return set != null && set.contains(member);
    }

    public Long sSize(String key) {
        Set<Object> set = setCache.get(key);
        return set != null ? (long) set.size() : 0L;
    }

    public Long sRemove(String key, Object... members) {
        Set<Object> set = setCache.get(key);
        if (set == null) {
            return 0L;
        }
        long count = 0;
        for (Object member : members) {
            if (set.remove(member)) {
                count++;
            }
        }
        return count;
    }

    // ==================== List操作 ====================

    public Long lLeftPush(String key, Object value) {
        ConcurrentLinkedDeque<Object> list = listCache.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        list.addFirst(value);
        return (long) list.size();
    }

    public Long lLeftPushAll(String key, Collection<Object> values) {
        ConcurrentLinkedDeque<Object> list = listCache.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        values.forEach(list::addFirst);
        return (long) list.size();
    }

    public Long lRightPush(String key, Object value) {
        ConcurrentLinkedDeque<Object> list = listCache.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        list.addLast(value);
        return (long) list.size();
    }

    public List<Object> lRange(String key, long start, long end) {
        ConcurrentLinkedDeque<Object> list = listCache.get(key);
        if (list == null) {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>(list);
        int size = result.size();
        int startIdx = Math.max(0, (int) start);
        int endIdx = Math.min(size - 1, (int) end);
        if (startIdx > endIdx) {
            return new ArrayList<>();
        }
        return result.subList(startIdx, endIdx + 1);
    }

    public Long lSize(String key) {
        ConcurrentLinkedDeque<Object> list = listCache.get(key);
        return list != null ? (long) list.size() : 0L;
    }

    public Object lIndex(String key, long index) {
        ConcurrentLinkedDeque<Object> list = listCache.get(key);
        if (list == null) {
            return null;
        }
        List<Object> result = new ArrayList<>(list);
        if (index < 0 || index >= result.size()) {
            return null;
        }
        return result.get((int) index);
    }

    public Long lRemove(String key, long count, Object value) {
        ConcurrentLinkedDeque<Object> list = listCache.get(key);
        if (list == null) {
            return 0L;
        }
        long removed = 0;
        if (count > 0) {
            Iterator<Object> iterator = list.iterator();
            while (iterator.hasNext() && removed < count) {
                if (Objects.equals(iterator.next(), value)) {
                    iterator.remove();
                    removed++;
                }
            }
        } else if (count < 0) {
            Iterator<Object> iterator = list.descendingIterator();
            while (iterator.hasNext() && removed < -count) {
                if (Objects.equals(iterator.next(), value)) {
                    iterator.remove();
                    removed++;
                }
            }
        } else {
            list.removeIf(v -> Objects.equals(v, value));
            removed = list.size();
        }
        return removed;
    }

    // ==================== 限流操作 ====================

    public boolean slidingWindowRateLimit(String key, int maxRequests, long windowSeconds) {
        String listKey = key + ":window";
        ConcurrentLinkedDeque<Object> window = listCache.computeIfAbsent(listKey, k -> new ConcurrentLinkedDeque<>());
        
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000;
        
        // 移除窗口外的旧数据
        while (!window.isEmpty()) {
            Long timestamp = (Long) window.peekFirst();
            if (timestamp != null && timestamp < now - windowMs) {
                window.pollFirst();
            } else {
                break;
            }
        }
        
        if (window.size() < maxRequests) {
            window.addLast(now);
            return true;
        }
        return false;
    }

    public boolean tokenBucketRateLimit(String key, int capacity, int refillRate) {
        String bucketKey = key + ":bucket";
        Map<String, Object> bucket = hashCache.computeIfAbsent(bucketKey, k -> new ConcurrentHashMap<>());
        
        long now = System.currentTimeMillis();
        double tokens = bucket.containsKey("tokens") ? ((Number) bucket.get("tokens")).doubleValue() : capacity;
        long lastRefill = bucket.containsKey("lastRefill") ? ((Number) bucket.get("lastRefill")).longValue() : now;
        
        long elapsed = now - lastRefill;
        double tokensToAdd = (elapsed / 1000.0) * refillRate;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        
        if (tokens >= 1) {
            tokens -= 1;
            bucket.put("tokens", tokens);
            bucket.put("lastRefill", now);
            return true;
        }
        
        bucket.put("tokens", tokens);
        bucket.put("lastRefill", now);
        return false;
    }

    public Long getRateLimitCount(String key) {
        String listKey = key + ":window";
        ConcurrentLinkedDeque<Object> window = listCache.get(listKey);
        return window != null ? (long) window.size() : 0L;
    }

    public Long getRateLimitTTL(String key) {
        return getExpire(key);
    }

    // ==================== 缓存操作 ====================

    public boolean cacheHit(String key) {
        String hitKey = key + ":hit";
        if (hasKey(hitKey)) {
            return false;
        }
        setEx(hitKey, "1", 24 * 3600);
        return true;
    }

    public void cacheMiss(String key) {
        setEx(key + ":miss", "1", 24 * 3600);
    }

    public boolean isCacheHit(String key) {
        return hasKey(key + ":hit");
    }
}