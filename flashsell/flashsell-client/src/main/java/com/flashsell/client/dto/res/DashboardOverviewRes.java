package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 仪表盘数据概览响应 DTO
 * 
 * Requirements: 13.1, 13.7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewRes {

    /**
     * 今日新品发现数
     */
    private Integer todayNewProducts;

    /**
     * 潜力爆品推荐数
     */
    private Integer potentialHotProducts;

    /**
     * 收藏产品数
     */
    private Integer favoriteCount;

    /**
     * AI 推荐准确率（0.0-1.0）
     */
    private Double aiAccuracyRate;

    /**
     * 数据最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}
