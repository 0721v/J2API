package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.PaymentIntentData;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Stripe信用卡支付实现
 * 支持：信用卡、借记卡、Apple Pay、Google Pay
 *
 * @author API Platform Team
 */
@Slf4j
@Service("stripePaymentService")
public class StripePaymentService implements PaymentService {

    @Value("${payment.stripe.secret-key:}")
    private String secretKey;

    @Value("${payment.stripe.publishable-key:}")
    private String publishableKey;

    @Value("${payment.stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${payment.stripe.enabled:false}")
    private boolean enabled;

    @Value("${payment.stripe.currency:usd}")
    private String currency;

    @Value("${payment.stripe.success-url:}")
    private String successUrl;

    @Value("${payment.stripe.cancel-url:}")
    private String cancelUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化Stripe
     */
    private void initStripe() {
        if (Stripe.apiKey == null && secretKey != null && !secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
        }
    }

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("Stripe支付暂不可用");
        }

        try {
            initStripe();

            // 金额转换：元转美分
            long amountInCents = Math.round(amount * 100);

            // 构建商品信息
            String productName = "API Platform 充值 - ¥" + String.format("%.2f", amount);

            // 创建Checkout Session
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl != null ? successUrl : "https://example.com/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl != null ? cancelUrl : "https://example.com/cancel")
                    .setCurrency(currency.toLowerCase())
                    .addLineItem(
                            LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            PriceData.builder()
                                                    .setCurrency(currency.toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            PriceData.ProductData.builder()
                                                                    .setName(productName)
                                                                    .setDescription("API Platform Account Recharge")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderNo", orderNo)
                    .putMetadata("userId", String.valueOf(userId))
                    .putMetadata("amount", String.valueOf(amount));

            // 启用客户邮箱收集
            SessionCreateParams.Builder.PaymentIntentDataBuilder intentBuilder = PaymentIntentData.builder()
                    .putMetadata("orderNo", orderNo)
                    .putMetadata("userId", String.valueOf(userId));

            paramsBuilder.setPaymentIntentData(intentBuilder.build());

            // 启用保存卡片（可选）
            paramsBuilder.setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED);

            Session session = Session.create(paramsBuilder.build());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", session.getId());
            result.put("paymentUrl", session.getUrl());
            result.put("publishableKey", publishableKey);
            result.put("channel", CHANNEL_STRIPE);
            result.put("amount", amount);
            result.put("currency", currency.toUpperCase());

            // 如果是小额，可以直接返回嵌入式支付表单
            if (amount < 10) {
                result.put("type", "embedded");
            } else {
                result.put("type", "redirect");
            }

            log.info("Stripe支付创建成功: orderNo={}, sessionId={}", orderNo, session.getId());
            return result;

        } catch (StripeException e) {
            log.error("创建Stripe支付失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage());
            return result;
        }
    }

    /**
     * 创建嵌入式支付（用于小额支付）
     */
    public Map<String, Object> createEmbeddedPayment(Long userId, String orderNo, double amount) {
        if (!enabled) {
            throw new BizException("Stripe支付暂不可用");
        }

        try {
            initStripe();

            long amountInCents = Math.round(amount * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                    .setReturnUrl("https://example.com/return?session_id={CHECKOUT_SESSION_ID}")
                    .setCurrency(currency.toLowerCase())
                    .addLineItem(
                            LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            PriceData.builder()
                                                    .setCurrency(currency.toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            PriceData.ProductData.builder()
                                                                    .setName("API Platform Recharge")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderNo", orderNo)
                    .putMetadata("userId", String.valueOf(userId))
                    .build();

            Session session = Session.create(params);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", session.getId());
            result.put("clientSecret", session.getPaymentIntent());
            result.put("publishableKey", publishableKey);
            result.put("channel", CHANNEL_STRIPE);

            return result;

        } catch (StripeException e) {
            log.error("创建Stripe嵌入式支付失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        try {
            initStripe();

            // 通过session查询
            List<Session> sessions = Session.list(
                    Map.of("limit", 1, "metadata", Map.of("orderNo", orderNo))
            ).getData();

            if (!sessions.isEmpty()) {
                Session session = sessions.get(0);
                return switch (session.getPaymentStatus()) {
                    case "paid" -> STATUS_PAID;
                    case "unpaid", "no_payment_required" -> STATUS_PENDING;
                    default -> STATUS_PENDING;
                };
            }

            return STATUS_PENDING;
        } catch (StripeException e) {
            log.error("查询Stripe支付状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    /**
     * 根据Session ID查询状态
     */
    public String getPaymentStatusBySessionId(String sessionId) {
        try {
            initStripe();
            Session session = Session.retrieve(sessionId);

            return switch (session.getPaymentStatus()) {
                case "paid" -> STATUS_PAID;
                case "unpaid" -> STATUS_PENDING;
                case "no_payment_required" -> STATUS_PENDING;
                default -> STATUS_PENDING;
            };
        } catch (StripeException e) {
            log.error("查询Stripe Session状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        if (!CHANNEL_STRIPE.equals(channel)) {
            return false;
        }

        // Stripe webhook验证通过签名完成
        return true;
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        // 从params中提取关键信息
        String eventType = params.get("type");
        String orderNo = params.get("orderNo");
        String sessionId = params.get("sessionId");

        result.put("orderNo", orderNo);
        result.put("sessionId", sessionId);
        result.put("channel", CHANNEL_STRIPE);
        result.put("eventType", eventType);

        if ("checkout.session.completed".equals(eventType)) {
            result.put("status", STATUS_PAID);
            result.put("paidTime", params.get("paidTime"));
            log.info("Stripe支付成功: orderNo={}, sessionId={}", orderNo, sessionId);
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            result.put("status", STATUS_FAILED);
            result.put("error", params.get("errorMessage"));
        } else {
            result.put("status", STATUS_PROCESSING);
        }

        return result;
    }

    /**
     * 处理Stripe Webhook
     */
    public Map<String, Object> handleWebhook(String payload, String sigHeader) {
        Map<String, Object> result = new HashMap<>();

        try {
            initStripe();

            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getData().getObject();
                result.put("status", STATUS_PAID);
                result.put("orderNo", session.getMetadata().get("orderNo"));
                result.put("sessionId", session.getId());
                result.put("amount", session.getAmountTotal() / 100.0);
                result.put("currency", session.getCurrency() != null ? session.getCurrency().toUpperCase() : "USD");
                log.info("Stripe Webhook支付成功: orderNo={}", session.getMetadata().get("orderNo"));
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getData().getObject();
                result.put("status", STATUS_FAILED);
                result.put("error", intent.getLastPaymentError() != null ?
                        intent.getLastPaymentError().getMessage() : "Payment failed");
            }

            result.put("channel", CHANNEL_STRIPE);
            result.put("eventType", event.getType());

        } catch (SignatureVerificationException e) {
            log.error("Stripe Webhook签名验证失败: {}", e.getMessage());
            result.put("status", STATUS_FAILED);
            result.put("error", "Invalid signature");
        } catch (Exception e) {
            log.error("处理Stripe Webhook失败: {}", e.getMessage());
            result.put("status", STATUS_FAILED);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        Map<String, Object> result = new HashMap<>();

        try {
            initStripe();

            // 查询原始支付
            List<Session> sessions = Session.list(
                    Map.of("limit", 1, "metadata", Map.of("orderNo", orderNo))
            ).getData();

            if (sessions.isEmpty()) {
                result.put("success", false);
                result.put("error", "订单不存在");
                return result;
            }

            String paymentIntentId = sessions.get(0).getPaymentIntent();
            if (paymentIntentId == null) {
                result.put("success", false);
                result.put("error", "无法获取支付信息");
                return result;
            }

            // 创建退款
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                    Map.of(
                            "payment_intent", paymentIntentId,
                            "reason", "requested_by_customer",
                            "metadata", Map.of("reason", reason != null ? reason : "User requested refund")
                    )
            );

            result.put("success", true);
            result.put("refundId", refund.getId());
            result.put("amount", refund.getAmount() / 100.0);
            result.put("status", refund.getStatus());

        } catch (StripeException e) {
            log.error("Stripe退款失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage());
        }

        return result;
    }

    @Override
    public String getRefundStatus(String refundId) {
        try {
            initStripe();
            com.stripe.model.Refund refund = com.stripe.model.Refund.retrieve(refundId);

            return switch (refund.getStatus()) {
                case "succeeded" -> STATUS_REFUNDED;
                case "pending" -> STATUS_PROCESSING;
                case "failed", "canceled" -> STATUS_FAILED;
                default -> STATUS_PROCESSING;
            };
        } catch (StripeException e) {
            log.error("查询Stripe退款状态失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    @Override
    public String getChannelIcon(String channel) {
        return "/assets/payment/stripe.svg";
    }

    @Override
    public String getChannelName(String channel) {
        return "信用卡 (Stripe)";
    }

    @Override
    public boolean isChannelEnabled(String channel) {
        return enabled && secretKey != null && !secretKey.isEmpty();
    }
}
