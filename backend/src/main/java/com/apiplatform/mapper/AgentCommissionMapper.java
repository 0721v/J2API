package com.apiplatform.mapper;

import com.apiplatform.entity.AgentCommission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 佣金记录 Mapper
 */
@Mapper
public interface AgentCommissionMapper extends BaseMapper<AgentCommission> {

    /**
     * 分页查询佣金记录
     */
    IPage<AgentCommission> selectByAgentId(Page<AgentCommission> page, @Param("agentId") Long agentId, @Param("type") String type);

    /**
     * 统计代理商佣金总额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM agent_commissions WHERE agent_id = #{agentId} AND status = 'completed' AND amount > 0")
    Long sumCommissionByAgentId(@Param("agentId") Long agentId);

    /**
     * 查询用户的所有上级代理商
     */
    @Select("SELECT a.* FROM agents a INNER JOIN users u ON a.user_id = u.id WHERE u.inviter_id = #{userId} AND a.status = 'active'")
    java.util.List<Agent> selectAncestorsByUserId(@Param("userId") Long userId);
}
