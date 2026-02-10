package com.flashsell.app.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.flashsell.app.assembler.ProductAssembler;
import com.flashsell.client.dto.res.CategoryRes;
import com.flashsell.client.dto.res.DashboardOverviewRes;
import com.flashsell.client.dto.res.HotKeywordDTO;
import com.flashsell.client.dto.res.HotKeywordsRes;
import com.flashsell.client.dto.res.HotProductDTO;
import com.flashsell.client.dto.res.HotRecommendationsRes;
import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.client.dto.res.RecentActivityRes;
import com.flashsell.client.dto.res.SearchHistoryDTO;
import com.flashsell.client.dto.res.TrendingCategoriesRes;
import com.flashsell.client.dto.res.TrendingCategoryDTO;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.gateway.HotProductGateway;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.favorite.gateway.FavoriteGateway;
import com.flashsell.domain.history.entity.BrowseHistory;
import com.flashsell.domain.history.entity.HotKeyword;
import com.flashsell.domain.history.entity.SearchHistory;
import com.flashsell.domain.history.gateway.BrowseHistoryGateway;
import com.flashsell.domain.history.gateway.HotKeywordGateway;
import com.flashsell.domain.history.gateway.SearchHistoryGateway;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 仪表盘应用服务
 * 
 * Requirements: 13.1, 13.2, 13.3, 13.4, 13.5
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardAppService {

    private final ProductGateway productGateway;
    private final FavoriteGateway favoriteGateway;
    private final HotProductGateway hotProductGateway;
    private final SearchHistoryGateway searchHistoryGateway;
    private final BrowseHistoryGateway browseHistoryGateway;
    private final HotKeywordGateway hotKeywordGateway;
    private final CategoryGateway categoryGateway;
    private final ProductAssembler productAssembler;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_OVERVIEW = "dashboard:overview";
    private static final String CACHE_KEY_HOT_RECOMMENDATIONS = "dashboard:hot_recommendations";
    private static final String CACHE_KEY_TRENDING_CATEGORIES = "dashboard:trending_categories";
    private static final String CACHE_KEY_HOT_KEYWORDS = "dashboard:hot_keywords";
    private static final String CACHE_KEY_RECENT_ACTIVITY = "user:recent_activity:";

    /**
     * 获取仪表盘数据概览
     * 
     * Requirements: 13.1, 13.7
     */
    public DashboardOverviewRes getOverview() {
        // 尝试从缓存获取
        DashboardOverviewRes cached = (DashboardOverviewRes) redisTemplate.opsForValue().get(CACHE_KEY_OVERVIEW);
        if (cached != null) {
            return cached;
        }

        // 计算今日新品发现数（今天创建的产品）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int todayNewProducts = productGateway.countCreatedAfter(todayStart);

        // 计算潜力爆品推荐数（今日爆品总数）
        LocalDate today = LocalDate.now();
        List<HotProductScore> todayHotProducts = hotProductGateway.findByDate(today);
        int potentialHotProducts = todayHotProducts.size();

        // AI 准确率（暂时使用固定值，后续可以根据实际数据计算）
        double aiAccuracyRate = 0.85;

        DashboardOverviewRes overview = DashboardOverviewRes.builder()
                .todayNewProducts(todayNewProducts)
                .potentialHotProducts(potentialHotProducts)
                .favoriteCount(0) // 需要用户ID，在Controller层设置
                .aiAccuracyRate(aiAccuracyRate)
                .lastUpdateTime(LocalDateTime.now())
                .build();

        // 缓存5分钟
        redisTemplate.opsForValue().set(CACHE_KEY_OVERVIEW, overview, Duration.ofMinutes(5));

        return overview;
    }

    /**
     * 获取用户的仪表盘数据概览（包含用户相关数据）
     */
    public DashboardOverviewRes getOverviewForUser(Long userId) {
        DashboardOverviewRes overview = getOverview();
        
        // 设置用户收藏数
        long favoriteCount = favoriteGateway.countByUserId(userId);
        overview.setFavoriteCount((int) favoriteCount);
        
        return overview;
    }

    /**
     * 获取 AI 爆品推荐 Top 4
     * 
     * Requirements: 13.2
     */
    public HotRecommendationsRes getHotRecommendations() {
        // 尝试从缓存获取
        HotRecommendationsRes cached = (HotRecommendationsRes) redisTemplate.opsForValue()
                .get(CACHE_KEY_HOT_RECOMMENDATIONS);
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        List<HotProductScore> hotProducts = hotProductGateway.findByDate(today);

        // 按爆品评分降序排序，取前4个
        List<HotProductDTO> top4 = hotProducts.stream()
                .sorted((a, b) -> b.getHotScore().compareTo(a.getHotScore()))
                .limit(4)
                .map(this::convertToHotProductDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        HotRecommendationsRes result = HotRecommendationsRes.builder()
                .products(top4)
                .updateTime(LocalDateTime.now())
                .build();

        // 缓存30分钟
        redisTemplate.opsForValue().set(CACHE_KEY_HOT_RECOMMENDATIONS, result, Duration.ofMinutes(30));

        return result;
    }

    /**
     * 获取用户最近活动
     * 
     * Requirements: 13.3, 14.3, 14.4
     */
    public RecentActivityRes getRecentActivity(Long userId) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_RECENT_ACTIVITY + userId;
        RecentActivityRes cached = (RecentActivityRes) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 获取最近10条搜索历史
        List<SearchHistory> searchHistoryList = searchHistoryGateway.findRecentByUserId(userId, 10);
        List<SearchHistoryDTO> recentSearches = searchHistoryList.stream()
                .map(this::convertToSearchHistoryDTO)
                .collect(Collectors.toList());

        // 获取最近8个浏览产品
        List<BrowseHistory> browseHistoryList = browseHistoryGateway.findRecentByUserId(userId, 8);
        List<ProductItemRes> recentBrowsed = browseHistoryList.stream()
                .map(bh -> {
                    Product product = productGateway.findByIdDirect(bh.getProductId());
                    return product != null ? productAssembler.toProductItemRes(product) : null;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        RecentActivityRes result = RecentActivityRes.builder()
                .recentSearches(recentSearches)
                .recentBrowsed(recentBrowsed)
                .build();

        // 缓存5分钟
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));

        return result;
    }

    /**
     * 获取热门品类趋势
     * 
     * Requirements: 13.4
     */
    public TrendingCategoriesRes getTrendingCategories() {
        // 尝试从缓存获取
        TrendingCategoriesRes cached = (TrendingCategoriesRes) redisTemplate.opsForValue()
                .get(CACHE_KEY_TRENDING_CATEGORIES);
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);

        // 获取今日爆品数据
        List<HotProductScore> todayHotProducts = hotProductGateway.findByDate(today);
        
        // 按品类分组统计
        Map<Long, List<HotProductScore>> categoryGroups = todayHotProducts.stream()
                .collect(Collectors.groupingBy(HotProductScore::getCategoryId));

        // 计算每个品类的趋势评分
        List<TrendingCategoryDTO> trendingCategories = new ArrayList<>();
        for (Map.Entry<Long, List<HotProductScore>> entry : categoryGroups.entrySet()) {
            Long categoryId = entry.getKey();
            List<HotProductScore> products = entry.getValue();

            Category category = categoryGateway.findById(categoryId).orElse(null);
            if (category == null) {
                continue;
            }

            // 计算趋势评分（基于爆品数量和平均评分）
            double avgScore = products.stream()
                    .mapToDouble(p -> p.getHotScore().doubleValue())
                    .average()
                    .orElse(0.0);
            double trendScore = avgScore * (products.size() / 20.0) * 100;

            // 计算周环比（简化版：比较今日和上周的爆品数量）
            List<HotProductScore> lastWeekProducts = hotProductGateway.findByDateAndCategory(lastWeek, categoryId);
            double weekOverWeek = calculateGrowthRate(lastWeekProducts.size(), products.size());

            TrendingCategoryDTO dto = TrendingCategoryDTO.builder()
                    .category(convertToCategoryRes(category))
                    .trendScore(trendScore)
                    .weekOverWeek(weekOverWeek)
                    .hotProductCount(products.size())
                    .build();

            trendingCategories.add(dto);
        }

        // 按趋势评分降序排序，取前10个
        trendingCategories = trendingCategories.stream()
                .sorted((a, b) -> Double.compare(b.getTrendScore(), a.getTrendScore()))
                .limit(10)
                .collect(Collectors.toList());

        TrendingCategoriesRes result = TrendingCategoriesRes.builder()
                .categories(trendingCategories)
                .build();

        // 缓存1小时
        redisTemplate.opsForValue().set(CACHE_KEY_TRENDING_CATEGORIES, result, Duration.ofHours(1));

        return result;
    }

    /**
     * 获取热门搜索关键词
     * 
     * Requirements: 13.5
     */
    public HotKeywordsRes getHotKeywords() {
        // 尝试从缓存获取
        HotKeywordsRes cached = (HotKeywordsRes) redisTemplate.opsForValue().get(CACHE_KEY_HOT_KEYWORDS);
        if (cached != null) {
            return cached;
        }

        // 获取最新日期的热门关键词（Top 10）
        List<HotKeyword> hotKeywords = hotKeywordGateway.findTopByDate(LocalDate.now(), 10);

        List<HotKeywordDTO> keywords = hotKeywords.stream()
                .map(this::convertToHotKeywordDTO)
                .collect(Collectors.toList());

        HotKeywordsRes result = HotKeywordsRes.builder()
                .keywords(keywords)
                .build();

        // 缓存30分钟
        redisTemplate.opsForValue().set(CACHE_KEY_HOT_KEYWORDS, result, Duration.ofMinutes(30));

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算增长率
     */
    private double calculateGrowthRate(int oldValue, int newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 1.0 : 0.0;
        }
        return (double) (newValue - oldValue) / oldValue;
    }

    /**
     * 转换为 HotProductDTO
     */
    private HotProductDTO convertToHotProductDTO(HotProductScore hotProductScore) {
        Product product = productGateway.findByIdDirect(hotProductScore.getProductId());
        if (product == null) {
            return null;
        }

        // 计算上榜天数（简化版：查询历史记录数）
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<HotProductScore> history = hotProductGateway.findProductHistory(
                hotProductScore.getProductId(),
                sevenDaysAgo,
                LocalDate.now()
        );
        long daysOnList = history.size();

        // 计算排名变化（简化版：与昨天比较）
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<HotProductScore> yesterdayData = hotProductGateway.findByDateAndCategory(
                yesterday,
                hotProductScore.getCategoryId()
        );
        int rankChange = calculateRankChange(hotProductScore, yesterdayData);

        return HotProductDTO.builder()
                .product(productAssembler.toProductItemRes(product))
                .hotScore(hotProductScore.getHotScore())
                .rankInCategory(hotProductScore.getRankInCategory())
                .daysOnList(daysOnList)
                .rankChange(rankChange)
                .recommendDate(hotProductScore.getRecommendDate())
                .build();
    }

    /**
     * 计算排名变化
     */
    private int calculateRankChange(HotProductScore current, List<HotProductScore> yesterdayData) {
        if (yesterdayData == null || yesterdayData.isEmpty()) {
            return 0;
        }

        // 查找昨天的排名
        for (int i = 0; i < yesterdayData.size(); i++) {
            if (yesterdayData.get(i).getProductId().equals(current.getProductId())) {
                int yesterdayRank = i + 1;
                return yesterdayRank - current.getRankInCategory();
            }
        }

        // 昨天不在榜单上，视为新上榜
        return 0;
    }

    /**
     * 转换为 SearchHistoryDTO
     */
    private SearchHistoryDTO convertToSearchHistoryDTO(SearchHistory searchHistory) {
        return SearchHistoryDTO.builder()
                .id(searchHistory.getId())
                .query(searchHistory.getQuery())
                .resultCount(searchHistory.getResultCount())
                .createdAt(searchHistory.getCreatedAt())
                .build();
    }

    /**
     * 转换为 CategoryRes
     */
    private CategoryRes convertToCategoryRes(Category category) {
        return CategoryRes.builder()
                .id(category.getId())
                .name(category.getName())
                .productCount(category.getProductCount())
                .build();
    }

    /**
     * 转换为 HotKeywordDTO
     */
    private HotKeywordDTO convertToHotKeywordDTO(HotKeyword hotKeyword) {
        return HotKeywordDTO.builder()
                .keyword(hotKeyword.getKeyword())
                .searchCount(hotKeyword.getSearchCount())
                .trend(hotKeyword.getTrend())
                .build();
    }
}
