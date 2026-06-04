package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.AgentNotification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 代理商通知服务接口
 */
public interface AgentNotificationService extends IService<AgentNotification> {

    /**
     * 发送通知
     */
    void sendNotification(Long agentId, String type, String title, String content, Long amount, Long orderId);

    /**
     * 获取通知列表
     */
    PageResult<AgentNotification> getNotifications(Long agentId, int page, int pageSize);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long agentId);

    /**
     * 标记已读
     */
    void markAsRead(Long agentId, Long notificationId);

    /**
     * 标记全部已读
     */
    void markAllAsRead(Long agentId);

    /**
     * 删除通知
     */
    void deleteNotification(Long agentId, Long notificationId);

    // ==================== 快捷通知方法 ====================

    /**
     * 佣金到账通知
     */
    void notifyCommissionCredited(Long agentId, Long amount, String type, Long orderId);

    /**
     * 提现申请成功通知
     */
    void notifyWithdrawalApplied(Long agentId, Long amount, String withdrawalNo);

    /**
     * 提现到账通知
     */
    void notifyWithdrawalCompleted(Long agentId, Long amount, String withdrawalNo);

    /**
     * 提现被拒绝通知
     */
    void notifyWithdrawalRejected(Long agentId, Long amount, String reason);

    /**
     * 申请审核结果通知
     */
    void notifyApplicationResult(Long agentId, boolean approved, String reason);

    /**
     * 等级升级通知
     */
    void notifyLevelUpgrade(Long agentId, String oldLevel, String newLevel);

    /**
     * 新下级用户通知
     */
    void notifyNewSubUser(Long agentId, String username);
}
