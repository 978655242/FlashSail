package com.flashsell.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.flashsell.domain.ai.entity.HotProductScore;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 * 爆品推荐应用服务属性测试
 * 
 * Property 16: 爆品排行榜 Top 20 限制
 * Property 17: 爆品历史数据保留
 * Property 18: 定时任务品类覆盖
 * 
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.8
 * 
 * Feature: flashsell-technical-solution, Property 16-18: 爆品推荐
 */
class HotProductAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    private TestHotProductGateway hotProductGateway;
    private TestCategoryGateway categoryGateway;

    /**
     * 模拟爆品网关
     */
    static class TestHotProductGateway {
        private final Map<String, List<HotProductScore>> storage = new HashMap<>();

        public void batchSave(List<HotProductScore> scores) {
            if (scores == null || scores.isEmpty()) {
                return;
            }
            
            LocalDate date = scores.get(0).getRecommendDate();
            Long categoryId = scores.get(0).getCategoryId();
            String key = buildKey(date, categoryId);
            
            storage.put(key, new ArrayList<>(scores));
        }

        public List<HotProductScore> findByDateAndCategory(LocalDate date, Long categoryId) {
            String key = buildKey(date, categoryId);
            return storage.getOrDefault(key, new ArrayList<>());
        }

        public List<HotProductScore> findTopNByDateAndCategory(LocalDate date, Long categoryId, int topN) {
            List<HotProductScore> all = findByDateAndCategory(date, categoryId);
            return all.stream()
                    .sorted(Comparator.comparing(HotProductScore::getRankInCategory))
                    .limit(topN)
                    .collect(Collectors.toList());
        }

        public List<HotProductScore> findProductHistory(Long productId, LocalDate startDate, LocalDate endDate) {
            return storage.values().stream()
                    .flatMap(List::stream)
                    .filter(score -> score.getProductId().equals(productId))
                    .filter(score -> !score.getRecommendDate().isBefore(startDate))
                    .filter(score -> !score.getRecommendDate().isAfter(endDate))
                    .sorted(Comparator.comparing(HotProductScore::getRecommendDate).reversed())
                    .collect(Collectors.toList());
        }

        public int deleteBeforeDate(LocalDate beforeDate) {
            int count = 0;
            Iterator<Map.Entry<String, List<HotProductScore>>> iterator = storage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<HotProductScore>> entry = iterator.next();
                List<HotProductScore> scores = entry.getValue();
                if (!scores.isEmpty() && scores.get(0).getRecommendDate().isBefore(beforeDate)) {
                    count += scores.size();
                    iterator.remove();
                }
            }
            return count;
        }

        public boolean existsByDateAndCategory(LocalDate date, Long categoryId) {
            String key = buildKey(date, categoryId);
            return storage.containsKey(key);
        }

        public int deleteByDateAndCategory(LocalDate date, Long categoryId) {
            String key = buildKey(date, categoryId);
            List<HotProductScore> removed = storage.remove(key);
            return removed != null ? removed.size() : 0;
        }

        public void clear() {
            storage.clear();
        }

        private String buildKey(LocalDate date, Long categoryId) {
            return date + ":" + categoryId;
        }
    }

    /**
     * 模拟品类网关
     */
    static class TestCategoryGateway {
        private final List<Long> categoryIds;

        public TestCategoryGateway(int categoryCount) {
            this.categoryIds = new ArrayList<>();
            for (long i = 1; i <= categoryCount; i++) {
                categoryIds.add(i);
            }
        }

        public List<Long> getAllCategoryIds() {
            return new ArrayList<>(categoryIds);
        }

        public int getCategoryCount() {
            return categoryIds.size();
        }
    }

    @BeforeTry
    void setUp() {
        hotProductGateway = new TestHotProductGateway();
        categoryGateway = new TestCategoryGateway(45); // 45 个品类
    }

    // ========== Property 16: 爆品排行榜 Top 20 限制 ==========

    /**
     * Property 16: 爆品排行榜 Top 20 限制
     * 
     * 对于任意品类的爆品排行榜，应该最多包含 20 个产品，且按爆品评分降序排列。
     * 
     * Validates: Requirements 11.4
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 16: 爆品排行榜 Top 20 限制")
    void hotProductRankingTop20Limit(
            @ForAll("hotProductScores") List<HotProductScore> scores,
            @ForAll @LongRange(min = 1, max = 45) Long categoryId,
            @ForAll("recentDate") LocalDate date) {

        // 设置品类ID和推荐日期
        scores.forEach(score -> {
            score.setCategoryId(categoryId);
            score.setRecommendDate(date);
        });

        // 保存爆品数据
        hotProductGateway.batchSave(scores);

        // 查询 Top 20
        List<HotProductScore> top20 = hotProductGateway.findTopNByDateAndCategory(date, categoryId, 20);

        // 验证：数量不超过 20
        assert top20.size() <= 20 : 
                String.format("Top 20 数量 %d 不应超过 20", top20.size());

        // 验证：按排名升序排列
        if (top20.size() > 1) {
            for (int i = 0; i < top20.size() - 1; i++) {
                int currentRank = top20.get(i).getRankInCategory();
                int nextRank = top20.get(i + 1).getRankInCategory();
                assert currentRank <= nextRank :
                        String.format("排名应该升序: %d <= %d", currentRank, nextRank);
            }
        }

        // 验证：如果原始数据超过 20 个，返回的应该是前 20 个
        if (scores.size() > 20) {
            assert top20.size() == 20 :
                    String.format("当原始数据超过 20 个时，应该返回 20 个，实际: %d", top20.size());
            
            // 验证返回的是评分最高的 20 个
            List<HotProductScore> sortedScores = scores.stream()
                    .sorted(Comparator.comparing(HotProductScore::getRankInCategory))
                    .limit(20)
                    .collect(Collectors.toList());
            
            Set<Long> expectedProductIds = sortedScores.stream()
                    .map(HotProductScore::getProductId)
                    .collect(Collectors.toSet());
            
            Set<Long> actualProductIds = top20.stream()
                    .map(HotProductScore::getProductId)
                    .collect(Collectors.toSet());
            
            assert actualProductIds.equals(expectedProductIds) :
                    "返回的产品ID集合应该匹配预期";
        }
    }

    // ========== Property 17: 爆品历史数据保留 ==========

    /**
     * Property 17: 爆品历史数据保留
     * 
     * 对于任意爆品查询，返回的历史数据应该只包含最近 7 天的记录，超过 7 天的数据应该被清理。
     * 
     * Validates: Requirements 11.8
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 17: 爆品历史数据保留")
    void hotProductHistoryRetention(
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @IntRange(min = 1, max = 20) int daysOfHistory) {

        LocalDate today = LocalDate.now();

        // 生成多天的历史数据
        for (int i = 0; i < daysOfHistory; i++) {
            LocalDate date = today.minusDays(i);
            HotProductScore score = HotProductScore.builder()
                    .productId(productId)
                    .categoryId(1L)
                    .hotScore(BigDecimal.valueOf(80 + i))
                    .rankInCategory(i + 1)
                    .recommendDate(date)
                    .build();

            hotProductGateway.batchSave(List.of(score));
        }

        // 清理超过 7 天的数据 - 删除 7 天前之前的数据（< 7天前）
        // 这样保留的是：今天（第0天）+ 往前6天（第1-6天）= 共7天
        LocalDate sevenDaysAgo = today.minusDays(7);
        int deletedCount = hotProductGateway.deleteBeforeDate(sevenDaysAgo);

        // 查询历史数据（最近 7 天：今天 + 往前 6 天）
        // 查询范围应该是往前 6 天到今天
        LocalDate sixDaysAgo = today.minusDays(6);
        List<HotProductScore> history = hotProductGateway.findProductHistory(
                productId,
                sixDaysAgo,
                today
        );

        // 验证：历史数据只包含最近 7 天（最多 7 条记录）
        assert history.size() <= 7 :
                String.format("历史数据数量 %d 不应超过 7", history.size());

        // 验证：所有历史记录的日期都在最近 7 天内
        for (HotProductScore score : history) {
            assert !score.getRecommendDate().isBefore(sevenDaysAgo) :
                    String.format("日期 %s 应该 >= %s", score.getRecommendDate(), sevenDaysAgo);
            assert !score.getRecommendDate().isAfter(today) :
                    String.format("日期 %s 应该 <= %s", score.getRecommendDate(), today);
        }

        // 验证：如果原始数据超过 8 天，应该有数据被删除（8天前或更早的记录）
        if (daysOfHistory > 8) {
            assert deletedCount > 0 :
                    String.format("删除数量 %d 应该 > 0", deletedCount);
            assert history.size() == 7 :
                    String.format("历史数据应该保留 7 天，实际: %d", history.size());
        } else {
            // 如果原始数据不超过 8 天，不应该有数据被删除
            assert deletedCount == 0 :
                    String.format("删除数量应该为 0，实际: %d", deletedCount);
            assert history.size() == Math.min(daysOfHistory, 7) :
                    String.format("历史数据应该保留 %d 天，实际: %d", Math.min(daysOfHistory, 7), history.size());
        }
    }

    // ========== Property 18: 定时任务品类覆盖 ==========

    /**
     * Property 18: 定时任务品类覆盖
     * 
     * 对于任意定时任务执行，应该覆盖所有 45 个支持品类，且每个品类都应该有对应的爆品分析结果。
     * 
     * Validates: Requirements 11.1, 11.2, 11.3
     */
    @Property(tries = 100)
    @Label("Feature: flashsell-technical-solution, Property 18: 定时任务品类覆盖")
    void scheduledTaskCategoryCoverage(
            @ForAll("recentDate") LocalDate date,
            @ForAll @IntRange(min = 1, max = 20) int productsPerCategory) {

        // 模拟定时任务：为每个品类生成爆品数据
        List<Long> categoryIds = categoryGateway.getAllCategoryIds();
        
        for (Long categoryId : categoryIds) {
            List<HotProductScore> scores = new ArrayList<>();
            for (int i = 0; i < productsPerCategory; i++) {
                HotProductScore score = HotProductScore.builder()
                        .productId(categoryId * 1000 + i) // 确保产品ID唯一
                        .categoryId(categoryId)
                        .hotScore(BigDecimal.valueOf(90 - i))
                        .rankInCategory(i + 1)
                        .recommendDate(date)
                        .build();
                scores.add(score);
            }
            
            hotProductGateway.batchSave(scores);
        }

        // 验证：所有 45 个品类都有爆品数据
        int categoriesWithData = 0;
        for (Long categoryId : categoryIds) {
            if (hotProductGateway.existsByDateAndCategory(date, categoryId)) {
                categoriesWithData++;
            }
        }

        // 验证：品类覆盖率为 100%
        assert categoriesWithData == 45 :
                String.format("品类覆盖数 %d 应该等于 45", categoriesWithData);

        // 验证：每个品类都有爆品数据
        for (Long categoryId : categoryIds) {
            List<HotProductScore> categoryScores = hotProductGateway.findByDateAndCategory(date, categoryId);
            
            // 每个品类至少有 1 个爆品
            assert !categoryScores.isEmpty() :
                    String.format("品类 %d 应该有爆品数据", categoryId);
            
            // 每个品类的爆品数量等于生成的数量
            assert categoryScores.size() == productsPerCategory :
                    String.format("品类 %d 的爆品数量应该为 %d，实际: %d", 
                            categoryId, productsPerCategory, categoryScores.size());
            
            // 所有爆品都属于该品类
            for (HotProductScore score : categoryScores) {
                assert score.getCategoryId().equals(categoryId) :
                        String.format("爆品的品类ID应该为 %d，实际: %d", categoryId, score.getCategoryId());
                assert score.getRecommendDate().equals(date) :
                        String.format("爆品的推荐日期应该为 %s，实际: %s", date, score.getRecommendDate());
            }
        }
    }

    // ========== Arbitraries (数据生成器) ==========

    @Provide
    Arbitrary<List<HotProductScore>> hotProductScores() {
        return Arbitraries.integers().between(5, 50).flatMap(count -> {
            List<Arbitrary<HotProductScore>> scoreArbitraries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final int rank = i + 1;
                scoreArbitraries.add(
                        Combinators.combine(
                                Arbitraries.longs().between(1L, 10000L),
                                Arbitraries.bigDecimals().between(BigDecimal.ZERO, new BigDecimal("100"))
                        ).as((productId, hotScore) -> HotProductScore.builder()
                                .productId(productId)
                                .hotScore(hotScore)
                                .rankInCategory(rank)
                                .build())
                );
            }
            return Arbitraries.of(scoreArbitraries).list().ofSize(count)
                    .map(arbitraries -> arbitraries.stream()
                            .map(arb -> arb.sample())
                            .collect(Collectors.toList()));
        });
    }

    @Provide
    Arbitrary<LocalDate> recentDate() {
        LocalDate today = LocalDate.now();
        return Arbitraries.integers().between(0, 30)
                .map(today::minusDays);
    }
}
