package com.apiplatform.service.impl;

import com.apiplatform.entity.*;
import com.apiplatform.mapper.TransactionMapper;
import com.apiplatform.mapper.UserMapper;
import com.apiplatform.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 计费服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final UserService userService;
    private final TokenService tokenService;
    private final ModelService modelService;
    private final ChannelService channelService;
    private final TransactionMapper transactionMapper;

    @Value("${system.billing.cache-hit-rate:0.5}")
    private double cacheHitRate;

    @Value("${system.billing.enable-cache-billing:true}")
    private boolean enableCacheBilling;

    @Override
    public Long calculateRequestCost(Token token, Model model, Channel channel,
                                     Integer inputTokens, Integer outputTokens,
                                     Boolean cacheHit) {
        if (model == null) {
            return 0L;
        }

        long cost = 0;

        if (model.isPerCallBilling()) {
            // 按次计费
            cost = model.getPricePerCall().multiply(BigDecimal.valueOf(100)).longValue();
        } else if (model.isTokenBilling()) {
            // 按Token计费
            if (inputTokens != null && inputTokens > 0) {
                cost += model.getInputPrice()
                        .multiply(BigDecimal.valueOf(inputTokens))
                        .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();
            }

            if (outputTokens != null && outputTokens > 0) {
                cost += model.getOutputPrice()
                        .multiply(BigDecimal.valueOf(outputTokens))
                        .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();
            }

            // 缓存命中折扣
            if (Boolean.TRUE.equals(cacheHit) && enableCacheBilling) {
                cost = (long) (cost * cacheHitRate);
            }
        }

        return cost;
    }

    @Override
    public boolean charge(Token token, Long userId, Long amount, String description) {
        if (amount == null || amount <= 0) {
            return true; // 免费请求
        }

        // 先尝试扣除额度
        if (token.hasQuotaLimit()) {
            if (!token.hasAvailableQuota()) {
                log.warn("令牌额度不足: tokenId={}", token.getId());
                return false;
            }
            tokenService.updateTokenQuota(token.getId(), amount);
        }

        // 再扣除余额
        if (!userService.deductBalance(userId, amount)) {
            // 余额不足，返还额度
            if (token.hasQuotaLimit()) {
                tokenService.updateTokenQuota(token.getId(), -amount);
            }
            log.warn("用户余额不足: userId={}, amount={}", userId, amount);
            return false;
        }

        log.debug("计费成功: userId={}, tokenId={}, amount={}, desc={}", 
                userId, token.getId(), amount, description);
        return true;
    }

    @Override
    public boolean deductQuota(Long tokenId, Long amount) {
        Token token = tokenService.getById(tokenId);
        if (token == null) {
            return false;
        }

        if (token.hasQuotaLimit() && !token.hasAvailableQuota()) {
            return false;
        }

        tokenService.updateTokenQuota(tokenId, amount);
        return true;
    }

    @Override
    public void refundCharge(Long userId, Long amount, String reason) {
        userService.addBalance(userId, amount);
        log.info("退还费用: userId={}, amount={}, reason={}", userId, amount, reason);
    }

    @Override
    public Double getCacheBillingRate() {
        return cacheHitRate;
    }

    @Override
    public boolean isCacheBillingEnabled() {
        return enableCacheBilling;
    }

    @Override
    public Long calculatePerCallBilling(Model model) {
        if (model == null || !model.isPerCallBilling()) {
            return 0L;
        }
        return model.getPricePerCall().multiply(BigDecimal.valueOf(100)).longValue();
    }

    @Override
    public Long calculateTokenBilling(Model model, Channel channel,
                                     Integer inputTokens, Integer outputTokens) {
        if (model == null || !model.isTokenBilling()) {
            return 0L;
        }

        long cost = 0;

        if (inputTokens != null && inputTokens > 0) {
            cost += model.getInputPrice()
                    .multiply(BigDecimal.valueOf(inputTokens))
                    .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();
        }

        if (outputTokens != null && outputTokens > 0) {
            cost += model.getOutputPrice()
                    .multiply(BigDecimal.valueOf(outputTokens))
                    .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();
        }

        return cost;
    }

    @Override
    public Long getUserBalance(Long userId) {
        return userService.getBalance(userId);
    }

    @Override
    public boolean hasEnoughBalance(Long userId, Long requiredAmount) {
        Long balance = getUserBalance(userId);
        return balance != null && balance >= requiredAmount;
    }

    @Override
    public Long getUserTodayConsumption(Long userId) {
        // TODO: 从usage_logs统计今日消费
        return 0L;
    }

    @Override
    public Long getUserMonthConsumption(Long userId) {
        // TODO: 从usage_logs统计本月消费
        return 0L;
    }
}
