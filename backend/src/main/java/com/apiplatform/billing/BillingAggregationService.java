package com.apiplatform.billing;

import com.apiplatform.entity.Model;
import com.apiplatform.entity.UsageLog;
import com.apiplatform.service.BillingService;
import com.apiplatform.service.ModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计费聚合服务
 * 统一管理多种计费策略
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingAggregationService {

    private final ModelService modelService;
    private final BillingService billingService;

    /**
     * 计算API调用费用
     *
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param usageLog 用量日志
     * @return 计费结果
     */
    public BillingResult calculate(Long userId, Long modelId, UsageLog usageLog) {
        Model model = modelService.getById(modelId);
        if (model == null) {
            log.error("模型不存在: modelId={}", modelId);
            return BillingResult.error("模型不存在");
        }

        // 根据模型类型选择合适的计费策略
        BillingStrategy strategy = BillingStrategyFactory.create(model);

        // 计算费用
        double usage = extractUsage(usageLog, model.getBillingType());
        double cost = strategy.calculate(usage, userId, modelId);

        // 构建计费结果
        return BillingResult.builder()
                .userId(userId)
                .modelId(modelId)
                .modelName(model.getName())
                .billingType(model.getBillingType())
                .usage(usage)
                .unitPrice(strategy.getUnitPrice())
                .cost(cost)
                .description(strategy.getDescription())
                .build();
    }

    /**
     * 根据模型名称计算费用
     */
    public BillingResult calculateByModelName(Long userId, String modelName, UsageLog usageLog) {
        Model model = modelService.getByName(modelName);
        if (model == null) {
            log.error("模型不存在: modelName={}", modelName);
            return BillingResult.error("模型不存在: " + modelName);
        }
        return calculate(userId, model.getId(), usageLog);
    }

    /**
     * 估算费用（用于展示价格预告）
     */
    public BillingEstimate estimate(Long modelId, double estimatedUsage) {
        Model model = modelService.getById(modelId);
        if (model == null) {
            return null;
        }

        BillingStrategy strategy = BillingStrategyFactory.create(model);
        double estimatedCost = strategy.calculate(estimatedUsage);

        return BillingEstimate.builder()
                .modelId(modelId)
                .modelName(model.getName())
                .billingType(model.getBillingType())
                .unitPrice(strategy.getUnitPrice())
                .estimatedUsage(estimatedUsage)
                .estimatedCost(estimatedCost)
                .description(strategy.getDescription())
                .build();
    }

    /**
     * 获取模型的计费信息
     */
    public BillingInfo getBillingInfo(Long modelId) {
        Model model = modelService.getById(modelId);
        if (model == null) {
            return null;
        }

        BillingStrategy strategy = BillingStrategyFactory.create(model);

        BillingInfo info = new BillingInfo();
        info.setModelId(model.getId());
        info.setModelName(model.getName());
        info.setBillingType(model.getBillingType());
        info.setUnitPrice(strategy.getUnitPrice());
        info.setDescription(strategy.getDescription());

        // 根据计费类型填充额外信息
        switch (model.getBillingType()) {
            case BillingStrategy.TYPE_TOKEN:
                info.setInputPrice(model.getInputPrice());
                info.setOutputPrice(model.getOutputPrice());
                break;
            case BillingStrategy.TYPE_PER_REQUEST:
                info.setPerRequestPrice(model.getPerRequestPrice());
                break;
            case BillingStrategy.TYPE_PER_SECOND:
                info.setPerSecondPrice(model.getPerSecondPrice());
                info.setMediaType(model.getMediaType());
                info.setMinBillableSeconds(model.getMinBillableSeconds());
                info.setMaxBillableSeconds(model.getMaxBillableSeconds());
                break;
            case BillingStrategy.TYPE_TIERED:
                info.setTieredConfig(model.getTieredConfig());
                info.setTiers(parseTieredConfig(model.getTieredConfig()));
                break;
        }

        return info;
    }

    /**
     * 获取所有模型的计费信息
     */
    public List<BillingInfo> getAllBillingInfo() {
        List<Model> models = modelService.list();
        return models.stream()
                .filter(Model::getEnabled)
                .map(model -> getBillingInfo(model.getId()))
                .toList();
    }

    /**
     * 从用量日志中提取计费用量
     */
    private double extractUsage(UsageLog usageLog, String billingType) {
        if (usageLog == null) {
            return 0.0;
        }

        switch (billingType) {
            case BillingStrategy.TYPE_TOKEN:
                // Token计费：inputTokens:outputTokens
                long inputTokens = usageLog.getInputTokens() != null ? usageLog.getInputTokens() : 0;
                long outputTokens = usageLog.getOutputTokens() != null ? usageLog.getOutputTokens() : 0;
                return inputTokens + outputTokens;

            case BillingStrategy.TYPE_PER_REQUEST:
                // 按次计费：1次请求
                return 1.0;

            case BillingStrategy.TYPE_PER_SECOND:
                // 按秒计费：使用duration字段
                Double duration = usageLog.getDuration();
                return duration != null ? duration : 0.0;

            case BillingStrategy.TYPE_TIERED:
                // 阶梯计费：使用累计用量或单次用量
                Double tieredUsage = usageLog.getTieredUsage();
                if (tieredUsage != null) {
                    return tieredUsage;
                }
                // 默认使用token总量
                long totalTokens = (usageLog.getInputTokens() != null ? usageLog.getInputTokens() : 0)
                        + (usageLog.getOutputTokens() != null ? usageLog.getOutputTokens() : 0);
                return totalTokens;

            default:
                // 默认使用token总量
                return (usageLog.getInputTokens() != null ? usageLog.getInputTokens() : 0)
                        + (usageLog.getOutputTokens() != null ? usageLog.getOutputTokens() : 0);
        }
    }

    /**
     * 解析阶梯配置
     */
    private List<Map<String, Object>> parseTieredConfig(String config) {
        if (config == null || config.isEmpty()) {
            return List.of();
        }

        try {
            String[] tierParts = config.split(",");
            return java.util.Arrays.stream(tierParts)
                    .map(tier -> {
                        String[] parts = tier.trim().split(":");
                        Map<String, Object> tierMap = new HashMap<>();
                        if (parts.length >= 2) {
                            String range = parts[0];
                            double price = Double.parseDouble(parts[1]);
                            tierMap.put("range", range);
                            tierMap.put("price", price);
                        }
                        return tierMap;
                    })
                    .filter(m -> !m.isEmpty())
                    .toList();
        } catch (Exception e) {
            log.error("解析阶梯配置失败: {}", config, e);
            return List.of();
        }
    }

    // ==================== 内部类 ====================

    /**
     * 计费结果
     */
    @lombok.Data
    @lombok.Builder
    public static class BillingResult {
        private Long userId;
        private Long modelId;
        private String modelName;
        private String billingType;
        private double usage;
        private double unitPrice;
        private double cost;
        private String description;
        private String error;

        public static BillingResult error(String message) {
            return BillingResult.builder()
                    .error(message)
                    .build();
        }

        public boolean isSuccess() {
            return error == null;
        }
    }

    /**
     * 计费预估
     */
    @lombok.Data
    @lombok.Builder
    public static class BillingEstimate {
        private Long modelId;
        private String modelName;
        private String billingType;
        private double unitPrice;
        private double estimatedUsage;
        private double estimatedCost;
        private String description;
    }

    /**
     * 计费信息
     */
    @lombok.Data
    public static class BillingInfo {
        private Long modelId;
        private String modelName;
        private String billingType;
        private double unitPrice;
        private String description;

        // Token计费用
        private Double inputPrice;
        private Double outputPrice;

        // 按次计费用
        private Double perRequestPrice;

        // 按秒计费用
        private Double perSecondPrice;
        private String mediaType;
        private Double minBillableSeconds;
        private Double maxBillableSeconds;

        // 阶梯计费用
        private String tieredConfig;
        private List<Map<String, Object>> tiers;
    }
}
