package com.flashsell.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeProperty;

/**
 * 市场分析属性测试
 * 
 * Property 20: 市场数据聚合正确性
 * *对于任意* 品类的市场分析，返回的销量分布和竞争强度评分应该基于该品类下所有产品的数据正确聚合计算。
 * 
 * Validates: Requirements 6.1, 6.3
 * 
 * Feature: flashsell-technical-solution, Property 20: 市场数据聚合正确性
 */
class MarketAppServicePropertyTest {

    /**
     * 产品数据模型（用于测试）
     */
    record Product(
        Long id,
        String title,
        BigDecimal price,
        Integer bsrRank,
        Integer reviewCount,
        Double rating,
        Long categoryId,
        Integer monthlySales
    ) {}

    /**
     * 市场分析结果模型（用于测试）
     */
    record MarketAnalysisResult(
        Long categoryId,
        List<SalesDistributionPoint> salesDistribution,
        Double competitionScore,
        Double weekOverWeek,
        Double monthOverMonth,
        BigDecimal avgPrice,
        Integer totalProducts,
        Integer totalSales
    ) {}

    /**
     * 销量分布点
     */
    record SalesDistributionPoint(
        String priceRange,
        Integer productCount,
        Integer totalSales
    ) {}

    /**
     * 市场分析服务（简化实现用于测试）
     */
    static class MarketAnalysisService {
        
        /**
         * 计算市场分析数据
         */
        public MarketAnalysisResult analyzeMarket(Long categoryId, List<Product> products) {
            if (products == null || products.isEmpty()) {
                return new MarketAnalysisResult(
                    categoryId,
                    Collections.emptyList(),
                    0.0,
                    0.0,
                    0.0,
                    BigDecimal.ZERO,
                    0,
                    0
                );
            }

            // 过滤出指定品类的产品
            List<Product> categoryProducts = products.stream()
                .filter(p -> p.categoryId().equals(categoryId))
                .toList();

            if (categoryProducts.isEmpty()) {
                return new MarketAnalysisResult(
                    categoryId,
                    Collections.emptyList(),
                    0.0,
                    0.0,
                    0.0,
                    BigDecimal.ZERO,
                    0,
                    0
                );
            }

            // 计算销量分布
            List<SalesDistributionPoint> salesDistribution = calculateSalesDistribution(categoryProducts);

            // 计算竞争强度评分
            Double competitionScore = calculateCompetitionScore(categoryProducts);

            // 计算平均价格
            BigDecimal avgPrice = calculateAveragePrice(categoryProducts);

            // 计算总产品数和总销量
            int totalProducts = categoryProducts.size();
            int totalSales = categoryProducts.stream()
                .mapToInt(p -> p.monthlySales() != null ? p.monthlySales() : 0)
                .sum();

            return new MarketAnalysisResult(
                categoryId,
                salesDistribution,
                competitionScore,
                0.0, // weekOverWeek 需要历史数据
                0.0, // monthOverMonth 需要历史数据
                avgPrice,
                totalProducts,
                totalSales
            );
        }

        /**
         * 计算销量分布（按价格区间分组）
         */
        private List<SalesDistributionPoint> calculateSalesDistribution(List<Product> products) {
            // 定义价格区间
            String[] priceRanges = {"0-10", "10-25", "25-50", "50-100", "100+"};
            Map<String, List<Product>> grouped = new LinkedHashMap<>();
            
            for (String range : priceRanges) {
                grouped.put(range, new ArrayList<>());
            }

            for (Product product : products) {
                BigDecimal price = product.price();
                if (price == null) continue;
                
                String range = getPriceRange(price);
                grouped.get(range).add(product);
            }

            return grouped.entrySet().stream()
                .map(entry -> new SalesDistributionPoint(
                    entry.getKey(),
                    entry.getValue().size(),
                    entry.getValue().stream()
                        .mapToInt(p -> p.monthlySales() != null ? p.monthlySales() : 0)
                        .sum()
                ))
                .toList();
        }

