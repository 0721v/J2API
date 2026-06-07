package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.Result;
import com.apiplatform.entity.OAuthBinding;
import com.apiplatform.entity.OAuthProvider;
import com.apiplatform.service.OAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * OAuth授权控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * 获取OAuth提供商列表
     */
    @GetMapping("/providers")
    public Result<List<Map<String, Object>>> getProviders() {
        List<OAuthProvider> providers = Arrays.asList(
                getLinuxDoProvider(),
                getTelegramProvider(),
                getOidcProvider()
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (OAuthProvider provider : providers) {
            if (provider != null && provider.getEnabled()) {
                result.add(Map.of(
                        "type", provider.getProvider(),
                        "name", getProviderName(provider.getProvider()),
                        "icon", getProviderIcon(provider.getProvider())
                ));
            }
        }
        return Result.success(result);
    }

    /**
     * 获取OAuth授权URL
     */
    @GetMapping("/authorize/{provider}")
    public Result<Map<String, String>> getAuthorizationUrl(
            @PathVariable String provider,
            @RequestParam(required = false) String redirectUri) {
        try {
            String state = UUID.randomUUID().toString();
            String authUrl = oAuthService.getAuthorizationUrl(provider, state);

            return Result.success(Map.of(
                    "authUrl", authUrl,
                    "state", state
            ));
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * OAuth回调处理
     */
    @GetMapping("/callback/{provider}")
    public Result<Map<String, Object>> callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        try {
            Map<String, Object> result = oAuthService.handleCallback(provider, code, state);
            return Result.success(result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * OAuth POST回调处理（部分提供商使用POST�?     */
    @PostMapping("/callback/{provider}")
    public Result<Map<String, Object>> postCallback(
            @PathVariable String provider,
            @RequestBody Map<String, String> params) {
        try {
            String code = params.get("code");
            String state = params.get("state");
            Map<String, Object> result = oAuthService.handleCallback(provider, code, state);
            return Result.success(result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * Telegram Mini App登录
     */
    @PostMapping("/telegram/login")
    public Result<Map<String, Object>> telegramLogin(@RequestBody TelegramLoginRequest request) {
        try {
            // Telegram登录需要验证initData
            Map<String, Object> result = oAuthService.oauthLogin("telegram", request.getInitData());
            return Result.success(result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 绑定OAuth账户
     */
    @PostMapping("/bind/{provider}")
    public Result<Void> bindAccount(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String provider,
            @RequestBody BindAccountRequest request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            oAuthService.bindAccount(userId, provider, request.getCode());
            return Result.success("绑定成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 解除OAuth绑定
     */
    @DeleteMapping("/unbind/{provider}")
    public Result<Void> unbindAccount(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable String provider) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        try {
            oAuthService.unbindAccount(userId, provider);
            return Result.success("解绑成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取用户已绑定的OAuth账户列表
     */
    @GetMapping("/bindings")
    public Result<List<Map<String, Object>>> getBindings(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        List<OAuthBinding> bindings = oAuthService.getUserBindings(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (OAuthBinding binding : bindings) {
            result.add(Map.of(
                    "id", binding.getId(),
                    "provider", binding.getProvider(),
                    "providerName", getProviderName(binding.getProvider()),
                    "providerUsername", binding.getProviderUsername(),
                    "providerAvatar", binding.getProviderAvatar(),
                    "boundAt", binding.getBoundAt(),
                    "lastUsedAt", binding.getLastUsedAt()
            ));
        }

        return Result.success(result);
    }

    // ==================== 辅助方法 ====================

    private String getProviderName(String provider) {
        return switch (provider) {
            case "linuxdo" -> "LinuxDO";
            case "telegram" -> "Telegram";
            case "oidc" -> "OIDC/OAuth2";
            default -> provider;
        };
    }

    private String getProviderIcon(String provider) {
        return switch (provider) {
            case "linuxdo" -> "/assets/providers/linuxdo.svg";
            case "telegram" -> "/assets/providers/telegram.svg";
            case "oidc" -> "/assets/providers/oidc.svg";
            default -> "/assets/providers/default.svg";
        };
    }

    private OAuthProvider getLinuxDoProvider() {
        OAuthProvider provider = new OAuthProvider();
        provider.setProvider("linuxdo");
        provider.setEnabled(true);
        return provider;
    }

    private OAuthProvider getTelegramProvider() {
        OAuthProvider provider = new OAuthProvider();
        provider.setProvider("telegram");
        provider.setEnabled(true);
        return provider;
    }

    private OAuthProvider getOidcProvider() {
        OAuthProvider provider = new OAuthProvider();
        provider.setProvider("oidc");
        provider.setEnabled(true);
        return provider;
    }

    // ==================== 请求DTO ====================

    @Data
    public static class BindAccountRequest {
        private String code;
    }

    @Data
    public static class TelegramLoginRequest {
        private String initData;
    }
}
