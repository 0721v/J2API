package com.apiplatform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.apiplatform.common.*;
import com.apiplatform.entity.Token;
import com.apiplatform.entity.TokenGroup;
import com.apiplatform.entity.UsageLog;
import com.apiplatform.mapper.TokenGroupMapper;
import com.apiplatform.mapper.TokenMapper;
import com.apiplatform.mapper.UsageLogMapper;
import com.apiplatform.service.BillingService;
import com.apiplatform.service.RateLimitService;
import com.apiplatform.service.TokenService;
import com.apiplatform.util.ApiKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 令牌服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token> implements TokenService {

    private final TokenMapper tokenMapper;
    private final TokenGroupMapper tokenGroupMapper;
    private final UsageLogMapper usageLogMapper;
    private final ApiKeyUtil apiKeyUtil;
    @Lazy
    private final RateLimitService rateLimitService;
    @Lazy
    private final BillingService billingService;

    @Override
    @Transactional
    public Token createToken(Long userId, String name, Long groupId,
                            String allowedModels, String allowedChannels,
                            Integer minuteLimit, Integer dayLimit,
                            Long quotaLimit, LocalDateTime expiresAt) {
        
        // 生成API Key
        String apiKey = apiKeyUtil.generateApiKey();
        
        Token token = Token.builder()
                .userId(userId)
                .name(name)
                .groupId(groupId)
                .apiKey(apiKey)
                .allowedModels(allowedModels)
                .allowedChannels(allowedChannels)
                .minuteLimit(minuteLimit != null ? minuteLimit : 0)
                .dayLimit(dayLimit != null ? dayLimit : 0)
                .quotaLimit(quotaLimit != null ? quotaLimit : 0L)
                .remainingQuota(quotaLimit != null ? quotaLimit : -1L)
                .expiresAt(expiresAt)
                .status("active")
                .build();

        // 如果有分组，应用分组默认配置
        if (groupId != null) {
            TokenGroup group = tokenGroupMapper.selectById(groupId);
            if (group != null) {
                if (token.getMinuteLimit() == 0 && group.getMinuteLimit() > 0) {
                    token.setMinuteLimit(group.getMinuteLimit());
                }
                if (token.getDayLimit() == 0 && group.getDayLimit() > 0) {
                    token.setDayLimit(group.getDayLimit());
                }
                if (token.getAllowedModels() == null) {
                    token.setAllowedModels(group.getDefaultModels());
                }
                if (token.getAllowedChannels() == null) {
                    token.setAllowedChannels(group.getDefaultChannels());
                }
            }
        }

        tokenMapper.insert(token);
        log.info("创建令牌成功: userId={}, tokenId={}", userId, token.getId());
        
        return token;
    }

    @Override
    @Transactional
    public Token updateToken(Long tokenId, Long userId, String name, Long groupId,
                            String allowedModels, String allowedChannels,
                            Integer minuteLimit, Integer dayLimit,
                            Long quotaLimit, LocalDateTime expiresAt) {
        
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }

        if (name != null) {
            token.setName(name);
        }
        if (groupId != null) {
            token.setGroupId(groupId);
        }
        if (allowedModels != null) {
            token.setAllowedModels(allowedModels);
        }
        if (allowedChannels != null) {
            token.setAllowedChannels(allowedChannels);
        }
        if (minuteLimit != null) {
            token.setMinuteLimit(minuteLimit);
        }
        if (dayLimit != null) {
            token.setDayLimit(dayLimit);
        }
        if (quotaLimit != null) {
            token.setQuotaLimit(quotaLimit);
            // 更新剩余额度
            if (token.getRemainingQuota() < 0 || token.getRemainingQuota() > quotaLimit) {
                token.setRemainingQuota(quotaLimit);
            }
        }
        if (expiresAt != null) {
            token.setExpiresAt(expiresAt);
        }

        updateById(token);
        log.info("更新令牌: tokenId={}", tokenId);
        
        return token;
    }

    @Override
    @Transactional
    public void deleteToken(Long tokenId, Long userId) {
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }
        removeById(tokenId);
        log.info("删除令牌: tokenId={}", tokenId);
    }

    @Override
    @Transactional
    public void enableToken(Long tokenId, Long userId) {
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }
        token.setStatus("active");
        updateById(token);
        log.info("启用令牌: tokenId={}", tokenId);
    }

    @Override
    @Transactional
    public void disableToken(Long tokenId, Long userId) {
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }
        token.setStatus("disabled");
        updateById(token);
        log.info("禁用令牌: tokenId={}", tokenId);
    }

    @Override
    public Token getByApiKey(String apiKey) {
        return tokenMapper.selectByApiKey(apiKey);
    }

    @Override
    public Token validateToken(String apiKey) {
        Token token = getByApiKey(apiKey);
        
        if (token == null) {
            return null;
        }
        
        // 检查状态
        if (!"active".equals(token.getStatus())) {
            throw BizException.tokenDisabled();
        }
        
        // 检查过期
        if (token.isExpired()) {
            token.setStatus("expired");
            updateById(token);
            throw BizException.operationNotAllowed("令牌已过期");
        }
        
        // 检查额度
        if (token.hasQuotaLimit() && !token.hasAvailableQuota()) {
            throw BizException.insufficientBalance();
        }
        
        return token;
    }

    @Override
    public boolean checkRateLimit(Token token) {
        // 检查分钟限制
        if (token.getMinuteLimit() > 0) {
            Long minuteCount = rateLimitService.getMinuteRequestCount(token);
            if (minuteCount >= token.getMinuteLimit()) {
                return false;
            }
        }
        
        // 检查日限制
        if (token.getDayLimit() > 0) {
            Long dayCount = rateLimitService.getDayRequestCount(token);
            if (dayCount >= token.getDayLimit()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public PageResult<Token> getUserTokens(Long userId, int page, int size) {
        Page<Token> pageParam = new Page<>(page, size);
        IPage<Token> pageResult = tokenMapper.selectByUserId(pageParam, userId);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size);
    }

    @Override
    public Map<String, Object> getTokenUsage(Long tokenId, Long userId) {
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }

        Map<String, Object> usage = new HashMap<>();
        usage.put("tokenId", token.getId());
        usage.put("usedQuota", token.getUsedQuota());
        usage.put("remainingQuota", token.getRemainingQuota());
        usage.put("quotaLimit", token.getQuotaLimit());

        // 获取今日使用
        Long todayCalls = rateLimitService.getDayRequestCount(token);
        usage.put("todayCalls", todayCalls);

        // 获取令牌使用记录
        LambdaQueryWrapper<UsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageLog::getTokenId, tokenId)
                .orderByDesc(UsageLog::getCreatedAt)
                .last("LIMIT 10");
        List<UsageLog> recentLogs = usageLogMapper.selectList(wrapper);
        usage.put("recentLogs", recentLogs);

        return usage;
    }

    @Override
    @Transactional
    public void updateTokenQuota(Long tokenId, Long amount) {
        Token token = getById(tokenId);
        if (token != null) {
            token.setUsedQuota(token.getUsedQuota() + amount);
            if (token.getRemainingQuota() > 0) {
                token.setRemainingQuota(Math.max(0, token.getRemainingQuota() - amount));
            }
            updateById(token);
        }
    }

    @Override
    @Transactional
    public void updateExpiredTokens() {
        LambdaQueryWrapper<Token> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Token::getStatus, "active")
                .isNotNull(Token::getExpiresAt)
                .lt(Token::getExpiresAt, LocalDateTime.now());
        
        List<Token> expiredTokens = list(wrapper);
        expiredTokens.forEach(token -> {
            token.setStatus("expired");
            updateById(token);
            log.info("令牌过期: tokenId={}", token.getId());
        });
    }

    @Override
    public List<Token> getExpiringTokens(Long userId, int days) {
        return tokenMapper.selectExpiringSoon(userId, days);
    }

    @Override
    @Transactional
    public Token renewToken(Long tokenId, Long userId) {
        Token token = getById(tokenId);
        if (token == null || !token.getUserId().equals(userId)) {
            throw BizException.tokenNotFound();
        }

        // 计算新的过期时间
        LocalDateTime newExpiresAt;
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            newExpiresAt = LocalDateTime.now().plusDays(token.getRenewDays());
        } else {
            newExpiresAt = token.getExpiresAt().plusDays(token.getRenewDays());
        }

        token.setExpiresAt(newExpiresAt);
        token.setStatus("active");
        updateById(token);
        
        log.info("续期令牌: tokenId={}, newExpiresAt={}", tokenId, newExpiresAt);
        return token;
    }
}
