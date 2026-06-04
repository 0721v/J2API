package com.apiplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * API Platform Backend Application
 * API聚合平台后端主启动类
 *
 * @author API Platform Team
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {
    RedisAutoConfiguration.class,
    RedissonAutoConfigurationV2.class
})
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
@EnableAsync
@MapperScan("com.apiplatform.mapper")
public class ApiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPlatformApplication.class, args);
    }
}
