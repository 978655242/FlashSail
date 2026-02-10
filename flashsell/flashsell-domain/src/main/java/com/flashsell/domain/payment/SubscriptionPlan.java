package com.flashsell.domain.payment;

import com.flashsell.domain.user.entity.SubscriptionLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 订阅套餐实体
 * 定义系统支持的订阅套餐类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 价格（元/月）
     */
    private BigDecimal price;

    /**
     * 订阅时长（天）
     */
    private Integer durationDays;

    /**
     * 搜索次数限制
     */
    private Integer searchLimit;

    /**
     * 导出次数限制
     */
    private Integer exportLimit;

    /**
     * 看板数量限制
     */
    private Integer boardLimit;

    /**
     * 是否支持AI分析
     */
    private Boolean aiAnalysisEnabled;

    /**
     * 是否支持API访问
     */
    private Boolean apiAccessEnabled;

    /**
     * 套餐级别
     */
    private SubscriptionLevel level;

    /**
     * 获取订阅时长作为 Duration
     */
    public Duration getDuration() {
        return Duration.ofDays(durationDays);
    }

    /**
     * 检查是否支持指定功能
     */
    public boolean supportsFeature(String feature) {
        return switch (feature) {
            case "ai_analysis" -> aiAnalysisEnabled;
            case "api_access" -> apiAccessEnabled;
            default -> true;
        };
    }
}
