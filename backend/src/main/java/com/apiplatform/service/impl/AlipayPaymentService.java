package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付实现（简化版）
 * 注意：完整功能需要安装支付宝SDK依赖
 *
 * @author API Platform Team
 */
@Slf4j
@Service("alipayPaymentService")
public class AlipayPaymentService implements PaymentService {

    @Value("${payment.alipay.app-id:}")
    private String appId;

    @Value("${payment.alipay.enabled:false}")
    private boolean enabled;

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("支付宝支付暂不可用");
        }
        throw new BizException("支付宝SDK未安装，请安装 alipay-sdk-java 依赖后使用");
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        log.warn("支付宝SDK未安装，无法查询订单状态");
        return STATUS_PENDING;
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        log.warn("支付宝SDK未安装，跳过签名验证");
        return false;
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", params.get("out_trade_no"));
        result.put("transactionId", params.get("trade_no"));
        result.put("amount", params.get("total_amount"));
        result.put("channel", CHANNEL_ALIPAY);
        result.put("status", STATUS_PENDING);
        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "支付宝SDK未安装，无法执行退款");
        return result;
    }

    @Override
    public String getRefundStatus(String refundNo) {
        return STATUS_PROCESSING;
    }

    @Override
    public String getChannelIcon(String channel) {
        return "/assets/payment/alipay.svg";
    }

    @Override
    public String getChannelName(String channel) {
        return "支付宝";
    }

    @Override
    public boolean isChannelEnabled(String channel) {
        return enabled && appId != null && !appId.isEmpty();
    }
}