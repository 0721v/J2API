package com.apiplatform.service;

import com.apiplatform.entity.Channel;
import java.util.List;
import java.util.Map;

/**
 * 渠道模板服务接口
 * 提供预设的渠道配置模板
 *
 * @author API Platform Team
 */
public interface ChannelTemplateService {

    /**
     * 获取所有可用渠道模板
     */
    List<ChannelTemplate> getAllTemplates();

    /**
     * 根据类型获取模板
     */
    List<ChannelTemplate> getTemplatesByCategory(String category);

    /**
     * 创建渠道（使用模板）
     */
    Channel createFromTemplate(Long templateId, String apiKey);

    /**
     * 获取模板详情
     */
    ChannelTemplate getTemplate(Long templateId);

    /**
     * 渠道模板
     */
    @lombok.Data
    class ChannelTemplate {
        private Long id;
        private String name;                    // 模板名称
        private String category;                // 分类: openai/anthropic/google/groq/cohere/mistral/other
        private String type;                    // 渠道类型
        private String subType;                  // 子类型: chat/embedding/realtime
        private String endpoint;                 // API端点
        private String authType;                // 认证类型
        private String defaultModel;             // 默认模型
        private String modelMapping;            // 模型映射配置
        private Integer defaultTimeout;          // 默认超时
        private String description;              // 描述
        private String docUrl;                   // 官方文档链接
        private List<String> supportedModels;    // 支持的模型列表
        private String pricingUrl;              // 价格页面链接
    }
}
