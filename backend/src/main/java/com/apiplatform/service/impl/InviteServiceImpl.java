package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.entity.*;
import com.apiplatform.mapper.*;
import com.apiplatform.service.InviteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 邀请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteServiceImpl extends ServiceImpl<InviteRecordMapper, InviteRecord> implements InviteService {

    private final UserMapper userMapper;
    private final InviteRewardMapper inviteRewardMapper;
    private final InviteRewardRecordMapper rewardRecordMapper;
    private final AgentMapper agentMapper;

    private static final String INVITE_CODE_PREFIX = "INV";

    @Override
    public InviteRecord validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            return null;
        }

        // 先查询是否是用户的邀请码
        User inviter = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getInviteCode, inviteCode)
                        .eq(User::getDeleted, false)
        );

        if (inviter != null) {
            InviteRecord record = new InviteRecord();
            record.setInviterId(inviter.getId());
            record.setInviteeId(null);
            record.setInviteCode(inviteCode);
            record.setStatus("active");
            return record;
        }

        // 查询是否是代理商的邀请码
        Agent agent = agentMapper.selectByAgentCode(inviteCode);
        if (agent != null && agent.isActive()) {
            InviteRecord record = new InviteRecord();
            record.setInviterId(agent.getUserId());
            record.setInviteCode(inviteCode);
            record.setStatus("active");
            return record;
        }

        return null;
    }

    @Override
    @Transactional
    public InviteRecord createInviteRecord(Long inviterId, Long inviteeId, String inviteCode, String source) {
        // 检查是否已有邀请关系
        InviteRecord existing = baseMapper.selectByInviteeId(inviteeId);
        if (existing != null) {
            return existing;
        }

        InviteRecord record = new InviteRecord();
        record.setInviterId(inviterId);
        record.setInviteeId(inviteeId);
        record.setInviteCode(inviteCode);
        record.setSource(source != null ? source : "manual");
        record.setStatus("active");
        record.setRewardsCredited(false);
        record.setRewardAmount(0L);
        record.setCreatedAt(LocalDateTime.now());

        baseMapper.insert(record);

        // 更新邀请人的用户数
        User inviter = userMapper.selectById(inviterId);
        if (inviter != null) {
            inviter.setInviteCode(inviter.getInviteCode());
            userMapper.updateById(inviter);
        }

        // 如果邀请人是代理商，更新其用户数
        Agent agent = agentMapper.selectByUserId(inviterId);
        if (agent != null) {
            agent.setTotalUsers(agent.getTotalUsers() + 1);
            agentMapper.updateById(agent);
        }

        log.info("创建邀请记录: inviterId={}, inviteeId={}, code={}", inviterId, inviteeId, inviteCode);
        return record;
    }

    @Override
    @Transactional
    @Async
    public void creditRegisterReward(Long inviterId, Long inviteeId) {
        log.info("处理注册奖励: inviterId={}, inviteeId={}", inviterId, inviteeId);

        // 获取注册奖励配置
        List<InviteReward> rewards = inviteRewardMapper.selectActiveRewardsByType("register");
        if (rewards.isEmpty()) {
            log.info("没有注册奖励配置");
            return;
        }

        InviteReward reward = rewards.get(0);

        // 检查邀请人奖励次数限制
        if (reward.getTotalLimit() != null) {
            int count = rewardRecordMapper.countByInviterId(inviterId);
            if (count >= reward.getTotalLimit()) {
                log.info("邀请人奖励次数已达上限: inviterId={}", inviterId);
                return;
            }
        }

        // 检查每日限制
        if (reward.getDailyLimit() != null) {
            int todayCount = rewardRecordMapper.countByInviterIdToday(inviterId);
            if (todayCount >= reward.getDailyLimit()) {
                log.info("今日奖励次数已达上限: inviterId={}", inviterId);
                return;
            }
        }

        // 计算奖励金额
        long inviterReward = reward.calculateInviterReward(0L);
        long inviteeReward = reward.calculateInviteeReward(0L);

        // 发放邀请人奖励
        if (inviterReward > 0) {
            creditReward(inviterId, inviteeId, reward, "inviter", inviterReward, "邀请新用户注册奖励");
        }

        // 发放被邀请人奖励
        if (inviteeReward > 0) {
            creditReward(inviterId, inviteeId, reward, "invitee", inviteeReward, "新用户注册奖励");
        }

        // 更新邀请记录
        InviteRecord record = baseMapper.selectByInviteeId(inviteeId);
        if (record != null) {
            record.setRewardsCredited(true);
            record.setRewardAmount(inviterReward + inviteeReward);
            baseMapper.updateById(record);
        }
    }

    @Override
    @Transactional
    @Async
    public void creditRechargeReward(Long inviterId, Long inviteeId, Long orderId, Long rechargeAmount) {
        log.info("处理充值奖励: inviterId={}, inviteeId={}, amount={}", inviterId, inviteeId, rechargeAmount);

        // 获取充值奖励配置
        List<InviteReward> rewards = inviteRewardMapper.selectActiveRewardsByType("recharge");
        if (rewards.isEmpty()) {
            return;
        }

        InviteReward reward = rewards.get(0);

        // 检查最低充值金额
        if (reward.getMinRechargeAmount() != null && rechargeAmount < reward.getMinRechargeAmount()) {
            log.info("充值金额未达到最低要求: {} < {}", rechargeAmount, reward.getMinRechargeAmount());
            return;
        }

        // 计算奖励
        long inviterReward = reward.calculateInviterReward(rechargeAmount);
        long inviteeReward = reward.calculateInviteeReward(rechargeAmount);

        if (inviterReward > 0) {
            creditReward(inviterId, inviteeId, reward, "inviter", inviterReward, 
                    String.format("下级用户充值奖励（充值¥%s）", formatMoney(rechargeAmount)));
        }

        if (inviteeReward > 0) {
            creditReward(inviterId, inviteeId, reward, "invitee", inviteeReward, 
                    String.format("充值返利（充值¥%s）", formatMoney(rechargeAmount)));
        }
    }

    @Override
    public Map<String, Object> getInviteStats(Long userId) {
        // 统计邀请人数
        int inviteeCount = baseMapper.countByInviterId(userId);

        // 获取有效的邀请奖励
        List<InviteReward> rewards = inviteRewardMapper.selectActiveRewardsByType("register");
        long registerReward = 0;
        if (!rewards.isEmpty()) {
            registerReward = rewards.get(0).getInviterRewardValue() != null ? 
                    rewards.get(0).getInviterRewardValue() : 0L;
        }

        // 获取用户的邀请码
        User user = userMapper.selectById(userId);
        String inviteCode = user != null ? user.getInviteCode() : null;

        // 获取邀请链接
        String inviteLink = null;
        if (inviteCode != null) {
            inviteLink = "/register?invite=" + inviteCode;
        }

        // 获取奖励金额统计
        LambdaQueryWrapper<InviteRewardRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InviteRewardRecord::getInviterId, userId);
        
        List<InviteRewardRecord> records = rewardRecordMapper.selectList(queryWrapper);
        long totalReward = records.stream()
                .filter(r -> "inviter".equals(r.getRewardTo()))
                .mapToLong(InviteRewardRecord::getAmount)
                .sum();

        return Map.of(
                "inviteCode", inviteCode != null ? inviteCode : "",
                "inviteLink", inviteLink != null ? inviteLink : "",
                "inviteeCount", inviteeCount,
                "registerReward", registerReward,
                "totalReward", totalReward
        );
    }

    @Override
    public List<Map<String, Object>> getInvitees(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<InviteRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InviteRecord::getInviterId, userId)
                .orderByDesc(InviteRecord::getCreatedAt);

        List<InviteRecord> records = baseMapper.selectList(queryWrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (InviteRecord record : records) {
            User invitee = userMapper.selectById(record.getInviteeId());
            if (invitee != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", invitee.getId());
                item.put("username", invitee.getUsername());
                item.put("email", invitee.getEmail());
                item.put("avatar", invitee.getAvatar());
                item.put("registeredAt", record.getCreatedAt());
                item.put("rewardsCredited", record.getRewardsCredited());
                item.put("rewardAmount", record.getRewardAmount());
                result.add(item);
            }
        }

        // 分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, result.size());
        if (start >= result.size()) {
            return Collections.emptyList();
        }
        return result.subList(start, end);
    }

    @Override
    public List<InviteReward> getActiveRewards(String type) {
        if (type != null && !type.isBlank()) {
            return inviteRewardMapper.selectActiveRewardsByType(type);
        }
        return inviteRewardMapper.selectActiveRewards();
    }

    @Override
    public String getUserInviteCode(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getInviteCode() != null) {
            return user.getInviteCode();
        }
        return generateInviteCode(userId);
    }

    @Override
    @Transactional
    public String generateInviteCode(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getInviteCode() != null) {
            return user.getInviteCode();
        }

        // 生成邀请码
        String code;
        do {
            code = INVITE_CODE_PREFIX + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getInviteCode, code)
        ) != null);

        // 保存到用户表
        if (user != null) {
            user.setInviteCode(code);
            userMapper.updateById(user);
        }

        return code;
    }

    // ==================== 私有方法 ====================

    /**
     * 发放奖励
     */
    private void creditReward(Long inviterId, Long inviteeId, InviteReward reward, 
                             String rewardTo, long amount, String description) {
        Long userId = "inviter".equals(rewardTo) ? inviterId : inviteeId;

        // 获取用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: {}", userId);
            return;
        }

        long balanceBefore = user.getBalance() != null ? user.getBalance() : 0L;
        long balanceAfter = balanceBefore + amount;

        // 更新用户余额
        user.setBalance(balanceAfter);
        userMapper.updateById(user);

        // 获取邀请记录
        InviteRecord inviteRecord = baseMapper.selectByInviteeId(inviteeId);

        // 记录奖励
        InviteRewardRecord rewardRecord = new InviteRewardRecord();
        rewardRecord.setInviteRecordId(inviteRecord != null ? inviteRecord.getId() : null);
        rewardRecord.setInviterId(inviterId);
        rewardRecord.setInviteeId(inviteeId);
        rewardRecord.setRewardType(reward.getType());
        rewardRecord.setRewardTo(rewardTo);
        rewardRecord.setAmount(amount);
        rewardRecord.setBalanceBefore(balanceBefore);
        rewardRecord.setBalanceAfter(balanceAfter);
        rewardRecord.setDescription(description);
        rewardRecord.setCreatedAt(LocalDateTime.now());
        rewardRecordMapper.insert(rewardRecord);

        log.info("发放邀请奖励: userId={}, amount={}, type={}", userId, amount, reward.getType());
    }

    private String formatMoney(Long amount) {
        if (amount == null) return "0.00";
        return new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
