package com.apiplatform.service;

import com.apiplatform.entity.ApiProxy;
import com.apiplatform.entity.ProxyResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * API代理服务接口
 *
 * @author API Platform Team
 */
public interface ApiProxyService extends IService<ApiProxy> {

    /**
     * 创建代理配置
     */
    ApiProxy createProxy(ApiProxy proxy);

    /**
     * 更新代理配置
     */
    ApiProxy updateProxy(Long id, ApiProxy proxy);

    /**
     * 删除代理配置
     */
    void deleteProxy(Long id);

    /**
     * 获取代理详情
     */
    ApiProxy getProxyById(Long id);

    /**
     * 获取所有启用的代理
     */
    List<ApiProxy> getEnabledProxies();

    /**
     * 分页查询代理
     */
    Page<ApiProxy> getProxyPage(int page, int size, String keyword, String type);

    /**
     * 匹配请求获取代理
     */
    ApiProxy matchProxy(String path, String method);

    /**
     * 执行代理请求
     */
    ProxyResult forwardRequest(String path, String method, String body,
                                java.util.Map<String, String> headers,
                                Long userId, String apiKey);

    /**
     * 增加请求计数
     */
    void incrementRequestCount(Long proxyId);

    /**
     * 测试代理连接
     */
    boolean testProxy(ApiProxy proxy);

    /**
     * 批量启用/禁用
     */
    void batchUpdateStatus(List<Long> ids, boolean enabled);

    /**
     * 复制代理配置
     */
    ApiProxy copyProxy(Long sourceId, String newName);
}
