package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Model;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 模型服务接口
 *
 * @author API Platform Team
 */
public interface ModelService extends IService<Model> {

    /**
     * 创建模型
     */
    Model createModel(String modelId, String name, String type, String channelType,
                      String allowedChannels, java.math.BigDecimal pricePerCall,
                      java.math.BigDecimal inputPrice, java.math.BigDecimal outputPrice,
                      Integer maxInputLength, Integer maxOutputLength,
                      Boolean supportsStreaming, Boolean supportsFunctionCall,
                      Boolean supportsVision, String description, String tags);

    /**
     * 更新模型
     */
    Model updateModel(Long modelId, String modelIdNew, String name, String type,
                      String channelType, String allowedChannels,
                      java.math.BigDecimal pricePerCall,
                      java.math.BigDecimal inputPrice, java.math.BigDecimal outputPrice,
                      Integer maxInputLength, Integer maxOutputLength,
                      Boolean supportsStreaming, Boolean supportsFunctionCall,
                      Boolean supportsVision, String description, String tags);

    /**
     * 删除模型
     */
    void deleteModel(Long modelId);

    /**
     * 启用模型
     */
    void enableModel(Long modelId);

    /**
     * 禁用模型
     */
    void disableModel(Long modelId);

    /**
     * 根据模型标识获取模型
     */
    Model getByModelId(String modelId);

    /**
     * 获取用户可用的模型列表
     */
    List<Model> getAvailableModels(Long userId, String tokenAllowedModels);

    /**
     * 计算请求费用
     */
    Long calculateCost(Model model, Integer inputTokens, Integer outputTokens, Boolean cacheHit);

    /**
     * 分页查询模型
     */
    PageResult<Model> pageModels(int page, int size, String keyword, String type, Boolean enabled);

    /**
     * 获取模型列表（按类型）
     */
    List<Model> getModelsByType(String type);

    /**
     * 验证模型是否允许访问
     */
    boolean isModelAllowed(Model model, String allowedModels);

    /**
     * 获取模型价格信息
     */
    Map<String, Object> getModelPricing(String modelId);

    /**
     * 获取支持特定功能的模型
     */
    List<Model> getModelsWithCapabilities(Boolean supportsStreaming,
                                          Boolean supportsFunctionCall,
                                          Boolean supportsVision);

    /**
     * 批量同步模型到缓存
     */
    void syncModelsToCache();

    /**
     * 获取启用的模型列表
     */
    List<Model> selectEnabled();
}
