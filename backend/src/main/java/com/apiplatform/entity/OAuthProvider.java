package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OAuth提供商配置实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oauth_providers")
public class OAuthProvider {

    /** 提供商ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提供商名称 */
    private String name;

    /** 提供商类型：linuxdo/telegram/oidc/google/github */
    private String provider;

    /** 客户端ID */
    private String clientId;

    /** 客户端密钥 */
    private String clientSecret;

    /** 授权URL */
    private String authorizationUri;

    /** Token URL */
    private String tokenUri;

    /** 用户信息URL */
    private String userInfoUri;

    /** 权限范围 */
    private String scopes;

    /** 图标URL */
    private String iconUrl;

    /** 按钮颜色 */
    private String buttonColor;

    /** 客户端ID字段名 */
    @Builder.Default
    private String clientIdField = "client_id";

    /** 客户端密钥字段名 */
    @Builder.Default
    private String clientSecretField = "client_secret";

    /** 响应类型 */
    @Builder.Default
    private String responseType = "code";

    /** 额外参数（JSON） */
    private String extraParams;

    /** 是否启用 */
    @Builder.Default
    private Boolean enabled = true;

    /** 是否显示在登录页 */
    @Builder.Default
    private Boolean visible = true;

    /** 排序顺序 */
    @Builder.Default
    private Integer sortOrder = 0;

    /** 描述 */
    private String description;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    @TableField(select = false)
    private Boolean deleted;

    // ==================== 内置提供商 ====================
    public static final String PROVIDER_LINUXDO = "linuxdo";
    public static final String PROVIDER_TELEGRAM = "telegram";
    public static final String PROVIDER_OIDC = "oidc";
    public static final String PROVIDER_GITHUB = "github";
    public static final String PROVIDER_GOOGLE = "google";
}
