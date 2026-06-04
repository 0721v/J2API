package com.apiplatform.mapper;

import com.apiplatform.entity.ApiProxy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API代理Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface ApiProxyMapper extends BaseMapper<ApiProxy> {

    /**
     * 分页查询代理
     */
    IPage<ApiProxy> selectProxyPage(Page<ApiProxy> page, @Param("keyword") String keyword, @Param("type") String type);

    /**
     * 查询所有启用的代理（按优先级排序）
     */
    @Select("SELECT * FROM api_proxies WHERE enabled = true AND deleted = false ORDER BY priority DESC")
    List<ApiProxy> selectEnabledProxies();

    /**
     * 根据名称查询
     */
    @Select("SELECT * FROM api_proxies WHERE name = #{name} AND deleted = false")
    ApiProxy selectByName(@Param("name") String name);

    /**
     * 根据类型查询
     */
    @Select("SELECT * FROM api_proxies WHERE type = #{type} AND enabled = true AND deleted = false")
    List<ApiProxy> selectByType(@Param("type") String type);

    /**
     * 增加请求计数
     */
    @Update("UPDATE api_proxies SET request_count = request_count + 1, last_used_at = #{lastUsedAt} WHERE id = #{id}")
    int incrementRequestCount(@Param("id") Long id, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}