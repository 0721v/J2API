package com.apiplatform.billing;

import com.apiplatform.entity.Model;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 计费策略工厂
 * 根据模型配置创建对应的计费策略
 *
 * @author API Platform Team
 */
@Slf4j
public class BillingStrategyFactory {

    /**
     * 创建计费策略
     *
     * @param model 模型配置
     * @return 计费策略实例
     */
    public static BillingStrategy create(Model model) {
        if (model == null) {
            log.warn("模型配置为空，使用默认按Token计费");
            return new TokenBillingStrategy();
        }

        String billingType = model.getBillingType();
        if (billingType == null || billingType.isEmpty()) {
            billingType = BillingStrategy.TYPE_TOKEN;
        }

        switch (billingType) {
            case BillingStrategy.TYPE_TOKEN:
                return createTokenStrategy(model);
            case BillingStrategy.TYPE_PER_REQUEST:
                return createPerRequestStrategy(model);
            case BillingStrategy.TYPE_PER_SECOND:
                return createPerSecondStrategy(model);
            case BillingStrategy.TYPE_TIERED:
                return createTieredStrategy(model);
            default:
                log.warn("未知的计费类型: {}, 使用默认按Token计费", billingType);
                return createTokenStrategy(model);
        }
    }

    /**
     * 创建Token计费策略
     */
    private static TokenBillingStrategy createTokenStrategy(Model model) {
        double inputPrice = model.getInputPrice() != null ? model.getInputPrice().doubleValue() : 0.0;
        double outputPrice = model.getOutputPrice() != null ? model.getOutputPrice().doubleValue() : 0.0;
        return new TokenBillingStrategy(inputPrice, outputPrice);
    }

    /**
     * 创建按次计费策略
     */
    private static PerRequestBillingStrategy createPerRequestStrategy(Model model) {
        double price = model.getPerRequestPrice() != null ? model.getPerRequestPrice().doubleValue() : 0.0;
        return new PerRequestBillingStrategy(price);
    }

    /**
     * 创建按秒计费策略
     */
    private static PerSecondBillingStrategy createPerSecondStrategy(Model model) {
        double price = model.getPerSecondPrice() != null ? model.getPerSecondPrice().doubleValue() : 0.0;
        double minSeconds = model.getMinBillableSeconds() != null ? model.getMinBillableSeconds().doubleValue() : 1.0;
        double maxSeconds = model.getMaxBillableSeconds() != null ? model.getMaxBillableSeconds().doubleValue() : 300.0;
        String mediaType = model.getMediaType();
        if (mediaType == null) mediaType = "video";

        return new PerSecondBillingStrategy(price, minSeconds, maxSeconds, mediaType);
    }

    /**
     * 创建阶梯计费策略
     */
    private static TieredBillingStrategy createTieredStrategy(Model model) {
        TieredBillingStrategy strategy = new TieredBillingStrategy();

        // 从模型配置中解析阶梯
        String tierConfig = model.getTieredConfig();
        if (tierConfig != null && !tierConfig.isEmpty()) {
            parseTierConfig(tierConfig, strategy);
        } else {
            // 使用默认阶梯配置
            strategy.addTier(0, 100_000, 0.10);
            strategy.addTier(100_000, 1_000_000, 0.08);
            strategy.addTier(1_000_000, Double.MAX_VALUE, 0.06);
        }

        return strategy;
    }

    /**
     * 解析阶梯配置
     * 格式: "0-100000:0.10,100000-1000000:0.08,1000000:+:0.06"
     */
    private static void parseTierConfig(String config, TieredBillingStrategy strategy) {
        try {
            String[] tierParts = config.split(",");
            for (String tier : tierParts) {
                String[] parts = tier.trim().split(":");
                if (parts.length >= 2) {
                    String range = parts[0];
                    double price = Double.parseDouble(parts[1]);

                    if (range.endsWith("+")) {
                        // 无上限阶梯
                        double minUsage = Double.parseDouble(range.substring(0, range.length() - 1));
                        strategy.addUnlimitedTier(minUsage, price);
                    } else {
                        // 有上限阶梯
                        String[] rangeParts = range.split("-");
                        if (rangeParts.length == 2) {
                            double minUsage = Double.parseDouble(rangeParts[0]);
                            double maxUsage = Double.parseDouble(rangeParts[1]);
                            strategy.addTier(minUsage, maxUsage, price);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析阶梯配置失败: {}, 使用默认配置", config, e);
        }
    }

    /**
     * 从配置字符串创建计费策略
     *
     * @param billingType 计费类型
     * @param config 配置（JSON格式或简单格式）
     * @return 计费策略实例
     */
    public static BillingStrategy createFromConfig(String billingType, String config) {
        if (billingType == null) {
            billingType = BillingStrategy.TYPE_TOKEN;
        }

        switch (billingType) {
            case BillingStrategy.TYPE_TOKEN:
                return parseTokenConfig(config);
            case BillingStrategy.TYPE_PER_REQUEST:
                return parsePerRequestConfig(config);
            case BillingStrategy.TYPE_PER_SECOND:
                return parsePerSecondConfig(config);
            case BillingStrategy.TYPE_TIERED:
                return parseTieredConfig(config);
            default:
                return new TokenBillingStrategy();
        }
    }

    private static TokenBillingStrategy parseTokenConfig(String config) {
        // 格式: {"input": 0.1, "output": 0.2} 或 "0.1:0.2"
        if (config == null || config.isEmpty()) {
            return new TokenBillingStrategy();
        }

        try {
            if (config.contains("{")) {
                // JSON格式
                Map<String, Double> map = parseJsonMap(config);
                return new TokenBillingStrategy(
                        map.getOrDefault("input", 0.0),
                        map.getOrDefault("output", 0.0)
                );
            } else if (config.contains(":")) {
                // 简单格式: "input:output"
                String[] parts = config.split(":");
                return new TokenBillingStrategy(
                        Double.parseDouble(parts[0]),
                        parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0
                );
            } else {
                // 单值格式
                return new TokenBillingStrategy(Double.parseDouble(config), 0.0);
            }
        } catch (Exception e) {
            log.error("解析Token计费配置失败: {}", config, e);
            return new TokenBillingStrategy();
        }
    }

    private static PerRequestBillingStrategy parsePerRequestConfig(String config) {
        if (config == null || config.isEmpty()) {
            return new PerRequestBillingStrategy();
        }
        try {
            return new PerRequestBillingStrategy(Double.parseDouble(config));
        } catch (Exception e) {
            log.error("解析按次计费配置失败: {}", config, e);
            return new PerRequestBillingStrategy();
        }
    }

    private static PerSecondBillingStrategy parsePerSecondConfig(String config) {
        if (config == null || config.isEmpty()) {
            return new PerSecondBillingStrategy();
        }
        try {
            return new PerSecondBillingStrategy(Double.parseDouble(config));
        } catch (Exception e) {
            log.error("解析按秒计费配置失败: {}", config, e);
            return new PerSecondBillingStrategy();
        }
    }

    private static TieredBillingStrategy parseTieredConfig(String config) {
        TieredBillingStrategy strategy = new TieredBillingStrategy();
        if (config == null || config.isEmpty()) {
            return strategy;
        }
        parseTierConfig(config, strategy);
        return strategy;
    }

    private static Map<String, Double> parseJsonMap(String json) {
        Map<String, Double> map = new HashMap<>();
        try {
            // 简单JSON解析
            json = json.replace("{", "").replace("}", "").replace("\"", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    map.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                }
            }
        } catch (Exception e) {
            log.error("解析JSON配置失败: {}", json, e);
        }
        return map;
    }
}
