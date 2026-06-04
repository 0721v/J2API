package com.apiplatform.service;

import com.apiplatform.entity.Token;
import com.apiplatform.entity.Model;
import com.apiplatform.entity.Channel;
import java.util.Map;

/**
 * 计费服务接口
 *
 * @author API Platform Team
 */
public interface BillingService {

    /**
     * 计算请求费用
     */
    Long calculateRequestCost(Token token, Model model, Channel channel,
                               Integer inputTokens, Integer outputTokens,
                               Boolean cacheHit);

    /**
     * 执行计费
     */
    boolean charge(Token token, Long userId, Long amount, String description);

    /**
     * 扣除额度
     */
    boolean deductQuota(Long tokenId, Long amount);

    /**
     * 返还费用（失败时）
     */
    void refundCharge(Long userId, Long amount, String reason);

    /**
     * 获取缓存计费比例
     */
    Double getCacheBillingRate();

    /**
     * 是否启用缓存计费
     */
    boolean isCacheBillingEnabled();

    /**
     * 计算按次计费
     */
    Long calculatePerCallBilling(Model model);

    /**
     * 计算Token计费
     */
    Long calculateTokenBilling(Model model, Channel channel, 
                               Integer inputTokens, Integer outputTokens);

    /**
     * 获取用户当前余额
     */
    Long getUserBalance(Long userId);

    /**
     * 验证余额是否足够
     */
    boolean hasEnoughBalance(Long userId, Long requiredAmount);

    /**
     * 获取用户今日消费
     */
    Long getUserTodayConsumption(Long userId);

    /**
     * 获取用户本月消费
     */
    Long getUserMonthConsumption(Long userId);
}
