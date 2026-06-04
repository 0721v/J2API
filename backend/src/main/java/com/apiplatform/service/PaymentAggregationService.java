package com.apiplatform.service;

import com.apiplatform.service.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 支付聚合服务
 * 统一管理多种支付渠道
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAggregationService {

    private final AlipayPaymentService alipayPaymentService;
    private final WechatPaymentService wechatPaymentService;
    private final OkxPaymentService okxPaymentService;
    private final StripePaymentService stripePaymentService;
    private final CreemPaymentService creemPaymentService;

    /**
     * 获取所有启用的支付渠道
     */
    public List<Map<String, Object>> getEnabledChannels() {
        List<Map<String, Object>> channels = new ArrayList<>();

        if (alipayPaymentService.isChannelEnabled(PaymentService.CHANNEL_ALIPAY)) {
            channels.add(createChannelInfo(
                    PaymentService.CHANNEL_ALIPAY,
                    alipayPaymentService.getChannelName(PaymentService.CHANNEL_ALIPAY),
                    alipayPaymentService.getChannelIcon(PaymentService.CHANNEL_ALIPAY),
                    "scan",  // 支付方式
                    0.0     // 优惠比例
            ));
        }

        if (wechatPaymentService.isChannelEnabled(PaymentService.CHANNEL_WECHAT)) {
            channels.add(createChannelInfo(
                    PaymentService.CHANNEL_WECHAT,
                    wechatPaymentService.getChannelName(PaymentService.CHANNEL_WECHAT),
                    wechatPaymentService.getChannelIcon(PaymentService.CHANNEL_WECHAT),
                    "scan",
                    0.0
            ));
        }

        if (okxPaymentService.isChannelEnabled(PaymentService.CHANNEL_OKX)) {
            Map<String, Object> channel = createChannelInfo(
                    PaymentService.CHANNEL_OKX,
                    okxPaymentService.getChannelName(PaymentService.CHANNEL_OKX),
                    okxPaymentService.getChannelIcon(PaymentService.CHANNEL_OKX),
                    "crypto",
                    5.0  // 虚拟币5%优惠
            );
            // 添加支持的币种信息
            channel.put("supportedCoins", okxPaymentService.getSupportedCoins());
            channels.add(channel);
        }

        if (stripePaymentService.isChannelEnabled(PaymentService.CHANNEL_STRIPE)) {
            channels.add(createChannelInfo(
                    PaymentService.CHANNEL_STRIPE,
                    stripePaymentService.getChannelName(PaymentService.CHANNEL_STRIPE),
                    stripePaymentService.getChannelIcon(PaymentService.CHANNEL_STRIPE),
                    "card",
                    0.0
            ));
        }

        if (creemPaymentService.isChannelEnabled(PaymentService.CHANNEL_CREEM)) {
            Map<String, Object> channel = createChannelInfo(
                    PaymentService.CHANNEL_CREEM,
                    creemPaymentService.getChannelName(PaymentService.CHANNEL_CREEM),
                    creemPaymentService.getChannelIcon(PaymentService.CHANNEL_CREEM),
                    "card",
                    0.0
            );
            // 添加支持的支付方式
            channel.put("supportedMethods", creemPaymentService.getSupportedPaymentMethods());
            channels.add(channel);
        }

        return channels;
    }

    /**
     * 创建渠道信息
     */
    private Map<String, Object> createChannelInfo(String code, String name, String icon, String type, double discount) {
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("code", code);
        channel.put("name", name);
        channel.put("icon", icon);
        channel.put("type", type);
        channel.put("discount", discount);
        channel.put("enabled", true);
        return channel;
    }

    /**
     * 获取支付渠道服务
     */
    public PaymentService getPaymentService(String channel) {
        return switch (channel) {
            case PaymentService.CHANNEL_ALIPAY -> alipayPaymentService;
            case PaymentService.CHANNEL_WECHAT -> wechatPaymentService;
            case PaymentService.CHANNEL_OKX -> okxPaymentService;
            case PaymentService.CHANNEL_STRIPE -> stripePaymentService;
            case PaymentService.CHANNEL_CREEM -> creemPaymentService;
            default -> throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        };
    }

    /**
     * 创建支付订单
     */
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        PaymentService service = getPaymentService(channel);
        return service.createPayment(userId, orderNo, amount, channel);
    }

    /**
     * 获取支付状态
     */
    public String getPaymentStatus(String orderNo, String channel) {
        PaymentService service = getPaymentService(channel);
        return service.getPaymentStatus(orderNo);
    }

    /**
     * 验证支付回调
     */
    public boolean verifyCallback(String channel, Map<String, String> params) {
        try {
            PaymentService service = getPaymentService(channel);
            return service.verifyCallback(channel, params);
        } catch (Exception e) {
            log.error("验证支付回调失败: channel={}, error={}", channel, e.getMessage());
            return false;
        }
    }

    /**
     * 处理支付回调
     */
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        try {
            PaymentService service = getPaymentService(channel);
            return service.handleCallback(channel, params);
        } catch (Exception e) {
            log.error("处理支付回调失败: channel={}, error={}", channel, e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("status", PaymentService.STATUS_FAILED);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 申请退款
     */
    public Map<String, Object> refund(String orderNo, String channel, String reason) {
        try {
            PaymentService service = getPaymentService(channel);
            return service.refund(orderNo, reason);
        } catch (Exception e) {
            log.error("申请退款失败: channel={}, error={}", channel, e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
