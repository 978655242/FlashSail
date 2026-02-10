package com.flashsell.domain.market.gateway;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.flashsell.domain.market.entity.MarketAnalysis;
import com.flashsell.domain.market.entity.SalesDataPoint;

/**
 * 市场分析网关接口
 * 定义市场数据访问的抽象接口，由 infrastructure 层实现
 */
public interface MarketGateway {

    /**
     * 根据品类ID和日期范围获取销量分布数据
     *
     * @param categoryId 品类ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销量数据点列表
     */
    List<SalesDataPoint> findSalesDistribution(Long categoryId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据品类ID获取最新的市场分析
     *
     * @param categoryId 品类ID
     * @return 市场分析实体（可能为空）
     */
    Optional<MarketAnalysis> findLatestAnalysis(Long categoryId);

    /**
     * 根据品类ID和时间范围获取市场分析
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数）
     * @return 市场分析实体（可能为空）
     */
    Optional<MarketAnalysis> findAnalysisByTimeRange(Long categoryId, Integer timeRangeDays);

    /**
     * 保存市场分析结果
     *
     * @param analysis 市场分析实体
     * @return 保存后的市场分析实体
     */
    MarketAnalysis saveAnalysis(MarketAnalysis analysis);

    /**
     * 获取品类下的产品总数
     *
     * @param categoryId 品类ID
     * @return 产品总数
     */
    Long countProductsByCategory(Long categoryId);

    /**
     * 获取品类下的平均BSR排名
     *
     * @param categoryId 品类ID
     * @return 平均BSR排名
     */
    Double getAverageBsrRank(Long categoryId);

    /**
     * 获取品类下的平均评分
     *
     * @param categoryId 品类ID
     * @return 平均评分
     */
    Double getAverageRating(Long categoryId);

    /**
     * 获取品类下的平均评论数
     *
     * @param categoryId 品类ID
     * @return 平均评论数
     */
    Double getAverageReviewCount(Long categoryId);

    /**
     * 获取品类下的平均竞争评分
     *
     * @param categoryId 品类ID
     * @return 平均竞争评分
     */
    Double getAverageCompetitionScore(Long categoryId);

    /**
     * 获取指定日期范围内品类的产品数量变化
     *
     * @param categoryId 品类ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 产品数量变化（正数表示增长，负数表示减少）
     */
    Long getProductCountChange(Long categoryId, LocalDate startDate, LocalDate endDate);

    /**
     * 检查品类是否有足够的数据进行分析
     *
     * @param categoryId 品类ID
     * @param minProductCount 最小产品数量
     * @return 是否有足够数据
     */
    boolean hasEnoughDataForAnalysis(Long categoryId, Integer minProductCount);
}
