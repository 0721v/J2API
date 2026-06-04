package com.apiplatform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.apiplatform.common.BizException;
import com.apiplatform.service.PaymentService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 支付宝支付实现
 * 支持：当面付、手机网站支付、电脑网站支付
 *
 * @author API Platform Team
 */
@Slf4j
@Service("alipayPaymentService")
public class AlipayPaymentService implements PaymentService {

    @Value("${payment.alipay.app-id:}")
    private String appId;

    @Value("${payment.alipay.private-key:}")
    private String privateKey;

    @Value("${payment.alipay.alipay-public-key:}")
    private String alipayPublicKey;

    @Value("${payment.alipay.gateway:https://openapi-sandbox.dl.alipaydev.com/gateway.do}")
    private String gateway;

    @Value("${payment.alipay.notify-url:}")
    private String notifyUrl;

    @Value("${payment.alipay.enabled:false}")
    private boolean enabled;

    @Value("${payment.alipay.mode:sandbox}")
    private String mode;

    private AlipayClient alipayClient;

    /**
     * 获取AlipayClient单例
     */
    private AlipayClient getClient() {
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(
                    gateway,
                    appId,
                    privateKey,
                    "json",
                    "UTF-8",
                    alipayPublicKey,
                    "RSA2"
            );
        }
        return alipayClient;
    }

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("支付宝支付暂不可用");
        }

        try {
            // 根据金额判断使用哪种支付方式
            // 小金额使用当面付扫码，大金额使用电脑网站支付
            if (amount < 100) {
                return createQrCodePayment(orderNo, amount);
            } else {
                return createWebPayment(orderNo, amount);
            }
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付失败: {}", e.getMessage());
            throw new BizException("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 当面付（扫码支付）
     */
    private Map<String, Object> createQrCodePayment(String orderNo, double amount) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent(new JSONObject()
                .fluentPut("out_trade_no", orderNo)
                .fluentPut("total_amount", String.format("%.2f", amount))
                .fluentPut("subject", "API Platform 充值")
                .fluentPut("store_id", "API_PLATFORM")
                .fluentPut("timeout_express", "30m")
                .toJSONString());

        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }

        AlipayTradePrecreateResponse response = getClient().execute(request);

        Map<String, Object> result = new HashMap<>();
        if (response.isSuccess()) {
            result.put("success", true);
            result.put("qrCode", response.getQrCode());
            result.put("paymentUrl", "alipays://platformapi=pay?appId=" + appId + "&qrCode=" + response.getQrCode());
            result.put("channel", CHANNEL_ALIPAY);
            log.info("支付宝扫码支付创建成功: orderNo={}, qrCode={}", orderNo, response.getQrCode());
        } else {
            result.put("success", false);
            result.put("error", response.getSubMsg());
            log.error("支付宝扫码支付创建失败: {}", response.getSubMsg());
        }

        return result;
    }

    /**
     * 电脑网站支付
     */
    private Map<String, Object> createWebPayment(String orderNo, double amount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizContent(new JSONObject()
                .fluentPut("out_trade_no", orderNo)
                .fluentPut("total_amount", String.format("%.2f", amount))
                .fluentPut("subject", "API Platform 充值")
                .fluentPut("product_code", "FAST_INSTANT_TRADE_PAY")
                .fluentPut("timeout_express", "2h")
                .toJSONString());

        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }
        request.setReturnUrl(notifyUrl != null ? notifyUrl.replace("/notify", "/return") : null);

        String form = getClient().pageExecute(request).getBody();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("paymentForm", form);
        result.put("paymentUrl", gateway + "?biz_content=" + orderNo);
        result.put("channel", CHANNEL_ALIPAY);

        return result;
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent(new JSONObject()
                    .fluentPut("out_trade_no", orderNo)
                    .toJSONString());

            AlipayTradeQueryResponse response = getClient().execute(request);

            if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                return STATUS_PAID;
            } else if ("TRADE_CLOSED".equals(response.getTradeStatus())) {
                return STATUS_EXPIRED;
            } else if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())) {
                return STATUS_PENDING;
            }
            return STATUS_PENDING;
        } catch (AlipayApiException e) {
            log.error("查询支付宝订单状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        if (!CHANNEL_ALIPAY.equals(channel)) {
            return false;
        }

        try {
            // 验证签名
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayPublicKey,
                    "UTF-8",
                    "RSA2"
            );
        } catch (AlipayApiException e) {
            log.error("支付宝回调签名验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        String tradeStatus = params.get("trade_status");
        String orderNo = params.get("out_trade_no");
        String transactionId = params.get("trade_no");
        String amount = params.get("total_amount");

        result.put("orderNo", orderNo);
        result.put("transactionId", transactionId);
        result.put("amount", amount);
        result.put("channel", CHANNEL_ALIPAY);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            result.put("status", STATUS_PAID);
            result.put("paidTime", params.get("gmt_payment"));
            log.info("支付宝支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
        } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
            result.put("status", STATUS_PENDING);
        } else {
            result.put("status", STATUS_FAILED);
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent(new JSONObject()
                    .fluentPut("out_trade_no", orderNo)
                    .fluentPut("refund_reason", reason != null ? reason : "用户申请退款")
                    .fluentPut("refund_amount", "0.01") // 实际应从订单获取
                    .fluentPut("out_request_no", UUID.randomUUID().toString())
                    .toJSONString());

            AlipayTradeRefundResponse response = getClient().execute(request);

            Map<String, Object> result = new HashMap<>();
            if (response.isSuccess()) {
                result.put("success", true);
                result.put("refundNo", response.getTradeNo());
                result.put("refundAmount", response.getRefundFee());
            } else {
                result.put("success", false);
                result.put("error", response.getSubMsg());
            }

            return result;
        } catch (AlipayApiException e) {
            log.error("支付宝退款失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public String getRefundStatus(String refundNo) {
        // 支付宝退款查询实现
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
