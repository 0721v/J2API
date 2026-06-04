package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Order;
import com.apiplatform.service.OrderService;
import com.apiplatform.service.PaymentAggregationService;
import com.apiplatform.service.PaymentService;
import com.apiplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 充值与订单控制器
 * 支持多种支付渠道：支付宝、微信、欧易(OKX)、Stripe、Creem
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
@Tag(name = "充值与订单", description = "充值和订单管理相关接口")
public class RechargeController {

    private final OrderService orderService;
    private final UserService userService;
    private final PaymentAggregationService paymentAggregationService;

    // ==================== 支付渠道相关 ====================

    /**
     * 获取可用的支付渠道
     */
    @GetMapping("/channels")
    @Operation(summary = "获取支付渠道", description = "获取当前可用的支付渠道列表")
    public Result<List<Map<String, Object>>> getPaymentChannels() {
        List<Map<String, Object>> channels = paymentAggregationService.getEnabledChannels();
        return Result.success(channels);
    }

    /**
     * 创建充值订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建充值订单", description = "创建充值订单并获取支付信息")
    public Result<Map<String, Object>> createRechargeOrder(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @Validated @RequestBody CreateRechargeRequest request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            // 验证金额
            if (request.getAmount() <= 0) {
                return Result.validateFailed("充值金额必须大于0");
            }

            // 创建本地订单
            String clientIp = "127.0.0.1";
            Order order = orderService.createRechargeOrder(userId, BigDecimal.valueOf(request.getAmount()), request.getChannel(), clientIp);

            // 调用支付服务创建支付
            Map<String, Object> paymentResult = paymentAggregationService.createPayment(
                    userId,
                    order.getOrderNo(),
                    request.getAmount(),
                    request.getChannel()
            );

            // 合并订单信息和支付信息
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("amount", order.getAmount());
            result.put("paidAmount", order.getPaidAmount());
            result.put("paymentMethod", order.getPaymentMethod());
            result.put("status", order.getStatus());
            result.put("expireTime", order.getExpireTime());
            result.putAll(paymentResult);

            return Result.success("充值订单创建成功", result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建充值订单失败: {}", e.getMessage());
            return Result.error("创建充值订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询充值订单状态
     */
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询订单状态", description = "查询指定订单的支付状态")
    public Result<Map<String, Object>> getOrderStatus(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String orderNo) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        Order order = orderService.getOrderDetail(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.notFound("订单不存在");
        }

        // 实时查询支付状态
        String realTimeStatus = paymentAggregationService.getPaymentStatus(orderNo, order.getPaymentMethod());

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("status", realTimeStatus);
        data.put("paymentStatus", order.getStatus());
        data.put("amount", order.getAmount());
        data.put("paidAmount", order.getPaidAmount());
        data.put("paidTime", order.getPaidTime());
        data.put("expireTime", order.getExpireTime());
        data.put("channel", order.getPaymentMethod());

