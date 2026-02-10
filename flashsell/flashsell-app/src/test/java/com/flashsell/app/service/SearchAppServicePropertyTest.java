package com.flashsell.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsell.client.dto.req.SearchReq;
import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.client.dto.res.SearchRes;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 搜索应用服务属性测试
 * 
 * Property 5: AI 搜索请求/响应序列化往返
 * Property 6: 搜索筛选条件正确应用
 * Property 7: 搜索缓存一致性
 * 
 * Validates: Requirements 2.3, 2.5, 2.6, 2.7
 * 
 * Feature: flashsell-technical-solution, Property 5-7: AI 搜索
 */
class SearchAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    private ObjectMapper objectMapper;
    private TestSearchCache searchCache;
    private TestSearchService searchService;

    /**
     * 模拟产品实体
     */
    record Product(
            Long id,
            String title,
            String imageUrl,
            BigDecimal currentPrice,
            Integer bsrRank,
            Integer reviewCount,
            Double rating,
            Long categoryId
    ) {}

    /**
     * 模拟搜索缓存
     */
    static class TestSearchCache {
        private final Map<String, SearchRes> cache = new ConcurrentHashMap<>();
        private int hitCount = 0;
        private int missCount = 0;

        public Optional<SearchRes> get(String key) {
            SearchRes result = cache.get(key);
            if (result != null) {
                hitCount++;
                return Optional.of(result);
            }
            missCount++;
            return Optional.empty();
        }

        public void put(String key, SearchRes result) {
            cache.put(key, result);
        }

        public void clear() {
            cache.clear();
            hitCount = 0;
            missCount = 0;
        }

        public int getHitCount() {
            return hitCount;
        }

        public int getMissCount() {
            return missCount;
        }

        public boolean containsKey(String key) {
            return cache.containsKey(key);
        }
    }

    /**
     * 模拟搜索服务
     */
    static class TestSearchService {
        private final TestSearchCache cache;
        private final List<Product> productDatabase;
        private int apiCallCount = 0;

        public TestSearchService(TestSearchCache cache) {
            this.cache = cache;
            this.productDatabase = generateProductDatabase();
        }

        private List<Product> generateProductDatabase() {
            List<Product> products = new ArrayList<>();
            Random random = new Random(42);
            for (long i = 1; i <= 100; i++) {
                products.add(new Product(
                        i,
                        "Product " + i,
                        "https://example.com/image" + i + ".jpg",
                        BigDecimal.valueOf(10 + random.nextDouble() * 990).setScale(2, java.math.RoundingMode.HALF_UP),
                        random.nextInt(100000) + 1,
                        random.nextInt(10000),
                        1.0 + random.nextDouble() * 4.0,
                        (long) (random.nextInt(45) + 1)
                ));
            }
            return products;
        }

        public SearchRes search(SearchReq req) {
            String cacheKey = buildCacheKey(req);

            // 检查缓存
            Optional<SearchRes> cached = cache.get(cacheKey);
            if (cached.isPresent()) {
                return cached.get();
            }

            // 模拟 API 调用
            apiCallCount++;

            // 应用筛选条件
            List<Product> filtered = applyFilters(
                    productDatabase,
                    req.getPriceMin(),
                    req.getPriceMax(),
                    req.getMinRating(),
                    req.getCategoryId()
            );

            // 分页
            int page = req.getPage() != null ? req.getPage() : 1;
            int pageSize = req.getPageSize() != null ? req.getPageSize() : 20;
            List<Product> paged = applyPagination(filtered, page, pageSize);

            // 构建响应
            List<ProductItemRes> items = paged.stream()
                    .map(this::toProductItemRes)
                    .collect(Collectors.toList());

            SearchRes result = SearchRes.builder()
                    .products(items)
                    .total((long) filtered.size())
                    .page(page)
                    .pageSize(pageSize)
                    .hasMore(page * pageSize < filtered.size())
                    .aiSummary("搜索: " + req.getQuery())
                    .dataFreshness("FRESH")
                    .build();

            // 缓存结果
            cache.put(cacheKey, result);

            return result;
        }

        private List<Product> applyFilters(List<Product> products,
                                           BigDecimal priceMin,
                                           BigDecimal priceMax,
                                           Double minRating,
                                           Long categoryId) {
            return products.stream()
                    .filter(p -> {
                        // 价格筛选
                        if (priceMin != null && p.currentPrice() != null
                                && p.currentPrice().compareTo(priceMin) < 0) {
                            return false;
                        }
                        if (priceMax != null && p.currentPrice() != null
                                && p.currentPrice().compareTo(priceMax) > 0) {
                            return false;
                        }
                        // 评分筛选
                        if (minRating != null && p.rating() != null
                                && p.rating() < minRating) {
                            return false;
                        }
                        // 品类筛选
                        if (categoryId != null && !categoryId.equals(p.categoryId())) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        private List<Product> applyPagination(List<Product> products, int page, int pageSize) {
            int start = (page - 1) * pageSize;
            if (start >= products.size()) {
                return new ArrayList<>();
            }
            int end = Math.min(start + pageSize, products.size());
            return products.subList(start, end);
        }

        private ProductItemRes toProductItemRes(Product product) {
            return ProductItemRes.builder()
                    .id(product.id())
                    .title(product.title())
                    .image(product.imageUrl())
                    .price(product.currentPrice())
                    .bsrRank(product.bsrRank())
                    .reviewCount(product.reviewCount())
                    .rating(product.rating())
                    .categoryId(product.categoryId())
                    .build();
        }

        private String buildCacheKey(SearchReq req) {
            StringBuilder sb = new StringBuilder("search:");
            sb.append(req.getQuery().hashCode());
            if (req.getCategoryId() != null) {
                sb.append(":c").append(req.getCategoryId());
            }
            if (req.getPriceMin() != null) {
                sb.append(":pmin").append(req.getPriceMin());
            }
            if (req.getPriceMax() != null) {
                sb.append(":pmax").append(req.getPriceMax());
            }
            if (req.getMinRating() != null) {
                sb.append(":r").append(req.getMinRating());
            }
            sb.append(":p").append(req.getPage());
            sb.append(":s").append(req.getPageSize());
            return sb.toString();
        }

        public int getApiCallCount() {
            return apiCallCount;
        }

        public void resetApiCallCount() {
            apiCallCount = 0;
        }

        public List<Product> getProductDatabase() {
            return productDatabase;
        }
    }

    // ========== Test Setup ==========

    @BeforeTry
    void setUp() {
        objectMapper = new ObjectMapper();
        searchCache = new TestSearchCache();
        searchService = new TestSearchService(searchCache);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<String> validQueries() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<Double> validRatings() {
        return Arbitraries.doubles()
                .between(1.0, 5.0);
    }

    @Provide
    Arbitrary<Long> validCategoryIds() {
        return Arbitraries.longs()
                .between(1L, 45L);
    }

    @Provide
    Arbitrary<SearchReq> validSearchRequests() {
        return Combinators.combine(
                validQueries(),
                Arbitraries.longs().between(1L, 45L).injectNull(0.5),
                validPrices().injectNull(0.5),
                validPrices().injectNull(0.5),
                validRatings().injectNull(0.5),
                Arbitraries.integers().between(1, 10),
                Arbitraries.integers().between(10, 50)
        ).as((query, categoryId, priceMin, priceMax, minRating, page, pageSize) -> {
            // 确保 priceMin <= priceMax
            BigDecimal actualPriceMin = priceMin;
            BigDecimal actualPriceMax = priceMax;
            if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
                actualPriceMin = priceMax;
                actualPriceMax = priceMin;
            }

            return SearchReq.builder()
                    .query(query)
                    .categoryId(categoryId)
                    .priceMin(actualPriceMin)
                    .priceMax(actualPriceMax)
                    .minRating(minRating)
                    .page(page)
                    .pageSize(pageSize)
                    .build();
        });
    }

    // ========== Property 5: AI 搜索请求/响应序列化往返 ==========

    /**
     * Property 5.1: SearchReq 序列化往返一致性
     * 
     * *对于任意* 有效的搜索请求对象，序列化为 JSON 后再反序列化应该得到等价的对象。
     */
    @Property(tries = 100)
    @Label("Property 5.1: SearchReq 序列化往返一致性")
    void searchReqSerializationRoundTrip(
            @ForAll("validSearchRequests") SearchReq original
    ) throws JsonProcessingException {
        // 序列化
        String json = objectMapper.writeValueAsString(original);

        // 反序列化
        SearchReq deserialized = objectMapper.readValue(json, SearchReq.class);

        // 验证等价性
        assert Objects.equals(original.getQuery(), deserialized.getQuery()) :
                "query 应该相等";
        assert Objects.equals(original.getCategoryId(), deserialized.getCategoryId()) :
                "categoryId 应该相等";
        assert compareBigDecimals(original.getPriceMin(), deserialized.getPriceMin()) :
                "priceMin 应该相等";
        assert compareBigDecimals(original.getPriceMax(), deserialized.getPriceMax()) :
                "priceMax 应该相等";
        assert compareDoubles(original.getMinRating(), deserialized.getMinRating()) :
                "minRating 应该相等";
        assert Objects.equals(original.getPage(), deserialized.getPage()) :
                "page 应该相等";
        assert Objects.equals(original.getPageSize(), deserialized.getPageSize()) :
                "pageSize 应该相等";
    }

    /**
     * Property 5.2: SearchRes 序列化往返一致性
     * 
     * *对于任意* 搜索响应对象，序列化为 JSON 后再反序列化应该保持数据完整性。
     */
    @Property(tries = 100)
    @Label("Property 5.2: SearchRes 序列化往返一致性")
    void searchResSerializationRoundTrip(
            @ForAll("validSearchRequests") SearchReq req
    ) throws JsonProcessingException {
        // 执行搜索获取响应
        SearchRes original = searchService.search(req);

        // 序列化
        String json = objectMapper.writeValueAsString(original);

        // 反序列化
        SearchRes deserialized = objectMapper.readValue(json, SearchRes.class);

        // 验证等价性
        assert Objects.equals(original.getTotal(), deserialized.getTotal()) :
                "total 应该相等";
        assert Objects.equals(original.getPage(), deserialized.getPage()) :
                "page 应该相等";
        assert Objects.equals(original.getPageSize(), deserialized.getPageSize()) :
                "pageSize 应该相等";
        assert Objects.equals(original.getHasMore(), deserialized.getHasMore()) :
                "hasMore 应该相等";
        assert Objects.equals(original.getAiSummary(), deserialized.getAiSummary()) :
                "aiSummary 应该相等";
        assert original.getProducts().size() == deserialized.getProducts().size() :
                "products 数量应该相等";
    }

    /**
     * Property 5.3: ProductItemRes 序列化往返一致性
     * 
     * *对于任意* 产品项响应对象，序列化为 JSON 后再反序列化应该保持数据完整性。
     */
    @Property(tries = 100)
    @Label("Property 5.3: ProductItemRes 序列化往返一致性")
    void productItemResSerializationRoundTrip(
            @ForAll("validSearchRequests") SearchReq req
    ) throws JsonProcessingException {
        SearchRes searchRes = searchService.search(req);

        if (searchRes.getProducts().isEmpty()) {
            return; // 跳过空结果
        }

        ProductItemRes original = searchRes.getProducts().get(0);

        // 序列化
        String json = objectMapper.writeValueAsString(original);

        // 反序列化
        ProductItemRes deserialized = objectMapper.readValue(json, ProductItemRes.class);

        // 验证等价性
        assert Objects.equals(original.getId(), deserialized.getId()) :
                "id 应该相等";
        assert Objects.equals(original.getTitle(), deserialized.getTitle()) :
                "title 应该相等";
        assert compareBigDecimals(original.getPrice(), deserialized.getPrice()) :
                "price 应该相等";
        assert Objects.equals(original.getBsrRank(), deserialized.getBsrRank()) :
                "bsrRank 应该相等";
        assert Objects.equals(original.getReviewCount(), deserialized.getReviewCount()) :
                "reviewCount 应该相等";
    }

    // ========== Property 6: 搜索筛选条件正确应用 ==========

    /**
     * Property 6.1: 价格筛选正确应用
     * 
     * *对于任意* 搜索结果和价格筛选条件，过滤后的所有产品价格都应该在指定范围内。
     */
    @Property(tries = 100)
    @Label("Property 6.1: 价格筛选正确应用")
    void priceFilterIsCorrectlyApplied(
            @ForAll("validQueries") String query,
            @ForAll("validPrices") BigDecimal priceMin,
            @ForAll("validPrices") BigDecimal priceMax
    ) {
        // 确保 priceMin <= priceMax
        BigDecimal actualMin = priceMin.min(priceMax);
        BigDecimal actualMax = priceMin.max(priceMax);

        SearchReq req = SearchReq.builder()
                .query(query)
                .priceMin(actualMin)
                .priceMax(actualMax)
                .page(1)
                .pageSize(100)
                .build();

        SearchRes result = searchService.search(req);

        // 验证所有产品价格在范围内
        for (ProductItemRes product : result.getProducts()) {
            if (product.getPrice() != null) {
                assert product.getPrice().compareTo(actualMin) >= 0 :
                        String.format("产品价格 %s 应该 >= %s", product.getPrice(), actualMin);
                assert product.getPrice().compareTo(actualMax) <= 0 :
                        String.format("产品价格 %s 应该 <= %s", product.getPrice(), actualMax);
            }
        }
    }

    /**
     * Property 6.2: 评分筛选正确应用
     * 
     * *对于任意* 搜索结果和评分筛选条件，过滤后的所有产品评分都应该 >= 指定的最低评分。
     */
    @Property(tries = 100)
    @Label("Property 6.2: 评分筛选正确应用")
    void ratingFilterIsCorrectlyApplied(
            @ForAll("validQueries") String query,
            @ForAll("validRatings") Double minRating
    ) {
        SearchReq req = SearchReq.builder()
                .query(query)
                .minRating(minRating)
                .page(1)
                .pageSize(100)
                .build();

        SearchRes result = searchService.search(req);

        // 验证所有产品评分 >= minRating
        for (ProductItemRes product : result.getProducts()) {
            if (product.getRating() != null) {
                assert product.getRating() >= minRating :
                        String.format("产品评分 %s 应该 >= %s", product.getRating(), minRating);
            }
        }
    }

    /**
     * Property 6.3: 品类筛选正确应用
     * 
     * *对于任意* 搜索结果和品类筛选条件，过滤后的所有产品都应该属于指定品类。
     */
    @Property(tries = 100)
    @Label("Property 6.3: 品类筛选正确应用")
    void categoryFilterIsCorrectlyApplied(
            @ForAll("validQueries") String query,
            @ForAll("validCategoryIds") Long categoryId
    ) {
        SearchReq req = SearchReq.builder()
                .query(query)
                .categoryId(categoryId)
                .page(1)
                .pageSize(100)
                .build();

        SearchRes result = searchService.search(req);

        // 验证所有产品属于指定品类
        for (ProductItemRes product : result.getProducts()) {
            if (product.getCategoryId() != null) {
                assert product.getCategoryId().equals(categoryId) :
                        String.format("产品品类 %s 应该等于 %s", product.getCategoryId(), categoryId);
            }
        }
    }

    /**
     * Property 6.4: 组合筛选条件正确应用
     * 
     * *对于任意* 搜索结果和多个筛选条件，过滤后的所有产品都应该满足所有指定的筛选条件。
     */
    @Property(tries = 100)
    @Label("Property 6.4: 组合筛选条件正确应用")
    void combinedFiltersAreCorrectlyApplied(
            @ForAll("validSearchRequests") SearchReq req
    ) {
        SearchRes result = searchService.search(req);

        for (ProductItemRes product : result.getProducts()) {
            // 验证价格筛选
            if (req.getPriceMin() != null && product.getPrice() != null) {
                assert product.getPrice().compareTo(req.getPriceMin()) >= 0 :
                        "产品价格应该 >= priceMin";
            }
            if (req.getPriceMax() != null && product.getPrice() != null) {
                assert product.getPrice().compareTo(req.getPriceMax()) <= 0 :
                        "产品价格应该 <= priceMax";
            }

            // 验证评分筛选
            if (req.getMinRating() != null && product.getRating() != null) {
                assert product.getRating() >= req.getMinRating() :
                        "产品评分应该 >= minRating";
            }

            // 验证品类筛选
            if (req.getCategoryId() != null && product.getCategoryId() != null) {
                assert product.getCategoryId().equals(req.getCategoryId()) :
                        "产品品类应该等于 categoryId";
            }
        }
    }

    // ========== Property 7: 搜索缓存一致性 ==========

    /**
     * Property 7.1: 相同查询返回相同结果
     * 
     * *对于任意* 相同的搜索查询，在缓存有效期内应该返回相同的结果。
     */
    @Property(tries = 100)
    @Label("Property 7.1: 相同查询返回相同结果")
    void sameQueryReturnsSameResult(
            @ForAll("validSearchRequests") SearchReq req
    ) {
        // 第一次搜索
        SearchRes result1 = searchService.search(req);

        // 第二次搜索（应该命中缓存）
        SearchRes result2 = searchService.search(req);

        // 验证结果相同
        assert Objects.equals(result1.getTotal(), result2.getTotal()) :
                "两次搜索的 total 应该相同";
        assert Objects.equals(result1.getPage(), result2.getPage()) :
                "两次搜索的 page 应该相同";
        assert result1.getProducts().size() == result2.getProducts().size() :
                "两次搜索的产品数量应该相同";
    }

    /**
     * Property 7.2: 缓存命中时不重复调用 API
     * 
     * *对于任意* 相同的搜索查询，第二次搜索应该命中缓存，不应重复调用 API。
     */
    @Property(tries = 100)
    @Label("Property 7.2: 缓存命中时不重复调用 API")
    void cacheHitDoesNotCallApi(
            @ForAll("validSearchRequests") SearchReq req
    ) {
        // 清空缓存和计数器
        searchCache.clear();
        searchService.resetApiCallCount();

        // 第一次搜索
        searchService.search(req);
        int callsAfterFirst = searchService.getApiCallCount();

        // 第二次搜索（应该命中缓存）
        searchService.search(req);
        int callsAfterSecond = searchService.getApiCallCount();

        // 验证 API 调用次数
        assert callsAfterFirst == 1 :
                "第一次搜索应该调用 API 一次";
        assert callsAfterSecond == 1 :
                "第二次搜索应该命中缓存，不应增加 API 调用次数";
    }

    /**
     * Property 7.3: 不同查询使用不同缓存
     * 
     * *对于任意* 不同的搜索查询，应该使用不同的缓存键。
     */
    @Property(tries = 100)
    @Label("Property 7.3: 不同查询使用不同缓存")
    void differentQueriesUseDifferentCache(
            @ForAll("validQueries") String query1,
            @ForAll("validQueries") String query2
    ) {
        Assume.that(!query1.equals(query2));

        // 清空缓存和计数器
        searchCache.clear();
        searchService.resetApiCallCount();

        SearchReq req1 = SearchReq.builder()
                .query(query1)
                .page(1)
                .pageSize(20)
                .build();

        SearchReq req2 = SearchReq.builder()
                .query(query2)
                .page(1)
                .pageSize(20)
                .build();

        // 执行两次不同的搜索
        searchService.search(req1);
        searchService.search(req2);

        // 验证 API 调用次数
        assert searchService.getApiCallCount() == 2 :
                "两次不同的搜索应该各调用 API 一次";
    }

    /**
     * Property 7.4: 筛选条件变化导致缓存未命中
     * 
     * *对于任意* 相同查询但不同筛选条件，应该使用不同的缓存键。
     */
    @Property(tries = 100)
    @Label("Property 7.4: 筛选条件变化导致缓存未命中")
    void differentFiltersUseDifferentCache(
            @ForAll("validQueries") String query,
            @ForAll("validPrices") BigDecimal priceMin1,
            @ForAll("validPrices") BigDecimal priceMin2
    ) {
        Assume.that(priceMin1.compareTo(priceMin2) != 0);

        // 清空缓存和计数器
        searchCache.clear();
        searchService.resetApiCallCount();

        SearchReq req1 = SearchReq.builder()
                .query(query)
                .priceMin(priceMin1)
                .page(1)
                .pageSize(20)
                .build();

        SearchReq req2 = SearchReq.builder()
                .query(query)
                .priceMin(priceMin2)
                .page(1)
                .pageSize(20)
                .build();

        // 执行两次搜索
        searchService.search(req1);
        searchService.search(req2);

        // 验证 API 调用次数
        assert searchService.getApiCallCount() == 2 :
                "不同筛选条件的搜索应该各调用 API 一次";
    }

    // ========== Helper Methods ==========

    private boolean compareBigDecimals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }

    private boolean compareDoubles(Double a, Double b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Math.abs(a - b) < 0.0001;
    }
}
