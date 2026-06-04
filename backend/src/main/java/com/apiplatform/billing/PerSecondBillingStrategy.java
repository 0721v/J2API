package com.apiplatform.billing;

import lombok.Data;

/**
 * 按秒计费策略
 * 适用于视频生成、图片生成等按时间/帧数计费的场景
 *
 * @author API Platform Team
 */
@Data
public class PerSecondBillingStrategy implements BillingStrategy {

    /**
     * 每秒单价（元/秒）
     */
    private double pricePerSecond;

    /**
     * 最小计费时长（秒），不足按此计算
     */
    private double minBillableSeconds;

    /**
     * 最大计费时长（秒），超过按此计算
     */
    private double maxBillableSeconds;

    /**
     * 计费精度（秒），如0.5表示按0.5秒精度计费
     */
    private double billingPrecision;

    /**
     * 视频类型: video, image, audio
     */
    private String mediaType;

    /**
     * 默认构造函数
     */
    public PerSecondBillingStrategy() {
        this.pricePerSecond = 0.0;
        this.minBillableSeconds = 1.0;
        this.maxBillableSeconds = 300.0;
        this.billingPrecision = 1.0;
        this.mediaType = "video";
    }

    /**
     * 构造函数
     */
    public PerSecondBillingStrategy(double pricePerSecond) {
        this.pricePerSecond = pricePerSecond;
        this.minBillableSeconds = 1.0;
        this.maxBillableSeconds = 300.0;
        this.billingPrecision = 1.0;
        this.mediaType = "video";
    }

    /**
     * 完整构造函数
     */
    public PerSecondBillingStrategy(double pricePerSecond, double minSeconds, double maxSeconds, String mediaType) {
        this.pricePerSecond = pricePerSecond;
        this.minBillableSeconds = minSeconds;
        this.maxBillableSeconds = maxSeconds;
        this.billingPrecision = 1.0;
        this.mediaType = mediaType;
    }

    @Override
    public String getType() {
        return TYPE_PER_SECOND;
    }

    @Override
    public String getUnit() {
        return UNIT_SECOND;
    }

    @Override
    public double calculate(double usage, Long userId, Long modelId) {
        if (usage <= 0) {
            return 0.0;
        }

        // usage 表示实际时长（秒）
        double actualSeconds = usage;

        // 应用最小计费时长
        double billableSeconds = Math.max(actualSeconds, minBillableSeconds);

        // 应用最大计费时长
        billableSeconds = Math.min(billableSeconds, maxBillableSeconds);

        // 按精度取整
        billableSeconds = Math.ceil(billableSeconds / billingPrecision) * billingPrecision;

        return billableSeconds * pricePerSecond;
    }

    @Override
    public double calculate(double usage) {
        return calculate(usage, null, null);
    }

    /**
     * 计算指定时长的费用
     *
     * @param seconds 时长（秒）
     * @return 费用（元）
     */
    public double calculateBySeconds(double seconds) {
        return calculate(seconds, null, null);
    }

    /**
     * 计算视频费用
     *
     * @param durationSeconds 时长（秒）
     * @param resolution 分辨率等级 (e.g., "720p", "1080p", "4k")
     * @return 费用（元）
     */
    public double calculateVideo(double durationSeconds, String resolution) {
        double basePrice = calculateBySeconds(durationSeconds);

        // 分辨率调整系数
        double resolutionMultiplier = switch (resolution.toLowerCase()) {
            case "4k", "3840x2160" -> 2.0;
            case "1080p", "1920x1080" -> 1.5;
            case "720p", "1280x720" -> 1.0;
            case "480p", "854x480" -> 0.7;
            default -> 1.0;
        };

        return basePrice * resolutionMultiplier;
    }

    /**
     * 计算图片费用
     *
     * @param count 图片数量
     * @param resolution 分辨率
     * @return 费用（元）
     */
    public double calculateImage(int count, String resolution) {
        // 图片按张计费，转换为等效秒数
        double basePrice = pricePerSecond; // 每张等效1秒
        double resolutionMultiplier = switch (resolution.toLowerCase()) {
            case "1024x1024" -> 1.0;
            case "512x512" -> 0.5;
            case "2048x2048" -> 2.0;
            default -> 1.0;
        };

        return count * basePrice * resolutionMultiplier;
    }

    @Override
    public double getUnitPrice() {
        return pricePerSecond;
    }

    @Override
    public String getDescription() {
        String media = switch (mediaType) {
            case "video" -> "视频";
            case "image" -> "图片";
            case "audio" -> "音频";
            default -> "媒体";
        };
        return String.format("¥%.4f/%s/%s", pricePerSecond, UNIT_SECOND, media);
    }

    /**
     * 获取每分钟价格
     */
    public double getPricePerMinute() {
        return pricePerSecond * 60;
    }

    /**
     * 获取每小时价格
     */
    public double getPricePerHour() {
        return pricePerSecond * 3600;
    }
}
