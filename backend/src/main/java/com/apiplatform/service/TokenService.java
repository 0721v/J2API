package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Token;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 令牌服务接口
 *
 * @author API Platform Team
 */
public interface TokenService extends IService<Token> {

    /**
     * 创建令牌
     */
    Token createToken(Long userId, String name, Long groupId, 
                      String allowedModels, String allowedChannels,
                      Integer minuteLimit, Integer dayLimit,
                      Long quotaLimit, LocalDateTime expiresAt);

    /**
     * 更新令牌
     */
    Token updateToken(Long tokenId, Long userId, String name, Long groupId,
                      String allowedModels, String allowedChannels,
                      Integer minuteLimit, Integer dayLimit,
                      Long quotaLimit, LocalDateTime expiresAt);

    /**
     * 删除令牌
     */
    void deleteToken(Long tokenId, Long userId);

    /**
     * 启用令牌
     */
    void enableToken(Long tokenId, Long userId);

    /**
     * 禁用令牌
     */
    void disableToken(Long tokenId, Long userId);

    /**
     * 根据API Key获取令牌
     */
    Token getByApiKey(String apiKey);

    /**
     * 验证令牌
     */
    Token validateToken(String apiKey);

    /**
     * 检查令牌限流
     */
    boolean checkRateLimit(Token token);

    /**
     * 获取用户令牌列表
     */
    PageResult<Token> getUserTokens(Long userId, int page, int size);

    /**
     * 获取令牌使用统计
     */
    Map<String, Object> getTokenUsage(Long tokenId, Long userId);

    /**
     * 更新令牌使用额度
     */
    void updateTokenQuota(Long tokenId, Long amount);

    /**
     * 批量更新令牌过期状态
     */
    void updateExpiredTokens();

    /**
     * 获取即将过期的令牌
     */
    List<Token> getExpiringTokens(Long userId, int days);

    /**
     * 续期令牌
     */
    Token renewToken(Long tokenId, Long userId);
}
