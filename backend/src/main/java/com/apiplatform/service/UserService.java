package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author API Platform Team
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(String username, String email, String password);

    /**
     * 用户登录
     */
    Map<String, Object> login(String loginKey, String password);

    /**
     * 退出登录
     */
    void logout(Long userId);

    /**
     * 刷新令牌
     */
    Map<String, Object> refreshToken(String refreshToken);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(String email, String verifyCode, String newPassword);

    /**
     * 更新用户信息
     */
    User updateProfile(Long userId, String displayName, String phone, String avatar);

    /**
     * 更新首选语言
     */
    void updatePreferredLanguage(Long userId, String language);

    /**
     * 更新首选主题
     */
    void updatePreferredTheme(Long userId, String theme);

    /**
     * 获取用户余额
     */
    Long getBalance(Long userId);

    /**
     * 扣减余额
     */
    boolean deductBalance(Long userId, Long amount);

    /**
     * 增加余额
     */
    void addBalance(Long userId, Long amount);

    /**
     * 分页查询用户
     */
    PageResult<User> pageUsers(int page, int size, String keyword);

    /**
     * 禁用用户
     */
    void disableUser(Long userId);

    /**
     * 启用用户
     */
    void enableUser(Long userId);

    /**
     * 更新最后登录信息
     */
    void updateLastLogin(Long userId, String ip);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStats();

    /**
     * 根据邮箱查询
     */
    User getByEmail(String email);

    /**
     * 验证密码
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);

    /**
     * 加密密码
     */
    String encodePassword(String rawPassword);
}
