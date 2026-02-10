package com.flashsell.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅订单实体
 * 记录用户的订阅订单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOrder {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 交易号（第三方支付平台返回）
     */
    private String tradeNo;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 订单创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 订单更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 订阅开始时间
     */
    private LocalDateTime subscribeFrom;

    /**
     * 订阅结束时间
     */
    private LocalDateTime subscribeTo;

    /**
     * 是否已支付
     */
    public boolean isPaid() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * 是否待支付
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return subscribeTo != null && LocalDateTime.now().isAfter(subscribeTo);
    }

    /**
     * 标记为已支付
     */
    public void markAsPaid(String tradeNo) {
        this.status = PaymentStatus.SUCCESS;
        this.tradeNo = tradeNo;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 标记为已取消
     */
    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }
}
