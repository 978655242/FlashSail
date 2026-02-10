package com.flashsell.domain.market.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.flashsell.domain.market.entity.MarketAnalysis;
import com.flashsell.domain.market.entity.SalesDataPoint;
import com.flashsell.domain.market.gateway.MarketGateway;
import com.flashsell.domain.product.gateway.ProductGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 市场分析领域服务
 * 处理市场分析相关的核心业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDomainService {

    private final MarketGateway marketGateway;
    private final ProductGateway productGateway;

    private static final Integer MIN_PRODUCT_COUNT_FOR_ANALYSIS = 10;

    /**
     * 根据品类ID和时间范围获取市场分析
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数：30、90、365）
     * @return 市场分析实体（可能为空）
     */
    public Optional<MarketAnalysis> getMarketAnalysis(Long categoryId, Integer timeRangeDays) {
        // 检查是否有足够的数据进行分析
        if (!marketGateway.hasEnoughDataForAnalysis(categoryId, MIN_PRODUCT_COUNT_FOR_ANALYSIS)) {
            log.warn("品类 {} 数据不足，无法进行市场分析", categoryId);
            return Optional.empty();
        }

        // 尝试从缓存或数据库获取已有的分析结果
        Optional<MarketAnalysis> existingAnalysis = marketGateway.findAnalysisByTimeRange(categoryId, timeRangeDays);
        if (existingAnalysis.isPresent()) {
            MarketAnalysis analysis = existingAnalysis.get();
            // 检查分析是否为今天的数据
            if (analysis.getAnalysisDate().equals(LocalDate.now())) {
                return existingAnalysis;
            }
        }

        // 生成新的市场分析
        return Optional.of(generateMarketAnalysis(categoryId, timeRangeDays));
    }

    /**
     * 生成市场分析
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数）
     * @return 市场分析实体
     */
    public MarketAnalysis generateMarketAnalysis(Long categoryId, Integer timeRangeDays) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(timeRangeDays);

        // 1. 获取销量分布数据
        List<SalesDataPoint> salesDistribution = marketGateway.findSalesDistribution(categoryId, startDate, endDate);

        // 2. 计算市场规模（基于产品数量和平均销量估算）
        Long marketSize = calculateMarketSize(categoryId, salesDistribution);

        // 3. 计算月增长率
        BigDecimal monthlyGrowthRate = calculateMonthlyGrowthRate(categoryId, timeRangeDays);

        // 4. 计算竞争强度评分
        BigDecimal competitionScore = calculateCompetitionScore(categoryId);

        // 5. 计算进入壁垒评分
        BigDecimal entryBarrier = calculateEntryBarrier(categoryId);

        // 6. 计算潜力评分
        BigDecimal potentialScore = calculatePotentialScore(monthlyGrowthRate, competitionScore, entryBarrier);

        // 7. 计算周环比和月环比
        BigDecimal weekOverWeek = calculateWeekOverWeek(categoryId);
        BigDecimal monthOverMonth = calculateMonthOverMonth(categoryId);

        // 8. 构建市场分析实体
        MarketAnalysis analysis = MarketAnalysis.builder()
                .categoryId(categoryId)
                .marketSize(marketSize)
                .monthlyGrowthRate(monthlyGrowthRate)
                .competitionScore(competitionScore)
                .entryBarrier(entryBarrier)
                .potentialScore(potentialScore)
                .salesDistribution(salesDistribution)
                .weekOverWeek(weekOverWeek)
                .monthOverMonth(monthOverMonth)
                .analysisDate(LocalDate.now())
                .timeRangeDays(timeRangeDays)
                .build();

        // 9. 保存分析结果
        return marketGateway.saveAnalysis(analysis);
    }

    /**
     * 计算市场规模
     *
     * @param categoryId 品类ID
     * @param salesDistribution 销量分布数据
     * @return 市场规模
     */
    private Long calculateMarketSize(Long categoryId, List<SalesDataPoint> salesDistribution) {
        if (salesDistribution == null || salesDistribution.isEmpty()) {
            return 0L;
        }

        // 计算平均日销量
        long totalSales = salesDistribution.stream()
                .mapToLong(SalesDataPoint::getSalesVolume)
                .sum();

        // 估算月销量（平均日销量 * 30）
        return (totalSales / salesDistribution.size()) * 30;
    }

    /**
     * 计算月增长率
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数）
     * @return 月增长率（百分比）
     */
    private BigDecimal calculateMonthlyGrowthRate(Long categoryId, Integer timeRangeDays) {
        if (timeRangeDays < 60) {
            // 时间范围不足，无法计算月增长率
            return BigDecimal.ZERO;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate midDate = endDate.minusDays(30);
        LocalDate startDate = endDate.minusDays(60);

        // 获取最近30天和前30天的产品数量变化
        Long recentChange = marketGateway.getProductCountChange(categoryId, midDate, endDate);
        Long previousChange = marketGateway.getProductCountChange(categoryId, startDate, midDate);

        if (previousChange == null || previousChange == 0) {
            return BigDecimal.ZERO;
        }

        // 计算增长率：(最近变化 - 之前变化) / 之前变化 * 100
        BigDecimal growthRate = BigDecimal.valueOf(recentChange - previousChange)
                .divide(BigDecimal.valueOf(Math.abs(previousChange)), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return growthRate.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算竞争强度评分
     * 基于平均BSR排名、平均评论数、产品数量
     *
     * @param categoryId 品类ID
     * @return 竞争强度评分（0.00-1.00）
     */
    private BigDecimal calculateCompetitionScore(Long categoryId) {
        Double avgBsrRank = marketGateway.getAverageBsrRank(categoryId);
        Double avgReviewCount = marketGateway.getAverageReviewCount(categoryId);
        Long productCount = marketGateway.countProductsByCategory(categoryId);

        if (avgBsrRank == null || avgReviewCount == null || productCount == null) {
            return new BigDecimal("0.5"); // 默认中等竞争
        }

        // BSR排名越低，竞争越激烈（权重：40%）
        BigDecimal bsrScore = calculateBsrScore(avgBsrRank);

        // 评论数越多，竞争越激烈（权重：30%）
        BigDecimal reviewScore = calculateReviewScore(avgReviewCount);

        // 产品数量越多，竞争越激烈（权重：30%）
        BigDecimal productCountScore = calculateProductCountScore(productCount);

        BigDecimal competitionScore = bsrScore.multiply(new BigDecimal("0.4"))
                .add(reviewScore.multiply(new BigDecimal("0.3")))
                .add(productCountScore.multiply(new BigDecimal("0.3")));

        return competitionScore.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算BSR评分
     *
     * @param avgBsrRank 平均BSR排名
     * @return BSR评分（0.00-1.00）
     */
    private BigDecimal calculateBsrScore(Double avgBsrRank) {
        if (avgBsrRank <= 1000) {
            return BigDecimal.ONE; // 极高竞争
        } else if (avgBsrRank <= 5000) {
            return new BigDecimal("0.8"); // 高竞争
        } else if (avgBsrRank <= 10000) {
            return new BigDecimal("0.6"); // 中等竞争
        } else if (avgBsrRank <= 50000) {
            return new BigDecimal("0.4"); // 低竞争
        } else {
            return new BigDecimal("0.2"); // 极低竞争
        }
    }

    /**
     * 计算评论数评分
     *
     * @param avgReviewCount 平均评论数
     * @return 评论数评分（0.00-1.00）
     */
    private BigDecimal calculateReviewScore(Double avgReviewCount) {
        if (avgReviewCount >= 1000) {
            return BigDecimal.ONE; // 极高竞争
        } else if (avgReviewCount >= 500) {
            return new BigDecimal("0.8"); // 高竞争
        } else if (avgReviewCount >= 100) {
            return new BigDecimal("0.6"); // 中等竞争
        } else if (avgReviewCount >= 50) {
            return new BigDecimal("0.4"); // 低竞争
        } else {
            return new BigDecimal("0.2"); // 极低竞争
        }
    }

    /**
     * 计算产品数量评分
     *
     * @param productCount 产品数量
     * @return 产品数量评分（0.00-1.00）
     */
    private BigDecimal calculateProductCountScore(Long productCount) {
        if (productCount >= 1000) {
            return BigDecimal.ONE; // 极高竞争
        } else if (productCount >= 500) {
            return new BigDecimal("0.8"); // 高竞争
        } else if (productCount >= 100) {
            return new BigDecimal("0.6"); // 中等竞争
        } else if (productCount >= 50) {
            return new BigDecimal("0.4"); // 低竞争
        } else {
            return new BigDecimal("0.2"); // 极低竞争
        }
    }

    /**
     * 计算进入壁垒评分
     * 基于平均竞争评分和平均评分
     *
     * @param categoryId 品类ID
     * @return 进入壁垒评分（0.00-1.00）
     */
    private BigDecimal calculateEntryBarrier(Long categoryId) {
        Double avgCompetitionScore = marketGateway.getAverageCompetitionScore(categoryId);
        Double avgRating = marketGateway.getAverageRating(categoryId);

        if (avgCompetitionScore == null || avgRating == null) {
            return new BigDecimal("0.5"); // 默认中等壁垒
        }

        // 竞争评分越高，壁垒越高（权重：60%）
        BigDecimal competitionBarrier = BigDecimal.valueOf(avgCompetitionScore);

        // 平均评分越高，壁垒越高（权重：40%）
        BigDecimal ratingBarrier = BigDecimal.valueOf(avgRating / 5.0);

        BigDecimal entryBarrier = competitionBarrier.multiply(new BigDecimal("0.6"))
                .add(ratingBarrier.multiply(new BigDecimal("0.4")));

        return entryBarrier.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算潜力评分
     * 综合考虑增长率、竞争强度、进入壁垒
     *
     * @param monthlyGrowthRate 月增长率
     * @param competitionScore 竞争强度评分
     * @param entryBarrier 进入壁垒评分
     * @return 潜力评分（0.00-1.00）
     */
    private BigDecimal calculatePotentialScore(BigDecimal monthlyGrowthRate, 
                                               BigDecimal competitionScore, 
                                               BigDecimal entryBarrier) {
        // 增长率权重：50%
        BigDecimal growthScore = monthlyGrowthRate.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (growthScore.compareTo(BigDecimal.ONE) > 0) {
            growthScore = BigDecimal.ONE;
        }

        // 竞争强度权重：30%（反向，竞争越低潜力越高）
        BigDecimal competitionPenalty = BigDecimal.ONE.subtract(competitionScore);

        // 进入壁垒权重：20%（反向，壁垒越低潜力越高）
        BigDecimal barrierPenalty = BigDecimal.ONE.subtract(entryBarrier);

        BigDecimal potentialScore = growthScore.multiply(new BigDecimal("0.5"))
                .add(competitionPenalty.multiply(new BigDecimal("0.3")))
                .add(barrierPenalty.multiply(new BigDecimal("0.2")));

        return potentialScore.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算周环比增长率
     *
     * @param categoryId 品类ID
     * @return 周环比增长率（百分比）
     */
    private BigDecimal calculateWeekOverWeek(Long categoryId) {
        LocalDate endDate = LocalDate.now();
        LocalDate midDate = endDate.minusDays(7);
        LocalDate startDate = endDate.minusDays(14);

        Long recentChange = marketGateway.getProductCountChange(categoryId, midDate, endDate);
        Long previousChange = marketGateway.getProductCountChange(categoryId, startDate, midDate);

        if (previousChange == null || previousChange == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal growthRate = BigDecimal.valueOf(recentChange - previousChange)
                .divide(BigDecimal.valueOf(Math.abs(previousChange)), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return growthRate.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算月环比增长率
     *
     * @param categoryId 品类ID
     * @return 月环比增长率（百分比）
     */
    private BigDecimal calculateMonthOverMonth(Long categoryId) {
        LocalDate endDate = LocalDate.now();
        LocalDate midDate = endDate.minusDays(30);
        LocalDate startDate = endDate.minusDays(60);

        Long recentChange = marketGateway.getProductCountChange(categoryId, midDate, endDate);
        Long previousChange = marketGateway.getProductCountChange(categoryId, startDate, midDate);

        if (previousChange == null || previousChange == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal growthRate = BigDecimal.valueOf(recentChange - previousChange)
                .divide(BigDecimal.valueOf(Math.abs(previousChange)), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return growthRate.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 检查品类是否有足够的数据进行分析
     *
     * @param categoryId 品类ID
     * @return 是否有足够数据
     */
    public boolean hasEnoughDataForAnalysis(Long categoryId) {
        return marketGateway.hasEnoughDataForAnalysis(categoryId, MIN_PRODUCT_COUNT_FOR_ANALYSIS);
    }
}
