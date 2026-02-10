package com.flashsell.app.assembler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.flashsell.client.dto.res.CategoryRes;
import com.flashsell.client.dto.res.MarketAnalysisRes;
import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.client.dto.res.SalesDataPointRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.market.entity.MarketAnalysis;
import com.flashsell.domain.market.entity.SalesDataPoint;
import com.flashsell.domain.product.entity.Product;

/**
 * 市场分析数据转换器
 * 负责领域对象与DTO之间的转换
 */
@Component
public class MarketAssembler {

    /**
     * 将市场分析实体转换为响应DTO
     *
     * @param analysis 市场分析实体
     * @param category 品类实体
     * @param topProducts 热门产品列表
     * @return 市场分析响应DTO
     */
    public MarketAnalysisRes toMarketAnalysisRes(MarketAnalysis analysis, Category category, List<Product> topProducts) {
        if (analysis == null) {
            return null;
        }

        return MarketAnalysisRes.builder()
                .category(toCategoryRes(category))
                .marketSize(analysis.getMarketSize())
                .monthlyGrowthRate(analysis.getMonthlyGrowthRate())
                .competitionScore(analysis.getCompetitionScore())
                .entryBarrier(analysis.getEntryBarrier())
                .potentialScore(analysis.getPotentialScore())
                .salesDistribution(toSalesDataPointResList(analysis.getSalesDistribution()))
                .weekOverWeek(analysis.getWeekOverWeek())
                .monthOverMonth(analysis.getMonthOverMonth())
                .topProducts(toProductItemResList(topProducts))
                .analysisDate(analysis.getAnalysisDate())
                .timeRangeDays(analysis.getTimeRangeDays())
                .trendDescription(analysis.getTrendDescription())
                .overallScore(analysis.calculateOverallScore())
                .build();
    }

    /**
     * 将品类实体转换为响应DTO
     *
     * @param category 品类实体
     * @return 品类响应DTO
     */
    private CategoryRes toCategoryRes(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryRes.builder()
                .id(category.getId())
                .name(category.getName())
                .productCount(category.getProductCount())
                .build();
    }

    /**
     * 将销量数据点列表转换为响应DTO列表
     *
     * @param dataPoints 销量数据点列表
     * @return 销量数据点响应DTO列表
     */
    private List<SalesDataPointRes> toSalesDataPointResList(List<SalesDataPoint> dataPoints) {
        if (dataPoints == null) {
            return null;
        }

        return dataPoints.stream()
                .map(this::toSalesDataPointRes)
                .collect(Collectors.toList());
    }

    /**
     * 将销量数据点转换为响应DTO
     *
     * @param dataPoint 销量数据点
     * @return 销量数据点响应DTO
     */
    private SalesDataPointRes toSalesDataPointRes(SalesDataPoint dataPoint) {
        if (dataPoint == null) {
            return null;
        }

        return SalesDataPointRes.builder()
                .date(dataPoint.getDate())
                .salesVolume(dataPoint.getSalesVolume())
                .averagePrice(dataPoint.getAveragePrice())
                .productCount(dataPoint.getProductCount())
                .growthRate(dataPoint.getGrowthRate())
                .build();
    }

    /**
     * 将产品列表转换为产品项响应DTO列表
     *
     * @param products 产品列表
     * @return 产品项响应DTO列表
     */
    private List<ProductItemRes> toProductItemResList(List<Product> products) {
        if (products == null) {
            return null;
        }

        return products.stream()
                .map(this::toProductItemRes)
                .collect(Collectors.toList());
    }

    /**
     * 将产品实体转换为产品项响应DTO
     *
     * @param product 产品实体
     * @return 产品项响应DTO
     */
    private ProductItemRes toProductItemRes(Product product) {
        if (product == null) {
            return null;
        }

        return ProductItemRes.builder()
                .id(product.getId())
                .title(product.getTitle())
                .image(product.getImageUrl())
                .price(product.getCurrentPrice())
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .categoryId(product.getCategoryId())
                .build();
    }
}
