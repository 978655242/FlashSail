package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅订单响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOrderRes {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 套餐ID
     */
    private Long planId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 支付宝交易号
     */
    private String alipayTradeNo;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 支付URL（用于跳转到支付宝）
     */
    private String paymentUrl;
}
