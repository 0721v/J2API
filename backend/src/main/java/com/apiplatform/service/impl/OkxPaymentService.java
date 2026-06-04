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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧易(OKX)虚拟货币支付实现
 * 支持USDT、BTC、ETH等多种加密货币支付
 *
 * @author API Platform Team
 */
@Slf4j
@Service("okxPaymentService")
public class OkxPaymentService implements PaymentService {

    @Value("${payment.okx.api-key:}")
    private String apiKey;

    @Value("${payment.okx.secret-key:}")
    private String secretKey;

    @Value("${payment.okx.passphrase:}")
    private String passphrase;

    @Value("${payment.okx.endpoint:https://www.okx.com}")
    private String endpoint;

    @Value("${payment.okx.enabled:false}")
    private boolean enabled;

    @Value("${payment.okx.receive-address:}")
    private String receiveAddress;

    @Value("${payment.okx.network:TRC20}")
    private String network;

    @Value("${payment.okx.discount:0.95}")
    private double discountRate;

    @Value("${payment.okx.notify-url:}")
    private String notifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 支持的加密货币
    private static final List<Map<String, String>> SUPPORTED_COINS = List.of(
            Map.of("symbol", "USDT", "name", "Tether USD", "network", "TRC20", "icon", "usdt.svg"),
            Map.of("symbol", "USDT", "name", "Tether USD", "network", "ERC20", "icon", "usdt.svg"),
            Map.of("symbol", "BTC", "name", "Bitcoin", "network", "BTC", "icon", "btc.svg"),
            Map.of("symbol", "ETH", "name", "Ethereum", "network", "ERC20", "icon", "eth.svg")
    );

