package com.apiplatform.mapper;

import com.apiplatform.entity.AgentWithdrawal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 代理商提现记录 Mapper
 */
@Mapper
public interface AgentWithdrawalMapper extends BaseMapper<AgentWithdrawal> {

    /**
     * 分页查询提现记录
     */
    IPage<AgentWithdrawal> selectByAgentId(Page<AgentWithdrawal> page, @Param("agentId") Long agentId, @Param("status") String status);

    /**
     * 分页查询所有提现记录（管理员）
     */
    IPage<AgentWithdrawal> selectAll(Page<AgentWithdrawal> page, @Param("status") String status, @Param("agentId") Long agentId);
}
