package com.flashsell.domain.payment;

import java.util.List;
import java.util.Optional;

/**
 * 支付网关接口
 * 定义支付相关的操作
 */
public interface PaymentGateway {

    /**
     * 创建订阅订单
     *
     * @param userId 用户ID
     * @param planId 套餐ID
     * @return 订单信息
     */
    SubscriptionOrder createOrder(Long userId, Long planId);

    /**
     * 根据订单号获取订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Optional<SubscriptionOrder> findByOrderNo(String orderNo);

    /**
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<SubscriptionOrder> findByUserId(Long userId);

    /**
     * 保存订单
     *
     * @param order 订单信息
     * @return 保存后的订单
     */
    SubscriptionOrder save(SubscriptionOrder order);

    /**
     * 获取所有可用套餐
     *
     * @return 套餐列表
     */
    List<SubscriptionPlan> findAllPlans();

    /**
     * 根据ID获取套餐
     *
     * @param planId 套餐ID
     * @return 套餐信息
     */
    Optional<SubscriptionPlan> findPlanById(Long planId);
}
