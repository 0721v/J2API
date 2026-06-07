package com.apiplatform.service.impl;

import com.apiplatform.common.*;
import com.apiplatform.entity.Token;
import com.apiplatform.entity.Transaction;
import com.apiplatform.entity.User;
import com.apiplatform.mapper.TokenMapper;
import com.apiplatform.mapper.TransactionMapper;
import com.apiplatform.mapper.UserMapper;
import com.apiplatform.service.UserService;
import com.apiplatform.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final TransactionMapper transactionMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(String username, String email, String password) {
        // 检查邮箱是否已注册
        User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null) {
            throw BizException.emailExists();
        }

        // 检查用户名是否已存在
        User existingUsername = userMapper.selectByUsername(username);
        if (existingUsername != null) {
            throw BizException.badRequest("用户名已被使用");
        }

        // 创建用户
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodePassword(password))
                .displayName(username)
                .balance(0L)
                .totalConsumption(0L)
                .role("user")
                .status("active")
                .emailVerified(false)
                .preferredLanguage("zh-CN")
                .preferredTheme("light")
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}", username);
        
        return user;
    }

    @Override
    @Transactional
    public User register(String username, String email, String password, Long inviterId) {
        // 检查邮箱是否已注册
        User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null) {
            throw BizException.emailExists();
        }

        // 检查用户名是否已存在
        User existingUsername = userMapper.selectByUsername(username);
        if (existingUsername != null) {
            throw BizException.badRequest("用户名已被使用");
        }

        // 创建用户
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodePassword(password))
                .displayName(username)
                .balance(0L)
                .totalConsumption(0L)
                .role("user")
                .status("active")
                .emailVerified(false)
                .preferredLanguage("zh-CN")
                .preferredTheme("light")
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}, 邀请人: {}", username, inviterId);
        
        return user;
    }

    @Override
    @Transactional
    public Map<String, Object> login(String loginKey, String password) {
        // 根据邮箱或用户名查询用户
        User user = userMapper.selectByEmail(loginKey);
        if (user == null) {
            user = userMapper.selectByUsername(loginKey);
        }

        if (user == null) {
            throw BizException.userNotFound();
        }

        // 验证密码
        if (!verifyPassword(password, user.getPassword())) {
            throw BizException.passwordError();
        }

        // 检查用户状态
        if ("disabled".equals(user.getStatus())) {
            throw BizException.userDisabled();
        }
        if ("banned".equals(user.getStatus())) {
            throw BizException.operationNotAllowed("账号已被封禁");
        }

        // 生成令牌
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername());

        // 更新最后登录信息
        updateLastLogin(user.getId(), null);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("balance", user.getBalance());
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);

        log.info("用户登录成功: {}", user.getUsername());
        return result;
    }

    @Override
    public void logout(Long userId) {
        // 可以在这里添加令牌黑名单等逻辑
        log.info("用户退出登录: {}", userId);
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw BizException.unauthorized("刷新令牌无效或已过期");
        }

        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw BizException.unauthorized("无效的刷新令牌类型");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }

        // 生成新的访问令牌
        String newAccessToken = jwtUtil.createAccessToken(userId, user.getUsername());
        String newRefreshToken = jwtUtil.createRefreshToken(userId, user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);

        return result;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }

        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw BizException.passwordError();
        }

        user.setPassword(encodePassword(newPassword));
        updateById(user);
        log.info("用户修改密码: {}", userId);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String verifyCode, String newPassword) {
        User user = getByEmail(email);
        if (user == null) {
            throw BizException.userNotFound();
        }

        // TODO: 验证验证码
        // verifyCode(verifyCode);

        user.setPassword(encodePassword(newPassword));
        updateById(user);
        log.info("用户重置密码: {}", email);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String displayName, String phone, String avatar) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        updateById(user);
        return user;
    }

    @Override
    public void updatePreferredLanguage(Long userId, String language) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }
        user.setPreferredLanguage(language);
        updateById(user);
    }

    @Override
    public void updatePreferredTheme(Long userId, String theme) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }
        user.setPreferredTheme(theme);
        updateById(user);
    }

    @Override
    public Long getBalance(Long userId) {
        User user = getById(userId);
        return user != null ? user.getBalance() : 0L;
    }

    @Override
    @Transactional
    public boolean deductBalance(Long userId, Long amount) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }

        if (user.getBalance() < amount) {
            return false;
        }

        Long beforeBalance = user.getBalance();
        user.setBalance(user.getBalance() - amount);
        user.setTotalConsumption(user.getTotalConsumption() + amount);
        updateById(user);

        // 记录交易
        recordTransaction(userId, "consume", -amount, beforeBalance, user.getBalance(), null);

        return true;
    }

    @Override
    @Transactional
    public void addBalance(Long userId, Long amount) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }

        Long beforeBalance = user.getBalance();
        user.setBalance(user.getBalance() + amount);
        updateById(user);

        // 记录交易
        recordTransaction(userId, "recharge", amount, beforeBalance, user.getBalance(), null);
    }

    @Override
    public PageResult<User> pageUsers(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> pageResult = userMapper.selectUserPage(pageParam, keyword);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }
        user.setStatus("disabled");
        updateById(user);
        
        // 禁用所有令牌
        LambdaQueryWrapper<Token> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Token::getUserId, userId);
        List<Token> tokens = tokenMapper.selectList(wrapper);
        tokens.forEach(token -> {
            token.setStatus("disabled");
            tokenMapper.updateById(token);
        });
        
        log.info("禁用用户: {}", userId);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw BizException.userNotFound();
        }
        user.setStatus("active");
        updateById(user);
        log.info("启用用户: {}", userId);
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        userMapper.updateLastLogin(userId, LocalDateTime.now(), ip);
        log.info("更新用户最后登录时间: userId={}, ip={}", userId, ip);
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectTotalCount());
        stats.put("newUsersToday", userMapper.selectNewUserCount(LocalDateTime.now().toLocalDate().atStartOfDay()));
        stats.put("activeUsers", count(new LambdaQueryWrapper<User>().eq(User::getStatus, "active")));
        
        // 计算活跃用户余额总和
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getStatus, "active");
        Long totalBalance = userMapper.selectList(wrapper).stream()
                .mapToLong(u -> u.getBalance() != null ? u.getBalance() : 0L)
                .sum();
        stats.put("totalBalance", totalBalance);
        
        return stats;
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 兼容旧的MD5密码（用于迁移）
        if (encodedPassword != null && !encodedPassword.startsWith("$2a$") && !encodedPassword.startsWith("$2b$") && !encodedPassword.startsWith("$2y$")) {
            // 旧的MD5格式
            String md5Encoded = cn.hutool.crypto.SecureUtil.md5(rawPassword);
            return md5Encoded.equals(encodedPassword);
        }
        // 使用BCrypt验证
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encodePassword(String rawPassword) {
        // 使用BCrypt加密
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 记录交易
     */
    private void recordTransaction(Long userId, String type, Long amount, 
                                   Long balanceBefore, Long balanceAfter, Long orderId) {
        Transaction transaction = Transaction.builder()
                .transactionNo("TX" + System.currentTimeMillis())
                .userId(userId)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .orderId(orderId)
                .status("completed")
                .build();
        transactionMapper.insert(transaction);
    }
}
