package com.apiplatform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.apiplatform.common.BizException;
import com.apiplatform.entity.OAuthBinding;
import com.apiplatform.entity.OAuthProvider;
import com.apiplatform.entity.User;
import com.apiplatform.mapper.OAuthBindingMapper;
import com.apiplatform.mapper.OAuthProviderMapper;
import com.apiplatform.service.OAuthService;
import com.apiplatform.service.UserService;
import com.apiplatform.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * OAuth服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl extends ServiceImpl<OAuthBindingMapper, OAuthBinding> implements OAuthService {

    private final OAuthProviderMapper providerMapper;
    private final OAuthBindingMapper bindingMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${app.oauth.callback-url:http://localhost:8080/auth/oauth/callback}")
    private String callbackUrl;

    // OAuth提供商类型常量
    public static final String PROVIDER_LINUXDO = "linuxdo";
    public static final String PROVIDER_TELEGRAM = "telegram";
    public static final String PROVIDER_OIDC = "oidc";

    @Override
    public OAuthProvider getProvider(String providerType) {
        return providerMapper.selectOne(
                new LambdaQueryWrapper<OAuthProvider>()
                        .eq(OAuthProvider::getProvider, providerType)
                        .eq(OAuthProvider::getEnabled, true)
        );
    }

    @Override
    public String getAuthorizationUrl(String providerType, String state) {
        OAuthProvider provider = getProvider(providerType);
        if (provider == null) {
            throw new BizException("不支持的OAuth提供商: " + providerType);
        }

        Map<String, String> config = parseConfig(provider.getExtraParams());
        String authUrl = provider.getAuthorizationUri();
        String clientId = provider.getClientId();
        String redirectUri = callbackUrl + "/" + providerType;

        // 根据不同提供商构建授权URL
        return switch (providerType) {
            case PROVIDER_LINUXDO -> buildLinuxDoAuthUrl(authUrl, clientId, redirectUri, state);
            case PROVIDER_TELEGRAM -> buildTelegramAuthUrl(config);
            case PROVIDER_OIDC -> buildOidcAuthUrl(authUrl, clientId, redirectUri, state, config);
            default -> throw new BizException("不支持的OAuth提供商: " + providerType);
        };
    }

    @Override
    @Transactional
    public Map<String, Object> handleCallback(String providerType, String code, String state) {
        OAuthProvider provider = getProvider(providerType);
        if (provider == null) {
            throw new BizException("不支持的OAuth提供商: " + providerType);
        }

        // 根据提供商类型处理回调
        Map<String, Object> providerUserInfo = switch (providerType) {
            case PROVIDER_LINUXDO -> handleLinuxDoCallback(code, provider);
            case PROVIDER_TELEGRAM -> handleTelegramCallback(state);
            case PROVIDER_OIDC -> handleOidcCallback(code, provider);
            default -> throw new BizException("不支持的OAuth提供商: " + providerType);
        };

        String providerUserId = (String) providerUserInfo.get("id");
        String providerEmail = (String) providerUserInfo.get("email");
        String providerUsername = (String) providerUserInfo.get("username");
        String providerAvatar = (String) providerUserInfo.get("avatar");

        // 查询是否已存在绑定
        OAuthBinding binding = bindingMapper.selectByProviderAndUserId(providerType, providerUserId);

        if (binding != null) {
            // 已绑定账户，登录
            User user = userService.getById(binding.getUserId());
            if (user == null || "disabled".equals(user.getStatus())) {
                throw new BizException("账户已被禁用");
            }
            return generateLoginResult(user);
        }

        // 检查邮箱是否已存在用户
        User existingUser = null;
        if (providerEmail != null && !providerEmail.isEmpty()) {
            existingUser = userService.getByEmail(providerEmail);
        }

        if (existingUser != null) {
            // 绑定OAuth到已有账户
            binding = createBinding(existingUser.getId(), provider.getId(), providerType,
                    providerUserId, providerUsername, providerEmail, providerAvatar, providerUserInfo);
            return generateLoginResult(existingUser);
        }

        // 创建新用户并绑定
        String tempUsername = providerUsername != null ? providerUsername : "user_" + System.currentTimeMillis();
        String tempEmail = providerEmail != null ? providerEmail : providerUserId + "@" + providerType + ".oauth";

        User newUser = userService.register(
                tempUsername,
                tempEmail,
                UUID.randomUUID().toString().substring(0, 16) // 随机密码
        );

        // 更新用户显示名和头像
        if (providerUsername != null) {
            newUser.setDisplayName(providerUsername);
        }
        if (providerAvatar != null) {
            newUser.setAvatar(providerAvatar);
        }

        // 创建OAuth绑定
        createBinding(newUser.getId(), provider.getId(), providerType,
                providerUserId, providerUsername, providerEmail, providerAvatar, providerUserInfo);

        return generateLoginResult(newUser);
    }

    @Override
    @Transactional
    public OAuthBinding bindAccount(Long userId, String providerType, String code) {
        OAuthProvider provider = getProvider(providerType);
        if (provider == null) {
            throw new BizException("不支持的OAuth提供商: " + providerType);
        }

        // 检查是否已绑定
        OAuthBinding existing = bindingMapper.selectByUserAndProvider(userId, providerType);
        if (existing != null) {
            throw new BizException("该账户已绑定" + getProviderName(providerType));
        }

        Map<String, Object> providerUserInfo = switch (providerType) {
            case PROVIDER_LINUXDO -> handleLinuxDoCallback(code, provider);
            case PROVIDER_TELEGRAM -> handleTelegramCallback(code);
            case PROVIDER_OIDC -> handleOidcCallback(code, provider);
            default -> throw new BizException("不支持的OAuth提供商: " + providerType);
        };

        return createBinding(
                userId,
                provider.getId(),
                providerType,
                (String) providerUserInfo.get("id"),
                (String) providerUserInfo.get("username"),
                (String) providerUserInfo.get("email"),
                (String) providerUserInfo.get("avatar"),
                providerUserInfo
        );
    }

    @Override
    @Transactional
    public void unbindAccount(Long userId, String providerType) {
        bindingMapper.delete(
                new LambdaQueryWrapper<OAuthBinding>()
                        .eq(OAuthBinding::getUserId, userId)
                        .eq(OAuthBinding::getProvider, providerType)
        );
    }

    @Override
    public List<OAuthBinding> getUserBindings(Long userId) {
        return bindingMapper.selectList(
                new LambdaQueryWrapper<OAuthBinding>()
                        .eq(OAuthBinding::getUserId, userId)
                        .orderByDesc(OAuthBinding::getBoundAt)
        );
    }

    @Override
    public Map<String, Object> oauthLogin(String providerType, String code) {
        return handleCallback(providerType, code, null);
    }

    @Override
    public boolean refreshOAuthToken(OAuthBinding binding) {
        if (binding.getRefreshToken() == null || binding.getRefreshToken().isEmpty()) {
            return false;
        }

        OAuthProvider provider = providerMapper.selectById(binding.getProviderId());
        if (provider == null || !provider.getEnabled()) {
            return false;
        }

        try {
            Map<String, String> config = parseConfig(provider.getExtraParams());
            String tokenUrl = provider.getTokenUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("grant_type", "refresh_token");
            body.put("refresh_token", binding.getRefreshToken());
            body.put("client_id", provider.getClientId());
            body.put("client_secret", provider.getClientSecret());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject tokenData = JSON.parseObject(response.getBody());
                binding.setAccessToken(tokenData.getString("access_token"));

                String newRefreshToken = tokenData.getString("refresh_token");
                if (newRefreshToken != null) {
                    binding.setRefreshToken(newRefreshToken);
                }

                Integer expiresIn = tokenData.getInteger("expires_in");
                if (expiresIn != null) {
                    binding.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                }

                bindingMapper.updateById(binding);
                return true;
            }
        } catch (Exception e) {
            log.error("刷新OAuth令牌失败: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Map<String, Object> getProviderUserInfo(String providerType, String accessToken) {
        OAuthProvider provider = getProvider(providerType);
        if (provider == null) {
            throw new BizException("不支持的OAuth提供商: " + providerType);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    provider.getUserInfoUri(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return JSON.parseObject(response.getBody());
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
        }
        return null;
    }

    // ==================== 私有方法 ====================

    private String buildLinuxDoAuthUrl(String authUrl, String clientId, String redirectUri, String state) {
        return authUrl + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=read:user" +
                "&state=" + (state != null ? state : UUID.randomUUID().toString());
    }

    private String buildTelegramAuthUrl(Map<String, String> config) {
        String botToken = config.get("bot_token");
        return "https://t.me/login_token?bot=" + botToken;
    }

    private String buildOidcAuthUrl(String authUrl, String clientId, String redirectUri, String state, Map<String, String> config) {
        String scope = config.getOrDefault("scope", "openid profile email");
        return authUrl + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope +
                "&state=" + (state != null ? state : UUID.randomUUID().toString());
    }

    private Map<String, Object> handleLinuxDoCallback(String code, OAuthProvider provider) {
        Map<String, String> config = parseConfig(provider.getExtraParams());
        String tokenUrl = provider.getTokenUri();
        String userInfoUrl = provider.getUserInfoUri();

        try {
            // 获取Access Token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("grant_type", "authorization_code");
            body.put("code", code);
            body.put("client_id", provider.getClientId());
            body.put("client_secret", provider.getClientSecret());
            body.put("redirect_uri", callbackUrl + "/" + PROVIDER_LINUXDO);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                throw new BizException("获取访问令牌失败");
            }

            JSONObject tokenData = JSON.parseObject(tokenResponse.getBody());
            String accessToken = tokenData.getString("access_token");

            // 获取用户信息
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            entity = new HttpEntity<>(headers);

            ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                throw new BizException("获取用户信息失败");
            }

            JSONObject userData = JSON.parseObject(userResponse.getBody());

            Map<String, Object> result = new HashMap<>();
            result.put("id", String.valueOf(userData.getLong("id")));
            result.put("username", userData.getString("name"));
            result.put("email", userData.getString("email"));
            result.put("avatar", userData.getString("avatar"));

            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("LinuxDO OAuth回调处理失败: {}", e.getMessage());
            throw new BizException("OAuth授权失败");
        }
    }

    private Map<String, Object> handleTelegramCallback(String initData) {
        // Telegram使用Web App初始化数据进行验证
        Map<String, Object> result = new HashMap<>();

        try {
            // 解析Telegram initData
            // 实际实现需要验证hash和token
            // 这里简化处理

            if (initData == null || initData.isEmpty()) {
                throw new BizException("无效的Telegram授权数据");
            }

            // 解析用户数据
            String[] params = initData.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");

                    if ("user".equals(key)) {
                        JSONObject userData = JSON.parseObject(value);
                        result.put("id", String.valueOf(userData.getLong("id")));
                        result.put("username", userData.getString("username"));
                        result.put("first_name", userData.getString("first_name"));
                        result.put("last_name", userData.getString("last_name"));
                        result.put("avatar", userData.getString("photo_url"));
                    }
                }
            }

            if (!result.containsKey("id")) {
                throw new BizException("无法解析Telegram用户信息");
            }

            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Telegram OAuth回调处理失败: {}", e.getMessage());
            throw new BizException("OAuth授权失败");
        }
    }

    private Map<String, Object> handleOidcCallback(String code, OAuthProvider provider) {
        Map<String, String> config = parseConfig(provider.getExtraParams());
        String tokenUrl = provider.getTokenUri();
        String userInfoUrl = provider.getUserInfoUri();

        try {
            // 获取Access Token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code" +
                    "&code=" + code +
                    "&client_id=" + provider.getClientId() +
                    "&client_secret=" + provider.getClientSecret() +
                    "&redirect_uri=" + callbackUrl + "/" + PROVIDER_OIDC;

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                throw new BizException("获取访问令牌失败");
            }

            JSONObject tokenData = JSON.parseObject(tokenResponse.getBody());
            String accessToken = tokenData.getString("access_token");
            String idToken = tokenData.getString("id_token");

            // 获取用户信息
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            entity = new HttpEntity<>(headers);

            ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                throw new BizException("获取用户信息失败");
            }

            JSONObject userData = JSON.parseObject(userResponse.getBody());

            Map<String, Object> oidcResult = new HashMap<>();
            oidcResult.put("id", userData.getString("sub"));
            oidcResult.put("username", userData.getString("preferred_username"));
            oidcResult.put("email", userData.getString("email"));
            oidcResult.put("avatar", userData.getString("picture"));
            oidcResult.put("name", userData.getString("name"));

            return oidcResult;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("OIDC OAuth回调处理失败: {}", e.getMessage());
            throw new BizException("OAuth授权失败");
        }
    }

    private OAuthBinding createBinding(Long userId, Long providerId, String providerType,
                                       String providerUserId, String providerUsername,
                                       String providerEmail, String providerAvatar,
                                       Map<String, Object> providerUserInfo) {
        OAuthBinding binding = OAuthBinding.builder()
                .userId(userId)
                .providerId(providerId)
                .provider(providerType)
                .providerUserId(providerUserId)
                .providerUsername(providerUsername)
                .providerEmail(providerEmail)
                .providerAvatar(providerAvatar)
                .boundAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build();

        // 保存令牌信息
        if (providerUserInfo.containsKey("access_token")) {
            binding.setAccessToken((String) providerUserInfo.get("access_token"));
        }
        if (providerUserInfo.containsKey("refresh_token")) {
            binding.setRefreshToken((String) providerUserInfo.get("refresh_token"));
        }
        if (providerUserInfo.containsKey("expires_in")) {
            binding.setTokenExpiresAt(LocalDateTime.now()
                    .plusSeconds(((Number) providerUserInfo.get("expires_in")).longValue()));
        }

        bindingMapper.insert(binding);
        return binding;
    }

    private Map<String, Object> generateLoginResult(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("displayName", user.getDisplayName());
        result.put("avatar", user.getAvatar());
        result.put("balance", user.getBalance());

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername());

        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);

        return result;
    }

    private Map<String, String> parseConfig(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return JSON.parseObject(configJson, Map.class);
        } catch (Exception e) {
            log.error("解析OAuth配置失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String getProviderName(String providerType) {
        return switch (providerType) {
            case PROVIDER_LINUXDO -> "LinuxDO";
            case PROVIDER_TELEGRAM -> "Telegram";
            case PROVIDER_OIDC -> "OIDC";
            default -> providerType;
        };
    }
}
