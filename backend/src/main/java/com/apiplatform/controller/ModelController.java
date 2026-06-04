package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Model;
import com.apiplatform.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 模型控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
@Tag(name = "模型管理", description = "AI模型配置管理相关接口")
public class ModelController {

    private final ModelService modelService;

    /**
     * 获取模型列表
     */
    @GetMapping
    @Operation(summary = "获取模型列表", description = "获取所有启用的AI模型列表")
    public Result<List<Model>> getModels() {
        List<Model> models = modelService.selectEnabled();
        return Result.success(models);
    }

    /**
     * 获取模型列表（分页）
     */
    @GetMapping("/page")
    @Operation(summary = "获取模型列表（分页）", description = "分页获取模型列表")
    public Result<PageResult<Model>> getModelsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean enabled) {
        
        PageResult<Model> result = modelService.pageModels(page, size, keyword, type, enabled);
        return Result.success(result);
    }

    /**
     * 根据类型获取模型
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "按类型获取模型", description = "获取指定类型的模型列表")
    public Result<List<Model>> getModelsByType(@PathVariable String type) {
        List<Model> models = modelService.getModelsByType(type);
        return Result.success(models);
    }

    /**
     * 获取模型详情
     */
    @GetMapping("/{modelId}")
    @Operation(summary = "获取模型详情", description = "获取指定模型的详细信息")
    public Result<Model> getModel(@PathVariable String modelId) {
        Model model = modelService.getByModelId(modelId);
        if (model == null) {
            return Result.notFound("模型不存在");
        }
        return Result.success(model);
    }

    /**
     * 获取模型价格
     */
    @GetMapping("/{modelId}/pricing")
    @Operation(summary = "获取模型价格", description = "获取指定模型的价格信息")
    public Result<Map<String, Object>> getModelPricing(@PathVariable String modelId) {
        Map<String, Object> pricing = modelService.getModelPricing(modelId);
        return Result.success(pricing);
    }

    /**
     * 创建模型（管理员）
     */
    @PostMapping
    @Operation(summary = "创建模型", description = "创建新的AI模型配置")
    public Result<Model> createModel(@Validated @RequestBody CreateModelRequest request) {
        try {
            Model model = modelService.createModel(
                    request.getModelId(),
                    request.getName(),
                    request.getType(),
                    request.getChannelType(),
                    request.getAllowedChannels(),
                    request.getPricePerCall(),
                    request.getInputPrice(),
                    request.getOutputPrice(),
                    request.getMaxInputLength(),
                    request.getMaxOutputLength(),
                    request.getSupportsStreaming(),
                    request.getSupportsFunctionCall(),
                    request.getSupportsVision(),
                    request.getDescription(),
                    request.getTags()
            );
            return Result.success("模型创建成功", model);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新模型（管理员）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新模型", description = "更新指定模型的配置")
    public Result<Model> updateModel(
            @PathVariable Long id,
            @Validated @RequestBody UpdateModelRequest request) {
        try {
            Model model = modelService.updateModel(
                    id,
                    request.getModelId(),
                    request.getName(),
                    request.getType(),
                    request.getChannelType(),
                    request.getAllowedChannels(),
                    request.getPricePerCall(),
                    request.getInputPrice(),
                    request.getOutputPrice(),
                    request.getMaxInputLength(),
                    request.getMaxOutputLength(),
                    request.getSupportsStreaming(),
                    request.getSupportsFunctionCall(),
                    request.getSupportsVision(),
                    request.getDescription(),
                    request.getTags()
            );
            return Result.success("模型更新成功", model);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除模型（管理员）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型", description = "删除指定的AI模型")
    public Result<Void> deleteModel(@PathVariable Long id) {
        try {
            modelService.deleteModel(id);
            return Result.success("模型删除成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 启用模型（管理员）
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用模型", description = "启用指定的AI模型")
    public Result<Void> enableModel(@PathVariable Long id) {
        modelService.enableModel(id);
        return Result.success("模型已启用", null);
    }

    /**
     * 禁用模型（管理员）
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用模型", description = "禁用指定的AI模型")
    public Result<Void> disableModel(@PathVariable Long id) {
        modelService.disableModel(id);
        return Result.success("模型已禁用", null);
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreateModelRequest {
        private String modelId;
        private String name;
        private String type = "chat";
        private String channelType;
        private String allowedChannels;
        private BigDecimal pricePerCall = BigDecimal.ZERO;
        private BigDecimal inputPrice = BigDecimal.ZERO;
        private BigDecimal outputPrice = BigDecimal.ZERO;
        private Integer maxInputLength = 128000;
        private Integer maxOutputLength = 4096;
        private Boolean supportsStreaming = true;
        private Boolean supportsFunctionCall = false;
        private Boolean supportsVision = false;
        private String description;
        private String tags;
    }

    @Data
    public static class UpdateModelRequest {
        private String modelId;
        private String name;
        private String type;
        private String channelType;
        private String allowedChannels;
        private BigDecimal pricePerCall;
        private BigDecimal inputPrice;
        private BigDecimal outputPrice;
        private Integer maxInputLength;
        private Integer maxOutputLength;
        private Boolean supportsStreaming;
        private Boolean supportsFunctionCall;
        private Boolean supportsVision;
        private String description;
        private String tags;
    }
}
