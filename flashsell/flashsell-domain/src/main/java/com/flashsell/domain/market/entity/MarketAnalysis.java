package com.flashsell.domain.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 市场分析领域实体
 * 包含品类市场分析的核心数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysis {

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 品类名称
     */
    private String categoryName;

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
     * 销量分布数据点
     */
    private List<SalesDataPoint> salesDistribution;

    /**
     * 周环比增长率（百分比）
     */
    private BigDecimal weekOverWeek;

    /**
     * 月环比增长率（百分比）
     */
    private BigDecimal monthOverMonth;

    /**
     * 分析日期
     */
    private LocalDate analysisDate;

    /**
     * 时间范围（天数）
     */
    private Integer timeRangeDays;

    // ==================== 领域行为 ====================

    /**
     * 检查市场是否为高增长市场（月增长率>=20%）
     *
     * @return 是否为高增长市场
     */
    public boolean isHighGrowthMarket() {
        return monthlyGrowthRate != null && 
               monthlyGrowthRate.compareTo(new BigDecimal("20")) >= 0;
    }

    /**
     * 检查市场是否为高竞争市场（竞争强度>=0.7）
     *
     * @return 是否为高竞争市场
     */
    public boolean isHighCompetitionMarket() {
        return competitionScore != null && 
               competitionScore.compareTo(new BigDecimal("0.7")) >= 0;
    }

    /**
     * 检查市场是否为低壁垒市场（进入壁垒<=0.3）
     *
     * @return 是否为低壁垒市场
     */
    public boolean isLowBarrierMarket() {
        return entryBarrier != null && 
               entryBarrier.compareTo(new BigDecimal("0.3")) <= 0;
    }

    /**
     * 检查市场是否为高潜力市场（潜力评分>=0.7）
     *
     * @return 是否为高潜力市场
     */
    public boolean isHighPotentialMarket() {
        return potentialScore != null && 
               potentialScore.compareTo(new BigDecimal("0.7")) >= 0;
    }

    /**
     * 检查市场是否呈上升趋势（周环比>0 且 月环比>0）
     *
     * @return 是否呈上升趋势
     */
    public boolean isUpwardTrend() {
        return weekOverWeek != null && weekOverWeek.compareTo(BigDecimal.ZERO) > 0 &&
               monthOverMonth != null && monthOverMonth.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查市场是否呈下降趋势（周环比<0 且 月环比<0）
     *
     * @return 是否呈下降趋势
     */
    public boolean isDownwardTrend() {
        return weekOverWeek != null && weekOverWeek.compareTo(BigDecimal.ZERO) < 0 &&
               monthOverMonth != null && monthOverMonth.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 获取市场趋势描述
     *
     * @return 趋势描述
     */
    public String getTrendDescription() {
        if (isUpwardTrend()) {
            return "上升";
        } else if (isDownwardTrend()) {
            return "下降";
        } else {
            return "稳定";
        }
    }

    /**
     * 计算综合市场评分（考虑增长率、竞争强度、潜力评分）
     *
     * @return 综合评分（0.00-1.00）
     */
    public BigDecimal calculateOverallScore() {
        if (monthlyGrowthRate == null || competitionScore == null || potentialScore == null) {
            return BigDecimal.ZERO;
        }

        // 增长率权重：30%，竞争强度权重：30%（反向），潜力评分权重：40%
        BigDecimal growthScore = monthlyGrowthRate.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal competitionPenalty = BigDecimal.ONE.subtract(competitionScore);
        
        BigDecimal score = growthScore.multiply(new BigDecimal("0.3"))
                .add(competitionPenalty.multiply(new BigDecimal("0.3")))
                .add(potentialScore.multiply(new BigDecimal("0.4")));

        // 确保评分在 0-1 之间
        if (score.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return score.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查市场数据是否完整
     *
     * @return 是否完整
     */
    public boolean isDataComplete() {
        return categoryId != null &&
               categoryName != null && !categoryName.isEmpty() &&
               marketSize != null &&
               monthlyGrowthRate != null &&
               competitionScore != null &&
               entryBarrier != null &&
               potentialScore != null &&
               salesDistribution != null && !salesDistribution.isEmpty() &&
               weekOverWeek != null &&
               monthOverMonth != null;
    }
}