        private String getPriceRange(BigDecimal price) {
            double p = price.doubleValue();
            if (p < 10) return "0-10";
            if (p < 25) return "10-25";
            if (p < 50) return "25-50";
            if (p < 100) return "50-100";
            return "100+";
        }

        /**
         * 计算竞争强度评分 (0-1)
         * 基于：产品数量、评论数分布、BSR 排名分布
         */
        private Double calculateCompetitionScore(List<Product> products) {
            if (products.isEmpty()) return 0.0;

            // 因子1: 产品数量（越多竞争越激烈）
            double productCountFactor = Math.min(products.size() / 100.0, 1.0);

            // 因子2: 平均评论数（越多说明市场越成熟）
            double avgReviews = products.stream()
                .mapToInt(p -> p.reviewCount() != null ? p.reviewCount() : 0)
                .average()
                .orElse(0);
            double reviewFactor = Math.min(avgReviews / 1000.0, 1.0);

            // 因子3: BSR 排名集中度（排名越集中竞争越激烈）
            double avgBsr = products.stream()
                .filter(p -> p.bsrRank() != null && p.bsrRank() > 0)
                .mapToInt(Product::bsrRank)
                .average()
                .orElse(100000);
            double bsrFactor = avgBsr < 10000 ? 1.0 : Math.max(0, 1.0 - (avgBsr - 10000) / 90000.0);

            // 综合评分
            double score = (productCountFactor * 0.3 + reviewFactor * 0.4 + bsrFactor * 0.3);
            return Math.round(score * 100.0) / 100.0;
        }

        /**
         * 计算平均价格
         */
        private BigDecimal calculateAveragePrice(List<Product> products) {
            if (products.isEmpty()) return BigDecimal.ZERO;

            BigDecimal sum = products.stream()
                .map(Product::price)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long count = products.stream()
                .filter(p -> p.price() != null)
                .count();

            if (count == 0) return BigDecimal.ZERO;

            return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }
    }

    private MarketAnalysisService marketAnalysisService;

    @BeforeProperty
    void setUp() {
        marketAnalysisService = new MarketAnalysisService();
    }

