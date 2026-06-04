package com.apiplatform.controller;

import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.*;
import com.apiplatform.service.*;
import com.apiplatform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 代理商控制器
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final CommissionSettlementService commissionSettlementService;
    private final AgentNotificationService notificationService;
    private final JwtUtil jwtUtil;

    @Value("${system.base-url:http://localhost:3000}")
    private String baseUrl;

    // ==================== 用户端接口 ====================

    /**
     * 获取代理商等级列表
     */
    @GetMapping("/levels")
    public Result<List<AgentLevel>> getLevels() {
        return Result.success(agentService.getAgentLevels());
    }

    /**
     * 检查用户是否是代理商
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkAgent(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        Map<String, Object> result = Map.of(
            "isAgent", agent != null && "active".equals(agent.getStatus()),
            "hasApplication", agent != null,
            "status", agent != null ? agent.getStatus() : "none"
        );
        return Result.success(result);
    }

    /**
     * 获取代理商信息
     */
    @GetMapping("/info")
    public Result<Agent> getAgentInfo(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(agentService.getAgentDetail(userId));
    }

    /**
     * 获取代理商统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(agentService.getAgentStats(userId));
    }

    /**
     * 申请成为代理商
     */
    @PostMapping("/apply")
    public Result<Agent> applyAgent(HttpServletRequest request, @RequestBody Map<String, Object> applyData) {
        Long userId = getUserId(request);
        return Result.success(agentService.apply(userId, applyData));
    }

    /**
     * 获取佣金记录
     */
    @GetMapping("/commissions")
    public Result<PageResult<AgentCommission>> getCommissions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type) {
        Long userId = getUserId(request);
        return Result.success(agentService.listCommissions(userId, page, pageSize, type));
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw")
    public Result<AgentWithdrawal> applyWithdrawal(
            HttpServletRequest request,
            @RequestBody Map<String, Object> withdrawalData) {
        Long userId = getUserId(request);
        Long amount = Long.valueOf(withdrawalData.get("amount").toString());
        String method = (String) withdrawalData.get("method");
        @SuppressWarnings("unchecked")
        Map<String, String> accountInfo = (Map<String, String>) withdrawalData.get("accountInfo");
        
        AgentWithdrawal withdrawal = agentService.applyWithdrawal(userId, amount, method, accountInfo);
        
        // 发送通知
        notificationService.notifyWithdrawalApplied(userId, amount, withdrawal.getWithdrawalNo());
        
        return Result.success(withdrawal);
    }

    /**
     * 获取提现记录
     */
    @GetMapping("/withdrawals")
    public Result<PageResult<AgentWithdrawal>> getWithdrawals(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = getUserId(request);
        return Result.success(agentService.listWithdrawals(userId, page, pageSize, status));
    }

    /**
     * 获取佣金比例
     */
    @GetMapping("/commission-rate")
    public Result<BigDecimal> getCommissionRate(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(agentService.getCommissionRate(userId));
    }

    /**
     * 获取提现手续费率
     */
    @GetMapping("/withdrawal-fee-rate")
    public Result<BigDecimal> getWithdrawalFeeRate() {
        return Result.success(agentService.getWithdrawalFeeRate());
    }

    /**
     * 根据邀请码查询代理商（公开接口）
     */
    @GetMapping("/by-code/{code}")
    public Result<Agent> getAgentByCode(@PathVariable String code) {
        Agent agent = agentService.getAgentByCode(code);
        if (agent != null) {
            return Result.success(Map.of(
                "agentCode", agent.getAgentCode(),
                "levelName", agent.getLevel() != null ? agent.getLevel().getName() : "代理商"
            ));
        }
        return Result.success(null);
    }

    // ==================== 邀请分享 ====================

    /**
     * 获取邀请链接
     */
    @GetMapping("/invite-link")
    public Result<Map<String, String>> getInviteLink(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null || !agent.isActive()) {
            return Result.fail("您还不是代理商");
        }
        
        String link = baseUrl + "/register?invite=" + agent.getAgentCode();
        String qrCodeUrl = baseUrl + "/api/agent/qrcode/" + agent.getAgentCode();
        
        return Result.success(Map.of(
            "link", link,
            "qrCodeUrl", qrCodeUrl,
            "code", agent.getAgentCode()
        ));
    }

    /**
     * 获取邀请统计
     */
    @GetMapping("/invite-stats")
    public Result<Map<String, Object>> getInviteStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        return Result.success(Map.of(
            "totalInvites", agent.getTotalUsers(),
            "directInvites", agent.getTotalUsers(), // TODO: 区分直属和间接
            "inviteCode", agent.getAgentCode()
        ));
    }

    /**
     * 获取邀请排行榜
     */
    @GetMapping("/invite-leaderboard")
    public Result<List<Map<String, Object>>> getInviteLeaderboard(HttpServletRequest request) {
        // TODO: 实现排行榜逻辑
        return Result.success(List.of());
    }

    // ==================== 下级管理 ====================

    /**
     * 获取直属用户列表
     */
    @GetMapping("/sub-users")
    public Result<PageResult<Map<String, Object>>> getSubUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        List<Map<String, Object>> users = commissionSettlementService.getReferralUsers(agent.getId(), page, pageSize);
        return Result.success(new PageResult<>(users, users.size()));
    }

    /**
     * 获取下级代理商列表
     */
    @GetMapping("/sub-agents")
    public Result<List<Agent>> getSubAgents(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        return Result.success(commissionSettlementService.getSubAgents(agent.getId()));
    }

    // ==================== 统计报表 ====================

    /**
     * 获取佣金统计
     */
    @GetMapping("/commission-stats")
    public Result<Map<String, Object>> getCommissionStats(
            HttpServletRequest request,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        LocalDateTime start = startDate != null ? 
            LocalDateTime.parse(startDate + "T00:00:00") : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? 
            LocalDateTime.parse(endDate + "T23:59:59") : LocalDateTime.now();
        
        return Result.success(commissionSettlementService.getCommissionStats(agent.getId(), start, end));
    }

    /**
     * 获取月度佣金汇总
     */
    @GetMapping("/commission-monthly")
    public Result<Map<String, Object>> getMonthlyCommission(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        return Result.success(commissionSettlementService.getMonthlyCommissionSummary(agent.getId(), year, month));
    }

    /**
     * 获取佣金趋势
     */
    @GetMapping("/commission-trend")
    public Result<List<Map<String, Object>>> getCommissionTrend(
            HttpServletRequest request,
            @RequestParam(defaultValue = "30") int days) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        return Result.success(commissionSettlementService.getCommissionTrend(agent.getId(), days));
    }

    // ==================== 消息通知 ====================

    /**
     * 获取通知列表
     */
    @GetMapping("/notifications")
    public Result<PageResult<AgentNotification>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent == null) {
            return Result.fail("您还不是代理商");
        }
        
        return Result.success(notificationService.getNotifications(agent.getId(), page, pageSize));
    }

    /**
     * 获取未读通知数
     */
    @GetMapping("/notifications/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        int count = 0;
        if (agent != null) {
            count = notificationService.getUnreadCount(agent.getId());
        }
        
        return Result.success(Map.of("count", count));
    }

    /**
     * 标记通知已读
     */
    @PutMapping("/notifications/{notificationId}/read")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long notificationId) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent != null) {
            notificationService.markAsRead(agent.getId(), notificationId);
        }
        
        return Result.success();
    }

    /**
     * 标记全部已读
     */
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        Agent agent = agentService.getAgentDetail(userId);
        
        if (agent != null) {
            notificationService.markAllAsRead(agent.getId());
        }
        
        return Result.success();
    }

    // ==================== 管理端接口 ====================

    /**
     * 获取代理商列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Agent>> listAgents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long levelId) {
        return Result.success(agentService.listAgents(page, pageSize, status, levelId));
    }

    /**
     * 审核代理商申请
     */
    @PostMapping("/admin/review/{agentId}")
    public Result<Void> reviewAgent(
            HttpServletRequest request,
            @PathVariable Long agentId,
            @RequestBody Map<String, String> reviewData) {
        Long reviewerId = getUserId(request);
        String status = reviewData.get("status");
        String rejectReason = reviewData.get("rejectReason");
        agentService.review(agentId, status, rejectReason, reviewerId);
        
        // 发送通知
        boolean approved = "active".equals(status);
        notificationService.notifyApplicationResult(agentId, approved, rejectReason);
        
        return Result.success();
    }

    /**
     * 获取所有提现申请列表
     */
    @GetMapping("/admin/withdrawals")
    public Result<PageResult<AgentWithdrawal>> listAllWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long agentId) {
        // TODO: 实现管理员查询所有提现记录
        return Result.success(new PageResult<>(List.of(), 0));
    }

    /**
     * 处理提现申请
     */
    @PostMapping("/admin/withdraw/{withdrawalId}")
    public Result<Void> processWithdrawal(
            HttpServletRequest request,
            @PathVariable Long withdrawalId,
            @RequestBody Map<String, String> processData) {
        Long processorId = getUserId(request);
        String status = processData.get("status");
        String rejectReason = processData.get("rejectReason");
        agentService.processWithdrawal(withdrawalId, status, rejectReason, processorId);
        
        // 发送通知
        AgentWithdrawal withdrawal = agentService.getById(withdrawalId);
        if (withdrawal != null) {
            if ("completed".equals(status)) {
                notificationService.notifyWithdrawalCompleted(
                    withdrawal.getAgentId(), 
                    withdrawal.getActualAmount(), 
                    withdrawal.getWithdrawalNo()
                );
            } else if ("rejected".equals(status)) {
                notificationService.notifyWithdrawalRejected(
                    withdrawal.getAgentId(), 
                    withdrawal.getAmount(), 
                    rejectReason
                );
            }
        }
        
        return Result.success();
    }

    /**
     * 获取代理商详情（管理员）
     */
    @GetMapping("/admin/{agentId}")
    public Result<Agent> getAgentDetailAdmin(@PathVariable Long agentId) {
        Agent agent = agentService.getById(agentId);
        if (agent != null && agent.getLevelId() != null) {
            List<AgentLevel> levels = agentService.getAgentLevels();
            agent.setLevel(levels.stream()
                    .filter(l -> l.getId().equals(agent.getLevelId()))
                    .findFirst()
                    .orElse(null));
        }
        return Result.success(agent);
    }

    /**
     * 获取代理商业绩统计（管理员）
     */
    @GetMapping("/admin/{agentId}/performance")
    public Result<Map<String, Object>> getAgentPerformance(@PathVariable Long agentId) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> stats = commissionSettlementService.getCommissionStats(
            agentId, 
            now.minusMonths(1), 
            now
        );
        return Result.success(stats);
    }

    // ==================== 辅助方法 ====================

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
