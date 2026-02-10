package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订阅套餐响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRes {

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
     * 价格
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
    private String level;
}
