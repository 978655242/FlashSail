package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 市场分析响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisRes {

    /**
     * 品类信息
     */
    private CategoryRes category;

    /**
     * 市场规模（总销量估算）
     */
    private Long marketSize;

    /**
     * 月增长率（百分比）
     */
    private BigDecimal monthlyGrowthRate;

    /**
     * 竞争强度评分（0.00-1.00）
     */
    private BigDecimal competitionScore;

    /**
     * 进入壁垒评分（0.00-1.00）
     */
    private BigDecimal entryBarrier;

    /**
     * 潜力评分（0.00-1.00）
     */
    private BigDecimal potentialScore;

    /**
     * 销量分布数据
     */
    private List<SalesDataPointRes> salesDistribution;

    /**
     * 周环比增长率（百分比）
     */
    private BigDecimal weekOverWeek;

    /**
     * 月环比增长率（百分比）
     */
    private BigDecimal monthOverMonth;

    /**
     * 热门产品列表（Top 10）
     */
    private List<ProductItemRes> topProducts;

    /**
     * 分析日期
     */
    private LocalDate analysisDate;

    /**
     * 时间范围（天数）
     */
    private Integer timeRangeDays;

    /**
     * 市场趋势描述
     */
    private String trendDescription;

    /**
     * 综合市场评分（0.00-1.00）
     */
    private BigDecimal overallScore;
}
