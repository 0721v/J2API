package com.apiplatform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * AI渠道适配器 - 统一处理各类AI服务商的API调用
 *
 * @author API Platform Team
 */
@Slf4j
@Component
public class ChannelAdapter {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ChannelAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用AI渠道
     */
    public AIResponse call(ChannelConfig config, Map<String, Object> request) {
        try {
            // 转换请求格式
            Map<String, Object> adaptedRequest = adaptRequest(config, request);

            // 构建HTTP请求
            HttpRequest httpRequest = buildRequest(config, adaptedRequest);

            // 发送请求
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            return parseResponse(response, config);

        } catch (Exception e) {
            log.error("AI渠道调用失败: channel={}, error={}", config.getName(), e.getMessage());
            return AIResponse.error(e.getMessage());
        }
    }

    /**
     * 异步调用AI渠道
     */
    public CompletableFuture<AIResponse> callAsync(ChannelConfig config, Map<String, Object> request) {
        return CompletableFuture.supplyAsync(() -> call(config, request));
    }

    /**
     * 根据渠道类型适配请求格式
     */
    private Map<String, Object> adaptRequest(ChannelConfig config, Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>(request);

        switch (config.getType()) {
            case "openai":
                return adaptOpenAIRequest(adapted);
            case "anthropic":
                return adaptClaudeRequest(adapted);
            case "gemini":
                return adaptGeminiRequest(adapted);
            case "groq":
                return adaptGroqRequest(adapted);
            case "cohere":
                return adaptCohereRequest(adapted);
            case "mistral":
                return adaptMistralRequest(adapted);
            case "azure":
                return adaptAzureRequest(adapted);
            case "ollama":
                return adaptOllamaRequest(adapted);
            default:
                return adapted;
        }
    }

    // ==================== OpenAI ====================

    private Map<String, Object> adaptOpenAIRequest(Map<String, Object> request) {
        // OpenAI格式已经是标准格式，直接返回
        return request;
    }

    // ==================== Claude (Anthropic) ====================

    private Map<String, Object> adaptClaudeRequest(Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>();

        // 模型映射
        String model = (String) request.get("model");
        adapted.put("model", mapModel("anthropic", model));

        // 消息格式转换
        Object messages = request.get("messages");
        if (messages instanceof List) {
            adapted.put("messages", messages);
        }

        // 参数映射
        if (request.containsKey("temperature")) {
            adapted.put("temperature", request.get("temperature"));
        }
        if (request.containsKey("max_tokens")) {
            adapted.put("max_tokens", request.get("max_tokens"));
        } else {
            adapted.put("max_tokens", 4096); // Claude默认值
        }
        if (request.containsKey("stream")) {
            adapted.put("stream", request.get("stream"));
        }

        // Anthropic特定的系统消息处理
        Object systemMessage = extractSystemMessage((List<?>) messages);
        if (systemMessage != null) {
            adapted.put("system", systemMessage);
        }

        return adapted;
    }

    // ==================== Google Gemini ====================

    private Map<String, Object> adaptGeminiRequest(Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>();

        // 模型映射
        String model = (String) request.get("model");
        adapted.put("model", "models/" + mapModel("gemini", model));

        // 消息格式转换为Gemini格式
        Object messages = request.get("messages");
        if (messages instanceof List) {
            adapted.put("contents", convertToGeminiContents((List<?>) messages));
        }

        // 参数映射
        if (request.containsKey("temperature")) {
            adapted.put("generationConfig", Map.of(
                    "temperature", request.get("temperature"),
                    "maxOutputTokens", request.getOrDefault("max_tokens", 2048)
            ));
        }

        // 安全设置
        adapted.put("safetySettings", Arrays.asList(
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_NONE")
        ));

        return adapted;
    }

