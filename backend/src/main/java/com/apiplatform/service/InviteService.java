package com.apiplatform.service;

import com.apiplatform.entity.InviteRecord;
import com.apiplatform.entity.InviteReward;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 邀请服务接口
 */
public interface InviteService extends IService<InviteRecord> {

    /**
     * 验证邀请码是否有效
     */
    InviteRecord validateInviteCode(String inviteCode);

    /**
     * 创建邀请记录
     */
    InviteRecord createInviteRecord(Long inviterId, Long inviteeId, String inviteCode, String source);

    /**
     * 处理用户注册邀请奖励
     */
    void creditRegisterReward(Long inviterId, Long inviteeId);

    /**
     * 处理充值邀请奖励
     */
    void creditRechargeReward(Long inviterId, Long inviteeId, Long orderId, Long rechargeAmount);

    /**
     * 获取邀请统计信息
     */
    Map<String, Object> getInviteStats(Long userId);

    /**
     * 获取邀请人列表
     */
    List<Map<String, Object>> getInvitees(Long userId, int page, int pageSize);

    /**
     * 获取有效的邀请奖励配置
     */
    List<InviteReward> getActiveRewards(String type);

    /**
     * 获取用户的邀请码
     */
    String getUserInviteCode(Long userId);

    /**
     * 生成用户邀请码
     */
    String generateInviteCode(Long userId);
}
