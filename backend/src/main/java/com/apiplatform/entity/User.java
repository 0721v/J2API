package com.apiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 密码（加密存储） */
    private String password;

    /** 显示名称 */
    private String displayName;

    /** 头像URL */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 余额（单位：分） */
    @Builder.Default
    private Long balance = 0L;

    /** 累计消费（单位：分） */
    @Builder.Default
    private Long totalConsumption = 0L;

    /** 用户角色：user/admin */
    @Builder.Default
    private String role = "user";

    /** 用户状态：active/disabled/banned */
    @Builder.Default
    private String status = "active";

    /** 邮箱验证状态 */
    @Builder.Default
    private Boolean emailVerified = false;

    /** 首选语言 */
    @Builder.Default
    private String preferredLanguage = "zh-CN";

    /** 首选主题 */
    @Builder.Default
    private String preferredTheme = "light";

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 注册IP */
    private String registerIp;

    /** 备注 */
    private String remark;

    /** 邀请人ID */
    private Long inviterId;

    /** 邀请码 */
    private String inviteCode;

    /** 用户分组ID */
    private Long groupId;

    /** 分组过期时间 */
    private LocalDateTime expireTime;

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
}
