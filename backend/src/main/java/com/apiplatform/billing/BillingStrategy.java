package com.apiplatform.billing;

/**
 * 计费策略接口
 * 支持多种计费方式：按Token、按次、按秒、阶梯计费
 *
 * @author API Platform Team
 */
public interface BillingStrategy {

    // ==================== 计费类型 ====================
    String TYPE_TOKEN = "token";           // 按Token计费
    String TYPE_PER_REQUEST = "per_request"; // 按次计费
    String TYPE_PER_SECOND = "per_second";  // 按秒计费（视频）
    String TYPE_TIERED = "tiered";          // 阶梯计费

    // ==================== 计费单位 ====================
    String UNIT_TOKEN = "1K tokens";       // 每1K Token
    String UNIT_REQUEST = "次";             // 每次请求
    String UNIT_SECOND = "秒";              // 每秒
    String UNIT_TIER = "阶梯";              // 阶梯用量

    /**
     * 获取计费策略类型
     */
    String getType();

    /**
     * 获取计费单位描述
     */
    String getUnit();

    /**
     * 计算费用
     *
     * @param usage 使用量
     * @param userId 用户ID（用于阶梯计费）
     * @param modelId 模型ID（用于阶梯计费）
     * @return 费用（元）
     */
    double calculate(double usage, Long userId, Long modelId);

    /**
     * 计算费用（简化版，用于纯计算场景）
     *
     * @param usage 使用量
     * @return 费用（元）
     */
    double calculate(double usage);

    /**
     * 获取单价（用于展示）
     */
    double getUnitPrice();

    /**
     * 获取计费描述
     */
    String getDescription();

    /**
     * 验证计费参数是否有效
     */
    default boolean isValid() {
        return getType() != null && !getType().isEmpty();
    }
}
