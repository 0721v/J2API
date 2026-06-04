package com.apiplatform.service.impl;

import com.apiplatform.common.*;
import com.apiplatform.entity.Model;
import com.apiplatform.mapper.ModelMapper;
import com.apiplatform.service.ModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Model createModel(String modelId, String name, String type, String channelType,
                             String allowedChannels, BigDecimal pricePerCall,
                             BigDecimal inputPrice, BigDecimal outputPrice,
                             Integer maxInputLength, Integer maxOutputLength,
                             Boolean supportsStreaming, Boolean supportsFunctionCall,
                             Boolean supportsVision, String description, String tags) {
        
        // 检查模型标识是否已存在
        Model existing = modelMapper.selectByModelId(modelId);
        if (existing != null) {
            throw BizException.badRequest("模型标识已存在: " + modelId);
        }

        Model model = Model.builder()
                .modelId(modelId)
                .name(name)
                .type(type != null ? type : "chat")
                .channelType(channelType)
                .allowedChannels(allowedChannels)
                .pricePerCall(pricePerCall != null ? pricePerCall : BigDecimal.ZERO)
                .inputPrice(inputPrice != null ? inputPrice : BigDecimal.ZERO)
                .outputPrice(outputPrice != null ? outputPrice : BigDecimal.ZERO)
                .maxInputLength(maxInputLength != null ? maxInputLength : 128000)
                .maxOutputLength(maxOutputLength != null ? maxOutputLength : 4096)
                .supportsStreaming(supportsStreaming != null ? supportsStreaming : true)
                .supportsFunctionCall(supportsFunctionCall != null ? supportsFunctionCall : false)
                .supportsVision(supportsVision != null ? supportsVision : false)
                .enabled(true)
                .description(description)
                .tags(tags)
                .build();

        modelMapper.insert(model);
        log.info("创建模型: id={}, modelId={}", model.getId(), modelId);
        
        return model;
    }

    @Override
    @Transactional
    public Model updateModel(Long modelId, String modelIdNew, String name, String type,
                            String channelType, String allowedChannels,
                            BigDecimal pricePerCall,
                            BigDecimal inputPrice, BigDecimal outputPrice,
                            Integer maxInputLength, Integer maxOutputLength,
                            Boolean supportsStreaming, Boolean supportsFunctionCall,
                            Boolean supportsVision, String description, String tags) {
        
        Model model = getById(modelId);
        if (model == null) {
            throw BizException.modelNotFound();
        }

        // 检查新标识是否冲突
        if (modelIdNew != null && !modelIdNew.equals(model.getModelId())) {
            Model existing = modelMapper.selectByModelId(modelIdNew);
            if (existing != null) {
                throw BizException.badRequest("模型标识已存在: " + modelIdNew);
            }
            model.setModelId(modelIdNew);
        }

        if (name != null) model.setName(name);
        if (type != null) model.setType(type);
        if (channelType != null) model.setChannelType(channelType);
        if (allowedChannels != null) model.setAllowedChannels(allowedChannels);
        if (pricePerCall != null) model.setPricePerCall(pricePerCall);
        if (inputPrice != null) model.setInputPrice(inputPrice);
        if (outputPrice != null) model.setOutputPrice(outputPrice);
        if (maxInputLength != null) model.setMaxInputLength(maxInputLength);
        if (maxOutputLength != null) model.setMaxOutputLength(maxOutputLength);
        if (supportsStreaming != null) model.setSupportsStreaming(supportsStreaming);
        if (supportsFunctionCall != null) model.setSupportsFunctionCall(supportsFunctionCall);
        if (supportsVision != null) model.setSupportsVision(supportsVision);
        if (description != null) model.setDescription(description);
        if (tags != null) model.setTags(tags);

        updateById(model);
        log.info("更新模型: id={}", modelId);
        
        return model;
    }

    @Override
    @Transactional
    @CacheEvict(value = "models", allEntries = true)
    public void deleteModel(Long modelId) {
        Model model = getById(modelId);
        if (model == null) {
            throw BizException.modelNotFound();
        }
        removeById(modelId);
        log.info("删除模型: id={}", modelId);
    }

    @Override
    @Transactional
    public void enableModel(Long modelId) {
        Model model = getById(modelId);
        if (model == null) {
            throw BizException.modelNotFound();
        }
        model.setEnabled(true);
        updateById(model);
        log.info("启用模型: id={}", modelId);
    }

    @Override
    @Transactional
    public void disableModel(Long modelId) {
        Model model = getById(modelId);
        if (model == null) {
            throw BizException.modelNotFound();
        }
        model.setEnabled(false);
        updateById(model);
        log.info("禁用模型: id={}", modelId);
    }

    @Override
    @Cacheable(value = "models", key = "#modelId")
    public Model getByModelId(String modelId) {
        return modelMapper.selectByModelId(modelId);
    }

    @Override
    public List<Model> getAvailableModels(Long userId, String tokenAllowedModels) {
        List<Model> allModels = selectEnabled();
        
        // 如果令牌有模型限制，进行过滤
        if (tokenAllowedModels != null && !tokenAllowedModels.isEmpty()) {
            Set<String> allowedSet = Arrays.stream(tokenAllowedModels.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            
            return allModels.stream()
                    .filter(m -> allowedSet.contains(m.getModelId()))
                    .collect(Collectors.toList());
        }
        
        return allModels;
    }

    @Override
    public Long calculateCost(Model model, Integer inputTokens, Integer outputTokens, Boolean cacheHit) {
        if (model.isPerCallBilling()) {
            // 按次计费
            return model.getPricePerCall().multiply(BigDecimal.valueOf(100)).longValue();
        }
        
        if (model.isTokenBilling()) {
            // 按Token计费
            long cost = 0;
            
            // 输入Token费用
            if (inputTokens != null && inputTokens > 0) {
                cost += model.getInputPrice()
                        .multiply(BigDecimal.valueOf(inputTokens))
                        .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();
            }
            
            // 输出Token费用
            if (outputTokens != null && outputTokens > 0) {
                cost += model.getOutputPrice()
                        .multiply(BigDecimal.valueOf(outputTokens))
                        .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();
            }
            
            // 缓存命中折扣
            if (Boolean.TRUE.equals(cacheHit)) {
                // TODO: 从系统设置获取缓存计费比例
                cost = (long) (cost * 0.5); // 默认5折
            }
            
            return cost;
        }
        
        return 0L; // 免费模型
    }

    @Override
    public PageResult<Model> pageModels(int page, int size, String keyword, String type, Boolean enabled) {
        Page<Model> pageParam = new Page<>(page, size);
        IPage<Model> pageResult = modelMapper.selectModelPage(pageParam, keyword, type, enabled);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public List<Model> getModelsByType(String type) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Model::getEnabled, true)
                .eq(Model::getDeleted, false);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Model::getType, type);
        }
        
        wrapper.orderByAsc(Model::getName);
        
        return list(wrapper);
    }

    @Override
    public boolean isModelAllowed(Model model, String allowedModels) {
        if (allowedModels == null || allowedModels.isEmpty()) {
            return true; // 无限制
        }
        
        Set<String> allowedSet = Arrays.stream(allowedModels.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        
        return allowedSet.contains(model.getModelId());
    }

    @Override
    public Map<String, Object> getModelPricing(String modelId) {
        Model model = getByModelId(modelId);
        if (model == null) {
            throw BizException.modelNotFound();
        }

        Map<String, Object> pricing = new HashMap<>();
        pricing.put("modelId", model.getModelId());
        pricing.put("name", model.getName());
        pricing.put("type", model.getType());
        pricing.put("pricePerCall", model.getPricePerCall());
        pricing.put("inputPrice", model.getInputPrice());
        pricing.put("outputPrice", model.getOutputPrice());
        pricing.put("maxInputLength", model.getMaxInputLength());
        pricing.put("maxOutputLength", model.getMaxOutputLength());
        pricing.put("supportsStreaming", model.getSupportsStreaming());
        pricing.put("supportsFunctionCall", model.getSupportsFunctionCall());
        pricing.put("supportsVision", model.getSupportsVision());
        
        return pricing;
    }

    @Override
    public List<Model> getModelsWithCapabilities(Boolean supportsStreaming,
                                                Boolean supportsFunctionCall,
                                                Boolean supportsVision) {
        return modelMapper.selectByCapabilities(supportsStreaming, supportsFunctionCall, supportsVision);
    }

    @Override
    @CacheEvict(value = "models", allEntries = true)
    public void syncModelsToCache() {
        // 刷新模型缓存
        log.info("同步模型到缓存");
    }

    @Override
    public List<Model> selectEnabled() {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Model::getEnabled, true)
                .eq(Model::getDeleted, false)
                .orderByAsc(Model::getType)
                .orderByAsc(Model::getName);
        return list(wrapper);
    }
}
