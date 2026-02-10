package com.flashsell.infrastructure.market.gatewayimpl;

import com.flashsell.domain.market.entity.MarketAnalysis;
import com.flashsell.domain.market.entity.SalesDataPoint;
import com.flashsell.domain.market.gateway.MarketGateway;
import com.flashsell.infrastructure.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 市场分析网关实现
 * 实现 MarketGateway 接口，提供市场数据访问的具体实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MarketGatewayImpl implements MarketGateway {

    private final ProductMapper productMapper;

    @Override
    public List<SalesDataPoint> findSalesDistribution(Long categoryId, LocalDate startDate, LocalDate endDate) {
        if (categoryId == null || startDate == null || endDate == null) {
            return List.of();
        }

        // TODO: 实现真实的销量分布查询
        // 目前返回模拟数据，实际应该从产品价格历史表聚合计算
        List<SalesDataPoint> dataPoints = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            SalesDataPoint point = SalesDataPoint.builder()
                    .date(currentDate)
                    .salesVolume(1000L + (long)(Math.random() * 500))
                    .averagePrice(new BigDecimal("29.99"))
                    .productCount(100)
                    .growthRate(new BigDecimal("5.0"))
                    .build();
            dataPoints.add(point);
            currentDate = currentDate.plusDays(1);
        }

        return dataPoints;
    }

    @Override
    public Optional<MarketAnalysis> findLatestAnalysis(Long categoryId) {
        // TODO: 实现从缓存或数据库查询最新的市场分析
        // 目前返回空，强制每次重新计算
        return Optional.empty();
    }

    @Override
    public Optional<MarketAnalysis> findAnalysisByTimeRange(Long categoryId, Integer timeRangeDays) {
        // TODO: 实现从缓存或数据库查询指定时间范围的市场分析
        // 目前返回空，强制每次重新计算
        return Optional.empty();
    }

    @Override
    public MarketAnalysis saveAnalysis(MarketAnalysis analysis) {
        // TODO: 实现市场分析结果的持久化
        // 目前直接返回，不保存
        log.debug("保存市场分析: categoryId={}, analysisDate={}", 
                analysis.getCategoryId(), analysis.getAnalysisDate());
        return analysis;
    }

    @Override
    public Long countProductsByCategory(Long categoryId) {
        if (categoryId == null) {
            return 0L;
        }
        return productMapper.countByCategoryId(categoryId);
    }

    @Override
    public Double getAverageBsrRank(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return productMapper.getAverageBsrRank(categoryId);
    }

    @Override
    public Double getAverageRating(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return productMapper.getAverageRating(categoryId);
    }

    @Override
    public Double getAverageReviewCount(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return productMapper.getAverageReviewCount(categoryId);
    }

    @Override
    public Double getAverageCompetitionScore(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return productMapper.getAverageCompetitionScore(categoryId);
    }

    @Override
    public Long getProductCountChange(Long categoryId, LocalDate startDate, LocalDate endDate) {
        if (categoryId == null || startDate == null || endDate == null) {
            return 0L;
        }
        
        // 计算指定日期范围内新增的产品数量
        Long count = productMapper.countProductsCreatedBetween(categoryId, 
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        
        return count != null ? count : 0L;
    }

    @Override
    public boolean hasEnoughDataForAnalysis(Long categoryId, Integer minProductCount) {
        if (categoryId == null || minProductCount == null) {
            return false;
        }
        
        Long productCount = countProductsByCategory(categoryId);
        return productCount >= minProductCount;
    }
}
