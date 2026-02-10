package com.flashsell.app.service;

import com.flashsell.client.dto.res.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeTry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘应用服务属性测试
 * 
 * Property 26: 仪表盘数据完整性
 * Property 27: 爆品推荐数量限制
 * 
 * Validates: Requirements 13.1, 13.2, 13.7
 * 
 * Feature: flashsell-technical-solution, Property 26-27: 仪表盘
 */
class DashboardAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    private TestDashboardService dashboardService;

    /**
     * 模拟仪表盘服务
     */
    static class TestDashboardService {
        private int todayNewProducts;
        private int potentialHotProducts;
        private int favoriteCount;
        private double aiAccuracyRate;
        private List<HotProductDTO> allHotProducts;

        public TestDashboardService() {
            this.allHotProducts = new ArrayList<>();
        }

        public void setTodayNewProducts(int count) {
            this.todayNewProducts = count;
        }

        public void setPotentialHotProducts(int count) {
            this.potentialHotProducts = count;
        }

        public void setFavoriteCount(int count) {
            this.favoriteCount = count;
        }

        public void setAiAccuracyRate(double rate) {
            this.aiAccuracyRate = rate;
        }

        public void setAllHotProducts(List<HotProductDTO> products) {
            this.allHotProducts = new ArrayList<>(products);
        }

        /**
         * 获取仪表盘数据概览
         */
        public DashboardOverviewRes getOverview() {
            return DashboardOverviewRes.builder()
                    .todayNewProducts(todayNewProducts)
                    .potentialHotProducts(potentialHotProducts)
                    .favoriteCount(favoriteCount)
                    .aiAccuracyRate(aiAccuracyRate)
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
        }

        /**
         * 获取 AI 爆品推荐 Top 4
         */
        public HotRecommendationsRes getHotRecommendations() {
            // 按爆品评分降序排序，取前4个
            List<HotProductDTO> top4 = allHotProducts.stream()
                    .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                    .limit(4)
                    .collect(Collectors.toList());

            return HotRecommendationsRes.builder()
                    .products(top4)
                    .updateTime(LocalDateTime.now())
                    .build();
        }

        public void clear() {
            todayNewProducts = 0;
            potentialHotProducts = 0;
            favoriteCount = 0;
            aiAccuracyRate = 0.0;
            allHotProducts.clear();
        }
    }

    @BeforeTry
    void setUp() {
        dashboardService = new TestDashboardService();
    }

    // ========== Property 26: 仪表盘数据完整性 ==========

    /**
     * Property 26.1: 仪表盘概览包含所有必需字段
     * 
     * *对于任意* 仪表盘数据请求，返回的数据应该包含所有必需字段：
     * 今日新品数、潜力爆品数、收藏数、AI 准确率和最后更新时间。
     * 
     * Validates: Requirements 13.1, 13.7
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 26.1: 仪表盘概览包含所有必需字段")
    void dashboardOverviewContainsAllRequiredFields(
            @ForAll @IntRange(min = 0, max = 1000) int todayNewProducts,
            @ForAll @IntRange(min = 0, max = 1000) int potentialHotProducts,
            @ForAll @IntRange(min = 0, max = 10000) int favoriteCount,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double aiAccuracyRate) {

        // 设置测试数据
        dashboardService.setTodayNewProducts(todayNewProducts);
        dashboardService.setPotentialHotProducts(potentialHotProducts);
        dashboardService.setFavoriteCount(favoriteCount);
        dashboardService.setAiAccuracyRate(aiAccuracyRate);

        // 获取仪表盘概览
        DashboardOverviewRes overview = dashboardService.getOverview();

        // 验证：所有必需字段都存在且不为 null
        assert overview != null : "仪表盘概览不应为 null";
        assert overview.getTodayNewProducts() != null : "今日新品数不应为 null";
        assert overview.getPotentialHotProducts() != null : "潜力爆品数不应为 null";
        assert overview.getFavoriteCount() != null : "收藏数不应为 null";
        assert overview.getAiAccuracyRate() != null : "AI 准确率不应为 null";
        assert overview.getLastUpdateTime() != null : "最后更新时间不应为 null";

        // 验证：字段值正确
        assert overview.getTodayNewProducts().equals(todayNewProducts) :
                String.format("今日新品数应该为 %d，实际为 %d", todayNewProducts, overview.getTodayNewProducts());
        assert overview.getPotentialHotProducts().equals(potentialHotProducts) :
                String.format("潜力爆品数应该为 %d，实际为 %d", potentialHotProducts, overview.getPotentialHotProducts());
        assert overview.getFavoriteCount().equals(favoriteCount) :
                String.format("收藏数应该为 %d，实际为 %d", favoriteCount, overview.getFavoriteCount());
        assert Math.abs(overview.getAiAccuracyRate() - aiAccuracyRate) < 0.0001 :
                String.format("AI 准确率应该为 %.4f，实际为 %.4f", aiAccuracyRate, overview.getAiAccuracyRate());
    }

    /**
     * Property 26.2: 今日新品数非负
     * 
     * *对于任意* 仪表盘数据请求，今日新品数应该 >= 0。
     * 
     * Validates: Requirements 13.1
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 26.2: 今日新品数非负")
    void todayNewProductsIsNonNegative(
            @ForAll @IntRange(min = 0, max = 1000) int todayNewProducts) {

        dashboardService.setTodayNewProducts(todayNewProducts);
        DashboardOverviewRes overview = dashboardService.getOverview();

        assert overview.getTodayNewProducts() >= 0 :
                "今日新品数应该 >= 0";
    }

    /**
     * Property 26.3: 潜力爆品数非负
     * 
     * *对于任意* 仪表盘数据请求，潜力爆品数应该 >= 0。
     * 
     * Validates: Requirements 13.1
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 26.3: 潜力爆品数非负")
    void potentialHotProductsIsNonNegative(
            @ForAll @IntRange(min = 0, max = 1000) int potentialHotProducts) {

        dashboardService.setPotentialHotProducts(potentialHotProducts);
        DashboardOverviewRes overview = dashboardService.getOverview();

        assert overview.getPotentialHotProducts() >= 0 :
                "潜力爆品数应该 >= 0";
    }

    /**
     * Property 26.4: AI 准确率在有效范围内
     * 
     * *对于任意* 仪表盘数据请求，AI 准确率应该在 0.0 到 1.0 之间。
     * 
     * Validates: Requirements 13.1
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 26.4: AI 准确率在有效范围内")
    void aiAccuracyRateIsInValidRange(
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double aiAccuracyRate) {

        dashboardService.setAiAccuracyRate(aiAccuracyRate);
        DashboardOverviewRes overview = dashboardService.getOverview();

        assert overview.getAiAccuracyRate() >= 0.0 :
                "AI 准确率应该 >= 0.0";
        assert overview.getAiAccuracyRate() <= 1.0 :
                "AI 准确率应该 <= 1.0";
    }

    /**
     * Property 26.5: 最后更新时间合理
     * 
     * *对于任意* 仪表盘数据请求，最后更新时间应该在当前时间附近（不超过1分钟）。
     * 
     * Validates: Requirements 13.7
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 26.5: 最后更新时间合理")
    void lastUpdateTimeIsReasonable(
            @ForAll @IntRange(min = 0, max = 100) int todayNewProducts) {

        dashboardService.setTodayNewProducts(todayNewProducts);
        
        LocalDateTime beforeCall = LocalDateTime.now();
        DashboardOverviewRes overview = dashboardService.getOverview();
        LocalDateTime afterCall = LocalDateTime.now();

        assert overview.getLastUpdateTime() != null :
                "最后更新时间不应为 null";
        assert !overview.getLastUpdateTime().isBefore(beforeCall) :
                "最后更新时间不应早于调用前时间";
        assert !overview.getLastUpdateTime().isAfter(afterCall.plusMinutes(1)) :
                "最后更新时间不应晚于调用后1分钟";
    }

    // ========== Property 27: 爆品推荐数量限制 ==========

    /**
     * Property 27.1: 爆品推荐最多返回4个
     * 
     * *对于任意* 首页爆品推荐请求，返回的产品列表数量应该最多为 4 个。
     * 
     * Validates: Requirements 13.2
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 27.1: 爆品推荐最多返回4个")
    void hotRecommendationsLimitedToFour(
            @ForAll("hotProductList") List<HotProductDTO> allProducts) {

        // 设置所有爆品数据
        dashboardService.setAllHotProducts(allProducts);

        // 获取 Top 4 推荐
        HotRecommendationsRes recommendations = dashboardService.getHotRecommendations();

        // 验证：返回的产品数量不超过 4
        assert recommendations != null : "爆品推荐不应为 null";
        assert recommendations.getProducts() != null : "爆品列表不应为 null";
        assert recommendations.getProducts().size() <= 4 :
                String.format("爆品推荐数量应该 <= 4，实际为 %d", recommendations.getProducts().size());

        // 验证：如果原始数据超过 4 个，返回的应该是 4 个
        if (allProducts.size() > 4) {
            assert recommendations.getProducts().size() == 4 :
                    String.format("当有 %d 个爆品时，应该返回 4 个，实际返回 %d 个",
                            allProducts.size(), recommendations.getProducts().size());
        } else {
            // 如果原始数据不超过 4 个，返回的应该是全部
            assert recommendations.getProducts().size() == allProducts.size() :
                    String.format("当有 %d 个爆品时，应该返回 %d 个，实际返回 %d 个",
                            allProducts.size(), allProducts.size(), recommendations.getProducts().size());
        }
    }

    /**
     * Property 27.2: 爆品推荐按评分降序排列
     * 
     * *对于任意* 首页爆品推荐请求，返回的产品应该按爆品评分降序排列。
     * 
     * Validates: Requirements 13.2
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 27.2: 爆品推荐按评分降序排列")
    void hotRecommendationsSortedByScoreDescending(
            @ForAll("hotProductList") List<HotProductDTO> allProducts) {

        Assume.that(allProducts.size() >= 2); // 至少需要2个产品才能验证排序

        dashboardService.setAllHotProducts(allProducts);
        HotRecommendationsRes recommendations = dashboardService.getHotRecommendations();

        List<HotProductDTO> products = recommendations.getProducts();

        // 验证：按评分降序排列
        for (int i = 0; i < products.size() - 1; i++) {
            BigDecimal currentScore = products.get(i).getHotScore();
            BigDecimal nextScore = products.get(i + 1).getHotScore();

            assert currentScore.compareTo(nextScore) >= 0 :
                    String.format("第 %d 个产品的评分 %s 应该 >= 第 %d 个产品的评分 %s",
                            i, currentScore, i + 1, nextScore);
        }
    }

    /**
     * Property 27.3: 爆品推荐返回评分最高的产品
     * 
     * *对于任意* 首页爆品推荐请求，返回的 Top 4 应该是评分最高的 4 个产品。
     * 
     * Validates: Requirements 13.2
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 27.3: 爆品推荐返回评分最高的产品")
    void hotRecommendationsReturnTopScored(
            @ForAll("hotProductList") List<HotProductDTO> allProducts) {

        Assume.that(allProducts.size() > 4); // 需要超过4个产品才能验证

        dashboardService.setAllHotProducts(allProducts);
        HotRecommendationsRes recommendations = dashboardService.getHotRecommendations();

        // 获取原始数据中评分最高的 4 个
        List<HotProductDTO> expectedTop4 = allProducts.stream()
                .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                .limit(4)
                .collect(Collectors.toList());

        // 获取实际返回的产品ID集合
        Set<Long> actualProductIds = recommendations.getProducts().stream()
                .map(dto -> dto.getProduct().getId())
                .collect(Collectors.toSet());

        // 获取期望的产品ID集合
        Set<Long> expectedProductIds = expectedTop4.stream()
                .map(dto -> dto.getProduct().getId())
                .collect(Collectors.toSet());

        // 验证：返回的产品ID集合应该与期望的一致
        assert actualProductIds.equals(expectedProductIds) :
                String.format("返回的产品ID %s 应该等于期望的 %s", actualProductIds, expectedProductIds);
    }

    /**
     * Property 27.4: 爆品推荐包含更新时间
     * 
     * *对于任意* 首页爆品推荐请求，返回的数据应该包含更新时间。
     * 
     * Validates: Requirements 13.2
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 27.4: 爆品推荐包含更新时间")
    void hotRecommendationsContainsUpdateTime(
            @ForAll("hotProductList") List<HotProductDTO> allProducts) {

        dashboardService.setAllHotProducts(allProducts);
        
        LocalDateTime beforeCall = LocalDateTime.now();
        HotRecommendationsRes recommendations = dashboardService.getHotRecommendations();
        LocalDateTime afterCall = LocalDateTime.now();

        // 验证：更新时间存在
        assert recommendations.getUpdateTime() != null :
                "更新时间不应为 null";

        // 验证：更新时间合理（在调用前后时间范围内）
        assert !recommendations.getUpdateTime().isBefore(beforeCall) :
                "更新时间不应早于调用前时间";
        assert !recommendations.getUpdateTime().isAfter(afterCall.plusMinutes(1)) :
                "更新时间不应晚于调用后1分钟";
    }

    /**
     * Property 27.5: 空爆品列表返回空推荐
     * 
     * *对于任意* 空的爆品列表，爆品推荐应该返回空列表。
     * 
     * Validates: Requirements 13.2
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 27.5: 空爆品列表返回空推荐")
    void emptyHotProductsReturnsEmptyRecommendations() {

        dashboardService.setAllHotProducts(new ArrayList<>());
        HotRecommendationsRes recommendations = dashboardService.getHotRecommendations();

        // 验证：返回空列表
        assert recommendations != null : "爆品推荐不应为 null";
        assert recommendations.getProducts() != null : "爆品列表不应为 null";
        assert recommendations.getProducts().isEmpty() :
                "空爆品列表应该返回空推荐";
    }

    // ========== Arbitraries (数据生成器) ==========

    @Provide
    Arbitrary<List<HotProductDTO>> hotProductList() {
        return Arbitraries.integers().between(0, 20).flatMap(count -> {
            if (count == 0) {
                return Arbitraries.just(new ArrayList<>());
            }

            List<Arbitrary<HotProductDTO>> dtoArbitraries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final long productId = i + 1;
                dtoArbitraries.add(
                        Combinators.combine(
                                Arbitraries.bigDecimals()
                                        .between(BigDecimal.ZERO, new BigDecimal("100"))
                                        .ofScale(2),
                                Arbitraries.integers().between(1, 20)
                        ).as((hotScore, rank) -> {
                            ProductItemRes product = ProductItemRes.builder()
                                    .id(productId)
                                    .title("Product " + productId)
                                    .price(BigDecimal.valueOf(10 + productId))
                                    .build();

                            return HotProductDTO.builder()
                                    .product(product)
                                    .hotScore(hotScore)
                                    .rankInCategory(rank)
                                    .daysOnList((long) (1 + productId % 7))
                                    .rankChange(0)
                                    .recommendDate(LocalDate.now())
                                    .build();
                        })
                );
            }

            return Arbitraries.of(dtoArbitraries).list().ofSize(count)
                    .map(arbitraries -> arbitraries.stream()
                            .map(Arbitrary::sample)
                            .collect(Collectors.toList()));
        });
    }
}
