package com.apiplatform.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 代理请求结果
 *
 * @author API Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResult {

    /** 是否成功 */
    private boolean success;

    /** HTTP状态码 */
    private int statusCode;

    /** 响应体 */
    private String body;

    /** 响应头 */
    private Map<String, String> headers;

    /** 错误消息 */
    private String errorMessage;

    /** 响应时间（毫秒） */
    private long responseTime;

    /** 请求ID（用于日志追踪） */
    private String requestId;

    /** 代理ID */
    private Long proxyId;

    /** 模型名称 */
    private String modelName;

    /** 渠道名称 */
    private String channelName;

    /** 令牌使用量 */
    private Long tokenUsage;

    /** 费用 */
    private java.math.BigDecimal cost;

    /** 创建成功结果 */
    public static ProxyResult success(int statusCode, String body, Map<String, String> headers) {
        return ProxyResult.builder()
                .success(true)
                .statusCode(statusCode)
                .body(body)
                .headers(headers)
                .build();
    }

    /** 创建失败结果 */
    public static ProxyResult error(String errorMessage) {
        return ProxyResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /** 创建带状态码的失败结果 */
    public static ProxyResult error(int statusCode, String errorMessage) {
        return ProxyResult.builder()
                .success(false)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .build();
    }
}