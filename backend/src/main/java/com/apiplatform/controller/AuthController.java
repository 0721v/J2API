package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.Result;
import com.apiplatform.entity.InviteRecord;
import com.apiplatform.entity.InviteReward;
import com.apiplatform.entity.User;
import com.apiplatform.service.InviteService;
import com.apiplatform.service.UserService;
import com.apiplatform.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证模块", description = "用户注册、登录、密码管理等认证相关接口")
public class AuthController {

    private final UserService userService;
    private final InviteService inviteService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号，支持邀请码注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或用户名/邮箱已存在", content = @Content)
    })
    public Result<Map<String, Object>> register(
            @Validated @RequestBody @Parameter(description = "注册请求") RegisterRequest request) {
        try {
            // 处理邀请码
            Long inviterId = null;
            String inviteCode = request.getInviteCode();
            
            if (inviteCode != null && !inviteCode.isBlank()) {
                InviteRecord inviteRecord = inviteService.validateInviteCode(inviteCode);
                if (inviteRecord != null) {
                    inviterId = inviteRecord.getInviterId();
                } else {
                    log.warn("无效的邀请码: {}", inviteCode);
                }
            }

            // 注册用户
            var user = userService.register(request.getUsername(), request.getEmail(), request.getPassword(), inviterId);
            
            // 创建邀请记录并发放奖励
            if (inviterId != null) {
                try {
                    // 创建邀请记录
                    InviteRecord record = inviteService.createInviteRecord(inviterId, user.getId(), inviteCode, "manual");
                    
                    // 发放注册奖励（异步）
                    inviteService.creditRegisterReward(inviterId, user.getId());
                    
                    log.info("用户注册，邀请人: {}, 被邀请人: {}", inviterId, user.getId());
                } catch (Exception e) {
                    log.error("处理邀请奖励失败", e);
                }
            }

            // 生成令牌
            String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            
            // 返回邀请奖励信息（如果有）
            if (inviterId != null) {
                List<InviteReward> rewards = inviteService.getActiveRewards("register");
                if (!rewards.isEmpty()) {
                    InviteReward reward = rewards.get(0);
                    data.put("inviteReward", Map.of(
                            "inviteeReward", reward.getInviteeRewardValue() != null ? reward.getInviteeRewardValue() : 0,
                            "message", "注册成功！您获得了新用户奖励"
                    ));
                }
            }
            
            return Result.success("注册成功", data);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名或邮箱登录系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回用户信息和令牌"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误", content = @Content)
    })
    public Result<Map<String, Object>> login(
            @Validated @RequestBody @Parameter(description = "登录请求") LoginRequest request) {
        try {
            Map<String, Object> result = userService.login(request.getLoginKey(), request.getPassword());
            return Result.success("登录成功", result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        // 可以在这里添加令牌黑名单等逻辑
        return Result.success("退出成功", null);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.validateFailed("刷新令牌不能为空");
        }
        
        try {
            Map<String, Object> result = userService.refreshToken(refreshToken);
            return Result.success("令牌刷新成功", result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        var user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("avatar", user.getAvatar());
        data.put("balance", user.getBalance());
        data.put("role", user.getRole());
        data.put("preferredLanguage", user.getPreferredLanguage());
        data.put("preferredTheme", user.getPreferredTheme());
        data.put("inviteCode", user.getInviteCode());
        
        return Result.success(data);
    }

    /**
     * 发送验证码（用于密码重置）
     */
    @PostMapping("/send-verify-code")
    public Result<Void> sendVerifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return Result.validateFailed("邮箱不能为空");
        }
        
        // TODO: 实现发送验证码逻辑
        return Result.success("验证码已发送", null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getEmail(), request.getVerifyCode(), request.getNewPassword());
            return Result.success("密码重置成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Validated @RequestBody ChangePasswordRequest request) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        
        try {
            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.success("密码修改成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== 邀请相关接口 ====================

    /**
     * 验证邀请码
     */
    @GetMapping("/invite/validate")
    public Result<Map<String, Object>> validateInviteCode(@RequestParam String inviteCode) {
        InviteRecord record = inviteService.validateInviteCode(inviteCode);
        if (record != null) {
            User inviter = userService.getById(record.getInviterId());
            return Result.success(Map.of(
                    "valid", true,
                    "inviterName", inviter != null ? inviter.getUsername() : "用户",
                    "inviteCode", inviteCode
            ));
        }
        return Result.success(Map.of("valid", false, "message", "邀请码无效或已过期"));
    }

    /**
     * 获取邀请奖励配置
     */
    @GetMapping("/invite/rewards")
    public Result<List<InviteReward>> getInviteRewards() {
        return Result.success(inviteService.getActiveRewards(null));
    }

    /**
     * 获取邀请统计
     */
    @GetMapping("/invite/stats")
    public Result<Map<String, Object>> getInviteStats(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(inviteService.getInviteStats(userId));
    }

    /**
     * 获取邀请人列表
     */
    @GetMapping("/invite/invitees")
    public Result<List<Map<String, Object>>> getInvitees(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(inviteService.getInvitees(userId, page, pageSize));
    }

    /**
     * 获取用户自己的邀请码
     */
    @GetMapping("/invite/code")
    public Result<Map<String, String>> getInviteCode(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        String code = inviteService.getUserInviteCode(userId);
        return Result.success(Map.of("inviteCode", code != null ? code : ""));
    }

    // ==================== 请求DTO ====================

    @Data
    @Schema(description = "登录请求")
    public static class LoginRequest {
        @Schema(description = "登录账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        private String loginKey; // 邮箱或用户名
        
        @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;
    }

    @Data
    @Schema(description = "注册请求")
    public static class RegisterRequest {
        @Schema(description = "用户名", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
        private String username;
        
        @Schema(description = "邮箱", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;
        
        @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;
        
        @Schema(description = "邀请码", example = "ABC123")
        private String inviteCode; // 邀请码
    }

    @Data
    @Schema(description = "重置密码请求")
    public static class ResetPasswordRequest {
        @Schema(description = "邮箱", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        private String email;
        
        @Schema(description = "验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        private String verifyCode;
        
        @Schema(description = "新密码", example = "newpassword123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String newPassword;
    }

    @Data
    @Schema(description = "修改密码请求")
    public static class ChangePasswordRequest {
        @Schema(description = "旧密码", example = "oldpassword", requiredMode = Schema.RequiredMode.REQUIRED)
        private String oldPassword;
        
        @Schema(description = "新密码", example = "newpassword123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String newPassword;
    }
}
