package com.apiplatform.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    /** 请求ID */
    private String requestId;

    /** 成功状态 */
    private Boolean success;

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .success(true)
                .build();
    }

    /**
     * 成功响应（带自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .success(true)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 失败响应（带状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 业务异常
     */
    public static <T> Result<T> fail(BizException e) {
        return Result.<T>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 验证失败
     */
    public static <T> Result<T> validateFailed(String message) {
        return Result.<T>builder()
                .code(400)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder()
                .code(401)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return Result.<T>builder()
                .code(403)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return Result.<T>builder()
                .code(404)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 服务器内部错误
     */
    public static <T> Result<T> serverError(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .success(false)
                .build();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }
}
