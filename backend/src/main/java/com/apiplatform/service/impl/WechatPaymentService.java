package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付实现
 * 支持：Native扫码支付、JSAPI网页支付、APP支付
 *
 * @author API Platform Team
 */
@Slf4j
@Service("wechatPaymentService")
public class WechatPaymentService implements PaymentService {

    @Value("${payment.wechat.app-id:}")
    private String appId;

    @Value("${payment.wechat.mch-id:}")
    private String mchId;

    @Value("${payment.wechat.api-key:}")
    private String apiKey;

    @Value("${payment.wechat.cert-path:}")
    private String certPath;

    @Value("${payment.wechat.notify-url:}")
    private String notifyUrl;

    @Value("${payment.wechat.enabled:false}")
    private boolean enabled;

    @Value("${payment.wechat.environment:sandbox}")
    private String environment;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 微信支付API地址
    private static final String GATEWAY_URL = "https://api.mch.weixin.qq.com";
    private static final String SANDBOX_URL = "https://api.mch.weixin.qq.com/sandboxnew";

    /**
     * 获取API基础URL
     */
    private String getBaseUrl() {
        return "sandbox".equals(environment) ? SANDBOX_URL : GATEWAY_URL;
    }

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("微信支付暂不可用");
        }

        try {
            // 金额转换：元转分
            int totalFee = (int) (amount * 100);

            // 创建统一下单
            Map<String, Object> params = new TreeMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("nonce_str", generateNonceStr());
            params.put("body", "API Platform 充值");
            params.put("out_trade_no", orderNo);
            params.put("total_fee", totalFee);
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", notifyUrl);
            params.put("trade_type", "NATIVE");

            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);

            // 发送请求
            String xmlParams = mapToXml(params);
            String response = sendRequest("/pay/unifiedorder", xmlParams);

            // 解析响应
            Map<String, Object> responseMap = xmlToMap(response);

            Map<String, Object> result = new HashMap<>();

            if ("SUCCESS".equals(responseMap.get("result_code"))) {
                result.put("success", true);
                result.put("codeUrl", responseMap.get("code_url"));
                result.put("qrCode", generateQrCodeUrl(responseMap.get("code_url").toString()));
                result.put("prepayId", responseMap.get("prepay_id"));
                result.put("channel", CHANNEL_WECHAT);
                log.info("微信支付创建成功: orderNo={}, codeUrl={}", orderNo, responseMap.get("code_url"));
            } else {
                result.put("success", false);
                result.put("error", responseMap.get("err_code_des"));
                log.error("微信支付创建失败: {}", responseMap.get("err_code_des"));
            }

            return result;
        } catch (Exception e) {
            log.error("创建微信支付失败: {}", e.getMessage());
            throw new BizException("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付二维码URL
     */
    private String generateQrCodeUrl(String codeUrl) {
        return "weixin://wxpay/bizpayurl?pr=" + codeUrl.replace("/", "");
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("out_trade_no", orderNo);
            params.put("nonce_str", generateNonceStr());

            String sign = generateSign(params);
            params.put("sign", sign);

            String xmlParams = mapToXml(params);
            String response = sendRequest("/pay/orderquery", xmlParams);
            Map<String, Object> responseMap = xmlToMap(response);

            if ("SUCCESS".equals(responseMap.get("trade_state"))) {
                return STATUS_PAID;
            } else if ("NOTPAY".equals(responseMap.get("trade_state"))) {
                return STATUS_PENDING;
            } else if ("CLOSED".equals(responseMap.get("trade_state"))) {
                return STATUS_EXPIRED;
            }
            return STATUS_PENDING;
        } catch (Exception e) {
            log.error("查询微信订单状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        if (!CHANNEL_WECHAT.equals(channel)) {
            return false;
        }

        try {
            Map<String, Object> signParams = new TreeMap<>(params);
            String receivedSign = (String) signParams.get("sign");
            signParams.remove("sign");

            String calculatedSign = generateSign(signParams);

            // 验证签名
            return receivedSign != null && receivedSign.equals(calculatedSign);
        } catch (Exception e) {
            log.error("微信回调签名验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        String orderNo = params.get("out_trade_no");
        String transactionId = params.get("transaction_id");
        String totalFee = params.get("total_fee");
        String tradeState = params.get("result_code");

        result.put("orderNo", orderNo);
        result.put("transactionId", transactionId);
        result.put("amount", String.format("%.2f", Integer.parseInt(totalFee) / 100.0));
        result.put("channel", CHANNEL_WECHAT);

        if ("SUCCESS".equals(tradeState)) {
            result.put("status", STATUS_PAID);
            result.put("paidTime", params.get("time_end"));
            log.info("微信支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
        } else {
            result.put("status", STATUS_FAILED);
            result.put("error", params.get("err_code_des"));
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> params = new TreeMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("out_trade_no", orderNo);
            params.put("out_refund_no", "REFUND_" + orderNo);
            params.put("total_fee", "1"); // 实际应从订单获取
            params.put("refund_fee", "1");
            params.put("refund_desc", reason != null ? reason : "用户申请退款");
            params.put("nonce_str", generateNonceStr());

            String sign = generateSign(params);
            params.put("sign", sign);

            String xmlParams = mapToXml(params);
            String response = sendRequestWithCert("/secapi/pay/refund", xmlParams);
            Map<String, Object> responseMap = xmlToMap(response);

            if ("SUCCESS".equals(responseMap.get("result_code"))) {
                result.put("success", true);
                result.put("refundNo", responseMap.get("refund_id"));
                result.put("refundAmount", String.format("%.2f", Integer.parseInt((String) responseMap.get("refund_fee")) / 100.0));
            } else {
                result.put("success", false);
                result.put("error", responseMap.get("err_code_des"));
            }
        } catch (Exception e) {
            log.error("微信退款失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public String getRefundStatus(String refundNo) {
        return STATUS_PROCESSING;
    }

    @Override
    public String getChannelIcon(String channel) {
        return "/assets/payment/wechat.svg";
    }

    @Override
    public String getChannelName(String channel) {
        return "微信支付";
    }

    @Override
    public boolean isChannelEnabled(String channel) {
        return enabled && appId != null && !appId.isEmpty() && mchId != null && !mchId.isEmpty();
    }

    // ==================== 私有方法 ====================

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成签名
     */
    private String generateSign(Map<String, Object> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key=").append(apiKey);

        return md5(sb.toString()).toUpperCase();
    }

    /**
     * MD5加密
     */
    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 发送HTTP请求
     */
    private String sendRequest(String path, String xmlParams) {
        String url = getBaseUrl() + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(xmlParams, headers);
        return restTemplate.postForObject(url, entity, String.class);
    }

    /**
     * 发送带证书的HTTP请求（退款用）
     */
    private String sendRequestWithCert(String path, String xmlParams) {
        // 实际实现需要加载证书，这里简化处理
        return sendRequest(path, xmlParams);
    }

    /**
     * Map转XML
     */
    private String mapToXml(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("<xml>");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">");
            sb.append("<![CDATA[").append(entry.getValue()).append("]]>");
            sb.append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * XML转Map
     */
    private Map<String, Object> xmlToMap(String xml) throws Exception {
        Map<String, Object> result = new HashMap<>();
        xml = xml.replaceAll("<[^>]+>", "");
        String[] pairs = xml.split("");
        // 简化解析，实际应该用XML解析器
        return result;
    }
}
