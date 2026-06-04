package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.entity.*;
import com.apiplatform.mapper.*;
import com.apiplatform.service.AgentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 代理商服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

    private final AgentLevelMapper agentLevelMapper;
    private final AgentCommissionMapper agentCommissionMapper;
    private final AgentWithdrawalMapper agentWithdrawalMapper;
    private final AgentCommissionRuleMapper agentCommissionRuleMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String AGENT_CODE_PREFIX = "AG";
    private static final BigDecimal WITHDRAWAL_FEE_RATE = new BigDecimal("0.01"); // 1%手续费
    private static final long COMMISSION_SETTLE_DAYS = 7; // 佣金结算天数

    @Override
    @Transactional
    public Agent apply(Long userId, Map<String, Object> applyData) {
        // 检查用户是否已是代理商
        Agent existing = baseMapper.selectByUserId(userId);
        if (existing != null) {
            throw new BizException("您已经是代理商，无需重复申请");
        }

        // 检查用户是否被邀请（可选）
        Long parentId = null;
        String parentCode = (String) applyData.get("inviteCode");
        if (parentCode != null && !parentCode.isEmpty()) {
            Agent parent = baseMapper.selectByAgentCode(parentCode);
            if (parent == null) {
                throw new BizException("无效的邀请码");
            }
            if (!parent.canSubAgent()) {
                throw new BizException("该代理商暂时无法发展下级代理");
            }
            parentId = parent.getId();
        }

        // 生成代理商代码
        String agentCode = generateAgentCode();

        // 创建代理商记录
        Agent agent = new Agent();
        agent.setUserId(userId);
        agent.setAgentCode(agentCode);
        agent.setParentId(parentId);
        agent.setStatus("pending");
        agent.setTotalUsers(0);
        agent.setTotalRecharge(0L);
        agent.setTotalCommission(0L);
        agent.setAvailableCommission(0L);
        agent.setFrozenCommission(0L);

        // 设置联系信息
        agent.setCompanyName((String) applyData.get("companyName"));
        agent.setContactName((String) applyData.get("contactName"));
        agent.setContactPhone((String) applyData.get("contactPhone"));
        agent.setContactEmail((String) applyData.get("contactEmail"));
        agent.setIdCard((String) applyData.get("idCard"));
        agent.setBusinessLicense((String) applyData.get("businessLicense"));

        baseMapper.insert(agent);

        // 更新用户的邀请码
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setInviteCode(agentCode);
            if (parentId != null) {
                user.setInviterId(parentId);
            }
            userMapper.updateById(user);
        }

        log.info("用户 {} 申请成为代理商，邀请码: {}", userId, agentCode);
        return agent;
    }

    @Override
    @Transactional
    public void review(Long agentId, String status, String rejectReason, Long reviewerId) {
        Agent agent = baseMapper.selectById(agentId);
        if (agent == null) {
            throw new BizException("代理商不存在");
        }

        if (!"pending".equals(agent.getStatus())) {
            throw new BizException("该申请已被处理");
        }

        if ("active".equals(status)) {
            // 审核通过，分配初始等级
            AgentLevel defaultLevel = getDefaultLevel();
            if (defaultLevel != null) {
                agent.setLevelId(defaultLevel.getId());
                agent.setLevel(defaultLevel);
            }
        }

        agent.setStatus(status);
        agent.setRejectReason(rejectReason);
        agent.setReviewedBy(reviewerId);
        agent.setReviewedAt(LocalDateTime.now());

        baseMapper.updateById(agent);

        // 如果拒绝，同步更新上级代理的用户数
        if ("rejected".equals(status) && agent.getParentId() != null) {
            updateParentUserCount(agent.getParentId());
        }

        log.info("代理商 {} 审核状态: {}", agentId, status);
    }

    @Override
    public Agent getAgentDetail(Long userId) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null) {
            return null;
        }

        // 加载关联数据
        if (agent.getLevelId() != null) {
            agent.setLevel(agentLevelMapper.selectById(agent.getLevelId()));
        }
        if (agent.getParentId() != null) {
            agent.setParent(baseMapper.selectById(agent.getParentId()));
            // 加载上级用户信息
            if (agent.getParent() != null) {
                User parentUser = userMapper.selectById(agent.getParent().getUserId());
                agent.getParent().setUser(parentUser);
            }
        }
        agent.setSubAgentCount(baseMapper.countSubAgents(agent.getId()));

        // 加载用户信息
        User user = userMapper.selectById(userId);
        agent.setUser(user);

        return agent;
    }

    @Override
    public Map<String, Object> getAgentStats(Long userId) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null) {
            return null;
        }

        // 获取佣金统计
        Long totalCommission = agentCommissionMapper.sumCommissionByAgentId(agent.getId());

        // 获取下级用户数
        Long directUsers = baseMapper.selectCount(
            new LambdaQueryWrapper<Agent>()
                .eq(Agent::getParentId, agent.getId())
                .eq(Agent::getDeleted, false)
        );

        return Map.of(
            "totalUsers", agent.getTotalUsers(),
            "totalRecharge", agent.getTotalRecharge(),
            "totalCommission", totalCommission != null ? totalCommission : 0L,
            "availableCommission", agent.getAvailableCommission(),
            "frozenCommission", agent.getFrozenCommission(),
            "directUsers", directUsers,
            "levelName", agent.getLevel() != null ? agent.getLevel().getName() : "无",
            "agentCode", agent.getAgentCode(),
            "status", agent.getStatus()
        );
    }

    @Override
    public PageResult<Agent> listAgents(int page, int pageSize, String status, Long levelId) {
        Page<Agent> pageParam = new Page<>(page, pageSize);
        IPage<Agent> result = baseMapper.selectAgentPage(pageParam, status, levelId);

        // 加载关联数据
        for (Agent agent : result.getRecords()) {
            if (agent.getLevelId() != null) {
                agent.setLevel(agentLevelMapper.selectById(agent.getLevelId()));
            }
            agent.setUser(userMapper.selectById(agent.getUserId()));
        }

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public PageResult<AgentCommission> listCommissions(Long userId, int page, int pageSize, String type) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null) {
            return new PageResult<>(List.of(), 0);
        }

        Page<AgentCommission> pageParam = new Page<>(page, pageSize);
        IPage<AgentCommission> result = agentCommissionMapper.selectByAgentId(pageParam, agent.getId(), type);

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional
    public AgentWithdrawal applyWithdrawal(Long userId, Long amount, String method, Map<String, String> accountInfo) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null || !"active".equals(agent.getStatus())) {
            throw new BizException("您还不是有效的代理商");
        }

        if (amount <= 0) {
            throw new BizException("提现金额必须大于0");
        }

        // 检查余额
        if (agent.getAvailableCommission() < amount) {
            throw new BizException("可提现余额不足，当前可提现: " + 
                new BigDecimal(agent.getAvailableCommission()).divide(new BigDecimal(100)) + "元");
        }

        // 计算手续费
        BigDecimal feeRate = getWithdrawalFeeRate();
        long fee = new BigDecimal(amount).multiply(feeRate).setScale(0, RoundingMode.DOWN).longValue();
        long actualAmount = amount - fee;

        // 生成提现单号
        String withdrawalNo = "WD" + System.currentTimeMillis() + 
            String.format("%04d", (int)(Math.random() * 10000));

        // 创建提现记录
        AgentWithdrawal withdrawal = new AgentWithdrawal();
        withdrawal.setWithdrawalNo(withdrawalNo);
        withdrawal.setAgentId(agent.getId());
        withdrawal.setAmount(amount);
        withdrawal.setFee(fee);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setMethod(method);
        withdrawal.setStatus("pending");

        // 设置收款信息
        switch (method) {
            case "bank" -> {
                withdrawal.setBankName(accountInfo.get("bankName"));
                withdrawal.setBankAccount(accountInfo.get("bankAccount"));
                withdrawal.setBankBranch(accountInfo.get("bankBranch"));
            }
            case "alipay" -> withdrawal.setAlipayAccount(accountInfo.get("alipayAccount"));
            case "wechat" -> withdrawal.setWechatAccount(accountInfo.get("wechatAccount"));
        }

        agentWithdrawalMapper.insert(withdrawal);

        // 冻结佣金
        agent.setAvailableCommission(agent.getAvailableCommission() - amount);
        agent.setFrozenCommission(agent.getFrozenCommission() + amount);
        baseMapper.updateById(agent);

        // 记录佣金变动
        AgentCommission commission = new AgentCommission();
        commission.setAgentId(agent.getId());
        commission.setAmount(-amount);
        commission.setBalanceBefore(agent.getAvailableCommission() + amount + agent.getFrozenCommission());
        commission.setBalanceAfter(agent.getAvailableCommission() + agent.getFrozenCommission());
        commission.setType("withdraw");
        commission.setStatus("completed");
        commission.setDescription("申请提现，单号: " + withdrawalNo);
        commission.setCreatedAt(LocalDateTime.now());
        agentCommissionMapper.insert(commission);

        log.info("代理商 {} 申请提现 {} 分", agent.getId(), amount);
        return withdrawal;
    }

    @Override
    public PageResult<AgentWithdrawal> listWithdrawals(Long userId, int page, int pageSize, String status) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null) {
            return new PageResult<>(List.of(), 0);
        }

        Page<AgentWithdrawal> pageParam = new Page<>(page, pageSize);
        IPage<AgentWithdrawal> result = agentWithdrawalMapper.selectByAgentId(pageParam, agent.getId(), status);

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional
    public void processWithdrawal(Long withdrawalId, String status, String rejectReason, Long processorId) {
        AgentWithdrawal withdrawal = agentWithdrawalMapper.selectById(withdrawalId);
        if (withdrawal == null) {
            throw new BizException("提现记录不存在");
        }

        if (!"pending".equals(withdrawal.getStatus())) {
            throw new BizException("该提现申请已被处理");
        }

        Agent agent = baseMapper.selectById(withdrawal.getAgentId());

        if ("completed".equals(status)) {
            // 提现完成
            withdrawal.setStatus("completed");
            withdrawal.setCompletedAt(LocalDateTime.now());
            withdrawal.setProcessedBy(processorId);
            withdrawal.setProcessedAt(LocalDateTime.now());
            agentWithdrawalMapper.updateById(withdrawal);

            // 解冻并扣除佣金
            agent.setFrozenCommission(agent.getFrozenCommission() - withdrawal.getAmount());
            baseMapper.updateById(agent);

            log.info("代理商 {} 提现 {} 分已完成", agent.getId(), withdrawal.getAmount());

        } else if ("rejected".equals(status)) {
            // 提现拒绝
            withdrawal.setStatus("rejected");
            withdrawal.setRejectReason(rejectReason);
            withdrawal.setProcessedBy(processorId);
            withdrawal.setProcessedAt(LocalDateTime.now());
            agentWithdrawalMapper.updateById(withdrawal);

            // 解冻并返还佣金
            agent.setFrozenCommission(agent.getFrozenCommission() - withdrawal.getAmount());
            agent.setAvailableCommission(agent.getAvailableCommission() + withdrawal.getAmount());
            baseMapper.updateById(agent);

            log.info("代理商 {} 提现 {} 分被拒绝，原因: {}", agent.getId(), withdrawal.getAmount(), rejectReason);
        }
    }

    @Override
    @Transactional
    public void creditCommission(Long agentId, Long orderId, String type, Long orderAmount, String description) {
        Agent agent = baseMapper.selectById(agentId);
        if (agent == null || !"active".equals(agent.getStatus())) {
            return;
        }

        // 获取佣金比例
        BigDecimal rate = getEffectiveCommissionRate(agent, type);

        // 计算佣金
        long commission = new BigDecimal(orderAmount)
                .multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .longValue();

        if (commission <= 0) {
            return;
        }

        // 记录佣金
        AgentCommission agentCommission = new AgentCommission();
        agentCommission.setAgentId(agentId);
        agentCommission.setType(type);
        agentCommission.setAmount(commission);
        agentCommission.setBalanceBefore(agent.getAvailableCommission());
        agentCommission.setBalanceAfter(agent.getAvailableCommission() + commission);
        agentCommission.setRate(rate);
        agentCommission.setOrderAmount(orderAmount);
        agentCommission.setOrderId(orderId);
        agentCommission.setDescription(description);
        agentCommission.setStatus("completed");
        agentCommission.setCreatedAt(LocalDateTime.now());
        agentCommissionMapper.insert(agentCommission);

        // 更新代理商余额
        agent.setAvailableCommission(agent.getAvailableCommission() + commission);
        agent.setTotalCommission(agent.getTotalCommission() + commission);
        baseMapper.updateById(agent);

        log.info("代理商 {} 获得佣金 {} 分（{}）", agentId, commission, type);
    }

    @Override
    public Agent getAgentByCode(String agentCode) {
        return baseMapper.selectByAgentCode(agentCode);
    }

    @Override
    @Transactional
    public void checkAndUpgradeLevel(Long userId) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null || !"active".equals(agent.getStatus())) {
            return;
        }

        // 获取所有等级，按level升序
        List<AgentLevel> levels = agentLevelMapper.selectList(
            new LambdaQueryWrapper<AgentLevel>()
                .eq(AgentLevel::getStatus, "active")
                .orderByAsc(AgentLevel::getLevel)
        );

        AgentLevel currentLevel = agent.getLevelId() != null ? 
            agentLevelMapper.selectById(agent.getLevelId()) : null;

        for (AgentLevel level : levels) {
            // 跳过当前等级
            if (currentLevel != null && level.getLevel() <= currentLevel.getLevel()) {
                continue;
            }

            // 检查是否满足升级条件
            boolean canUpgrade = true;

            if (level.getMinUsers() != null && level.getMinUsers() > 0) {
                if (agent.getTotalUsers() < level.getMinUsers()) {
                    canUpgrade = false;
                }
            }

            if (level.getMinRecharge() != null && level.getMinRecharge() > 0) {
                if (agent.getTotalRecharge() < level.getMinRecharge()) {
                    canUpgrade = false;
                }
            }

            if (canUpgrade) {
                // 执行升级
                agent.setLevelId(level.getId());
                agent.setLevel(level);
                baseMapper.updateById(agent);

                // 记录佣金（升级奖励）
                creditCommission(agent.getId(), null, "upgrade", 
                    level.getMinRecharge() != null ? level.getMinRecharge() : 0L,
                    "升级到" + level.getName());

                log.info("代理商 {} 升级到等级 {}", agent.getId(), level.getName());
                break;
            }
        }
    }

    @Override
    public BigDecimal getCommissionRate(Long userId) {
        Agent agent = baseMapper.selectByUserId(userId);
        if (agent == null || agent.getLevel() == null) {
            return new BigDecimal("0.05"); // 默认5%
        }
        return agent.getLevel().getCommissionRate();
    }

    @Override
    public BigDecimal getWithdrawalFeeRate() {
        return WITHDRAWAL_FEE_RATE;
    }

    @Override
    public List<AgentLevel> getAgentLevels() {
        return agentLevelMapper.selectList(
            new LambdaQueryWrapper<AgentLevel>()
                .eq(AgentLevel::getStatus, "active")
                .orderByAsc(AgentLevel::getLevel)
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 生成唯一的代理商代码
     */
    private String generateAgentCode() {
        String code;
        do {
            code = AGENT_CODE_PREFIX + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (baseMapper.selectByAgentCode(code) != null);
        return code;
    }

    /**
     * 获取默认等级
     */
    private AgentLevel getDefaultLevel() {
        return agentLevelMapper.selectOne(
            new LambdaQueryWrapper<AgentLevel>()
                .eq(AgentLevel::getStatus, "active")
                .orderByAsc(AgentLevel::getLevel)
                .last("LIMIT 1")
        );
    }

    /**
     * 获取有效的佣金比例
     */
    private BigDecimal getEffectiveCommissionRate(Agent agent, String type) {
        // 先查询配置的规则
        List<AgentCommissionRule> rules = agentCommissionRuleMapper.selectActiveRulesByType(type);
        if (!rules.isEmpty()) {
            // 使用第一条匹配的规则
            AgentCommissionRule rule = rules.get(0);
            if (rule.getLevelId() == null || rule.getLevelId().equals(agent.getLevelId())) {
                return rule.getCommissionValue();
            }
        }

        // 使用等级配置的佣金比例
        if (agent.getLevel() != null) {
            return agent.getLevel().getCommissionRate();
        }

        return new BigDecimal("0.05");
    }

    /**
     * 更新上级代理商的用户数
     */
    private void updateParentUserCount(Long parentId) {
        if (parentId == null) return;

        Agent parent = baseMapper.selectById(parentId);
        if (parent == null) return;

        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Agent>()
                .eq(Agent::getParentId, parentId)
                .eq(Agent::getDeleted, false)
        );

        parent.setTotalUsers(count.intValue());
        baseMapper.updateById(parent);

        // 递归更新上上级
        if (parent.getParentId() != null) {
            updateParentUserCount(parent.getParentId());
        }
    }
}
