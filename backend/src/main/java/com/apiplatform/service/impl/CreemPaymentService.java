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
import java.time.Instant;
import java.util.*;

/**
 * Creem支付实现
 * Creem是一个面向创作者和SaaS的支付平台，支持全球支付
 *
 * @author API Platform Team
 */
@Slf4j
@Service("creemPaymentService")
public class CreemPaymentService implements PaymentService {

    @Value("${payment.creem.api-key:}")
    private String apiKey;

    @Value("${payment.creem.endpoint:https://api.creem.io}")
    private String endpoint;

    @Value("${payment.creem.enabled:false}")
    private boolean enabled;

    @Value("${payment.creem.currency:usd}")
    private String currency;

    @Value("${payment.creem.success-url:}")
    private String successUrl;

    @Value("${payment.creem.cancel-url:}")
    private String cancelUrl;

    @Value("${payment.creem.webhook-secret:}")
    private String webhookSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("Creem支付暂不可用");
        }

        try {
            // 金额转换
            long amountInCents = Math.round(amount * 100);

            // 构建支付链接请求
            String url = endpoint + "/v1/products/payment-link";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("product_id", getProductId(amount)); // 根据金额选择产品
            requestBody.put("customizations", Map.of(
                    "name", "API Platform 充值",
                    "description", "充值 ¥" + String.format("%.2f", amount)
            ));
            requestBody.put("metadata", Map.of(
                    "orderNo", orderNo,
                    "userId", String.valueOf(userId)
            ));

            // 设置回跳URL
            if (successUrl != null) {
                requestBody.put("success_url", successUrl + "?order=" + orderNo);
            }
            if (cancelUrl != null) {
                requestBody.put("cancel_url", cancelUrl);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            Map<String, Object> result = new HashMap<>();

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode data = json.get("data");

                if (data != null) {
                    result.put("success", true);
                    result.put("paymentUrl", data.get("url").asText());
                    result.put("checkoutId", data.get("id").asText());
                    result.put("channel", CHANNEL_CREEM);
                    result.put("amount", amount);
                    result.put("currency", currency.toUpperCase());

                    log.info("Creem支付创建成功: orderNo={}, checkoutId={}", orderNo, data.get("id").asText());
                } else {
                    result.put("success", false);
                    result.put("error", "无效的响应");
                }
            } else {
                JsonNode json = objectMapper.readTree(response.getBody());
                result.put("success", false);
                result.put("error", json.has("message") ? json.get("message").asText() : "支付创建失败");
                log.error("Creem支付创建失败: {}", response.getBody());
            }

            return result;

        } catch (Exception e) {
            log.error("创建Creem支付失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 根据金额获取产品ID（需要在Creem后台配置对应产品）
     */
    private String getProductId(double amount) {
        // 实际应该从数据库或缓存获取产品映射
        // 这里简化处理，返回默认产品
        return "prod_default";
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        try {
            String url = endpoint + "/v1/orders?metadata[orderNo]=" + orderNo;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode orders = json.get("data");

                if (orders != null && orders.isArray() && orders.size() > 0) {
                    String status = orders.get(0).get("status").asText();
                    return mapCreemStatus(status);
                }
            }

            return STATUS_PENDING;
        } catch (Exception e) {
            log.error("查询Creem订单状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    /**
     * 根据Checkout ID查询状态
     */
    public String getPaymentStatusByCheckoutId(String checkoutId) {
        try {
            String url = endpoint + "/v1/checkouts/" + checkoutId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String status = json.get("status").asText();
                return mapCreemStatus(status);
            }

            return STATUS_PENDING;
        } catch (Exception e) {
            log.error("查询Creem Checkout状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    /**
     * 映射Creem状态到统一状态
     */
    private String mapCreemStatus(String creemStatus) {
        return switch (creemStatus.toLowerCase()) {
            case "completed", "paid", "success" -> STATUS_PAID;
            case "pending", "processing", "awaiting" -> STATUS_PENDING;
            case "failed", "declined", "error" -> STATUS_FAILED;
            case "refunded", "cancelled" -> STATUS_REFUNDED;
            default -> STATUS_PENDING;
        };
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        if (!CHANNEL_CREEM.equals(channel)) {
            return false;
        }

        // 验证Creem webhook签名
        try {
            String signature = params.get("signature");
            String payload = params.get("payload");

            if (signature == null || webhookSecret == null) {
                return false;
            }

            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(keySpec);
            byte[] hash = sha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return signature.equals(hexString.toString());
        } catch (Exception e) {
            log.error("Creem回调签名验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 解析webhook事件
            String eventType = params.get("event");
            JsonNode eventData = objectMapper.readTree(params.get("data"));

            String orderNo = eventData.has("metadata") && eventData.get("metadata").has("orderNo")
                    ? eventData.get("metadata").get("orderNo").asText()
                    : null;
            String transactionId = eventData.has("id") ? eventData.get("id").asText() : null;
            String status = eventData.has("status") ? eventData.get("status").asText() : null;

            result.put("orderNo", orderNo);
            result.put("transactionId", transactionId);
            result.put("channel", CHANNEL_CREEM);
            result.put("eventType", eventType);
            result.put("rawStatus", status);
            result.put("status", mapCreemStatus(status));

            if ("order.completed".equals(eventType) || "payment.completed".equals(eventType)) {
                result.put("status", STATUS_PAID);
                result.put("paidTime", Instant.now().toString());
                log.info("Creem支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
            } else if ("payment.failed".equals(eventType)) {
                result.put("status", STATUS_FAILED);
                result.put("error", eventData.has("error") ? eventData.get("error").asText() : "Payment failed");
            }

        } catch (Exception e) {
            log.error("处理Creem回调失败: {}", e.getMessage());
            result.put("status", STATUS_FAILED);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 处理Creem Webhook
     */
    public Map<String, Object> handleWebhook(String payload, String signature) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证签名
            Map<String, String> params = new HashMap<>();
            params.put("signature", signature);
            params.put("payload", payload);

            if (!verifyCallback(CHANNEL_CREEM, params)) {
                result.put("status", STATUS_FAILED);
                result.put("error", "Invalid signature");
                return result;
            }

            JsonNode json = objectMapper.readTree(payload);
            String eventType = json.has("event") ? json.get("event").asText() : null;
            JsonNode data = json.get("data");

            result.put("channel", CHANNEL_CREEM);
            result.put("eventType", eventType);

            if (data != null) {
                if (data.has("metadata") && data.get("metadata").has("orderNo")) {
                    result.put("orderNo", data.get("metadata").get("orderNo").asText());
                }
                result.put("transactionId", data.has("id") ? data.get("id").asText() : null);
                result.put("status", mapCreemStatus(data.has("status") ? data.get("status").asText() : ""));

                if ("order.completed".equals(eventType)) {
                    log.info("Creem Webhook支付成功: orderNo={}", result.get("orderNo"));
                }
            }

        } catch (Exception e) {
            log.error("处理Creem Webhook失败: {}", e.getMessage());
            result.put("status", STATUS_FAILED);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询原始订单
            String orderId = getOrderIdByOrderNo(orderNo);
            if (orderId == null) {
                result.put("success", false);
                result.put("error", "订单不存在");
                return result;
            }

            // 创建退款请求
            String url = endpoint + "/v1/orders/" + orderId + "/refund";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("reason", reason != null ? reason : "User requested refund");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode refundData = json.get("data");

                result.put("success", true);
                result.put("refundId", refundData.has("id") ? refundData.get("id").asText() : null);
                result.put("status", refundData.has("status") ? refundData.get("status").asText() : "pending");
            } else {
                JsonNode json = objectMapper.readTree(response.getBody());
                result.put("success", false);
                result.put("error", json.has("message") ? json.get("message").asText() : "退款失败");
            }

        } catch (Exception e) {
            log.error("Creem退款失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 根据订单号获取Creem订单ID
     */
    private String getOrderIdByOrderNo(String orderNo) {
        try {
            String url = endpoint + "/v1/orders?metadata[orderNo]=" + orderNo;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode orders = json.get("data");

                if (orders != null && orders.isArray() && orders.size() > 0) {
                    return orders.get(0).get("id").asText();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("查询Creem订单ID失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getRefundStatus(String refundId) {
        return STATUS_PROCESSING;
    }

    @Override
    public String getChannelIcon(String channel) {
        return "/assets/payment/creem.svg";
    }

    @Override
    public String getChannelName(String channel) {
        return "Creem (全球支付)";
    }

    @Override
    public boolean isChannelEnabled(String channel) {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    /**
     * 获取支持的支付方式
     */
    public List<String> getSupportedPaymentMethods() {
        return List.of(
                "card",      // 信用卡/借记卡
                "apple_pay", // Apple Pay
                "google_pay", // Google Pay
                "paypal"     // PayPal
        );
    }
}
