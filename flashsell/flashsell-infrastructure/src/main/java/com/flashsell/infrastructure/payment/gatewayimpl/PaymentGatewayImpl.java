package com.flashsell.infrastructure.payment.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsell.domain.payment.PaymentGateway;
import com.flashsell.domain.payment.SubscriptionOrder;
import com.flashsell.domain.payment.SubscriptionPlan;
import com.flashsell.infrastructure.payment.convertor.PaymentConvertor;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionOrderDO;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionPlanDO;
import com.flashsell.infrastructure.payment.mapper.SubscriptionOrderMapper;
import com.flashsell.infrastructure.payment.mapper.SubscriptionPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付网关实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentGatewayImpl implements PaymentGateway {

    private final SubscriptionOrderMapper orderMapper;
    private final SubscriptionPlanMapper planMapper;
    private final PaymentConvertor paymentConvertor;

    @Override
    public SubscriptionOrder createOrder(Long userId, Long planId) {
        log.info("创建订阅订单: userId={}, planId={}", userId, planId);

        // 获取套餐信息
        SubscriptionPlanDO planDO = planMapper.selectById(planId);
        if (planDO == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        // 生成订单号
        String orderNo = "SUB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 计算订阅周期
        String period = calculatePeriod(planDO.getDurationDays());

        // 创建订单
        SubscriptionOrderDO orderDO = SubscriptionOrderDO.builder()
                .userId(userId)
                .planId(planId)
                .orderNo(orderNo)
                .period(period)
                .amount(planDO.getPrice())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        orderMapper.insert(orderDO);

        log.info("订单创建成功: orderNo={}", orderNo);
        return paymentConvertor.toOrderEntity(orderDO);
    }

    @Override
    public SubscriptionOrder save(SubscriptionOrder order) {
        log.info("保存订单: orderNo={}", order.getOrderNo());

        SubscriptionOrderDO orderDO = paymentConvertor.toOrderDO(order);
        orderDO.setCreatedAt(LocalDateTime.now());

        if (order.getId() == null) {
            orderMapper.insert(orderDO);
        } else {
            orderMapper.updateById(orderDO);
        }

        return paymentConvertor.toOrderEntity(orderDO);
    }

    @Override
    public java.util.Optional<SubscriptionOrder> findByOrderNo(String orderNo) {
        SubscriptionOrderDO orderDO = orderMapper.selectByOrderNo(orderNo);
        return java.util.Optional.ofNullable(paymentConvertor.toOrderEntity(orderDO));
    }

    @Override
    public List<SubscriptionOrder> findByUserId(Long userId) {
        List<SubscriptionOrderDO> orderDOs = orderMapper.selectByUserId(userId);
        return orderDOs.stream()
                .map(paymentConvertor::toOrderEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<SubscriptionPlan> findAllPlans() {
        List<SubscriptionPlanDO> planDOs = planMapper.selectList(null);
        return paymentConvertor.toPlanEntityList(planDOs);
    }

    @Override
    public java.util.Optional<SubscriptionPlan> findPlanById(Long planId) {
        SubscriptionPlanDO planDO = planMapper.selectById(planId);
        return java.util.Optional.ofNullable(paymentConvertor.toPlanEntity(planDO));
    }

    /**
     * 根据天数计算订阅周期
     */
    private String calculatePeriod(Integer durationDays) {
        if (durationDays <= 30) {
            return "MONTHLY";
        } else if (durationDays <= 90) {
            return "QUARTERLY";
        } else if (durationDays <= 180) {
            return "HALF_YEARLY";
        } else {
            return "YEARLY";
        }
    }
}
