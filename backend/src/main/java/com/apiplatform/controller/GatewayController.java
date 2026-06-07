package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.entity.Token;
import com.apiplatform.service.BillingService;
import com.apiplatform.service.ChannelService;
import com.apiplatform.service.ModelService;
import com.apiplatform.service.RateLimitService;
import com.apiplatform.service.TokenService;
import com.apiplatform.service.UsageLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI API网关控制器
 * 提供OpenAI兼容的API接口
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final TokenService tokenService;
    private final ModelService modelService;
    private final ChannelService channelService;
    private final BillingService billingService;
    private final RateLimitService rateLimitService;
    private final UsageLogService usageLogService;

    // ==================== Chat Completions ====================

    /**
     * Chat Completions API
     * POST /v1/chat/completions
     */
    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> request) {
        
        String apiKey = extractApiKey(auth);
        Token token = validateAndGetToken(apiKey);
        
        // 限流检查
        if (!rateLimitService.checkMinuteLimit(token)) {
            throw BizException.rateLimitExceeded("每分钟请求");
        }
        if (!rateLimitService.checkDayLimit(token)) {
            throw BizException.rateLimitExceeded("每日请求");
        }
        
        // 获取模型信息
        String modelName = (String) request.get("model");
        var model = modelService.getByModelId(modelName);
        if (model == null) {
            throw BizException.modelNotFound();
        }
        
        // 选择渠道
        var channel = channelService.selectChannel(modelName, model.getChannelType());
        if (channel == null) {
            throw BizException.channelNotFound();
        }
        
        // 计算费用
        int inputTokens = estimateTokens(request);
        int outputTokens = 0;
        boolean cacheHit = false;
        Long cost = billingService.calculateRequestCost(token, model, channel, inputTokens, outputTokens, cacheHit);
        
        // 扣费
        if (!billingService.charge(token, token.getUserId(), cost, "Chat API: " + modelName)) {
            throw BizException.insufficientBalance();
        }
        
        // 记录使用
        usageLogService.recordUsage(
                token.getUserId(), token.getId(), model.getId(), modelName,
                channel.getId(), channel.getName(), "chat",
                inputTokens, outputTokens, cacheHit, cost,
                System.currentTimeMillis(), 200, "success",
                null, null, "/v1/chat/completions"
        );
        
        // 更新限流计数
        rateLimitService.incrementRequestCount(token);
        
        // TODO: 实际调用AI渠道并返回响应
        Map<String, Object> response = buildMockResponse(request, modelName);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 流式Chat Completions
     */
    @PostMapping("/chat/completions-stream")
    public SseEmitter chatCompletionsStream(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> request) {
        
        String apiKey = extractApiKey(auth);
        Token token = validateAndGetToken(apiKey);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        CompletableFuture.runAsync(() -> {
            try {
                // 限流检查
                if (!rateLimitService.checkMinuteLimit(token)) {
                    emitter.completeWithError(BizException.rateLimitExceeded("每分钟请求"));
                    return;
                }
                
                String modelName = (String) request.get("model");
                var model = modelService.getByModelId(modelName);
                if (model == null) {
                    emitter.completeWithError(BizException.modelNotFound());
                    return;
                }
                
                // 流式处理...
                // 实际实现需要调用AI渠道并逐块发送SSE事件
                
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }

    // ==================== Completions ====================

    /**
     * Text Completions API
     * POST /v1/completions
     */
    @PostMapping("/completions")
    public ResponseEntity<?> completions(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> request) {
        
        String apiKey = extractApiKey(auth);
        Token token = validateAndGetToken(apiKey);
        
        // 类似chat completions的处理逻辑
        Map<String, Object> response = buildMockCompletionResponse(request);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Embeddings ====================

    /**
     * Embeddings API
     * POST /v1/embeddings
     */
    @PostMapping("/embeddings")
    public ResponseEntity<?> embeddings(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> request) {
        
        String apiKey = extractApiKey(auth);
        Token token = validateAndGetToken(apiKey);
        
        String modelName = (String) request.get("model");
        var model = modelService.getByModelId(modelName);
        if (model == null) {
            throw BizException.modelNotFound();
        }
        
        // 处理embedding请求...
        Map<String, Object> response = buildMockEmbeddingResponse(request, modelName);
        
        return ResponseEntity.ok(response);
    }

    // ==================== Models ====================

    /**
     * List Models
     * GET /v1/models
     */
    @GetMapping("/models")
    public ResponseEntity<?> listModels() {
        var models = modelService.selectEnabled();
        
        Map<String, Object> response = Map.of(
                "object", "list",
                "data", models.stream().map(m -> Map.of(
                        "id", m.getModelId(),
                        "object", "model",
                        "created", System.currentTimeMillis() / 1000,
                        "owned_by", m.getChannelType()
                )).toList()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get Model
     * GET /v1/models/{model}
     */
    @GetMapping("/models/{model}")
    public ResponseEntity<?> getModel(@PathVariable String model) {
        var modelEntity = modelService.getByModelId(model);
        if (modelEntity == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = Map.of(
                "id", modelEntity.getModelId(),
                "object", "model",
                "created", System.currentTimeMillis() / 1000,
                "owned_by", modelEntity.getChannelType()
        );
        
        return ResponseEntity.ok(response);
    }

    // ==================== 私有方法 ====================

    /**
     * 从Authorization头提取API Key
     */
    private String extractApiKey(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw BizException.unauthorized("缺少有效的API Key");
        }
        return auth.substring(7);
    }

    /**
     * 验证并获取令�?     */
    private Token validateAndGetToken(String apiKey) {
        Token token = tokenService.validateToken(apiKey);
        if (token == null) {
            throw BizException.unauthorized("无效的API Key");
        }
        if (!token.isValid()) {
            throw BizException.operationNotAllowed("令牌已禁用或过期");
        }
        if (!token.hasAvailableQuota()) {
            throw BizException.insufficientBalance();
        }
        return token;
    }

    /**
     * 估算Token数量
     */
    private int estimateTokens(Map<String, Object> request) {
        Object messages = request.get("messages");
        if (messages instanceof java.util.List) {
            String content = messages.toString();
            return content.length() / 4; // 粗略估算
        }
        Object prompt = request.get("prompt");
        if (prompt instanceof String) {
            return ((String) prompt).length() / 4;
        }
        return 100; // 默认值
    }

    /**
     * 构建模拟响应（实际需要调用真实AI服务）
     */
    private Map<String, Object> buildMockResponse(Map<String, Object> request, String modelName) {
        return Map.of(
                "id", "chatcmpl-" + System.currentTimeMillis(),
                "object", "chat.completion",
                "created", System.currentTimeMillis() / 1000,
                "model", modelName,
                "choices", java.util.List.of(Map.of(
                        "index", 0,
                        "message", Map.of(
                                "role", "assistant",
                                "content", "This is a mock response. In production, this would be the actual AI response."
                        ),
                        "finish_reason", "stop"
                )),
                "usage", Map.of(
                        "prompt_tokens", 50,
                        "completion_tokens", 30,
                        "total_tokens", 80
                )
        );
    }

    private Map<String, Object> buildMockCompletionResponse(Map<String, Object> request) {
        return Map.of(
                "id", "cmpl-" + System.currentTimeMillis(),
                "object", "text_completion",
                "created", System.currentTimeMillis() / 1000,
                "model", request.get("model"),
                "choices", java.util.List.of(Map.of(
                        "text", "This is a mock completion response.",
                        "index", 0,
                        "finish_reason", "stop"
                )),
                "usage", Map.of(
                        "prompt_tokens", 10,
                        "completion_tokens", 10,
                        "total_tokens", 20
                )
        );
    }

    private Map<String, Object> buildMockEmbeddingResponse(Map<String, Object> request, String modelName) {
        return Map.of(
                "object", "list",
                "data", java.util.List.of(Map.of(
                        "object", "embedding",
                        "embedding", java.util.stream.IntStream.range(0, 1536)
                                .mapToObj(i -> Math.random() * 2 - 1)
                                .toList(),
                        "index", 0
                )),
                "model", modelName,
                "usage", Map.of(
                        "prompt_tokens", 10,
                        "total_tokens", 10
                )
        );
    }
}