    private List<Map<String, Object>> convertToGeminiContents(List<?> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();

        for (Object msg : messages) {
            if (msg instanceof Map) {
                Map<?, ?> message = (Map<?, ?>) msg;
                String role = (String) message.get("role");
                Object content = message.get("content");

                // Gemini使用role作为parts的role
                Map<String, Object> part = new HashMap<>();
                part.put("role", "user".equals(role) ? "user" : "model");
                part.put("parts", Collections.singletonList(
                        Collections.singletonMap("text", content != null ? content.toString() : "")
                ));

                contents.add(part);
            }
        }

        return contents;
    }

    // ==================== Groq ====================

    private Map<String, Object> adaptGroqRequest(Map<String, Object> request) {
        // Groq兼容OpenAI格式
        Map<String, Object> adapted = new HashMap<>(request);
        adapted.put("model", mapModel("groq", (String) request.get("model")));
        return adapted;
    }

    // ==================== Cohere ====================

    private Map<String, Object> adaptCohereRequest(Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>();

        String model = (String) request.get("model");
        String mappedModel = mapModel("cohere", model);

        if (mappedModel.contains("embed")) {
            // Embedding请求
            adapted.put("model", mappedModel.replace("embed-", ""));
            Object input = request.get("input");
            if (input instanceof List) {
                adapted.put("texts", input);
            } else {
                adapted.put("texts", Collections.singletonList(input));
            }
        } else {
            // Chat请求
            adapted.put("model", mappedModel);

            Object messages = request.get("messages");
            if (messages instanceof List) {
                adapted.put("message", extractLastUserMessage((List<?>) messages));
                adapted.put("chat_history", convertToCohereHistory((List<?>) messages));
            }

            if (request.containsKey("temperature")) {
                adapted.put("temperature", request.get("temperature"));
            }
            if (request.containsKey("max_tokens")) {
                adapted.put("max_tokens", request.get("max_tokens"));
            }
        }

        return adapted;
    }

    // ==================== Mistral ====================

    private Map<String, Object> adaptMistralRequest(Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>();

        adapted.put("model", mapModel("mistral", (String) request.get("model")));

        Object messages = request.get("messages");
        if (messages instanceof List) {
            adapted.put("messages", messages);
        }

        if (request.containsKey("temperature")) {
            adapted.put("temperature", request.get("temperature"));
        }
        if (request.containsKey("max_tokens")) {
            adapted.put("max_tokens", request.get("max_tokens"));
        }

        return adapted;
    }

    // ==================== Azure OpenAI ====================

    private Map<String, Object> adaptAzureRequest(Map<String, Object> request) {
        // Azure OpenAI使用OpenAI格式，但需要调整API版本
        Map<String, Object> adapted = new HashMap<>(request);
        // 移除model参数，Azure使用部署名称
        adapted.remove("model");
        return adapted;
    }

    // ==================== Ollama ====================

    private Map<String, Object> adaptOllamaRequest(Map<String, Object> request) {
        Map<String, Object> adapted = new HashMap<>();

        adapted.put("model", mapModel("ollama", (String) request.get("model")));

        Object messages = request.get("messages");
        if (messages instanceof List) {
            // Ollama使用不同的消息格式
            StringBuilder prompt = new StringBuilder();
            for (Object msg : (List<?>) messages) {
                if (msg instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) msg;
                    String role = (String) m.get("role");
                    Object content = m.get("content");
                    prompt.append("\n").append(role).append(": ").append(content);
                }
            }
            adapted.put("prompt", prompt.toString());
        }

        if (request.containsKey("temperature")) {
            adapted.put("temperature", request.get("temperature"));
        }
        if (request.containsKey("stream")) {
            adapted.put("stream", request.get("stream"));
        }

