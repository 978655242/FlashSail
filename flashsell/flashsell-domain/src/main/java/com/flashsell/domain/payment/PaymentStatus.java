package com.flashsell.domain.payment;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {

    /**
     * 待支付
     */
    PENDING,

    /**
     * 支付成功
     */
    SUCCESS,

    /**
     * 支付失败
     */
    FAILED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已退款
     */
    REFUNDED
}
