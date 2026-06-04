package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Agent;
import com.apiplatform.entity.AgentCommission;
import com.apiplatform.entity.AgentLevel;
import com.apiplatform.entity.AgentWithdrawal;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 代理商服务接口
 */
public interface AgentService extends IService<Agent> {

    /**
     * 申请成为代理商
     */
    Agent apply(Long userId, Map<String, Object> applyData);

    /**
     * 审核代理商申请
     */
    void review(Long agentId, String status, String rejectReason, Long reviewerId);

    /**
     * 获取代理商详情
     */
    Agent getAgentDetail(Long userId);

    /**
     * 获取代理商统计信息
     */
    Map<String, Object> getAgentStats(Long userId);

    /**
     * 分页查询代理商列表（管理员）
     */
    PageResult<Agent> listAgents(int page, int pageSize, String status, Long levelId);

    /**
     * 获取代理商佣金记录
     */
    PageResult<AgentCommission> listCommissions(Long userId, int page, int pageSize, String type);

    /**
     * 申请提现
     */
    AgentWithdrawal applyWithdrawal(Long userId, Long amount, String method, Map<String, String> accountInfo);

    /**
     * 获取提现记录
     */
    PageResult<AgentWithdrawal> listWithdrawals(Long userId, int page, int pageSize, String status);

    /**
     * 处理提现申请（管理员）
     */
    void processWithdrawal(Long withdrawalId, String status, String rejectReason, Long processorId);

    /**
     * 发放佣金
     */
    void creditCommission(Long agentId, Long orderId, String type, Long orderAmount, String description);

    /**
     * 根据邀请码获取代理商
     */
    Agent getAgentByCode(String agentCode);

    /**
     * 计算用户是否达到升级条件
     */
    void checkAndUpgradeLevel(Long userId);

    /**
     * 获取佣金比例
     */
    BigDecimal getCommissionRate(Long userId);

    /**
     * 提现手续费率
     */
    BigDecimal getWithdrawalFeeRate();

    /**
     * 获取代理商等级列表
     */
    List<AgentLevel> getAgentLevels();
}
