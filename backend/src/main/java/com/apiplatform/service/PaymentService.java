package com.apiplatform.service;

import java.util.Map;

/**
 * 支付服务接口
 * 支持多种支付渠道：支付宝、微信、欧易(OKX)、Stripe、Creem
 *
 * @author API Platform Team
 */
public interface PaymentService {

    // ==================== 支付渠道类型 ====================
    String CHANNEL_ALIPAY = "alipay";
    String CHANNEL_WECHAT = "wechat";
    String CHANNEL_OKX = "okx";
    String CHANNEL_STRIPE = "stripe";
    String CHANNEL_CREEM = "creem";

    // ==================== 支付状态 ====================
    String STATUS_PENDING = "pending";
    String STATUS_PROCESSING = "processing";
    String STATUS_PAID = "paid";
    String STATUS_FAILED = "failed";
    String STATUS_REFUNDED = "refunded";
    String STATUS_EXPIRED = "expired";

    /**
     * 创建支付订单
     *
     * @param userId 用户ID
     * @param orderNo 订单号
     * @param amount 支付金额
     * @param channel 支付渠道
     * @return 支付参数（包含支付链接/二维码等）
     */
    Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel);

    /**
     * 获取支付状态
     *
     * @param orderNo 订单号
     * @return 支付状态
     */
    String getPaymentStatus(String orderNo);

    /**
     * 验证支付回调签名
     *
     * @param channel 支付渠道
     * @param params 回调参数
     * @return 是否验证通过
     */
    boolean verifyCallback(String channel, Map<String, String> params);

    /**
     * 处理支付回调
     *
     * @param channel 支付渠道
     * @param params 回调参数
     * @return 处理结果
     */
    Map<String, Object> handleCallback(String channel, Map<String, String> params);

    /**
     * 申请退款
     *
     * @param orderNo 订单号
     * @param reason 退款原因
     * @return 退款结果
     */
    Map<String, Object> refund(String orderNo, String reason);

    /**
     * 查询退款状态
     *
     * @param refundNo 退款单号
     * @return 退款状态
     */
    String getRefundStatus(String refundNo);

    /**
     * 获取渠道图标
     *
     * @param channel 渠道
     * @return 图标路径
     */
    String getChannelIcon(String channel);

    /**
     * 获取渠道名称
     *
     * @param channel 渠道
     * @return 渠道显示名称
     */
    String getChannelName(String channel);

    /**
     * 检查渠道是否启用
     *
     * @param channel 渠道
     * @return 是否启用
     */
    boolean isChannelEnabled(String channel);
}
