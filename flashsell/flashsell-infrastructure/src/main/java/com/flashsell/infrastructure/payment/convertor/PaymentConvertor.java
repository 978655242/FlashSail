package com.flashsell.infrastructure.payment.convertor;

import com.flashsell.domain.payment.PaymentStatus;
import com.flashsell.domain.payment.SubscriptionOrder;
import com.flashsell.domain.payment.SubscriptionPlan;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionOrderDO;
import com.flashsell.infrastructure.payment.dataobject.SubscriptionPlanDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付模块转换器
 */
@Component
public class PaymentConvertor {

    /**
     * 订单DO转换为实体
     */
    public SubscriptionOrder toOrderEntity(SubscriptionOrderDO orderDO) {
        if (orderDO == null) {
            return null;
        }
        return SubscriptionOrder.builder()
                .id(orderDO.getId())
                .userId(orderDO.getUserId())
                .planId(orderDO.getPlanId())
                .orderNo(orderDO.getOrderNo())
                .tradeNo(orderDO.getAlipayTradeNo())
                .amount(orderDO.getAmount())
                .status(PaymentStatus.valueOf(orderDO.getStatus()))
                .paymentMethod("alipay")
                .paidAt(orderDO.getPaidAt())
                .createdAt(orderDO.getCreatedAt())
                .build();
    }

    /**
     * 订单实体转换为DO
     */
    public SubscriptionOrderDO toOrderDO(SubscriptionOrder order) {
        if (order == null) {
            return null;
        }
        return SubscriptionOrderDO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .planId(order.getPlanId())
                .orderNo(order.getOrderNo())
                .alipayTradeNo(order.getTradeNo())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * 套餐DO转换为实体
     */
    public SubscriptionPlan toPlanEntity(SubscriptionPlanDO planDO) {
        if (planDO == null) {
            return null;
        }
        return SubscriptionPlan.builder()
                .id(planDO.getId())
                .name(planDO.getName())
                .description(planDO.getDescription())
                .price(planDO.getPrice())
                .durationDays(planDO.getDurationDays())
                .searchLimit(planDO.getSearchLimit())
                .exportLimit(planDO.getExportLimit())
                .boardLimit(planDO.getBoardLimit())
                .aiAnalysisEnabled(planDO.getAiAnalysisEnabled())
                .apiAccessEnabled(planDO.getApiAccessEnabled())
                .level(com.flashsell.domain.user.entity.SubscriptionLevel.valueOf(planDO.getLevel()))
                .build();
    }

    /**
     * 套餐DO列表转换为实体列表
     */
    public List<SubscriptionPlan> toPlanEntityList(List<SubscriptionPlanDO> planDOs) {
        if (planDOs == null) {
            return List.of();
        }
        return planDOs.stream()
                .map(this::toPlanEntity)
                .collect(Collectors.toList());
    }
}