        return adapted;
    }

    // ==================== 响应解析 ====================

    private AIResponse parseResponse(HttpResponse<String> response, ChannelConfig config) {
        int statusCode = response.statusCode();

        try {
            JsonNode jsonResponse = objectMapper.readTree(response.body());

            if (statusCode >= 200 && statusCode < 300) {
                return parseSuccessResponse(jsonResponse, config);
            } else {
                String error = parseErrorResponse(jsonResponse);
                return AIResponse.error(error);
            }
        } catch (Exception e) {
            return AIResponse.error("解析响应失败: " + e.getMessage());
        }
    }

    private AIResponse parseSuccessResponse(JsonNode response, ChannelConfig config) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setSuccess(true);

        switch (config.getType()) {
            case "openai":
            case "groq":
                aiResponse = parseOpenAIResponse(response);
                break;
            case "anthropic":
                aiResponse = parseClaudeResponse(response);
                break;
            case "gemini":
                aiResponse = parseGeminiResponse(response);
                break;
            case "cohere":
                aiResponse = parseCohereResponse(response);
                break;
            default:
                aiResponse = parseOpenAIResponse(response);
        }

        return aiResponse;
    }

    private AIResponse parseOpenAIResponse(JsonNode response) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setSuccess(true);

        // 解析Usage
        if (response.has("usage")) {
            JsonNode usage = response.get("usage");
            aiResponse.setInputTokens(usage.has("prompt_tokens") ? usage.get("prompt_tokens").asLong() : 0);
            aiResponse.setOutputTokens(usage.has("completion_tokens") ? usage.get("completion_tokens").asLong() : 0);
            aiResponse.setTotalTokens(usage.has("total_tokens") ? usage.get("total_tokens").asLong() : 0);
        }

        // 解析内容
        if (response.has("choices") && response.get("choices").isArray()) {
            JsonNode choice = response.get("choices").get(0);
            if (choice.has("message")) {
                aiResponse.setContent(choice.get("message").has("content") ?
                        choice.get("message").get("content").asText() : "");
            }
            aiResponse.setFinishReason(choice.has("finish_reason") ?
                    choice.get("finish_reason").asText() : "stop");
        }

        // 保留原始响应
        aiResponse.setRawResponse(response.toString());

        return aiResponse;
    }

    private AIResponse parseClaudeResponse(JsonNode response) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setSuccess(true);

        // 解析Usage
        if (response.has("usage")) {
            JsonNode usage = response.get("usage");
            aiResponse.setInputTokens(usage.has("input_tokens") ? usage.get("input_tokens").asLong() : 0);
            aiResponse.setOutputTokens(usage.has("output_tokens") ? usage.get("output_tokens").asLong() : 0);
            aiResponse.setTotalTokens(aiResponse.getInputTokens() + aiResponse.getOutputTokens());
        }

        // 解析内容
        if (response.has("content") && response.get("content").isArray()) {
            StringBuilder content = new StringBuilder();
            for (JsonNode block : response.get("content")) {
                if (block.has("text")) {
                    content.append(block.get("text").asText());
                }
            }
            aiResponse.setContent(content.toString());
        }

        aiResponse.setFinishReason(response.has("stop_reason") ?
                response.get("stop_reason").asText() : "end_turn");

        aiResponse.setRawResponse(response.toString());

        return aiResponse;
    }

    private AIResponse parseGeminiResponse(JsonNode response) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setSuccess(true);

        // 解析Usage
        if (response.has("usageMetadata")) {
            JsonNode usage = response.get("usageMetadata");
            aiResponse.setPromptTokens(usage.has("promptTokenCount") ? usage.get("promptTokenCount").asLong() : 0);
            aiResponse.setCandidatesTokenCount(usage.has("candidatesTokenCount") ? usage.get("candidatesTokenCount").asLong() : 0);
            aiResponse.setTotalTokens(usage.has("totalTokenCount") ? usage.get("totalTokenCount").asLong() : 0);
            aiResponse.setInputTokens(aiResponse.getPromptTokens());
            aiResponse.setOutputTokens(aiResponse.getCandidatesTokenCount());
        }

        // 解析内容
        if (response.has("candidates") && response.get("candidates").isArray()) {
            JsonNode candidate = response.get("candidates").get(0);
            if (candidate.has("content") && candidate.get("content").has("parts")) {
                StringBuilder content = new StringBuilder();
                for (JsonNode part : candidate.get("content").get("parts")) {
                    if (part.has("text")) {
                        content.append(part.get("text").asText());
                    }
                }
                aiResponse.setContent(content.toString());
            }
        }

        aiResponse.setFinishReason(response.has("finishReason") ?
                response.get("finishReason").asText() : "STOP");

        aiResponse.setRawResponse(response.toString());

        return aiResponse;
    }

    private AIResponse parseCohereResponse(JsonNode response) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setSuccess(true);

        // 解析Usage
        if (response.has("usage")) {
            JsonNode usage = response.get("usage");
            aiResponse.setInputTokens(usage.has("inputTokens") ? usage.get("inputTokens").asLong() : 0);
            aiResponse.setOutputTokens(usage.has("outputTokens") ? usage.get("outputTokens").asLong() : 0);
            aiResponse.setTotalTokens(aiResponse.getInputTokens() + aiResponse.getOutputTokens());
        }

        // 解析内容
        if (response.has("text")) {
            aiResponse.setContent(response.get("text").asText());
        } else if (response.has("generations")) {
            StringBuilder content = new StringBuilder();
            for (JsonNode gen : response.get("generations")) {
                if (gen.has("text")) {
                    content.append(gen.get("text").asText());
                }
            }
            aiResponse.setContent(content.toString());
        }

        aiResponse.setFinishReason("COMPLETED");

        aiResponse.setRawResponse(response.toString());

        return aiResponse;
    }

    private String parseErrorResponse(JsonNode response) {
        if (response.has("error")) {
            JsonNode error = response.get("error");
            if (error.isTextual()) {
                return error.asText();
            } else if (error.has("message")) {
                return error.get("message").asText();
            }
        }
        if (response.has("message")) {
            return response.get("message").asText();
        }
        return "未知错误";
    }

    // ==================== 工具方法 ====================

    private HttpRequest buildRequest(ChannelConfig config, Map<String, Object> request) {
        String url = config.getEndpoint();

        // 根据渠道类型调整URL
        switch (config.getType()) {
            case "anthropic":
                url += "/v1/messages";
                break;
            case "gemini":
                url += ":generateContent";
                break;
            case "cohere":
                if (config.getModel().contains("embed")) {
                    url += "/v1/embed";
                } else {
                    url += "/v1/chat";
                }
                break;
            default:
                url += "/v1/chat/completions";
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout() / 1000));

            // 添加认证头
            switch (config.getAuthType()) {
                case "bearer":
                    builder.header("Authorization", "Bearer " + config.getApiKey());
                    break;
                case "api-key":
                    builder.header("X-API-Key", config.getApiKey());
                    break;
                case "basic":
                    builder.header("Authorization", "Basic " + config.getApiKey());
                    break;
            }

            // Anthropic特定的请求头
            if ("anthropic".equals(config.getType())) {
                builder.header("anthropic-version", "2023-06-01");
            }

            // Azure特定的请求头
            if ("azure".equals(config.getType())) {
                builder.header("api-key", config.getApiKey());
            }

            // 添加自定义头
            if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
                Map<String, String> headers = parseCustomHeaders(config.getCustomHeaders());
                headers.forEach(builder::header);
            }

            return builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("构建请求失败", e);
        }
    }

    private Map<String, String> parseCustomHeaders(String headersJson) {
        try {
            return objectMapper.readValue(headersJson, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String extractLastUserMessage(List<?> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof Map) {
                Map<?, ?> msg = (Map<?, ?>) messages.get(i);
                if ("user".equals(msg.get("role"))) {
                    return msg.get("content") != null ? msg.get("content").toString() : "";
                }
            }
        }
        return "";
    }

    private List<Map<String, String>> convertToCohereHistory(List<?> messages) {
        List<Map<String, String>> history = new ArrayList<>();
        for (int i = 0; i < messages.size() - 1; i++) {
            if (messages.get(i) instanceof Map) {
                Map<?, ?> msg = (Map<?, ?>) messages.get(i);
                String role = (String) msg.get("role");
                if ("user".equals(role) || "assistant".equals(role)) {
                    history.add(Map.of(
                            "role", role,
                            "message", msg.get("content") != null ? msg.get("content").toString() : ""
                    ));
                }
            }
        }
        return history;
    }

    private Object extractSystemMessage(List<?> messages) {
        for (Object msg : messages) {
            if (msg instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) msg;
                if ("system".equals(m.get("role"))) {
                    return m.get("content");
                }
            }
        }
        return null;
    }

    /**
     * 模型名称映射
     */
    public String mapModel(String provider, String modelName) {
        // 默认映射表
        Map<String, Map<String, String>> modelMappings = new HashMap<>();

        // Anthropic模型
        Map<String, String> anthropicModels = new HashMap<>();
        anthropicModels.put("claude-3-opus", "claude-3-opus-20240229");
        anthropicModels.put("claude-3-sonnet", "claude-3-sonnet-20240229");
        anthropicModels.put("claude-3-haiku", "claude-3-haiku-20240307");
        anthropicModels.put("claude-2.1", "claude-2.1");
        anthropicModels.put("claude-2", "claude-2.0");
        anthropicModels.put("claude-instant", "claude-instant-1.2");
        modelMappings.put("anthropic", anthropicModels);

        // Google Gemini模型
        Map<String, String> geminiModels = new HashMap<>();
        geminiModels.put("gemini-pro", "gemini-1.5-pro");
        geminiModels.put("gemini-flash", "gemini-1.5-flash");
        geminiModels.put("gemini-1.5-pro", "gemini-1.5-pro");
        geminiModels.put("gemini-1.5-flash", "gemini-1.5-flash");
        geminiModels.put("gemini-1.0-pro", "gemini-1.0-pro");
        modelMappings.put("gemini", geminiModels);

        // Groq模型
        Map<String, String> groqModels = new HashMap<>();
        groqModels.put("llama-3.1-70b", "llama-3.1-70b-versatile");
        groqModels.put("llama-3.1-8b", "llama-3.1-8b-instant");
        groqModels.put("mixtral-8x7b", "mixtral-8x7b-32768");
        groqModels.put("gemma-7b", "gemma2-9b-it");
        modelMappings.put("groq", groqModels);

        // Cohere模型
        Map<String, String> cohereModels = new HashMap<>();
        cohereModels.put("command-r", "command-r-plus");
        cohereModels.put("command", "command");
        cohereModels.put("embed", "embed-english-v3.0");
        cohereModels.put("embed-multilingual", "embed-multilingual-v3.0");
        modelMappings.put("cohere", cohereModels);

        // Mistral模型
        Map<String, String> mistralModels = new HashMap<>();
        mistralModels.put("mistral-large", "mistral-large-latest");
        mistralModels.put("mistral-medium", "mistral-medium-latest");
        mistralModels.put("mistral-small", "mistral-small-latest");
        mistralModels.put("mistral-tiny", "mistral-tiny-latest");
        modelMappings.put("mistral", mistralModels);

        // 执行映射
        Map<String, String> providerMaps = modelMappings.get(provider);
        if (providerMaps != null && providerMaps.containsKey(modelName)) {
            return providerMaps.get(modelName);
        }

        return modelName;
    }

    // ==================== 内部类 ====================

    /**
     * 渠道配置
     */
    @lombok.Data
    public static class ChannelConfig {
        private Long id;
        private String name;
        private String type;
        private String subType;
        private String endpoint;
        private String apiKey;
        private String authType;
        private String customHeaders;
        private Integer timeout;
        private String model;
    }

    /**
     * AI响应
     */
    @lombok.Data
    public static class AIResponse {
        private boolean success;
        private String content;
        private String finishReason;
        private long inputTokens;
        private long outputTokens;
        private long totalTokens;
        private long promptTokens;
        private long candidatesTokenCount;
        private String rawResponse;
        private String error;

        public static AIResponse error(String message) {
            AIResponse response = new AIResponse();
            response.setSuccess(false);
            response.setError(message);
            return response;
        }
    }
}
