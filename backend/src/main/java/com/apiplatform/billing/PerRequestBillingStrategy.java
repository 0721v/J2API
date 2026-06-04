package com.apiplatform.billing;

import lombok.Data;

/**
 * 按次计费策略
 * 每次API调用计费一次，不区分输入输出
 *
 * @author API Platform Team
 */
@Data
public class PerRequestBillingStrategy implements BillingStrategy {

    /**
     * 每次请求的单价（元）
     */
    private double pricePerRequest;

    /**
     * 是否区分成功/失败计费
     */
    private boolean chargeOnFailure;

    /**
     * 失败时的计费比例（如0.5表示失败按半价计）
     */
    private double failureRate;

    /**
     * 默认构造函数
     */
    public PerRequestBillingStrategy() {
        this.pricePerRequest = 0.0;
        this.chargeOnFailure = false;
        this.failureRate = 1.0;
    }

    /**
     * 构造函数
     */
    public PerRequestBillingStrategy(double pricePerRequest) {
        this.pricePerRequest = pricePerRequest;
        this.chargeOnFailure = false;
        this.failureRate = 1.0;
    }

    /**
     * 完整构造函数
     */
    public PerRequestBillingStrategy(double pricePerRequest, boolean chargeOnFailure, double failureRate) {
        this.pricePerRequest = pricePerRequest;
        this.chargeOnFailure = chargeOnFailure;
        this.failureRate = failureRate;
    }

    @Override
    public String getType() {
        return TYPE_PER_REQUEST;
    }

    @Override
    public String getUnit() {
        return UNIT_REQUEST;
    }

    @Override
    public double calculate(double usage, Long userId, Long modelId) {
        if (usage <= 0) {
            return 0.0;
        }

        // usage 表示请求次数
        int requestCount = (int) usage;
        return requestCount * pricePerRequest;
    }

    @Override
    public double calculate(double usage) {
        return calculate(usage, null, null);
    }

    /**
     * 计算单次请求费用
     */
    public double calculateSingleRequest(boolean success) {
        if (!success && !chargeOnFailure) {
            return 0.0;
        }
        return chargeOnFailure ? pricePerRequest * failureRate : pricePerRequest;
    }

    @Override
    public double getUnitPrice() {
        return pricePerRequest;
    }

    @Override
    public String getDescription() {
        String base = String.format("¥%.4f/次", pricePerRequest);
        if (chargeOnFailure) {
            base += String.format(" (失败¥%.4f)", pricePerRequest * failureRate);
        }
        return base;
    }

    /**
     * 计算批量请求费用
     */
    public double calculateBatch(int successCount, int failCount) {
        double total = successCount * pricePerRequest;
        if (chargeOnFailure) {
            total += failCount * pricePerRequest * failureRate;
        }
        return total;
    }
}
