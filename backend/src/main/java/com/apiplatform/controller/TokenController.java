package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Token;
import com.apiplatform.service.TokenService;
import com.apiplatform.util.ApiKeyUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
@Tag(name = "令牌管理", description = "API令牌管理相关接口")
public class TokenController {

    private final TokenService tokenService;
    private final ApiKeyUtil apiKeyUtil;

    /**
     * 获取用户令牌列表
     */
    @GetMapping
    @Operation(summary = "获取令牌列表", description = "获取当前用户的API令牌列表")
    public Result<PageResult<Token>> getTokens(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        PageResult<Token> result = tokenService.getUserTokens(userId, page, size);
        return Result.success(result);
    }

    /**
     * 创建令牌
     */
    @PostMapping
    @Operation(summary = "创建令牌", description = "创建新的API令牌")
    public Result<Token> createToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @Validated @RequestBody CreateTokenRequest request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            Token token = tokenService.createToken(
                    userId,
                    request.getName(),
                    request.getGroupId(),
                    request.getAllowedModels(),
                    request.getAllowedChannels(),
                    request.getMinuteLimit(),
                    request.getDayLimit(),
                    request.getQuotaLimit(),
                    request.getExpiresAt()
            );
            
            // 返回完整API Key（仅创建时显示一次）
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("apiKey", token.getApiKey());
            
            return Result.success("令牌创建成功", token);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取令牌详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取令牌详情", description = "获取指定令牌的详细信息")
    public Result<Token> getToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        Token token = tokenService.getById(id);
        if (token == null || !token.getUserId().equals(userId)) {
            return Result.notFound("令牌不存在");
        }
        
        return Result.success(token);
    }

    /**
     * 更新令牌
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新令牌", description = "更新指定令牌的配置")
    public Result<Token> updateToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id,
            @Validated @RequestBody UpdateTokenRequest request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            Token token = tokenService.updateToken(
                    id,
                    userId,
                    request.getName(),
                    request.getGroupId(),
                    request.getAllowedModels(),
                    request.getAllowedChannels(),
                    request.getMinuteLimit(),
                    request.getDayLimit(),
                    request.getQuotaLimit(),
                    request.getExpiresAt()
            );
            return Result.success("令牌更新成功", token);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除令牌
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除令牌", description = "删除指定的API令牌")
    public Result<Void> deleteToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            tokenService.deleteToken(id, userId);
            return Result.success("令牌删除成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 启用令牌
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用令牌", description = "启用指定的API令牌")
    public Result<Void> enableToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            tokenService.enableToken(id, userId);
            return Result.success("令牌已启用", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 禁用令牌
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用令牌", description = "禁用指定的API令牌")
    public Result<Void> disableToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            tokenService.disableToken(id, userId);
            return Result.success("令牌已禁用", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取令牌使用统计
     */
    @GetMapping("/{id}/usage")
    @Operation(summary = "获取令牌使用统计", description = "获取指定令牌的使用统计信息")
    public Result<Map<String, Object>> getTokenUsage(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            Map<String, Object> stats = tokenService.getTokenUsage(id, userId);
            return Result.success(stats);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 续期令牌
     */
    @PostMapping("/{id}/renew")
    @Operation(summary = "续期令牌", description = "延长令牌的过期时间")
    public Result<Token> renewToken(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            Token token = tokenService.renewToken(id, userId);
            return Result.success("令牌已续期", token);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreateTokenRequest {
        private String name;              // 令牌名称
        private Long groupId;             // 分组ID
        private String allowedModels;     // 允许的模型
        private String allowedChannels;   // 允许的渠道
        private Integer minuteLimit;      // 每分钟限制
        private Integer dayLimit;         // 每日限制
        private Long quotaLimit;          // 额度上限
        private LocalDateTime expiresAt; // 过期时间
    }

    @Data
    public static class UpdateTokenRequest {
        private String name;
        private Long groupId;
        private String allowedModels;
        private String allowedChannels;
        private Integer minuteLimit;
        private Integer dayLimit;
        private Long quotaLimit;
        private LocalDateTime expiresAt;
    }
}
