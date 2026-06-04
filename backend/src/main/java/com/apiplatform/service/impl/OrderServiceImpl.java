package com.apiplatform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.apiplatform.common.*;
import com.apiplatform.entity.Order;
import com.apiplatform.entity.Package;
import com.apiplatform.entity.UserPackage;
import com.apiplatform.mapper.OrderMapper;
import com.apiplatform.mapper.PackageMapper;
import com.apiplatform.mapper.UserPackageMapper;
import com.apiplatform.service.OrderService;
import com.apiplatform.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final PackageMapper packageMapper;
    private final UserPackageMapper userPackageMapper;
    private final UserService userService;

    @Override
    @Transactional
    public Order createRechargeOrder(Long userId, BigDecimal amount, String paymentMethod, String clientIp) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BizException.invalidParameter("充值金额");
        }

        String orderNo = "RC" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);
        
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .type("recharge")
                .amount(amount.multiply(BigDecimal.valueOf(100))) // 转换为分
                .paidAmount(BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .status("pending")
                .clientIp(clientIp)
                .expireTime(LocalDateTime.now().plusHours(24))
                .build();

        orderMapper.insert(order);
        log.info("创建充值订单: orderNo={}, userId={}, amount={}", orderNo, userId, amount);
        
        return order;
    }

    @Override
    @Transactional
    public Order createPackageOrder(Long userId, Long packageId, String paymentMethod, String clientIp) {
        Package pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw BizException.packageNotFound();
        }

        String orderNo = "PK" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);
        
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .type("package")
                .relatedId(packageId)
                .amount(pkg.getPrice().multiply(BigDecimal.valueOf(100)))
                .paidAmount(BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .status("pending")
                .clientIp(clientIp)
                .expireTime(LocalDateTime.now().plusHours(24))
                .build();

        orderMapper.insert(order);
        log.info("创建套餐订单: orderNo={}, userId={}, packageId={}", orderNo, userId, packageId);
        
        return order;
    }

    @Override
    @Transactional
    public void payOrder(String orderNo, String transactionId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw BizException.orderNotFound();
        }

        if (!"pending".equals(order.getStatus())) {
            throw BizException.badRequest("订单状态不正确");
        }

        if (order.isExpired()) {
            order.setStatus("expired");
            updateById(order);
            throw BizException.orderExpired();
        }

        order.setStatus("paid");
        order.setTransactionId(transactionId);
        order.setPaidTime(LocalDateTime.now());
        order.setPaidAmount(order.getAmount());
        updateById(order);

        // 根据订单类型处理
        if ("recharge".equals(order.getType())) {
            // 充值：增加用户余额
            userService.addBalance(order.getUserId(), order.getAmount().longValue());
        } else if ("package".equals(order.getType())) {
            // 套餐：创建用户套餐记录
            createUserPackage(order);
        }

        log.info("订单支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw BizException.orderNotFound();
        }

        if (!order.getUserId().equals(userId)) {
            throw BizException.forbidden("无权操作此订单");
        }

        if (!"pending".equals(order.getStatus())) {
            throw BizException.badRequest("只有待支付的订单才能取消");
        }

        order.setStatus("cancelled");
        updateById(order);
        log.info("取消订单: orderNo={}", orderNo);
    }

    @Override
    @Transactional
    public void refundOrder(String orderNo, String reason) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw BizException.orderNotFound();
        }

        if (!"paid".equals(order.getStatus())) {
            throw BizException.badRequest("只有已支付的订单才能退款");
        }

        order.setStatus("refunded");
        order.setRefundAmount(order.getPaidAmount());
        order.setRefundReason(reason);
        order.setRefundTime(LocalDateTime.now());
        updateById(order);

        // 退还余额
        userService.addBalance(order.getUserId(), order.getRefundAmount() != null ? order.getRefundAmount().longValue() : 0L);

        log.info("订单退款: orderNo={}, amount={}", orderNo, order.getRefundAmount());
    }

    @Override
    @Transactional
    public void expireOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "pending")
                .lt(Order::getExpireTime, LocalDateTime.now());
        
        List<Order> expiredOrders = list(wrapper);
        expiredOrders.forEach(order -> {
            order.setStatus("expired");
            updateById(order);
            log.info("订单过期: orderNo={}", order.getOrderNo());
        });
    }

    @Override
    public Order getOrderDetail(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult<Order> getUserOrders(Long userId, int page, int size, String type) {
        Page<Order> pageParam = new Page<>(page, size);
        IPage<Order> pageResult = orderMapper.selectByUserId(pageParam, userId, type);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size);
    }

    @Override
    public List<Order> getPendingOrders(Long userId) {
        return orderMapper.selectPendingByUserId(userId);
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String orderNo, String status, String transactionId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.warn("支付回调订单不存在: orderNo={}", orderNo);
            return;
        }

        if ("success".equals(status)) {
            payOrder(orderNo, transactionId);
        } else if ("failed".equals(status)) {
            log.warn("支付失败: orderNo={}", orderNo);
            // 可以记录失败原因等
        }
    }

    @Override
    public Map<String, Object> getOrderStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计订单数量
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (startTime != null) {
            wrapper.ge(Order::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(Order::getCreatedAt, endTime);
        }
        
        long totalOrders = count(wrapper);
        stats.put("totalOrders", totalOrders);

        // 统计已支付订单
        wrapper.eq(Order::getStatus, "paid");
        long paidOrders = count(wrapper);
        stats.put("paidOrders", paidOrders);

        // 统计总金额
        wrapper.eq(Order::getStatus, "paid");
        BigDecimal totalAmount = orderMapper.sumPaidAmount(userId, startTime, endTime);
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        stats.put("totalAmount", totalAmount.longValue());

        // 日统计
        List<Map<String, Object>> dailyStats = orderMapper.selectDailyStats(startTime);
        stats.put("dailyStats", dailyStats);

        return stats;
    }

    @Override
    public boolean verifyOrderStatus(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        return order != null && "paid".equals(order.getStatus());
    }

    @Override
    public Long getOrderExpireSeconds(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getExpireTime() == null) {
            return 0L;
        }
        
        long seconds = java.time.Duration.between(LocalDateTime.now(), order.getExpireTime()).getSeconds();
        return Math.max(0, seconds);
    }

    /**
     * 创建用户套餐记录
     */
    private void createUserPackage(Order order) {
        Package pkg = packageMapper.selectById(order.getRelatedId());
        if (pkg == null) {
            log.error("套餐不存在: packageId={}", order.getRelatedId());
            return;
        }

        UserPackage userPackage = UserPackage.builder()
                .userId(order.getUserId())
                .packageId(order.getRelatedId())
                .remainingCalls(pkg.getTotalCalls())
                .quotaBalance(pkg.getQuota())
                .startTime(LocalDateTime.now())
                .expireTime(pkg.getValidDays() > 0 
                        ? LocalDateTime.now().plusDays(pkg.getValidDays()) 
                        : null)
                .status("active")
                .build();

        userPackageMapper.insert(userPackage);
        log.info("创建用户套餐: userId={}, packageId={}, userPackageId={}", 
                order.getUserId(), order.getRelatedId(), userPackage.getId());
    }
}