        return Result.success(data);
    }

    /**
     * 获取充值历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取充值历史", description = "获取用户的充值记录")
    public Result<PageResult<Order>> getRechargeHistory(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        PageResult<Order> result = orderService.getUserOrders(userId, page, size, "recharge");
        return Result.success(result);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取充值统计", description = "获取用户的充值统计信息")
    public Result<Map<String, Object>> getRechargeStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        if (startTime == null) startTime = LocalDateTime.now().minusMonths(1);
        if (endTime == null) endTime = LocalDateTime.now();

        Map<String, Object> stats = orderService.getOrderStats(userId, startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderNo}")
    @Operation(summary = "取消订单", description = "取消未支付的订单")
    public Result<Void> cancelOrder(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String orderNo) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            orderService.cancelOrder(orderNo, userId);
            return Result.success("订单已取消", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== 支付回调 ====================

    /**
     * 支付宝回调
     */
    @PostMapping("/callback/alipay")
    @Operation(summary = "支付宝回调", description = "接收支付宝支付结果回调")
    public String alipayCallback(@RequestBody Map<String, String> params) {
        log.info("支付宝回调: {}", params);

        try {
            if (!paymentAggregationService.verifyCallback(PaymentService.CHANNEL_ALIPAY, params)) {
                log.warn("支付宝回调签名验证失败");
                return "fail";
            }

            Map<String, Object> result = paymentAggregationService.handleCallback(
                    PaymentService.CHANNEL_ALIPAY, params
            );

            if (PaymentService.STATUS_PAID.equals(result.get("status"))) {
                String orderNo = (String) result.get("orderNo");
                String transactionId = (String) result.get("transactionId");
                orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, transactionId);
                log.info("支付宝支付成功处理: orderNo={}", orderNo);
            }

            return "success";
        } catch (Exception e) {
            log.error("处理支付宝回调异常: {}", e.getMessage());
            return "fail";
        }
    }

    /**
     * 微信支付回调
     */
    @PostMapping("/callback/wechat")
    @Operation(summary = "微信支付回调", description = "接收微信支付结果回调")
    public String wechatCallback(@RequestBody Map<String, String> params) {
        log.info("微信支付回调: {}", params);

        try {
            if (!paymentAggregationService.verifyCallback(PaymentService.CHANNEL_WECHAT, params)) {
                log.warn("微信支付回调签名验证失败");
                return "<xml><return_code>FAIL</return_code></xml>";
            }

            Map<String, Object> result = paymentAggregationService.handleCallback(
                    PaymentService.CHANNEL_WECHAT, params
            );

            if (PaymentService.STATUS_PAID.equals(result.get("status"))) {
                String orderNo = (String) result.get("orderNo");
                String transactionId = (String) result.get("transactionId");
                orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, transactionId);
                log.info("微信支付成功处理: orderNo={}", orderNo);
            }

            return "<xml><return_code>SUCCESS</return_code></xml>";
        } catch (Exception e) {
            log.error("处理微信支付回调异常: {}", e.getMessage());
            return "<xml><return_code>FAIL</return_code></xml>";
        }
    }

    /**
     * 欧易(OKX)加密货币回调
     */
    @PostMapping("/callback/okx")
    @Operation(summary = "OKX加密货币回调", description = "接收OKX链上交易回调")
    public String okxCallback(@RequestBody Map<String, String> params) {
        log.info("OKX加密货币回调: {}", params);

        try {
            if (!paymentAggregationService.verifyCallback(PaymentService.CHANNEL_OKX, params)) {
                log.warn("OKX回调签名验证失败");
                return "fail";
            }

            Map<String, Object> result = paymentAggregationService.handleCallback(
                    PaymentService.CHANNEL_OKX, params
            );

            if (PaymentService.STATUS_PAID.equals(result.get("status"))) {
                String orderNo = (String) result.get("orderNo");
                String transactionId = (String) result.get("transactionId");
                orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, transactionId);
                log.info("OKX加密货币支付成功处理: orderNo={}, txHash={}", orderNo, transactionId);
            }

            return "success";
        } catch (Exception e) {
            log.error("处理OKX回调异常: {}", e.getMessage());
            return "fail";
        }
    }

    /**
     * Stripe Webhook回调
     */
    @PostMapping("/callback/stripe")
    @Operation(summary = "Stripe回调", description = "接收Stripe Webhook回调")
    public String stripeCallback(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        log.info("Stripe Webhook回调收到");

        try {
            Map<String, String> params = new HashMap<>();
            params.put("payload", payload);
            params.put("signature", signature);

            // Stripe服务会自己验证签名
            Map<String, Object> result = paymentAggregationService.handleCallback(
                    PaymentService.CHANNEL_STRIPE, params
            );

            if (PaymentService.STATUS_PAID.equals(result.get("status"))) {
                String orderNo = (String) result.get("orderNo");
                String sessionId = (String) result.get("sessionId");
                orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, sessionId);
                log.info("Stripe支付成功处理: orderNo={}", orderNo);
            }

            return "success";
        } catch (Exception e) {
            log.error("处理Stripe Webhook异常: {}", e.getMessage());
            return "fail";
        }
    }

    /**
     * Creem Webhook回调
     */
    @PostMapping("/callback/creem")
    @Operation(summary = "Creem回调", description = "接收Creem Webhook回调")
    public String creemCallback(
            @RequestBody String payload,
            @RequestHeader(value = "X-Creem-Signature", required = false) String signature) {
        log.info("Creem Webhook回调收到");

        try {
            Map<String, String> params = new HashMap<>();
            params.put("payload", payload);
            params.put("signature", signature);

            Map<String, Object> result = paymentAggregationService.handleCallback(
                    PaymentService.CHANNEL_CREEM, params
            );

            if (PaymentService.STATUS_PAID.equals(result.get("status"))) {
                String orderNo = (String) result.get("orderNo");
                String transactionId = (String) result.get("transactionId");
                orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, transactionId);
                log.info("Creem支付成功处理: orderNo={}", orderNo);
            }

            return "success";
        } catch (Exception e) {
            log.error("处理Creem Webhook异常: {}", e.getMessage());
            return "fail";
        }
    }

    /**
     * 模拟回调（用于测试）
     */
    @PostMapping("/callback/simulate/{orderNo}")
    @Operation(summary = "模拟支付回调", description = "模拟支付成功回调（仅测试环境使用）")
    public Result<Void> simulateCallback(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String orderNo) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            Order order = orderService.getOrderDetail(orderNo);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.notFound("订单不存在");
            }

            orderService.handlePaymentCallback(orderNo, PaymentService.STATUS_PAID, "SIMULATE_" + System.currentTimeMillis());
            return Result.success("模拟回调已处理", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== 充值渠道配置 ====================

    /**
     * 获取可用支付方式
     */
    @GetMapping("/payment-methods")
    @Operation(summary = "获取支付方式", description = "获取当前可用的支付方式列表")
    public Result<List<Map<String, Object>>> getPaymentMethods() {
        List<Map<String, Object>> methods = paymentAggregationService.getEnabledChannels();
        return Result.success(methods);
    }

    /**
     * 获取充值金额选项
     */
    @GetMapping("/amount-options")
    @Operation(summary = "获取充值金额选项", description = "获取预设的充值金额选项")
    public Result<List<Map<String, Object>>> getAmountOptions() {
        List<Map<String, Object>> options = List.of(
                Map.of("amount", 10, "bonus", 0, "label", "¥10"),
                Map.of("amount", 50, "bonus", 0, "label", "¥50"),
                Map.of("amount", 100, "bonus", 0, "label", "¥100"),
                Map.of("amount", 200, "bonus", 10, "label", "¥200 (送¥10)"),
                Map.of("amount", 500, "bonus", 50, "label", "¥500 (送¥50)"),
                Map.of("amount", 1000, "bonus", 150, "label", "¥1000 (送¥150)"),
                Map.of("amount", 2000, "bonus", 400, "label", "¥2000 (送¥400)")
        );
        return Result.success(options);
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreateRechargeRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        private double amount;

        @com.fasterxml.jackson.annotation.JsonProperty("channel")
        private String channel;
    }
}
