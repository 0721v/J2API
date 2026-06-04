package com.apiplatform.billing;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 阶梯计费策略
 * 用量越多，单价越低
 *
 * @author API Platform Team
 */
@Data
public class TieredBillingStrategy implements BillingStrategy {

    /**
     * 阶梯配置列表（按用量从低到高排序）
     */
    private List<Tier> tiers;

    /**
     * 默认构造函数
     */
    public TieredBillingStrategy() {
        this.tiers = new ArrayList<>();
    }

    /**
     * 构造函数，传入阶梯配置
     */
    public TieredBillingStrategy(List<Tier> tiers) {
        this.tiers = tiers;
    }

    /**
     * 添加阶梯
     */
    public void addTier(double minUsage, double maxUsage, double unitPrice) {
        tiers.add(new Tier(minUsage, maxUsage, unitPrice));
        // 按minUsage排序
        tiers.sort((a, b) -> Double.compare(a.minUsage, b.minUsage));
    }

    /**
     * 添加阶梯（无上限）
     */
    public void addUnlimitedTier(double minUsage, double unitPrice) {
        tiers.add(new Tier(minUsage, Double.MAX_VALUE, unitPrice));
        tiers.sort((a, b) -> Double.compare(a.minUsage, b.minUsage));
    }

    @Override
    public String getType() {
        return TYPE_TIERED;
    }

    @Override
    public String getUnit() {
        return UNIT_TIER;
    }

    @Override
    public double calculate(double usage, Long userId, Long modelId) {
        if (usage <= 0 || tiers.isEmpty()) {
            return 0.0;
        }

        // 计算每个阶梯的费用
        double totalCost = 0.0;
        double remainingUsage = usage;

        for (Tier tier : tiers) {
            if (remainingUsage <= 0) {
                break;
            }

            // 当前阶梯的可用用量
            double tierCapacity = tier.maxUsage - tier.minUsage;
            if (tier.maxUsage == Double.MAX_VALUE) {
                tierCapacity = remainingUsage;
            }

            // 当前阶梯的实际用量
            double tierUsage = Math.min(remainingUsage, tierCapacity);

            // 累加费用
            totalCost += tierUsage * tier.unitPrice;

            remainingUsage -= tierUsage;
        }

        return totalCost;
    }

    @Override
    public double calculate(double usage) {
        return calculate(usage, null, null);
    }

    @Override
    public double getUnitPrice() {
        // 返回基础阶梯单价（用于展示）
        if (tiers.isEmpty()) {
            return 0.0;
        }
        return tiers.get(0).unitPrice;
    }

    @Override
    public String getDescription() {
        if (tiers.isEmpty()) {
            return "未定价";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("阶梯计费:\n");
        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);
            String range;
            if (tier.maxUsage == Double.MAX_VALUE) {
                range = tier.minUsage + "+";
            } else {
                range = tier.minUsage + "-" + tier.maxUsage;
            }
            sb.append(String.format("  %s: ¥%.4f/%s\n", range, tier.unitPrice, getBaseUnit()));
        }
        return sb.toString().trim();
    }

    /**
     * 获取基础计费单位
     */
    private String getBaseUnit() {
        if (tiers.isEmpty()) return "";
        Tier firstTier = tiers.get(0);
        if (firstTier.unitPrice > 0) {
            return "元";
        }
        return "";
    }

    /**
     * 获取当前用量所处的阶梯索引
     */
    public int getCurrentTierIndex(double usage) {
        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);
            if (usage >= tier.minUsage && usage < tier.maxUsage) {
                return i;
            }
        }
        return tiers.size() - 1;
    }

    /**
     * 获取当前阶梯的单价
     */
    public double getCurrentTierPrice(double usage) {
        int index = getCurrentTierIndex(usage);
        if (index >= 0 && index < tiers.size()) {
            return tiers.get(index).unitPrice;
        }
        return 0.0;
    }

    /**
     * 计算预计费用（用于预览）
     */
    public double calculatePreview(double usage) {
        double currentCost = calculate(usage);
        double avgPrice = usage > 0 ? currentCost / usage : 0;
        return avgPrice;
    }

    /**
     * 阶梯配置
     */
    @Data
    public static class Tier {
        /**
         * 阶梯最小用量（包含）
         */
        private double minUsage;

        /**
         * 阶梯最大用量（不包含），使用Double.MAX_VALUE表示无上限
         */
        private double maxUsage;

        /**
         * 该阶梯的单价
         */
        private double unitPrice;

        public Tier() {
        }

        public Tier(double minUsage, double maxUsage, double unitPrice) {
            this.minUsage = minUsage;
            this.maxUsage = maxUsage;
            this.unitPrice = unitPrice;
        }
    }

    /**
     * 创建预设的月度阶梯（Token计费）
     */
    public static TieredBillingStrategy createMonthlyTokenTiered() {
        List<Tier> tiers = new ArrayList<>();
        // 0-100K: 0.1元/1K
        tiers.add(new Tier(0, 100_000, 0.10));
        // 100K-1M: 0.08元/1K
        tiers.add(new Tier(100_000, 1_000_000, 0.08));
        // 1M-10M: 0.06元/1K
        tiers.add(new Tier(1_000_000, 10_000_000, 0.06));
        // 10M+: 0.04元/1K
        tiers.add(new Tier(10_000_000, Double.MAX_VALUE, 0.04));

        return new TieredBillingStrategy(tiers);
    }

    /**
     * 创建预设的请求阶梯（按次计费）
     */
    public static TieredBillingStrategy createRequestTiered() {
        List<Tier> tiers = new ArrayList<>();
        // 0-1000次: 0.1元/次
        tiers.add(new Tier(0, 1000, 0.10));
        // 1000-10000次: 0.08元/次
        tiers.add(new Tier(1000, 10_000, 0.08));
        // 10000-100000次: 0.06元/次
        tiers.add(new Tier(10_000, 100_000, 0.06));
        // 100000+: 0.04元/次
        tiers.add(new Tier(100_000, Double.MAX_VALUE, 0.04));

        return new TieredBillingStrategy(tiers);
    }
}
