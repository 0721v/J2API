package com.apiplatform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 缓存配置（内存缓存回退）
 * 当Redis不可用时使用内存缓存
 *
 * @author API Platform Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 内存缓存管理器（作为回退）
     */
    @Bean
    @ConditionalOnMissingBean(name = "cacheManager")
    public CacheManager memoryCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("users", "tokens", "channels", "models", "settings"));
        return cacheManager;
    }
}