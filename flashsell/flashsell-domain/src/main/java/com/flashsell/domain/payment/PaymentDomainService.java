package com.flashsell.domain.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付领域服务
 * 处理支付相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final PaymentGateway paymentGateway;

    /**
     * 创建订阅订单
     *
     * @param userId 用户ID
     * @param planId 套餐ID
     * @return 创建的订单
     */
    public SubscriptionOrder createSubscriptionOrder(Long userId, Long planId) {
        // 获取套餐信息
        SubscriptionPlan plan = paymentGateway.findPlanById(planId)
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在"));

        // 计算订阅时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime subscribeTo = now.plusDays(plan.getDurationDays());

        // 创建订单
        return paymentGateway.createOrder(userId, planId);
    }

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    public String generateOrderNo() {
        return "SUB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 验证订单金额
     *
     * @param order   订单
     * @param paidAmount 实际支付金额
     * @return 是否匹配
     */
    public boolean validateAmount(SubscriptionOrder order, BigDecimal paidAmount) {
        return order.getAmount().compareTo(paidAmount) == 0;
    }

    /**
     * 计算订阅结束时间
     *
     * @param plan 套餐
     * @param from  开始时间
     * @return 结束时间
     */
    public LocalDateTime calculateExpireTime(SubscriptionPlan plan, LocalDateTime from) {
        return from.plusDays(plan.getDurationDays());
    }
}
