package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.ApiProxy;
import com.apiplatform.service.ApiProxyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API代理控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/admin/proxies")
@RequiredArgsConstructor
public class ApiProxyController {

    private final ApiProxyService apiProxyService;

    /**
     * 获取代理列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ApiProxy>> getProxyList() {
        List<ApiProxy> proxies = apiProxyService.list();
        return Result.success(proxies);
    }

    /**
     * 分页查询代理
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ApiProxy>> getProxyPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        PageResult<ApiProxy> result = apiProxyService.getProxyPage(page, size, keyword, type);
        return Result.success(result);
    }

    /**
     * 获取代理详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApiProxy> getProxyDetail(@PathVariable Long id) {
        ApiProxy proxy = apiProxyService.getProxyById(id);
        if (proxy == null) {
            return Result.notFound("代理不存在");
        }
        return Result.success(proxy);
    }

    /**
     * 创建代理
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApiProxy> createProxy(@Validated @RequestBody ApiProxyRequest request) {
        try {
            ApiProxy proxy = buildProxyFromRequest(request);
            proxy = apiProxyService.createProxy(proxy);
            return Result.success("代理创建成功", proxy);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新代理
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApiProxy> updateProxy(@PathVariable Long id, @RequestBody ApiProxyRequest request) {
        try {
            ApiProxy proxy = buildProxyFromRequest(request);
            proxy = apiProxyService.updateProxy(id, proxy);
            return Result.success("代理更新成功", proxy);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除代理
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteProxy(@PathVariable Long id) {
        try {
            apiProxyService.deleteProxy(id);
            return Result.success("代理删除成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 复制代理
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApiProxy> copyProxy(@PathVariable Long id, @RequestParam String newName) {
        try {
            ApiProxy proxy = apiProxyService.copyProxy(id, newName);
            return Result.success("代理复制成功", proxy);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 测试代理连接
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> testProxy(@PathVariable Long id) {
        ApiProxy proxy = apiProxyService.getProxyById(id);
        if (proxy == null) {
            return Result.notFound("代理不存在");
        }

        boolean success = apiProxyService.testProxy(proxy);
        if (success) {
            return Result.success("连接测试成功", true);
        } else {
            return Result.error(400, "连接测试失败，请检查目标URL是否可访问");
        }
    }

    /**
     * 批量启用/禁用
     */
    @PostMapping("/batch-status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchUpdateStatus(
            @RequestBody BatchStatusRequest request) {
        apiProxyService.batchUpdateStatus(request.getIds(), request.isEnabled());
        return Result.success("状态更新成功", null);
    }

    /**
     * 切换代理状态
     */
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleProxy(@PathVariable Long id) {
        ApiProxy proxy = apiProxyService.getById(id);
        if (proxy == null) {
            return Result.notFound("代理不存在");
        }

        proxy.setEnabled(!proxy.getEnabled());
        apiProxyService.updateById(proxy);
        return Result.success(proxy.getEnabled() ? "代理已启用" : "代理已禁用", null);
    }

    /**
     * 获取启用的代理
     */
    @GetMapping("/enabled")
    public Result<List<ApiProxy>> getEnabledProxies() {
        List<ApiProxy> proxies = apiProxyService.getEnabledProxies();
        return Result.success(proxies);
    }

    // ==================== 辅助方法 ====================

    private ApiProxy buildProxyFromRequest(ApiProxyRequest request) {
        return ApiProxy.builder()
                .name(request.getName())
                .type(request.getType())
                .targetUrl(request.getTargetUrl())
                .sourcePath(request.getSourcePath())
                .targetPath(request.getTargetPath())
                .methods(request.getMethods())
                .headerTransforms(request.getHeaderTransforms())
                .requestTransforms(request.getRequestTransforms())
                .responseTransforms(request.getResponseTransforms())
                .modelIds(request.getModelIds())
                .channelIds(request.getChannelIds())
                .allowedGroups(request.getAllowedGroups())
                .priority(request.getPriority())
                .timeout(request.getTimeout())
                .retryCount(request.getRetryCount())
                .logRequests(request.getLogRequests())
                .logResponses(request.getLogResponses())
                .cacheEnabled(request.getCacheEnabled())
                .cacheTtl(request.getCacheTtl())
                .authRequired(request.getAuthRequired())
                .rateLimit(request.getRateLimit())
                .enabled(request.getEnabled())
                .description(request.getDescription())
                .build();
    }

    // ==================== 请求DTO ====================

    @Data
    public static class ApiProxyRequest {
        private String name;
        private String type;
        private String targetUrl;
        private String sourcePath;
        private String targetPath;
        private String methods;
        private String headerTransforms;
        private String requestTransforms;
        private String responseTransforms;
        private String modelIds;
        private String channelIds;
        private String allowedGroups;
        private Integer priority;
        private Integer timeout;
        private Integer retryCount;
        private Boolean logRequests;
        private Boolean logResponses;
        private Boolean cacheEnabled;
        private Integer cacheTtl;
        private String authRequired;
        private String rateLimit;
        private Boolean enabled;
        private String description;
    }

    @Data
    public static class BatchStatusRequest {
        private List<Long> ids;
        private boolean enabled;
    }
}
