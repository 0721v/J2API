package com.apiplatform.billing;

import lombok.Data;

/**
 * 按Token计费策略
 * 以上游API返回的实际Token数量为准进行计费
 *
 * @author API Platform Team
 */
@Data
public class TokenBillingStrategy implements BillingStrategy {

    /**
     * 输入单价（元/1K tokens）
     */
    private double inputPrice;

    /**
     * 输出单价（元/1K tokens）
     */
    private double outputPrice;

    /**
     * 默认构造函数
     */
    public TokenBillingStrategy() {
        this.inputPrice = 0.0;
        this.outputPrice = 0.0;
    }

    /**
     * 构造函数
     */
    public TokenBillingStrategy(double inputPrice, double outputPrice) {
        this.inputPrice = inputPrice;
        this.outputPrice = outputPrice;
    }

    @Override
    public String getType() {
        return TYPE_TOKEN;
    }

    @Override
    public String getUnit() {
        return UNIT_TOKEN;
    }

    @Override
    public double calculate(double usage, Long userId, Long modelId) {
        // usage 格式: inputTokens:outputTokens (例如 "1000:2000")
        if (usage <= 0) {
            return 0.0;
        }

        // 如果usage是组合格式（input:output），分别计算
        String usageStr = String.valueOf(usage);
        if (usageStr.contains(":")) {
            String[] parts = usageStr.split(":");
            double inputTokens = Double.parseDouble(parts[0]);
            double outputTokens = Double.parseDouble(parts[1]);
            return (inputTokens / 1000.0) * inputPrice + (outputTokens / 1000.0) * outputPrice;
        }

        // 如果是单一数值，默认按输入计算
        return (usage / 1000.0) * inputPrice;
    }

    @Override
    public double calculate(double usage) {
        return calculate(usage, null, null);
    }

    @Override
    public double getUnitPrice() {
        // 返回平均单价（用于展示）
        return (inputPrice + outputPrice) / 2.0;
    }

    @Override
    public String getDescription() {
        if (inputPrice > 0 && outputPrice > 0) {
            return String.format("输入: ¥%.6f/1K | 输出: ¥%.6f/1K", inputPrice, outputPrice);
        } else if (inputPrice > 0) {
            return String.format("¥%.6f/1K tokens", inputPrice);
        } else if (outputPrice > 0) {
            return String.format("¥%.6f/1K tokens", outputPrice);
        }
        return "未定价";
    }

    /**
     * 计算指定Token数的费用
     *
     * @param inputTokens 输入Token数
     * @param outputTokens 输出Token数
     * @return 费用（元）
     */
    public double calculateByTokens(long inputTokens, long outputTokens) {
        return (inputTokens / 1000.0) * inputPrice + (outputTokens / 1000.0) * outputPrice;
    }

    /**
     * 仅计算输入费用
     */
    public double calculateInput(long tokens) {
        return (tokens / 1000.0) * inputPrice;
    }

    /**
     * 仅计算输出费用
     */
    public double calculateOutput(long tokens) {
        return (tokens / 1000.0) * outputPrice;
    }
}