    /**
     * 产品生成器
     */
    @Provide
    Arbitrary<Product> products() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),           // id
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100), // title
            Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(500))
                .ofScale(2),                                   // price
            Arbitraries.integers().between(1, 100000),        // bsrRank
            Arbitraries.integers().between(0, 10000),         // reviewCount
            Arbitraries.doubles().between(1.0, 5.0),          // rating
            Arbitraries.longs().between(1L, 10L),             // categoryId
            Arbitraries.integers().between(0, 5000)           // monthlySales
        ).as(Product::new);
    }

    /**
     * 产品列表生成器（同一品类）
     */
    @Provide
    Arbitrary<List<Product>> productListForCategory() {
        Long fixedCategoryId = 1L;
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
            Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(500))
                .ofScale(2),
            Arbitraries.integers().between(1, 100000),
            Arbitraries.integers().between(0, 10000),
            Arbitraries.doubles().between(1.0, 5.0),
            Arbitraries.just(fixedCategoryId),
            Arbitraries.integers().between(0, 5000)
        ).as(Product::new)
        .list()
        .ofMinSize(1)
        .ofMaxSize(50);
    }

    /**
     * Property 20: 市场数据聚合正确性 - 销量分布总和等于总销量
     * 
     * *对于任意* 品类的市场分析，销量分布中各价格区间的销量总和应该等于该品类的总销量。
     */
    @Property(tries = 100)
    @Label("Property 20.1: 销量分布总和等于总销量")
    void salesDistributionSumEqualsTotalSales(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 计算销量分布中的总销量
        int distributionTotalSales = result.salesDistribution().stream()
            .mapToInt(SalesDistributionPoint::totalSales)
            .sum();

        // 验证：销量分布总和应该等于总销量
        assert distributionTotalSales == result.totalSales() :
            String.format("销量分布总和 (%d) 应该等于总销量 (%d)", 
                distributionTotalSales, result.totalSales());
    }

    /**
     * Property 20: 市场数据聚合正确性 - 产品数量分布总和等于总产品数
     * 
     * *对于任意* 品类的市场分析，销量分布中各价格区间的产品数量总和应该等于该品类的总产品数。
     */
    @Property(tries = 100)
    @Label("Property 20.2: 产品数量分布总和等于总产品数")
    void productCountDistributionSumEqualsTotalProducts(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 计算销量分布中的总产品数（只计算有价格的产品）
        int distributionTotalProducts = result.salesDistribution().stream()
            .mapToInt(SalesDistributionPoint::productCount)
            .sum();

        // 计算有价格的产品数
        long productsWithPrice = products.stream()
            .filter(p -> p.categoryId().equals(categoryId) && p.price() != null)
            .count();

        // 验证：产品数量分布总和应该等于有价格的产品数
        assert distributionTotalProducts == productsWithPrice :
            String.format("产品数量分布总和 (%d) 应该等于有价格的产品数 (%d)", 
                distributionTotalProducts, productsWithPrice);
    }

    /**
     * Property 20: 市场数据聚合正确性 - 竞争强度评分在有效范围内
     * 
     * *对于任意* 品类的市场分析，竞争强度评分应该在 0 到 1 之间。
     */
    @Property(tries = 100)
    @Label("Property 20.3: 竞争强度评分在有效范围内 (0-1)")
    void competitionScoreInValidRange(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 验证：竞争强度评分应该在 0 到 1 之间
        assert result.competitionScore() >= 0.0 && result.competitionScore() <= 1.0 :
            String.format("竞争强度评分 (%f) 应该在 0 到 1 之间", result.competitionScore());
    }

    /**
     * Property 20: 市场数据聚合正确性 - 平均价格计算正确
     * 
     * *对于任意* 品类的市场分析，平均价格应该等于所有产品价格的算术平均值。
     */
    @Property(tries = 100)
    @Label("Property 20.4: 平均价格计算正确")
    void averagePriceCalculatedCorrectly(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 手动计算平均价格
        List<BigDecimal> prices = products.stream()
            .filter(p -> p.categoryId().equals(categoryId) && p.price() != null)
            .map(Product::price)
            .toList();

        if (prices.isEmpty()) {
            assert result.avgPrice().compareTo(BigDecimal.ZERO) == 0 :
                "没有产品时平均价格应该为 0";
            return;
        }

        BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedAvg = sum.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

        // 验证：平均价格应该正确计算
        assert result.avgPrice().compareTo(expectedAvg) == 0 :
            String.format("平均价格 (%s) 应该等于预期值 (%s)", 
                result.avgPrice(), expectedAvg);
    }

    /**
     * 混合品类产品列表生成器（包含多个品类的产品）
     */
    @Provide
    Arbitrary<List<Product>> mixedCategoryProducts() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
            Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(500))
                .ofScale(2),
            Arbitraries.integers().between(1, 100000),
            Arbitraries.integers().between(0, 10000),
            Arbitraries.doubles().between(1.0, 5.0),
            Arbitraries.longs().between(1L, 10L),  // 多个品类
            Arbitraries.integers().between(0, 5000)
        ).as(Product::new)
        .list()
        .ofMinSize(1)
        .ofMaxSize(50);
    }

    /**
     * Property 20: 市场数据聚合正确性 - 只聚合指定品类的数据
     * 
     * *对于任意* 品类的市场分析，应该只包含该品类下的产品数据，不包含其他品类的数据。
     */
    @Property(tries = 100)
    @Label("Property 20.5: 只聚合指定品类的数据")
    void onlyAggregatesSpecifiedCategoryData(
        @ForAll("mixedCategoryProducts") List<Product> products
    ) {
        Long targetCategoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(targetCategoryId, products);

        // 手动计算目标品类的产品数
        long expectedProductCount = products.stream()
            .filter(p -> p.categoryId().equals(targetCategoryId))
            .count();

        // 验证：总产品数应该只包含目标品类的产品
        assert result.totalProducts() == expectedProductCount :
            String.format("总产品数 (%d) 应该等于目标品类产品数 (%d)", 
                result.totalProducts(), expectedProductCount);
    }

    /**
     * Property 20: 市场数据聚合正确性 - 总销量计算正确
     * 
     * *对于任意* 品类的市场分析，总销量应该等于该品类下所有产品月销量的总和。
     */
    @Property(tries = 100)
    @Label("Property 20.6: 总销量计算正确")
    void totalSalesCalculatedCorrectly(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 手动计算总销量
        int expectedTotalSales = products.stream()
            .filter(p -> p.categoryId().equals(categoryId))
            .mapToInt(p -> p.monthlySales() != null ? p.monthlySales() : 0)
            .sum();

        // 验证：总销量应该正确计算
        assert result.totalSales() == expectedTotalSales :
            String.format("总销量 (%d) 应该等于预期值 (%d)", 
                result.totalSales(), expectedTotalSales);
    }

    /**
     * Property 20: 市场数据聚合正确性 - 空品类返回空结果
     * 
     * *对于任意* 不存在产品的品类，市场分析应该返回空结果（零值）。
     */
    @Property(tries = 100)
    @Label("Property 20.7: 空品类返回空结果")
    void emptyCategoryReturnsEmptyResult(
        @ForAll("productListForCategory") List<Product> products
    ) {
        // 使用一个不存在的品类 ID
        Long nonExistentCategoryId = 999L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(nonExistentCategoryId, products);

        // 验证：空品类应该返回零值
        assert result.totalProducts() == 0 : "空品类的总产品数应该为 0";
        assert result.totalSales() == 0 : "空品类的总销量应该为 0";
        assert result.competitionScore() == 0.0 : "空品类的竞争强度应该为 0";
        assert result.avgPrice().compareTo(BigDecimal.ZERO) == 0 : "空品类的平均价格应该为 0";
        assert result.salesDistribution().isEmpty() : "空品类的销量分布应该为空";
    }

    /**
     * Property 20: 市场数据聚合正确性 - 产品按价格区间正确分组
     * 
     * *对于任意* 品类的市场分析，每个产品应该被分配到正确的价格区间。
     */
    @Property(tries = 100)
    @Label("Property 20.8: 产品按价格区间正确分组")
    void productsGroupedByPriceRangeCorrectly(
        @ForAll("productListForCategory") List<Product> products
    ) {
        Long categoryId = 1L;
        
        MarketAnalysisResult result = marketAnalysisService.analyzeMarket(categoryId, products);

        // 手动计算每个价格区间的产品数
        Map<String, Long> expectedCounts = products.stream()
            .filter(p -> p.categoryId().equals(categoryId) && p.price() != null)
            .collect(Collectors.groupingBy(
                p -> getPriceRange(p.price()),
                Collectors.counting()
            ));

        // 验证每个价格区间的产品数
        for (SalesDistributionPoint point : result.salesDistribution()) {
            long expected = expectedCounts.getOrDefault(point.priceRange(), 0L);
            assert point.productCount() == expected :
                String.format("价格区间 %s 的产品数 (%d) 应该等于预期值 (%d)", 
                    point.priceRange(), point.productCount(), expected);
        }
    }

    private String getPriceRange(BigDecimal price) {
        double p = price.doubleValue();
        if (p < 10) return "0-10";
        if (p < 25) return "10-25";
        if (p < 50) return "25-50";
        if (p < 100) return "50-100";
        return "100+";
    }
}
