package com.apiplatform.mapper;

import com.apiplatform.entity.Agent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 代理商 Mapper
 */
@Mapper
public interface AgentMapper extends BaseMapper<Agent> {

    /**
     * 分页查询代理商列表
     */
    IPage<Agent> selectAgentPage(Page<Agent> page, @Param("status") String status, @Param("levelId") Long levelId);

    /**
     * 根据邀请码查询
     */
    @Select("SELECT * FROM agents WHERE agent_code = #{code} AND status = 'active' AND deleted = 0 LIMIT 1")
    Agent selectByAgentCode(@Param("code") String code);

    /**
     * 统计下级代理商数量
     */
    @Select("SELECT COUNT(*) FROM agents WHERE parent_id = #{agentId} AND deleted = 0")
    Long countSubAgents(@Param("agentId") Long agentId);

    /**
     * 查询用户的代理商信息
     */
    @Select("SELECT * FROM agents WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    Agent selectByUserId(@Param("userId") Long userId);
}
