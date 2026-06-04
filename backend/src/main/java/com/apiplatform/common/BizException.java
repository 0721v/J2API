package com.apiplatform.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author API Platform Team
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;

    /** 错误消息 */
    private final String message;

    public BizException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.message = message;
    }

    public BizException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    // ==================== 通用异常 ====================

    public static BizException badRequest(String message) {
        return new BizException(400, message);
    }

    public static BizException unauthorized(String message) {
        return new BizException(401, message);
    }

    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }

    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    public static BizException serverError(String message) {
        return new BizException(500, message);
    }

    // ==================== 业务异常 ====================

    public static BizException userNotFound() {
        return new BizException(404, "用户不存在");
    }

    public static BizException userDisabled() {
        return new BizException(403, "用户已被禁用");
    }

    public static BizException passwordError() {
        return new BizException(400, "密码错误");
    }

    public static BizException emailExists() {
        return new BizException(400, "邮箱已被注册");
    }

    public static BizException tokenNotFound() {
        return new BizException(404, "令牌不存在");
    }

    public static BizException tokenDisabled() {
        return new BizException(403, "令牌已被禁用");
    }

    public static BizException insufficientBalance() {
        return new BizException(400, "余额不足");
    }

    public static BizException packageNotFound() {
        return new BizException(404, "套餐不存在");
    }

    public static BizException packageExpired() {
        return new BizException(400, "套餐已过期");
    }

    public static BizException channelNotFound() {
        return new BizException(404, "渠道不存在");
    }

    public static BizException channelDisabled() {
        return new BizException(403, "渠道已禁用");
    }

    public static BizException modelNotFound() {
        return new BizException(404, "模型不存在");
    }

    public static BizException modelDisabled() {
        return new BizException(403, "模型已禁用");
    }

    public static BizException rateLimitExceeded(String limitType) {
        return new BizException(429, limitType + "请求频率超限，请稍后再试");
    }

    public static BizException orderNotFound() {
        return new BizException(404, "订单不存在");
    }

    public static BizException orderExpired() {
        return new BizException(400, "订单已过期");
    }

    public static BizException paymentFailed(String reason) {
        return new BizException(400, "支付失败：" + reason);
    }

    public static BizException invalidParameter(String paramName) {
        return new BizException(400, "无效的参数：" + paramName);
    }

    public static BizException operationNotAllowed(String reason) {
        return new BizException(403, "操作不允许：" + reason);
    }

    public static BizException oauthBindExists() {
        return new BizException(400, "该第三方账号已绑定其他用户");
    }
}
