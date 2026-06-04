package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OAuth绑定记录实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oauth_bindings")
public class OAuthBinding {

    /** 绑定ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** OAuth提供商ID */
    private Long providerId;

    /** 提供商类型 */
    private String provider;

    /** 第三方用户ID */
    private String providerUserId;

    /** 第三方用户名 */
    private String providerUsername;

    /** 第三方用户邮箱 */
    private String providerEmail;

    /** 第三方用户头像 */
    private String providerAvatar;

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 令牌过期时间 */
    private LocalDateTime tokenExpiresAt;

    /** 绑定时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime boundAt;

    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否需要刷新令牌
     */
    public boolean needsRefresh() {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }
        if (tokenExpiresAt == null) {
            return false;
        }
        // 提前5分钟刷新
        return tokenExpiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }
}
