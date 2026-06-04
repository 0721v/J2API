package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author API Platform Team
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建充值订单
     */
    Order createRechargeOrder(Long userId, BigDecimal amount, String paymentMethod, String clientIp);

    /**
     * 创建套餐订单
     */
    Order createPackageOrder(Long userId, Long packageId, String paymentMethod, String clientIp);

    /**
     * 支付订单
     */
    void payOrder(String orderNo, String transactionId);

    /**
     * 取消订单
     */
    void cancelOrder(String orderNo, Long userId);

    /**
     * 退款订单
     */
    void refundOrder(String orderNo, String reason);

    /**
     * 标记过期订单
     */
    void expireOrders();

    /**
     * 获取订单详情
     */
    Order getOrderDetail(String orderNo);

    /**
     * 获取用户订单列表
     */
    PageResult<Order> getUserOrders(Long userId, int page, int size, String type);

    /**
     * 获取待支付订单
     */
    List<Order> getPendingOrders(Long userId);

    /**
     * 处理支付回调
     */
    void handlePaymentCallback(String orderNo, String status, String transactionId);

    /**
     * 获取订单统计数据
     */
    Map<String, Object> getOrderStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 验证订单状态
     */
    boolean verifyOrderStatus(String orderNo);

    /**
     * 获取订单剩余有效期（秒）
     */
    Long getOrderExpireSeconds(String orderNo);
}
