package com.apiplatform.service;

import com.apiplatform.entity.Agent;
import com.apiplatform.entity.AgentCommission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 佣金结算服务
 * 负责自动计算和发放佣金
 */
public interface CommissionSettlementService extends IService<AgentCommission> {

    /**
     * 处理充值佣金
     * 当用户充值时，自动发放佣金给上级代理商
     */
    void settleRechargeCommission(Long userId, Long orderId, Long amount);

    /**
     * 处理套餐购买佣金
     * 当用户购买套餐时，自动发放佣金给上级代理商
     */
    void settlePackageCommission(Long userId, Long orderId, Long amount);

    /**
     * 处理升级奖励
     * 当用户升级套餐时，自动发放佣金给上级代理商
     */
    void settleUpgradeCommission(Long userId, Long orderId, Long amount);

    /**
     * 计算用户的所有上级代理商链
     * 按距离远近排序（直属上级排第一）
     */
    List<Agent> getAgentChain(Long userId);

    /**
     * 获取佣金统计
     */
    Map<String, Object> getCommissionStats(Long agentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 月度佣金汇总
     */
    Map<String, Object> getMonthlyCommissionSummary(Long agentId, int year, int month);

    /**
     * 获取佣金趋势数据
     */
    List<Map<String, Object>> getCommissionTrend(Long agentId, int days);

    /**
     * 批量结算佣金（定时任务调用）
     */
    void batchSettleCommissions();

    /**
     * 解冻已结算的佣金
     */
    void unfreezeCommissions(Long agentId, Long amount);

    /**
     * 冻结佣金
     */
    void freezeCommissions(Long agentId, Long amount);

    /**
     * 获取推荐用户列表
     */
    List<Map<String, Object>> getReferralUsers(Long agentId, int page, int pageSize);

    /**
     * 获取下级代理商列表
     */
    List<Agent> getSubAgents(Long agentId);
}
