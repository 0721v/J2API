package com.apiplatform.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置
 * 只有当 spring.redis.host 配置时才生效
 *
 * @author API Platform Team
 */
@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisConfig {

    /**
     * Redis模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON序列化配置
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL, 
                JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new JavaTimeModule());
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // Hash的Key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // Value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash的Value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Redis缓存管理器
     */
    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        serializer.setObjectMapper(objectMapper);

        RedisSerializer<String> stringSerializer = new StringRedisSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 默认缓存时间：1小时
                .entryTtl(Duration.ofHours(1))
                // Key序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                // Value序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                // 禁止缓存null值
                .disableCachingNullValues();

        // 不同缓存配置
        RedisCacheConfiguration userConfig = config.entryTtl(Duration.ofMinutes(30)); // 用户信息30分钟
        RedisCacheConfiguration tokenConfig = config.entryTtl(Duration.ofHours(2)); // Token配置2小时
        RedisCacheConfiguration channelConfig = config.entryTtl(Duration.ofMinutes(10)); // 渠道配置10分钟
        RedisCacheConfiguration modelConfig = config.entryTtl(Duration.ofHours(1)); // 模型配置1小时

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("users", userConfig)
                .withCacheConfiguration("tokens", tokenConfig)
                .withCacheConfiguration("channels", channelConfig)
                .withCacheConfiguration("models", modelConfig)
                .withCacheConfiguration("settings", config.entryTtl(Duration.ofHours(24)))
                .transactionAware()
                .build();
    }
}