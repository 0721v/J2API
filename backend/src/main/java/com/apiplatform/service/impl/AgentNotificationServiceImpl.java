package com.apiplatform.service.impl;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.AgentNotification;
import com.apiplatform.mapper.AgentNotificationMapper;
import com.apiplatform.service.AgentNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理商通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentNotificationServiceImpl 
    extends ServiceImpl<AgentNotificationMapper, AgentNotification> 
    implements AgentNotificationService {

    private static final String PRIORITY_NORMAL = "normal";
    private static final String PRIORITY_HIGH = "high";

    @Override
    @Async
    public void sendNotification(Long agentId, String type, String title, String content, 
                                 Long amount, Long orderId) {
        AgentNotification notification = new AgentNotification();
        notification.setAgentId(agentId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setAmount(amount);
        notification.setOrderId(orderId);
        notification.setStatus("unread");
        notification.setPriority(PRIORITY_NORMAL);
        notification.setPushed(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        baseMapper.insert(notification);
        log.info("发送代理商通知: agentId={}, type={}, title={}", agentId, type, title);
    }

    @Override
    public PageResult<AgentNotification> getNotifications(Long agentId, int page, int pageSize) {
        Page<AgentNotification> pageParam = new Page<>(page, pageSize);
        IPage<AgentNotification> result = baseMapper.selectByAgentId(pageParam, agentId);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public int getUnreadCount(Long agentId) {
        return baseMapper.countUnread(agentId);
    }

    @Override
    public void markAsRead(Long agentId, Long notificationId) {
        AgentNotification notification = baseMapper.selectById(notificationId);
        if (notification != null && notification.getAgentId().equals(agentId)) {
            notification.setStatus("read");
            notification.setReadAt(LocalDateTime.now());
            baseMapper.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long agentId) {
        baseMapper.markAllRead(agentId);
    }

    @Override
    public void deleteNotification(Long agentId, Long notificationId) {
        LambdaQueryWrapper<AgentNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentNotification::getAgentId, agentId)
                .eq(AgentNotification::getId, notificationId);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public void notifyCommissionCredited(Long agentId, Long amount, String type, Long orderId) {
        String typeName = getTypeName(type);
        String title = "佣金到账";
        String content = String.format("您获得了%s %s元，请查收！", 
                typeName, formatMoney(amount));
        
        AgentNotification notification = new AgentNotification();
        notification.setAgentId(agentId);
        notification.setType("commission");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setAmount(amount);
        notification.setOrderId(orderId);
        notification.setStatus("unread");
        notification.setPriority(PRIORITY_HIGH);
        notification.setPushed(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        baseMapper.insert(notification);
    }

    @Override
    public void notifyWithdrawalApplied(Long agentId, Long amount, String withdrawalNo) {
        String title = "提现申请已提交";
        String content = String.format("您的提现申请已提交，预计1-3个工作日到账。\n" +
                "提现金额：%s元\n提现单号：%s", formatMoney(amount), withdrawalNo);
        
        sendNotification(agentId, "withdrawal", title, content, amount, null);
    }

    @Override
    public void notifyWithdrawalCompleted(Long agentId, Long amount, String withdrawalNo) {
        String title = "提现到账";
        String content = String.format("您的提现已到账！\n" +
                "到账金额：%s元\n提现单号：%s", formatMoney(amount), withdrawalNo);
        
        sendNotification(agentId, "withdrawal", title, content, amount, null);
    }

    @Override
    public void notifyWithdrawalRejected(Long agentId, Long amount, String reason) {
        String title = "提现申请被拒绝";
        String content = String.format("您的提现申请被拒绝。\n" +
                "提现金额：%s元\n拒绝原因：%s", formatMoney(amount), reason);
        
        sendNotification(agentId, "withdrawal", title, content, amount, null);
    }

    @Override
    public void notifyApplicationResult(Long agentId, boolean approved, String reason) {
        String title = approved ? "代理商申请已通过" : "代理商申请被拒绝";
        String content;
        
        if (approved) {
            content = "恭喜！您的代理商申请已审核通过，您可以开始享受代理商权益了。";
        } else {
            content = String.format("很抱歉，您的代理商申请被拒绝。\n拒绝原因：%s", reason);
        }
        
        sendNotification(agentId, "application", title, content, null, null);
    }

    @Override
    public void notifyLevelUpgrade(Long agentId, String oldLevel, String newLevel) {
        String title = "恭喜！等级提升";
        String content = String.format("您已从%s升级为%s，享受更多权益和更高佣金比例！", 
                oldLevel, newLevel);
        
        AgentNotification notification = new AgentNotification();
        notification.setAgentId(agentId);
        notification.setType("level_upgrade");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus("unread");
        notification.setPriority(PRIORITY_HIGH);
        notification.setPushed(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        baseMapper.insert(notification);
    }

    @Override
    public void notifyNewSubUser(Long agentId, String username) {
        String title = "新下级用户";
        String content = String.format("恭喜！您有新的下级用户注册：%s", username);
        
        sendNotification(agentId, "sub_user", title, content, null, null);
    }

    // ==================== 辅助方法 ====================

    private String getTypeName(String type) {
        return switch (type) {
            case "recharge" -> "充值佣金";
            case "package" -> "套餐佣金";
            case "upgrade" -> "升级奖励";
            case "bonus" -> "推荐奖励";
            default -> "佣金";
        };
    }

    private String formatMoney(Long amount) {
        if (amount == null) return "0.00";
        return new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
