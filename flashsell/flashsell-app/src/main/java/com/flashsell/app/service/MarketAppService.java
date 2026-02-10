package com.flashsell.app.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.flashsell.app.assembler.MarketAssembler;
import com.flashsell.client.dto.req.MarketAnalysisReq;
import com.flashsell.client.dto.res.MarketAnalysisRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.market.entity.MarketAnalysis;
import com.flashsell.domain.market.service.MarketDomainService;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 市场分析应用服务
 * 提供市场分析相关的业务编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketAppService {

    private final MarketDomainService marketDomainService;
    private final CategoryGateway categoryGateway;
    private final ProductGateway productGateway;
    private final MarketAssembler marketAssembler;

    private static final int TOP_PRODUCTS_LIMIT = 10;

    /**
     * 获取市场分析
     *
     * @param req 市场分析请求
     * @return 市场分析响应（可能为空）
     */
    public Optional<MarketAnalysisRes> getMarketAnalysis(MarketAnalysisReq req) {
        log.debug("获取市场分析: categoryId={}, timeRangeDays={}", req.getCategoryId(), req.getTimeRangeDays());

        // 1. 验证时间范围参数
        Integer timeRangeDays = validateAndNormalizeTimeRange(req.getTimeRangeDays());

        // 2. 检查品类是否存在
        Optional<Category> categoryOpt = categoryGateway.findById(req.getCategoryId());
        if (categoryOpt.isEmpty()) {
            log.warn("品类不存在: categoryId={}", req.getCategoryId());
            return Optional.empty();
        }
        Category category = categoryOpt.get();

        // 3. 检查是否有足够的数据进行分析
        if (!marketDomainService.hasEnoughDataForAnalysis(req.getCategoryId())) {
            log.warn("品类数据不足，无法进行市场分析: categoryId={}", req.getCategoryId());
            return Optional.empty();
        }

        // 4. 获取或生成市场分析
        Optional<MarketAnalysis> analysisOpt = marketDomainService.getMarketAnalysis(req.getCategoryId(), timeRangeDays);
        if (analysisOpt.isEmpty()) {
            log.warn("无法生成市场分析: categoryId={}", req.getCategoryId());
            return Optional.empty();
        }
        MarketAnalysis analysis = analysisOpt.get();

        // 5. 获取热门产品（Top 10）
        List<Product> topProducts = getTopProducts(req.getCategoryId());

        // 6. 组装响应
        MarketAnalysisRes res = marketAssembler.toMarketAnalysisRes(analysis, category, topProducts);

        log.debug("市场分析获取成功: categoryId={}, overallScore={}", req.getCategoryId(), res.getOverallScore());
        return Optional.of(res);
    }

    /**
     * 验证并规范化时间范围参数
     * 只允许 30、90、365 三个值
     *
     * @param timeRangeDays 时间范围（天数）
     * @return 规范化后的时间范围
     */
    private Integer validateAndNormalizeTimeRange(Integer timeRangeDays) {
        if (timeRangeDays == null) {
            return 30; // 默认30天
        }

        // 只允许 30、90、365
        if (timeRangeDays == 30 || timeRangeDays == 90 || timeRangeDays == 365) {
            return timeRangeDays;
        }

        // 其他值映射到最接近的有效值
        if (timeRangeDays < 60) {
            return 30;
        } else if (timeRangeDays < 180) {
            return 90;
        } else {
            return 365;
        }
    }

    /**
     * 获取品类下的热门产品（Top 10）
     * 按BSR排名升序排列
     *
     * @param categoryId 品类ID
     * @return 热门产品列表
     */
    private List<Product> getTopProducts(Long categoryId) {
        List<Product> products = productGateway.findByCategoryId(categoryId);

        return products.stream()
                .filter(Product::hasBsrRank) // 只包含有BSR排名的产品
                .sorted(Comparator.comparing(Product::getBsrRank)) // 按BSR排名升序
                .limit(TOP_PRODUCTS_LIMIT) // 取前10个
                .collect(Collectors.toList());
    }

    /**
     * 刷新市场分析
     * 强制重新生成市场分析数据
     *
     * @param categoryId 品类ID
     * @param timeRangeDays 时间范围（天数）
     * @return 市场分析响应（可能为空）
     */
    public Optional<MarketAnalysisRes> refreshMarketAnalysis(Long categoryId, Integer timeRangeDays) {
        log.debug("刷新市场分析: categoryId={}, timeRangeDays={}", categoryId, timeRangeDays);

        // 1. 验证时间范围参数
        Integer normalizedTimeRange = validateAndNormalizeTimeRange(timeRangeDays);

        // 2. 检查品类是否存在
        Optional<Category> categoryOpt = categoryGateway.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            log.warn("品类不存在: categoryId={}", categoryId);
            return Optional.empty();
        }
        Category category = categoryOpt.get();

        // 3. 检查是否有足够的数据进行分析
        if (!marketDomainService.hasEnoughDataForAnalysis(categoryId)) {
            log.warn("品类数据不足，无法进行市场分析: categoryId={}", categoryId);
            return Optional.empty();
        }

        // 4. 强制生成新的市场分析
        MarketAnalysis analysis = marketDomainService.generateMarketAnalysis(categoryId, normalizedTimeRange);

        // 5. 获取热门产品（Top 10）
        List<Product> topProducts = getTopProducts(categoryId);

        // 6. 组装响应
        MarketAnalysisRes res = marketAssembler.toMarketAnalysisRes(analysis, category, topProducts);

        log.debug("市场分析刷新成功: categoryId={}, overallScore={}", categoryId, res.getOverallScore());
        return Optional.of(res);
    }

    /**
     * 检查品类是否有足够的数据进行分析
     *
     * @param categoryId 品类ID
     * @return 是否有足够数据
     */
    public boolean hasEnoughDataForAnalysis(Long categoryId) {
        return marketDomainService.hasEnoughDataForAnalysis(categoryId);
    }
}
