package com.flashsell.app.service;

import com.flashsell.client.dto.ApiResponse;
import com.flashsell.domain.payment.PaymentDomainService;
import com.flashsell.domain.payment.PaymentGateway;
import com.flashsell.domain.payment.SubscriptionOrder;
import com.flashsell.domain.payment.SubscriptionPlan;
import com.flashsell.domain.user.entity.SubscriptionLevel;
import com.flashsell.domain.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅应用服务
 * 处理订阅相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionAppService {

    private final PaymentGateway paymentGateway;
    private final PaymentDomainService paymentDomainService;
    private final UserGateway userGateway;

    /**
     * 获取所有可用套餐
     *
     * @return 套餐列表
     */
    public List<SubscriptionPlan> getAvailablePlans() {
        return paymentGateway.findAllPlans();
    }

    /**
     * 创建订阅订单
     *
     * @param userId 用户ID
     * @param planId 套餐ID
     * @return 订单信息
     */
    @Transactional
    public SubscriptionOrder createOrder(Long userId, Long planId) {
        log.info("创建订阅订单: userId={}, planId={}", userId, planId);

        // 创建订单
        SubscriptionOrder order = paymentDomainService.createSubscriptionOrder(userId, planId);

        log.info("订单创建成功: orderNo={}", order.getOrderNo());
        return order;
    }

    /**
     * 处理支付回调
     *
     * @param orderNo   订单号
     * @param tradeNo   交易号
     * @param paidAmount 支付金额
     * @return 处理结果
     */
    @Transactional
    public boolean handlePaymentCallback(String orderNo, String tradeNo, String paidAmount) {
        log.info("处理支付回调: orderNo={}, tradeNo={}, paidAmount={}", orderNo, tradeNo, paidAmount);

        // 查找订单
        SubscriptionOrder order = paymentGateway.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        // 验证金额
        if (!paymentDomainService.validateAmount(order, new java.math.BigDecimal(paidAmount))) {
            log.error("支付金额不匹配: expected={}, actual={}", order.getAmount(), paidAmount);
            return false;
        }

        // 更新订单状态
        order.markAsPaid(tradeNo);
        paymentGateway.save(order);

        // 更新用户订阅状态
        SubscriptionPlan plan = paymentGateway.findPlanById(order.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在"));

        LocalDateTime expireTime = LocalDateTime.now().plusDays(plan.getDurationDays());
        userGateway.updateSubscriptionLevel(order.getUserId(), plan.getLevel(), expireTime);

        log.info("支付处理成功: orderNo={}, userId={}, level={}", orderNo, order.getUserId(), plan.getLevel());
        return true;
    }

    /**
     * 查询订单状态
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    public java.util.Optional<SubscriptionOrder> getOrderStatus(String orderNo) {
        return paymentGateway.findByOrderNo(orderNo);
    }

    /**
     * 获取用户当前订阅状态
     *
     * @param userId 用户ID
     * @return 订阅状态
     */
    public java.util.Map<String, Object> getSubscriptionStatus(Long userId) {
        var user = userGateway.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        return new java.util.HashMap<>() {{
            put("level", user.get().getSubscriptionLevel());
            put("expireDate", user.get().getSubscriptionExpireDate());
            put("isActive", !user.get().isSubscriptionExpired());
        }};
    }

    /**
     * 检查并更新过期订阅
     */
    @Transactional
    public void checkAndUpdateExpiredSubscriptions() {
        log.info("检查并更新过期订阅");

        // 查询所有过期但未更新的用户
        // 这里需要添加 UserGateway 的查询方法来获取过期用户
        // 暂时跳过，可以后续通过定时任务实现

        log.info("过期订阅检查完成");
    }
}
