package com.apiplatform.service.impl;

import com.apiplatform.entity.*;
import com.apiplatform.mapper.*;
import com.apiplatform.service.AgentService;
import com.apiplatform.service.CommissionSettlementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 佣金结算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionSettlementServiceImpl 
    extends ServiceImpl<AgentCommissionMapper, AgentCommission> 
    implements CommissionSettlementService {

    private final AgentMapper agentMapper;
    private final AgentLevelMapper agentLevelMapper;
    private final AgentCommissionRuleMapper commissionRuleMapper;
    private final UserMapper userMapper;
    private final AgentService agentService;

    // 多级代理佣金分配比例（从上到下依次递减）
    private static final BigDecimal[] LEVEL_RATES = {
        new BigDecimal("0.50"),  // 第一级（直属上级）：50%
        new BigDecimal("0.30"),  // 第二级：30%
        new BigDecimal("0.20")   // 第三级：20%
    };

    private static final int MAX_LEVEL = 3; // 最多三级代理

    @Override
    @Transactional
    @Async
    public void settleRechargeCommission(Long userId, Long orderId, Long amount) {
        log.info("开始处理充值佣金: userId={}, orderId={}, amount={}", userId, orderId, amount);

        List<Agent> agentChain = getAgentChain(userId);
        if (agentChain.isEmpty()) {
            log.info("用户 {} 没有上级代理商，跳过佣金结算", userId);
            return;
        }

        // 获取充值佣金规则
        List<AgentCommissionRule> rules = commissionRuleMapper.selectActiveRulesByType("recharge");
        BigDecimal baseRate = getBaseRate(rules, "recharge");

        // 逐级分配佣金
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        long remainingAmount = amount;
        for (int i = 0; i < Math.min(agentChain.size(), MAX_LEVEL); i++) {
            Agent agent = agentChain.get(i);
            if (!agent.isActive()) continue;

            // 计算该级别的佣金
            BigDecimal levelRate = LEVEL_RATES[i];
            BigDecimal agentRate = getAgentEffectiveRate(agent, "recharge", baseRate);
            BigDecimal finalRate = levelRate.multiply(agentRate);

            long commission = amountBD.multiply(finalRate)
                    .setScale(0, RoundingMode.DOWN)
                    .longValue();

            if (commission <= 0) continue;

            // 发放佣金
            creditCommission(agent, orderId, "recharge", amount, commission, finalRate,
                    String.format("下级用户充值佣金（%d级）", i + 1));

            // 更新代理商累计充值
            updateAgentRecharge(agent.getId(), amount);

            remainingAmount -= commission;
        }

        log.info("充值佣金结算完成: orderId={}, 发放总额={}", orderId, amount - remainingAmount);
    }

    @Override
    @Transactional
    @Async
    public void settlePackageCommission(Long userId, Long orderId, Long amount) {
        log.info("开始处理套餐购买佣金: userId={}, orderId={}, amount={}", userId, orderId, amount);

        List<Agent> agentChain = getAgentChain(userId);
        if (agentChain.isEmpty()) {
            log.info("用户 {} 没有上级代理商，跳过佣金结算", userId);
            return;
        }

        List<AgentCommissionRule> rules = commissionRuleMapper.selectActiveRulesByType("package");
        BigDecimal baseRate = getBaseRate(rules, "package");
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        long remainingAmount = amount;

        for (int i = 0; i < Math.min(agentChain.size(), MAX_LEVEL); i++) {
            Agent agent = agentChain.get(i);
            if (!agent.isActive()) continue;

            BigDecimal levelRate = LEVEL_RATES[i];
            BigDecimal agentRate = getAgentEffectiveRate(agent, "package", baseRate);
            BigDecimal finalRate = levelRate.multiply(agentRate);

            long commission = amountBD.multiply(finalRate)
                    .setScale(0, RoundingMode.DOWN)
                    .longValue();

            if (commission <= 0) continue;

            creditCommission(agent, orderId, "package", amount, commission, finalRate,
                    String.format("下级用户套餐购买佣金（%d级）", i + 1));

            remainingAmount -= commission;
        }

        log.info("套餐购买佣金结算完成: orderId={}", orderId);
    }

    @Override
    @Transactional
    @Async
    public void settleUpgradeCommission(Long userId, Long orderId, Long amount) {
        log.info("开始处理升级奖励: userId={}, orderId={}, amount={}", userId, orderId, amount);

        List<Agent> agentChain = getAgentChain(userId);
        if (agentChain.isEmpty()) return;

        List<AgentCommissionRule> rules = commissionRuleMapper.selectActiveRulesByType("upgrade");
        BigDecimal baseRate = getBaseRate(rules, "upgrade");
        BigDecimal amountBD = BigDecimal.valueOf(amount);

        for (int i = 0; i < Math.min(agentChain.size(), MAX_LEVEL); i++) {
            Agent agent = agentChain.get(i);
            if (!agent.isActive()) continue;

            BigDecimal levelRate = LEVEL_RATES[i];
            BigDecimal agentRate = getAgentEffectiveRate(agent, "upgrade", baseRate);
            BigDecimal finalRate = levelRate.multiply(agentRate);

            long commission = amountBD.multiply(finalRate)
                    .setScale(0, RoundingMode.DOWN)
                    .longValue();

            if (commission <= 0) continue;

            creditCommission(agent, orderId, "upgrade", amount, commission, finalRate,
                    String.format("下级用户升级奖励（%d级）", i + 1));
        }

        log.info("升级奖励结算完成: orderId={}", orderId);
    }

    @Override
    public List<Agent> getAgentChain(Long userId) {
        List<Agent> chain = new ArrayList<>();
        Long currentUserId = userId;

        Set<Long> visited = new HashSet<>(); // 防止循环引用
        int maxDepth = MAX_LEVEL;

        while (currentUserId != null && maxDepth > 0) {
            if (visited.contains(currentUserId)) break;
            visited.add(currentUserId);

            // 查找用户的上级代理商
            User user = userMapper.selectById(currentUserId);
            if (user == null || user.getInviterId() == null) break;

            // 检查邀请人是否是代理商
            Agent agent = agentMapper.selectByUserId(user.getInviterId());
            if (agent != null && agent.isActive()) {
                chain.add(agent);
                maxDepth--;
            }

            currentUserId = user.getInviterId();
        }

        return chain;
    }

    @Override
    public Map<String, Object> getCommissionStats(Long agentId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AgentCommission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentCommission::getAgentId, agentId)
                .between(AgentCommission::getCreatedAt, startTime, endTime)
                .eq(AgentCommission::getStatus, "completed");

        List<AgentCommission> commissions = baseMapper.selectList(queryWrapper);

        Map<String, Long> byType = commissions.stream()
                .filter(c -> c.getAmount() > 0)
                .collect(Collectors.groupingBy(
                        AgentCommission::getType,
                        Collectors.summingLong(AgentCommission::getAmount)
                ));

        long total = commissions.stream()
                .filter(c -> c.getAmount() > 0)
                .mapToLong(AgentCommission::getAmount)
                .sum();

        int count = (int) commissions.stream()
                .filter(c -> c.getAmount() > 0)
                .count();

        return Map.of(
                "total", total,
                "count", count,
                "average", count > 0 ? total / count : 0,
                "byType", byType
        );
    }

    @Override
    public Map<String, Object> getMonthlyCommissionSummary(Long agentId, int year, int month) {
        LocalDateTime startTime = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endTime = startTime.plusMonths(1);

        Map<String, Object> stats = getCommissionStats(agentId, startTime, endTime);

        // 获取每日汇总
        LambdaQueryWrapper<AgentCommission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentCommission::getAgentId, agentId)
                .between(AgentCommission::getCreatedAt, startTime, endTime)
                .eq(AgentCommission::getStatus, "completed")
                .gt(AgentCommission::getAmount, 0);

        List<AgentCommission> commissions = baseMapper.selectList(queryWrapper);

        Map<String, Long> dailyStats = commissions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().toString(),
                        Collectors.summingLong(AgentCommission::getAmount)
                ));

        stats.put("dailyStats", dailyStats);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getCommissionTrend(Long agentId, int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();

            Map<String, Object> dayStat = getCommissionStats(agentId, startTime, endTime);
            trend.add(Map.of(
                    "date", date.toString(),
                    "amount", dayStat.get("total"),
                    "count", dayStat.get("count")
            ));
        }

        return trend;
    }

    @Override
    @Transactional
    public void batchSettleCommissions() {
        log.info("开始执行批量佣金结算任务");
        // 这里是定时任务入口，实际结算在各个业务操作时实时处理
        // 如需批量处理未结算佣金，可以在这里补充逻辑
    }

    @Override
    @Transactional
    public void unfreezeCommissions(Long agentId, Long amount) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent != null && agent.getFrozenCommission() >= amount) {
            agent.setFrozenCommission(agent.getFrozenCommission() - amount);
            agent.setAvailableCommission(agent.getAvailableCommission() + amount);
            agentMapper.updateById(agent);
            log.info("解冻佣金: agentId={}, amount={}", agentId, amount);
        }
    }

    @Override
    @Transactional
    public void freezeCommissions(Long agentId, Long amount) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent != null && agent.getAvailableCommission() >= amount) {
            agent.setAvailableCommission(agent.getAvailableCommission() - amount);
            agent.setFrozenCommission(agent.getFrozenCommission() + amount);
            agentMapper.updateById(agent);
            log.info("冻结佣金: agentId={}, amount={}", agentId, amount);
        }
    }

    @Override
    public List<Map<String, Object>> getReferralUsers(Long agentId, int page, int pageSize) {
        // 获取直属用户
        User user = agentMapper.selectByUserId(agentId).getUser();
        if (user == null) return Collections.emptyList();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getInviterId, agentId)
                .orderByDesc(User::getCreatedAt);

        // 分页查询
        long offset = (long) (page - 1) * pageSize;
        queryWrapper.last("LIMIT " + offset + ", " + pageSize);

        List<User> users = userMapper.selectList(queryWrapper);

        return users.stream().map(u -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", u.getId());
            userInfo.put("username", u.getUsername());
            userInfo.put("email", u.getEmail());
            userInfo.put("createdAt", u.getCreatedAt());

            // 统计该用户的累计充值
            // TODO: 关联查询订单表获取
            userInfo.put("totalRecharge", 0L);

            return userInfo;
        }).collect(Collectors.toList());
    }

    @Override
    public long countReferralUsers(Long agentId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getInviterId, agentId);
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public List<Agent> getSubAgents(Long agentId) {
        return agentMapper.selectList(
                new LambdaQueryWrapper<Agent>()
                        .eq(Agent::getParentId, agentId)
                        .eq(Agent::getDeleted, false)
                        .eq(Agent::getStatus, "active")
                        .orderByDesc(Agent::getCreatedAt)
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 发放佣金
     */
    private void creditCommission(Agent agent, Long orderId, String type, Long orderAmount,
                                   Long commission, BigDecimal rate, String description) {
        // 记录佣金
        AgentCommission agentCommission = new AgentCommission();
        agentCommission.setAgentId(agent.getId());
        agentCommission.setType(type);
        agentCommission.setAmount(commission);
        agentCommission.setBalanceBefore(agent.getAvailableCommission());
        agentCommission.setBalanceAfter(agent.getAvailableCommission() + commission);
        agentCommission.setRate(rate);
        agentCommission.setOrderAmount(orderAmount);
        agentCommission.setOrderId(orderId);
        agentCommission.setDescription(description);
        agentCommission.setStatus("completed");
        agentCommission.setSettledAt(LocalDateTime.now());
        agentCommission.setCreatedAt(LocalDateTime.now());
        baseMapper.insert(agentCommission);

        // 更新代理商余额
        agent.setAvailableCommission(agent.getAvailableCommission() + commission);
        agent.setTotalCommission(agent.getTotalCommission() + commission);
        agentMapper.updateById(agent);

        log.info("代理商 {} 获得佣金 {} 分: {}", agent.getId(), commission, description);
    }

    /**
     * 更新代理商累计充值
     */
    private void updateAgentRecharge(Long agentId, Long amount) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent != null) {
            agent.setTotalRecharge(agent.getTotalRecharge() + amount);
            agentMapper.updateById(agent);
        }
    }

    /**
     * 获取基础佣金比例
     */
    private BigDecimal getBaseRate(List<AgentCommissionRule> rules, String type) {
        if (!rules.isEmpty()) {
            return rules.get(0).getCommissionValue();
        }
        // 默认比例
        return switch (type) {
            case "recharge" -> new BigDecimal("0.10");   // 10%
            case "package" -> new BigDecimal("0.15");    // 15%
            case "upgrade" -> new BigDecimal("0.20");    // 20%
            default -> new BigDecimal("0.10");
        };
    }

    /**
     * 获取代理商的有效佣金比例
     */
    private BigDecimal getAgentEffectiveRate(Agent agent, String type, BigDecimal baseRate) {
        // 使用等级配置的佣金比例
        if (agent.getLevel() != null) {
            return agent.getLevel().getCommissionRate();
        }
        return baseRate;
    }
}
