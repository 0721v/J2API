package com.apiplatform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.apiplatform.common.BizException;
import com.apiplatform.entity.ApiProxy;
import com.apiplatform.mapper.ApiProxyMapper;
import com.apiplatform.service.ApiProxyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * API代理服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiProxyServiceImpl extends ServiceImpl<ApiProxyMapper, ApiProxy> implements ApiProxyService {

    private final ApiProxyMapper apiProxyMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public ApiProxy createProxy(ApiProxy proxy) {
        if (proxy.getName() == null || proxy.getName().trim().isEmpty()) {
            throw new BizException("代理名称不能为空");
        }
        if (proxy.getTargetUrl() == null || proxy.getTargetUrl().trim().isEmpty()) {
            throw new BizException("目标URL不能为空");
        }
        if (proxy.getType() == null || proxy.getType().trim().isEmpty()) {
            proxy.setType("proxy_forward");
        }

        save(proxy);
        log.info("创建API代理: {}", proxy.getName());
        return proxy;
    }

    @Override
    @Transactional
    public ApiProxy updateProxy(Long id, ApiProxy proxy) {
        ApiProxy existing = getById(id);
        if (existing == null) {
            throw new BizException("代理不存在");
        }

        if (proxy.getName() != null) existing.setName(proxy.getName());
        if (proxy.getType() != null) existing.setType(proxy.getType());
        if (proxy.getTargetUrl() != null) existing.setTargetUrl(proxy.getTargetUrl());
        if (proxy.getSourcePath() != null) existing.setSourcePath(proxy.getSourcePath());
        if (proxy.getTargetPath() != null) existing.setTargetPath(proxy.getTargetPath());
        if (proxy.getMethods() != null) existing.setMethods(proxy.getMethods());
        if (proxy.getHeaderTransforms() != null) existing.setHeaderTransforms(proxy.getHeaderTransforms());
        if (proxy.getRequestTransforms() != null) existing.setRequestTransforms(proxy.getRequestTransforms());
        if (proxy.getResponseTransforms() != null) existing.setResponseTransforms(proxy.getResponseTransforms());
        if (proxy.getModelIds() != null) existing.setModelIds(proxy.getModelIds());
        if (proxy.getChannelIds() != null) existing.setChannelIds(proxy.getChannelIds());
        if (proxy.getAllowedGroups() != null) existing.setAllowedGroups(proxy.getAllowedGroups());
        if (proxy.getPriority() != null) existing.setPriority(proxy.getPriority());
        if (proxy.getTimeout() != null) existing.setTimeout(proxy.getTimeout());
        if (proxy.getRetryCount() != null) existing.setRetryCount(proxy.getRetryCount());
        if (proxy.getLogRequests() != null) existing.setLogRequests(proxy.getLogRequests());
        if (proxy.getLogResponses() != null) existing.setLogResponses(proxy.getLogResponses());
        if (proxy.getCacheEnabled() != null) existing.setCacheEnabled(proxy.getCacheEnabled());
        if (proxy.getCacheTtl() != null) existing.setCacheTtl(proxy.getCacheTtl());
        if (proxy.getAuthRequired() != null) existing.setAuthRequired(proxy.getAuthRequired());
        if (proxy.getRateLimit() != null) existing.setRateLimit(proxy.getRateLimit());
        if (proxy.getEnabled() != null) existing.setEnabled(proxy.getEnabled());
        if (proxy.getDescription() != null) existing.setDescription(proxy.getDescription());

        updateById(existing);
        log.info("更新API代理: {}", existing.getName());
        return existing;
    }

    @Override
    @Transactional
    public void deleteProxy(Long id) {
        ApiProxy proxy = getById(id);
        if (proxy == null) {
            throw new BizException("代理不存在");
        }
        removeById(id);
        log.info("删除API代理: {}", proxy.getName());
    }

    @Override
    public ApiProxy getProxyById(Long id) {
        return getById(id);
    }

    @Override
    public List<ApiProxy> getEnabledProxies() {
        return list(new LambdaQueryWrapper<ApiProxy>()
                .eq(ApiProxy::getEnabled, true)
                .eq(ApiProxy::getDeleted, false)
                .orderByDesc(ApiProxy::getPriority));
    }

    @Override
    public Page<ApiProxy> getProxyPage(int page, int size, String keyword, String type) {
        LambdaQueryWrapper<ApiProxy> wrapper = new LambdaQueryWrapper<ApiProxy>()
                .eq(ApiProxy::getDeleted, false)
                .orderByDesc(ApiProxy::getPriority)
                .orderByDesc(ApiProxy::getCreatedAt);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(ApiProxy::getType, type);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(ApiProxy::getName, keyword)
                    .or()
                    .like(ApiProxy::getTargetUrl, keyword));
        }

        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public ApiProxy matchProxy(String path, String method) {
        List<ApiProxy> proxies = getEnabledProxies();

        for (ApiProxy proxy : proxies) {
            if (proxy.matchesPath(path) && proxy.matchesMethod(method)) {
                return proxy;
            }
        }

        return null;
    }

    @Override
    public ProxyResult forwardRequest(String path, String method, String body,
                                      Map<String, String> headers,
                                      Long userId, String apiKey) {
        ApiProxy proxy = matchProxy(path, method);
        if (proxy == null) {
            return ProxyResult.error(404, "未找到匹配的代理配置");
        }

        try {
            // 构建目标URL
            String targetUrl = proxy.getRewrittenTargetUrl(path);

            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            // 应用请求头转换
            applyHeaderTransforms(headers, httpHeaders, proxy);

            // 构建请求体
            String requestBody = body;
            if (proxy.getRequestTransforms() != null) {
                requestBody = transformRequest(body, proxy.getRequestTransforms());
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);

            // 发送请求
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    httpMethod,
                    entity,
                    String.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            // 转换响应
            String responseBody = response.getBody();
            if (proxy.getResponseTransforms() != null) {
                responseBody = transformResponse(responseBody, proxy.getResponseTransforms());
            }

            // 增加计数
            incrementRequestCount(proxy.getId());

            return ProxyResult.builder()
                    .success(true)
                    .status(response.getStatusCode().value())
                    .body(responseBody)
                    .headers(response.getHeaders())
                    .responseTime(responseTime)
                    .proxyId(proxy.getId())
                    .build();

        } catch (Exception e) {
            log.error("代理转发失败: path={}, error={}", path, e.getMessage());
            return ProxyResult.error(502, "代理转发失败: " + e.getMessage());
        }
    }

    /**
     * 应用请求头转换
     */
    private void applyHeaderTransforms(Map<String, String> original, HttpHeaders target, ApiProxy proxy) {
        // 复制原始头
        for (Map.Entry<String, String> entry : original.entrySet()) {
            if (!isFilteredHeader(entry.getKey())) {
                target.add(entry.getKey(), entry.getValue());
            }
        }

        // 应用转换规则
        if (proxy.getHeaderTransforms() != null) {
            try {
                JSONObject transforms = JSON.parseObject(proxy.getHeaderTransforms());

                // 添加头
                if (transforms.containsKey("add")) {
                    JSONObject addHeaders = transforms.getJSONObject("add");
                    addHeaders.forEach((k, v) -> target.add(k, v.toString()));
                }

                // 移除头
                if (transforms.containsKey("remove")) {
                    // 需要在目标headers中过滤
                }

                // 重写头
                if (transforms.containsKey("rename")) {
                    JSONObject renameHeaders = transforms.getJSONObject("rename");
                    renameHeaders.forEach((k, v) -> {
                        String value = target.getFirst(k);
                        if (value != null) {
                            target.remove(k);
                            target.add(v.toString(), value);
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("解析请求头转换规则失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 检查是否过滤该请求头
     */
    private boolean isFilteredHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.equals("host") ||
                lower.equals("content-length") ||
                lower.equals("connection") ||
                lower.equals("transfer-encoding");
    }

    /**
     * 转换请求体
     */
    private String transformRequest(String body, String transforms) {
        // 简化实现，实际可以根据transforms进行复杂的JSON转换
        return body;
    }

    /**
     * 转换响应体
     */
    private String transformResponse(String body, String transforms) {
        // 简化实现，实际可以根据transforms进行复杂的JSON转换
        return body;
    }

    @Override
    public void incrementRequestCount(Long proxyId) {
        ApiProxy proxy = new ApiProxy();
        proxy.setId(proxyId);
        proxy.setLastUsedAt(LocalDateTime.now());
        updateById(proxy);

        // 原子递增计数
        apiProxyMapper.incrementRequestCount(proxyId);
    }

    @Override
    public boolean testProxy(ApiProxy proxy) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>("{\"test\": true}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    proxy.getTargetUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("代理连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, boolean enabled) {
        for (Long id : ids) {
            ApiProxy proxy = new ApiProxy();
            proxy.setId(id);
            proxy.setEnabled(enabled);
            updateById(proxy);
        }
    }

    @Override
    @Transactional
    public ApiProxy copyProxy(Long sourceId, String newName) {
        ApiProxy source = getById(sourceId);
        if (source == null) {
            throw new BizException("代理不存在");
        }

        ApiProxy copy = ApiProxy.builder()
                .name(newName)
                .description("复制自: " + source.getName())
                .type(source.getType())
                .targetUrl(source.getTargetUrl())
                .sourcePath(source.getSourcePath())
                .targetPath(source.getTargetPath())
                .methods(source.getMethods())
                .headerTransforms(source.getHeaderTransforms())
                .requestTransforms(source.getRequestTransforms())
                .responseTransforms(source.getResponseTransforms())
                .modelIds(source.getModelIds())
                .channelIds(source.getChannelIds())
                .allowedGroups(source.getAllowedGroups())
                .priority(source.getPriority())
                .timeout(source.getTimeout())
                .retryCount(source.getRetryCount())
                .logRequests(source.getLogRequests())
                .logResponses(source.getLogResponses())
                .cacheEnabled(source.getCacheEnabled())
                .cacheTtl(source.getCacheTtl())
                .authRequired(source.getAuthRequired())
                .rateLimit(source.getRateLimit())
                .enabled(false) // 复制后默认禁用
                .build();

        save(copy);
        log.info("复制API代理: {} -> {}", source.getName(), newName);
        return copy;
    }

    /**
     * 代理结果
     */
    @lombok.Data
    @lombok.Builder
    public static class ProxyResult {
        private boolean success;
        private int status;
        private String body;
        private HttpHeaders headers;
        private long responseTime;
        private Long proxyId;
        private String error;

        public static ProxyResult error(int status, String error) {
            return ProxyResult.builder()
                    .success(false)
                    .status(status)
                    .error(error)
                    .build();
        }
    }
}
