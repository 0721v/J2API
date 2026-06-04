package com.apiplatform.service;

import com.apiplatform.entity.Token;
import java.util.Map;

/**
 * 限流服务接口
 *
 * @author API Platform Team
 */
public interface RateLimitService {

    /**
     * 检查令牌分钟限流
     */
    boolean checkMinuteLimit(Token token);

    /**
     * 检查令牌日限流
     */
    boolean checkDayLimit(Token token);

    /**
     * 检查全局限流
     */
    boolean checkGlobalLimit();

    /**
     * 检查用户限流
     */
    boolean checkUserLimit(Long userId);

    /**
     * 检查IP限流
     */
    boolean checkIpLimit(String ipAddress);

    /**
     * 增加请求计数
     */
    void incrementRequestCount(Token token);

    /**
     * 获取令牌分钟请求数
     */
    Long getMinuteRequestCount(Token token);

    /**
     * 获取令牌日请求数
     */
    Long getDayRequestCount(Token token);

    /**
     * 获取用户分钟请求数
     */
    Long getUserMinuteRequestCount(Long userId);

    /**
     * 获取用户日请求数
     */
    Long getUserDayRequestCount(Long userId);

    /**
     * 获取限流配置
     */
    Map<String, Object> getRateLimitConfig();

    /**
     * 重置信令计数
     */
    void resetTokenCounts(Long tokenId);

    /**
     * 清理过期计数
     */
    void cleanExpiredCounts();
}
