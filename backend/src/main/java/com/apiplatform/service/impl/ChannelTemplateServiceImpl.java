package com.apiplatform.service.impl;

import com.apiplatform.entity.Channel;
import com.apiplatform.service.ChannelService;
import com.apiplatform.service.ChannelTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 渠道模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelTemplateServiceImpl implements ChannelTemplateService {

    private final ChannelService channelService;
    private final ObjectMapper objectMapper;

    private final Map<Long, ChannelTemplate> templates = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        initTemplates();
    }

    private void initTemplates() {
        // ========== OpenAI ==========
        templates.put(1L, new ChannelTemplate() {{
            setId(1L);
            setName("OpenAI 官方");
            setCategory("openai");
            setType("openai");
            setSubType("chat");
            setEndpoint("https://api.openai.com");
            setAuthType("bearer");
            setDefaultModel("gpt-3.5-turbo");
            setDefaultTimeout(120000);
            setDescription("OpenAI官方渠道，支持GPT-4、GPT-3.5等模型");
            setDocUrl("https://platform.openai.com/docs/api-reference");
            setSupportedModels(Arrays.asList(
                    "gpt-4-turbo", "gpt-4", "gpt-4-32k",
                    "gpt-3.5-turbo", "gpt-3.5-turbo-16k",
                    "gpt-4o", "gpt-4o-mini"
            ));
            setPricingUrl("https://openai.com/pricing");
        }});

        // ========== Anthropic (Claude) ==========
        templates.put(2L, new ChannelTemplate() {{
            setId(2L);
            setName("Anthropic Claude");
            setCategory("anthropic");
            setType("anthropic");
            setSubType("chat");
            setEndpoint("https://api.anthropic.com");
            setAuthType("bearer");
            setDefaultModel("claude-3-5-sonnet");
            setDefaultTimeout(120000);
            setDescription("Anthropic官方渠道，支持Claude 3系列模型");
            setDocUrl("https://docs.anthropic.com/claude/reference");
            setSupportedModels(Arrays.asList(
                    "claude-3-5-sonnet", "claude-3-5-haiku",
                    "claude-3-opus", "claude-3-sonnet", "claude-3-haiku",
                    "claude-2.1", "claude-2.0", "claude-instant"
            ));
            setPricingUrl("https://www.anthropic.com/pricing");
        }});

        // ========== Google Gemini ==========
        templates.put(3L, new ChannelTemplate() {{
            setId(3L);
            setName("Google Gemini");
            setCategory("google");
            setType("gemini");
            setSubType("chat");
            setEndpoint("https://generativelanguage.googleapis.com");
            setAuthType("api-key");
            setDefaultModel("gemini-1.5-flash");
            setDefaultTimeout(60000);
            setDescription("Google AI Gemini模型，支持长上下文");
            setDocUrl("https://ai.google.dev/docs");
            setSupportedModels(Arrays.asList(
                    "gemini-2.0-flash", "gemini-1.5-pro", "gemini-1.5-flash",
                    "gemini-1.0-pro", "gemini-pro"
            ));
            setPricingUrl("https://ai.google.dev/pricing");
        }});

        // ========== Azure OpenAI ==========
        templates.put(4L, new ChannelTemplate() {{
            setId(4L);
            setName("Azure OpenAI");
            setCategory("azure");
            setType("azure");
            setSubType("chat");
            setEndpoint("https://{your-resource}.openai.azure.com");
            setAuthType("api-key");
            setDefaultModel("gpt-35-turbo");
            setDefaultTimeout(120000);
            setDescription("Azure平台OpenAI服务，企业级支持");
            setDocUrl("https://learn.microsoft.com/azure/ai-services/openai/");
            setSupportedModels(Arrays.asList(
                    "gpt-4-turbo", "gpt-4", "gpt-4-32k",
                    "gpt-35-turbo", "gpt-35-turbo-16k"
            ));
            setPricingUrl("https://azure.microsoft.com/pricing/details/cognitive-services/openai-service/");
        }});

        // ========== Groq ==========
        templates.put(5L, new ChannelTemplate() {{
            setId(5L);
            setName("Groq");
            setCategory("groq");
            setType("groq");
            setSubType("chat");
            setEndpoint("https://api.groq.com/openai/v1");
            setAuthType("bearer");
            setDefaultModel("llama-3.1-70b");
            setDefaultTimeout(30000);
            setDescription("Groq高速推理平台，LPU芯片加速");
            setDocUrl("https://console.groq.com/docs/models");
            setSupportedModels(Arrays.asList(
                    "llama-3.3-70b", "llama-3.1-70b", "llama-3.1-8b",
                    "mixtral-8x7b", "gemma2-9b-it"
            ));
            setPricingUrl("https://console.groq.com/pricing");
        }});

        // ========== Cohere ==========
        templates.put(6L, new ChannelTemplate() {{
            setId(6L);
            setName("Cohere");
            setCategory("cohere");
            setType("cohere");
            setSubType("chat");
            setEndpoint("https://api.cohere.ai");
            setAuthType("bearer");
            setDefaultModel("command-r-plus");
            setDefaultTimeout(60000);
            setDescription("Cohere AI，支持Command和Embed模型");
            setDocUrl("https://docs.cohere.com/");
            setSupportedModels(Arrays.asList(
                    "command-r-plus", "command-r", "command",
                    "command-light", "command-light-night",
                    "embed-english-v3.0", "embed-multilingual-v3.0"
            ));
            setPricingUrl("https://cohere.com/pricing");
        }});

        // ========== Mistral AI ==========
        templates.put(7L, new ChannelTemplate() {{
            setId(7L);
            setName("Mistral AI");
            setCategory("mistral");
            setType("mistral");
            setSubType("chat");
            setEndpoint("https://api.mistral.ai/v1");
            setAuthType("bearer");
            setDefaultModel("mistral-large-latest");
            setDefaultTimeout(120000);
            setDescription("Mistral AI法国开源模型");
            setDocUrl("https://docs.mistral.ai/");
            setSupportedModels(Arrays.asList(
                    "mistral-large-latest", "mistral-medium-latest",
                    "mistral-small-latest", "mistral-tiny-latest",
                    "mistral-7b-instruct", "mixtral-8x7b-instruct"
            ));
            setPricingUrl("https://mistral.ai/pricing/");
        }});

        // ========== Together AI ==========
        templates.put(8L, new ChannelTemplate() {{
            setId(8L);
            setName("Together AI");
            setCategory("together");
            setType("openai");
            setSubType("chat");
            setEndpoint("https://api.together.xyz/v1");
            setAuthType("bearer");
            setDefaultModel("meta-llama/Llama-3-70b-chat-hf");
            setDefaultTimeout(120000);
            setDescription("Together AI开源模型聚合平台");
            setDocUrl("https://docs.together.ai/");
            setSupportedModels(Arrays.asList(
                    "meta-llama/Llama-3-70b-chat-hf",
                    "meta-llama/Llama-3-8b-chat-hf",
                    "mistralai/Mixtral-8x22B-Instruct-v0.1",
                    "Qwen/Qwen1.5-72B-Chat"
            ));
            setPricingUrl("https://together.ai/pricing");
        }});

        // ========== Fireworks AI ==========
        templates.put(9L, new ChannelTemplate() {{
            setId(9L);
            setName("Fireworks AI");
            setCategory("fireworks");
            setType("openai");
            setSubType("chat");
            setEndpoint("https://api.fireworks.ai/inference/v1");
            setAuthType("bearer");
            setDefaultModel("accounts/fireworks/models/llama-v3-70b-instruct");
            setDefaultTimeout(120000);
            setDescription("Fireworks AI高性能推理平台");
            setDocUrl("https://docs.fireworks.ai/");
            setSupportedModels(Arrays.asList(
                    "accounts/fireworks/models/llama-v3-70b-instruct",
                    "accounts/fireworks/models/llama-v3-8b-instruct",
                    "accounts/fireworks/models/mixtral-8x22b-instruct"
            ));
            setPricingUrl("https://fireworks.ai/pricing");
        }});

        // ========== Perplexity ==========
        templates.put(10L, new ChannelTemplate() {{
            setId(10L);
            setName("Perplexity");
            setCategory("perplexity");
            setType("openai");
            setSubType("chat");
            setEndpoint("https://api.perplexity.ai");
            setAuthType("bearer");
            setDefaultModel("llama-3.1-sonar-small-128k-online");
            setDefaultTimeout(60000);
            setDescription("Perplexity AI，支持在线搜索的模型");
            setDocUrl("https://docs.perplexity.ai/");
            setSupportedModels(Arrays.asList(
                    "llama-3.1-sonar-large-128k-online",
                    "llama-3.1-sonar-small-128k-online",
                    "llama-3.1-sonar-huge-128k-online"
            ));
            setPricingUrl("https://perplexity.ai/pro");
        }});

        // ========== Ollama (本地) ==========
        templates.put(11L, new ChannelTemplate() {{
            setId(11L);
            setName("Ollama (本地部署)");
            setCategory("ollama");
            setType("ollama");
            setSubType("chat");
            setEndpoint("http://localhost:11434/api");
            setAuthType("none");
            setDefaultModel("llama3");
            setDefaultTimeout(300000);
            setDescription("Ollama本地部署，开源模型离线运行");
            setDocUrl("https://github.com/ollama/ollama");
            setSupportedModels(Arrays.asList(
                    "llama3", "llama3.1", "llama2",
                    "mistral", "mixtral",
                    "codellama", "phi3",
                    "qwen2", "baichuan"
            ));
            setPricingUrl("");
        }});

        // ========== LM Studio (本地) ==========
        templates.put(12L, new ChannelTemplate() {{
            setId(12L);
            setName("LM Studio (本地部署)");
            setCategory("lmstudio");
            setType("openai");
            setSubType("chat");
            setEndpoint("http://localhost:1234/v1");
            setAuthType("bearer");
            setDefaultModel("local-model");
            setDefaultTimeout(300000);
            setDescription("LM Studio本地部署，OpenAI兼容接口");
            setDocUrl("https://lmstudio.ai/");
            setSupportedModels(Arrays.asList(
                    "local-model"
            ));
            setPricingUrl("");
        }});

        log.info("已加载 {} 个渠道模板", templates.size());
    }

    @Override
    public List<ChannelTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    @Override
    public List<ChannelTemplate> getTemplatesByCategory(String category) {
        return templates.values().stream()
                .filter(t -> category.equals(t.getCategory()))
                .toList();
    }

    @Override
    public Channel createFromTemplate(Long templateId, String apiKey) {
        ChannelTemplate template = templates.get(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateId);
        }

        String modelMapping = null;
        try {
            if (template.getSupportedModels() != null && !template.getSupportedModels().isEmpty()) {
                Map<String, String> mapping = new HashMap<>();
                for (String model : template.getSupportedModels()) {
                    mapping.put(model, model); // 一对一映射
                }
                modelMapping = objectMapper.writeValueAsString(mapping);
            }
        } catch (JsonProcessingException e) {
            log.error("序列化模型映射失败", e);
        }

        return channelService.createChannel(
                template.getName(),
                template.getType(),
                template.getSubType(),
                template.getEndpoint(),
                apiKey,
                template.getAuthType(),
                null,
                template.getDefaultTimeout(),
                modelMapping,
                template.getDefaultModel(),
                100, // 默认权重
                null,
                0,
                3,
                null,
                null,
                template.getDescription()
        );
    }

    @Override
    public ChannelTemplate getTemplate(Long templateId) {
        return templates.get(templateId);
    }
}
