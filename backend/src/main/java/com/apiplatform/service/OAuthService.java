package com.apiplatform.service;

import com.apiplatform.entity.OAuthBinding;
import com.apiplatform.entity.OAuthProvider;
import com.apiplatform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * OAuth服务接口
 *
 * @author API Platform Team
 */
public interface OAuthService extends IService<Object> {

    /**
     * 获取OAuth提供商配置
     */
    OAuthProvider getProvider(String providerType);

    /**
     * 获取OAuth登录URL
     */
    String getAuthorizationUrl(String providerType, String state);

    /**
     * 处理OAuth回调
     */
    Map<String, Object> handleCallback(String providerType, String code, String state);

    /**
     * 绑定OAuth账户
     */
    OAuthBinding bindAccount(Long userId, String providerType, String code);

    /**
     * 解除OAuth绑定
     */
    void unbindAccount(Long userId, String providerType);

    /**
     * 获取用户的OAuth绑定列表
     */
    java.util.List<OAuthBinding> getUserBindings(Long userId);

    /**
     * 通过OAuth登录
     */
    Map<String, Object> oauthLogin(String providerType, String code);

    /**
     * 刷新OAuth令牌
     */
    boolean refreshOAuthToken(OAuthBinding binding);

    /**
     * 获取提供商用户信息
     */
    Map<String, Object> getProviderUserInfo(String providerType, String accessToken);
}
