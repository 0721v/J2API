package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.entity.Channel;
import com.apiplatform.service.ChannelService;
import com.apiplatform.service.ChannelTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 渠道模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/channels")
@RequiredArgsConstructor
@Tag(name = "渠道模板", description = "渠道配置模板相关接口")
public class ChannelTemplateController {

    private final ChannelTemplateService channelTemplateService;
    private final ChannelService channelService;

    /**
     * 获取所有渠道模板
     */
    @GetMapping("/templates")
    @Operation(summary = "获取渠道模板列表", description = "获取所有可用的渠道配置模板")
    public Result<List<ChannelTemplateService.ChannelTemplate>> getAllTemplates() {
        return Result.success(channelTemplateService.getAllTemplates());
    }

    /**
     * 按分类获取渠道模板
     */
    @GetMapping("/templates/category/{category}")
    @Operation(summary = "按分类获取模板", description = "根据分类获取渠道模板")
    public Result<List<ChannelTemplateService.ChannelTemplate>> getTemplatesByCategory(
            @PathVariable String category) {
        return Result.success(channelTemplateService.getTemplatesByCategory(category));
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/templates/{templateId}")
    @Operation(summary = "获取模板详情", description = "获取指定模板的详细信息")
    public Result<ChannelTemplateService.ChannelTemplate> getTemplate(@PathVariable Long templateId) {
        ChannelTemplateService.ChannelTemplate template = channelTemplateService.getTemplate(templateId);
        if (template == null) {
            return Result.notFound("模板不存在");
        }
        return Result.success(template);
    }

    /**
     * 从模板创建渠道
     */
    @PostMapping("/templates/{templateId}")
    @Operation(summary = "从模板创建渠道", description = "使用模板快速创建渠道")
    public Result<Channel> createFromTemplate(
            @PathVariable Long templateId,
            @RequestBody @Validated CreateFromTemplateRequest request) {

        try {
            Channel channel = channelTemplateService.createFromTemplate(templateId, request.getApiKey());
            return Result.success(channel);
        } catch (Exception e) {
            log.error("从模板创建渠道失败: {}", e.getMessage());
            return Result.error(500, e.getMessage());
        }
    }

    // ==================== 渠道管理（扩展） ====================

    /**
     * 获取渠道支持的模型列表
     */
    @GetMapping("/{channelId}/models")
    @Operation(summary = "获取渠道支持的模型", description = "获取指定渠道支持的模型列表")
    public Result<List<String>> getChannelModels(@PathVariable Long channelId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            return Result.notFound("渠道不存在");
        }

        List<String> models = List.of();
        if (channel.getModelMapping() != null && !channel.getModelMapping().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> mapping = mapper.readValue(
                        channel.getModelMapping(), java.util.Map.class);
                models = new java.util.ArrayList<>(mapping.keySet());
            } catch (Exception e) {
                log.error("解析模型映射失败", e);
            }
        }

        return Result.success(models);
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreateFromTemplateRequest {
        @Parameter(description = "API密钥")
        private String apiKey;
    }
}
