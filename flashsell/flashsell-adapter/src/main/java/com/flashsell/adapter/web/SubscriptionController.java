package com.flashsell.adapter.web;

import com.flashsell.app.service.SubscriptionAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.CreateOrderReq;
import com.flashsell.client.dto.res.SubscriptionOrderRes;
import com.flashsell.client.dto.res.SubscriptionPlanRes;
import com.flashsell.domain.payment.SubscriptionOrder;
import com.flashsell.domain.payment.SubscriptionPlan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订阅支付控制器
 * 处理订阅相关的 API 请求
 */
@Slf4j
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionAppService subscriptionAppService;

    /**
     * 获取所有可用套餐
     *
     * GET /api/subscription/plans
     *
     * @return 套餐列表
     */
    @GetMapping("/plans")
    public ApiResponse<List<SubscriptionPlanRes>> getPlans() {
        log.info("获取订阅套餐列表");

        List<SubscriptionPlan> plans = subscriptionAppService.getAvailablePlans();

        List<SubscriptionPlanRes> planResList = plans.stream()
                .map(this::toPlanRes)
                .collect(Collectors.toList());

        return ApiResponse.success(planResList);
    }

    /**
     * 创建订阅订单
     *
     * POST /api/subscription/orders
     *
     * @param req 创建订单请求
     * @return 订单信息
     */
    @PostMapping("/orders")
    public ApiResponse<SubscriptionOrderRes> createOrder(@Valid @RequestBody CreateOrderReq req) {
        log.info("创建订阅订单: planId={}", req.getPlanId());

        // 获取当前用户ID（从JWT中获取，这里暂时使用固定值）
        Long userId = 1L;

        // 创建订单
        SubscriptionOrder order = subscriptionAppService.createOrder(userId, req.getPlanId());

        // 生成支付URL（这里暂时返回模拟URL）
        String paymentUrl = "https://openapi.alipay.com/gateway.do?appId=123&method=alipay.trade.page.pay&out_trade_no="
                + order.getOrderNo()
                + "&total_amount=" + order.getAmount()
                + "&subject=" + "订阅会员";

        SubscriptionOrderRes orderRes = toOrderRes(order);
        orderRes.setPaymentUrl(paymentUrl);

        return ApiResponse.success(orderRes);
    }

    /**
     * 查询订单状态
     *
     * GET /api/subscription/orders/status?orderNo=xxx
     *
     * @param orderNo 订单号
     * @return 订单状态
     */
    @GetMapping("/orders/status")
    public ApiResponse<SubscriptionOrderRes> getOrderStatus(@RequestParam String orderNo) {
        log.info("查询订单状态: orderNo={}", orderNo);

        var order = subscriptionAppService.getOrderStatus(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        return ApiResponse.success(toOrderRes(order));
    }

    /**
     * 获取用户订阅状态
     *
     * GET /api/subscription/status
     *
     * @return 订阅状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getSubscriptionStatus() {
        log.info("获取用户订阅状态");

        // 获取当前用户ID
        Long userId = 1L;

        Map<String, Object> status = subscriptionAppService.getSubscriptionStatus(userId);

        return ApiResponse.success(status);
    }

    /**
     * 支付回调（支付宝异步通知）
     *
     * POST /api/subscription/callback
     *
     * @param params 支付宝回调参数
     * @return 处理结果
     */
    @PostMapping("/callback")
    public String handlePaymentCallback(@RequestBody Map<String, String> params) {
        log.info("收到支付回调: {}", params);

        try {
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");

            boolean success = subscriptionAppService.handlePaymentCallback(orderNo, tradeNo, totalAmount);

            return success ? "success" : "failure";
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return "failure";
        }
    }

    private SubscriptionPlanRes toPlanRes(SubscriptionPlan plan) {
        return SubscriptionPlanRes.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .searchLimit(plan.getSearchLimit())
                .exportLimit(plan.getExportLimit())
                .boardLimit(plan.getBoardLimit())
                .aiAnalysisEnabled(plan.getAiAnalysisEnabled())
                .apiAccessEnabled(plan.getApiAccessEnabled())
                .level(plan.getLevel().name())
                .build();
    }

    private SubscriptionOrderRes toOrderRes(SubscriptionOrder order) {
        return SubscriptionOrderRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .planId(order.getPlanId())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .alipayTradeNo(order.getTradeNo())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
