package com.apiplatform.mapper;

import com.apiplatform.entity.AgentCommissionRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 代理分成配置 Mapper
 */
@Mapper
public interface AgentCommissionRuleMapper extends BaseMapper<AgentCommissionRule> {

    /**
     * 获取有效规则
     */
    @Select("SELECT * FROM agent_commission_rules WHERE status = 'active' " +
            "AND (start_time IS NULL OR start_time <= NOW()) " +
            "AND (end_time IS NULL OR end_time >= NOW()) " +
            "ORDER BY priority DESC")
    List<AgentCommissionRule> selectActiveRules();

    /**
     * 根据类型获取有效规则
     */
    @Select("SELECT * FROM agent_commission_rules WHERE status = 'active' AND type = #{type} " +
            "AND (start_time IS NULL OR start_time <= NOW()) " +
            "AND (end_time IS NULL OR end_time >= NOW()) " +
            "ORDER BY priority DESC")
    List<AgentCommissionRule> selectActiveRulesByType(@Param("type") String type);
}