    @Override
    public Map<String, Object> createPayment(Long userId, String orderNo, double amount, String channel) {
        if (!enabled) {
            throw new BizException("加密货币支付暂不可用");
        }

        try {
            // 计算优惠后的CNY金额
            double discountedAmount = amount * discountRate;

            // 估算USDT价格（实际应该从OKX获取实时价格）
            double usdtPrice = getUsdtPrice();
            double cryptoAmount = discountedAmount / usdtPrice;

            // 生成支付地址（使用商户自己的收款地址）
            String paymentAddress = receiveAddress;

            // 生成唯一支付ID
            String paymentId = UUID.randomUUID().toString();

            // 构建支付信息
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orderNo", orderNo);
            result.put("paymentAddress", paymentAddress);
            result.put("paymentId", paymentId);
            result.put("amount", amount);
            result.put("discountedAmount", discountedAmount);
            result.put("cryptoAmount", String.format("%.6f", cryptoAmount));
            result.put("cryptoSymbol", "USDT");
            result.put("network", network);
            result.put("channel", CHANNEL_OKX);
            result.put("expiresAt", Instant.now().plusSeconds(1800).toString()); // 30分钟过期
            result.put("qrCode", generateCryptoQrCode(paymentAddress, cryptoAmount, "USDT"));

            // 存储支付信息到Redis（用于回调验证）
            storePaymentInfo(orderNo, result);

            log.info("OKX加密货币支付创建成功: orderNo={}, cryptoAmount={} USDT", orderNo, cryptoAmount);

            return result;
        } catch (Exception e) {
            log.error("创建OKX支付失败: {}", e.getMessage());
            throw new BizException("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 生成加密货币二维码
     */
    private String generateCryptoQrCode(String address, double amount, String symbol) {
        // 生成支持的钱包链接
        return switch (network) {
            case "TRC20" -> "trx:" + address + "?amount=" + amount;
            case "ERC20" -> "ethereum:" + address + "?value=" + (amount * 1e18);
            default -> address;
        };
    }

    /**
     * 获取USDT价格（从OKX API）
     */
    private double getUsdtPrice() {
        try {
            // 这里简化处理，实际应该调用OKX价格API
            // String url = endpoint + "/api/v5/market/ticker?instId=USDT-CNY";
            return 7.25; // 固定汇率简化
        } catch (Exception e) {
            log.warn("获取USDT价格失败，使用默认汇率");
            return 7.25;
        }
    }

    /**
     * 存储支付信息
     */
    private void storePaymentInfo(String orderNo, Map<String, Object> paymentInfo) {
        // 实际应该存储到Redis或数据库
        log.debug("存储支付信息: orderNo={}, paymentInfo={}", orderNo, paymentInfo);
    }

    @Override
    public String getPaymentStatus(String orderNo) {
        // 虚拟币支付状态需要通过轮询链上交易或 webhook 回调获取
        // 这里简化为待处理
        return STATUS_PENDING;
    }

    /**
     * 主动查询链上交易（可选实现）
     */
    public String queryChainTransaction(String txHash) {
        try {
            // 根据网络类型查询链上交易
            if ("TRC20".equals(network)) {
                return queryTronTransaction(txHash);
            }
            return STATUS_PENDING;
        } catch (Exception e) {
            log.error("查询链上交易失败: {}", e.getMessage());
            return STATUS_PENDING;
        }
    }

    /**
     * 查询TRON(TRC20)交易
     */
    private String queryTronTransaction(String txHash) {
        // 实际应该调用TRON API查询交易状态
        // 例如: https://apilist.tronscan.org/api/transaction-info?hash={txHash}
        return STATUS_PENDING;
    }

    @Override
    public boolean verifyCallback(String channel, Map<String, String> params) {
        if (!CHANNEL_OKX.equals(channel)) {
            return false;
        }

        // 验证OKX webhook签名
        try {
            String signature = params.get("signature");
            String timestamp = params.get("timestamp");
            String message = timestamp + params.get("body");

            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(secretKeySpec);
            byte[] hash = sha256.doFinal(message.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return signature != null && signature.equals(hexString.toString());
        } catch (Exception e) {
            log.error("OKX回调签名验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> handleCallback(String channel, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        // 解析OKX webhook回调
        String orderNo = params.get("orderNo");
        String txHash = params.get("txHash");
        String fromAddress = params.get("fromAddress");
        String amount = params.get("amount");
        String symbol = params.get("symbol");
        String status = params.get("status");

        result.put("orderNo", orderNo);
        result.put("transactionId", txHash);
        result.put("fromAddress", fromAddress);
        result.put("amount", amount);
        result.put("cryptoSymbol", symbol);
        result.put("channel", CHANNEL_OKX);

        // 验证收款地址
        if (fromAddress != null && fromAddress.equalsIgnoreCase(receiveAddress)) {
            if ("success".equalsIgnoreCase(status)) {
                result.put("status", STATUS_PAID);
                result.put("paidTime", Instant.now().toString());
                log.info("OKX加密货币支付成功: orderNo={}, txHash={}", orderNo, txHash);
            } else {
                result.put("status", STATUS_PROCESSING);
            }
        } else {
            result.put("status", STATUS_FAILED);
            result.put("error", "收款地址不匹配");
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String reason) {
        // 虚拟货币通常不支持退款，或者需要手动处理
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "加密货币支付不支持自动退款，请联系客服处理");
        return result;
    }

    @Override
    public String getRefundStatus(String refundNo) {
        return STATUS_FAILED;
    }

    @Override
    public String getChannelIcon(String channel) {
        return "/assets/payment/okx.svg";
    }

    @Override
    public String getChannelName(String channel) {
        return "加密货币 (OKX)";
    }

    @Override
    public boolean isChannelEnabled(String channel) {
        return enabled && receiveAddress != null && !receiveAddress.isEmpty();
    }

    /**
     * 获取支持的加密货币列表
     */
    public List<Map<String, String>> getSupportedCoins() {
        return SUPPORTED_COINS;
    }

    /**
     * 生成OKX订单（如果需要使用OKX托管支付）
     */
    private Map<String, Object> createOkxOrder(String orderNo, double amount, String symbol) {
        try {
            String timestamp = Instant.now().atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 构建签名
            String message = timestamp + "GET" + "/api/v5/crosstoken/pmt/create";
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(keySpec);
            byte[] hash = sha256.doFinal(message.getBytes(StandardCharsets.UTF_8));

            StringBuilder signBuilder = new StringBuilder();
            for (byte b : hash) {
                signBuilder.append(String.format("%02x", b));
            }
            String sign = signBuilder.toString();

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("OK-ACCESS-KEY", apiKey);
            headers.set("OK-ACCESS-SIGN", sign);
            headers.set("OK-ACCESS-TIMESTAMP", timestamp);
            headers.set("OK-ACCESS-PASSPHRASE", passphrase);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", String.format("%.2f", amount));
            requestBody.put("symbol", symbol);
            requestBody.put("orderId", orderNo);
            requestBody.put("type", "DIRECT");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            String url = endpoint + "/api/v5/crosstoken/pmt/create";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode data = json.get("data");
                if (data != null && data.isArray() && data.size() > 0) {
                    JsonNode orderData = data.get(0);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("paymentUrl", orderData.get("paymentUrl").asText());
                    result.put("orderId", orderData.get("orderId").asText());
                    return result;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "OKX订单创建失败");
            return result;
        } catch (Exception e) {
            log.error("创建OKX托管订单失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
