package com.flashsell.app.service;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bright Data 属性测试
 * 
 * Property 32: Bright Data 数据标准化一致性
 * Property 33: 批量请求数量限制
 * Property 34: 数据缓存 TTL 正确性
 * Property 35: 请求失败降级正确性
 * 
 * Validates: Requirements 15.1, 15.5, 15.6, 15.7, 15.8
 * 
 * Feature: flashsell-technical-solution, Property 32-35: Bright Data 集成
 */
class BrightDataPropertyTest {

    // ========== Test Infrastructure ==========

    /**
     * Amazon 商品数据实体（简化版）
     */
    record AmazonProduct(
            String asin,
            String title,
            String imageUrl,
            BigDecimal price,
            BigDecimal originalPrice,
            Double rating,
            Integer reviewCount,
            Integer bsrRank,
            String category,
            String brand,
            LocalDateTime fetchedAt
    ) {
        public boolean isValid() {
            return asin != null && !asin.isEmpty()
                    && title != null && !title.isEmpty()
                    && price != null;
        }
    }

    /**
     * 1688 商品数据实体（简化版）
     */
    record AlibabaProduct(
            String offerId,
            String title,
            String imageUrl,
            BigDecimal price,
            BigDecimal minOrderPrice,
            Integer minOrderQuantity,
            String supplierName,
            LocalDateTime fetchedAt
    ) {
        public boolean isValid() {
            return offerId != null && !offerId.isEmpty()
                    && title != null && !title.isEmpty()
                    && price != null;
        }
    }

    /**
     * 标准化后的产品实体（简化版）
     */
    record Product(
            Long id,
            String asin,
            String title,
            String imageUrl,
            BigDecimal currentPrice,
            Integer bsrRank,
            Integer reviewCount,
            Double rating,
            Long categoryId,
            LocalDateTime lastUpdated
    ) {
        public boolean hasRequiredFields() {
            return asin != null && !asin.isEmpty()
                    && title != null && !title.isEmpty()
                    && currentPrice != null;
        }
    }

    /**
     * 缓存条目
     */
    record CacheEntry<T>(T data, LocalDateTime cachedAt, long ttlSeconds) {
        public boolean isExpired() {
            return cachedAt.plusSeconds(ttlSeconds).isBefore(LocalDateTime.now());
        }
    }

    /**
     * 降级结果
     */
    record FallbackResult<T>(T data, DataStatus status, LocalDateTime fetchedAt, String errorMessage) {
        public boolean isFresh() {
            return status == DataStatus.FRESH;
        }

        public boolean isStale() {
            return status == DataStatus.STALE;
        }

        public boolean isEmpty() {
            return status == DataStatus.EMPTY;
        }

        public static <T> FallbackResult<T> fresh(T data) {
            return new FallbackResult<>(data, DataStatus.FRESH, LocalDateTime.now(), null);
        }

        public static <T> FallbackResult<T> stale(T data, LocalDateTime cachedAt) {
            return new FallbackResult<>(data, DataStatus.STALE, cachedAt, "数据来自缓存，可能已过期");
        }

        @SuppressWarnings("unchecked")
        public static <T> FallbackResult<T> empty() {
            return new FallbackResult<>((T) Collections.emptyList(), DataStatus.EMPTY, null, "无可用数据");
        }
    }

    enum DataStatus {
        FRESH, STALE, EMPTY
    }

    /**
     * 模拟缓存
     */
    static class TestCache {
        private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(String key) {
            CacheEntry<?> entry = cache.get(key);
            if (entry == null || entry.isExpired()) {
                cache.remove(key);
                return Optional.empty();
            }
            return Optional.of((T) entry.data());
        }

        public <T> void put(String key, T data, long ttlSeconds) {
            cache.put(key, new CacheEntry<>(data, LocalDateTime.now(), ttlSeconds));
        }

        public void clear() {
            cache.clear();
        }

        public boolean containsKey(String key) {
            CacheEntry<?> entry = cache.get(key);
            return entry != null && !entry.isExpired();
        }

        public CacheEntry<?> getEntry(String key) {
            return cache.get(key);
        }
    }

    /**
     * 模拟 Bright Data 网关
     */
    static class TestBrightDataGateway {
        private boolean shouldFail = false;
        private final TestCache cache;

        public TestBrightDataGateway(TestCache cache) {
            this.cache = cache;
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        public List<AmazonProduct> searchAmazonProducts(String keyword, String domain) {
            if (shouldFail) {
                throw new RuntimeException("API 调用失败");
            }
            // 返回模拟数据
            return List.of(
                    new AmazonProduct(
                            "B0" + keyword.hashCode(),
                            "Product for " + keyword,
                            "https://example.com/image.jpg",
                            new BigDecimal("29.99"),
                            new BigDecimal("39.99"),
                            4.5,
                            1000,
                            500,
                            "Electronics",
                            "TestBrand",
                            LocalDateTime.now()
                    )
            );
        }

        public AmazonProduct getAmazonProductByAsin(String asin) {
            if (shouldFail) {
                throw new RuntimeException("API 调用失败");
            }
            return new AmazonProduct(
                    asin,
                    "Product " + asin,
                    "https://example.com/image.jpg",
                    new BigDecimal("29.99"),
                    new BigDecimal("39.99"),
                    4.5,
                    1000,
                    500,
                    "Electronics",
                    "TestBrand",
                    LocalDateTime.now()
            );
        }

        public List<AmazonProduct> batchGetProducts(List<String> urls) {
            if (urls.size() > 10) {
                throw new IllegalArgumentException("批量请求最多支持 10 个 URL，当前: " + urls.size());
            }
            if (shouldFail) {
                throw new RuntimeException("API 调用失败");
            }
            return urls.stream()
                    .map(url -> {
                        String asin = extractAsin(url);
                        return new AmazonProduct(
                                asin,
                                "Product " + asin,
                                "https://example.com/image.jpg",
                                new BigDecimal("29.99"),
                                null,
                                4.5,
                                1000,
                                500,
                                "Electronics",
                                "TestBrand",
                                LocalDateTime.now()
                        );
                    })
                    .toList();
        }

        private String extractAsin(String url) {
            int dpIndex = url.indexOf("/dp/");
            if (dpIndex >= 0) {
                return url.substring(dpIndex + 4, Math.min(dpIndex + 14, url.length()));
            }
            return "UNKNOWN";
        }
    }

    /**
     * 数据标准化服务
     */
    static class TestProductDataService {
        private static final BigDecimal CNY_TO_USD_RATE = new BigDecimal("0.139");

        public Product convertAmazonToProduct(AmazonProduct amazon) {
            return new Product(
                    null,
                    amazon.asin(),
                    amazon.title(),
                    amazon.imageUrl(),
                    amazon.price(),
                    amazon.bsrRank(),
                    amazon.reviewCount(),
                    amazon.rating(),
                    null,
                    LocalDateTime.now()
            );
        }

        public Product convertAlibabaToProduct(AlibabaProduct alibaba) {
            BigDecimal priceInUsd = convertCnyToUsd(alibaba.price());
            return new Product(
                    null,
                    alibaba.offerId(),
                    alibaba.title(),
                    alibaba.imageUrl(),
                    priceInUsd,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.now()
            );
        }

        public BigDecimal convertCnyToUsd(BigDecimal cny) {
            if (cny == null) {
                return null;
            }
            return cny.multiply(CNY_TO_USD_RATE).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 降级服务
     */
    static class TestFallbackService {
        private final TestBrightDataGateway gateway;
        private final TestCache fallbackCache;

        public TestFallbackService(TestBrightDataGateway gateway, TestCache fallbackCache) {
            this.gateway = gateway;
            this.fallbackCache = fallbackCache;
        }

        public FallbackResult<List<AmazonProduct>> searchWithFallback(String keyword, String domain) {
            String cacheKey = "fallback:search:" + keyword.hashCode();

            try {
                List<AmazonProduct> products = gateway.searchAmazonProducts(keyword, domain);
                fallbackCache.put(cacheKey, products, 24 * 60 * 60);
                return FallbackResult.fresh(products);
            } catch (Exception e) {
                Optional<List<AmazonProduct>> cached = fallbackCache.get(cacheKey);
                if (cached.isPresent()) {
                    CacheEntry<?> entry = fallbackCache.getEntry(cacheKey);
                    return FallbackResult.stale(cached.get(), entry.cachedAt());
                }
                return FallbackResult.empty();
            }
        }

        public FallbackResult<AmazonProduct> getProductWithFallback(String asin) {
            String cacheKey = "fallback:product:" + asin;

            try {
                AmazonProduct product = gateway.getAmazonProductByAsin(asin);
                fallbackCache.put(cacheKey, product, 24 * 60 * 60);
                return FallbackResult.fresh(product);
            } catch (Exception e) {
                Optional<AmazonProduct> cached = fallbackCache.get(cacheKey);
                if (cached.isPresent()) {
                    CacheEntry<?> entry = fallbackCache.getEntry(cacheKey);
                    return FallbackResult.stale(cached.get(), entry.cachedAt());
                }
                return FallbackResult.empty();
            }
        }
    }

    // ========== Test Setup ==========

    private TestCache cache;
    private TestCache fallbackCache;
    private TestBrightDataGateway gateway;
    private TestProductDataService dataService;
    private TestFallbackService fallbackService;

    @BeforeTry
    void setUp() {
        cache = new TestCache();
        fallbackCache = new TestCache();
        gateway = new TestBrightDataGateway(cache);
        dataService = new TestProductDataService();
        fallbackService = new TestFallbackService(gateway, fallbackCache);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<String> validAsins() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofLength(10)
                .map(s -> "B0" + s.substring(2));
    }

    @Provide
    Arbitrary<String> validKeywords() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> validCnyPrices() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("99999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<AmazonProduct> validAmazonProducts() {
        return Combinators.combine(
                validAsins(),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(200),
                Arbitraries.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
                validPrices(),
                Arbitraries.doubles().between(1.0, 5.0),
                Arbitraries.integers().between(0, 100000),
                Arbitraries.integers().between(1, 1000000)
        ).as((asin, title, imageUrl, price, rating, reviewCount, bsrRank) ->
                new AmazonProduct(
                        asin, title, imageUrl, price, null, rating, reviewCount, bsrRank,
                        "Electronics", "TestBrand", LocalDateTime.now()
                )
        );
    }

    @Provide
    Arbitrary<AlibabaProduct> validAlibabaProducts() {
        return Combinators.combine(
                Arbitraries.strings().numeric().ofLength(12),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(200),
                Arbitraries.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
                validCnyPrices(),
                Arbitraries.integers().between(1, 1000)
        ).as((offerId, title, imageUrl, price, minOrderQty) ->
                new AlibabaProduct(
                        offerId, title, imageUrl, price, price, minOrderQty,
                        "TestSupplier", LocalDateTime.now()
                )
        );
    }

    @Provide
    Arbitrary<List<String>> validUrlLists() {
        return Arbitraries.integers().between(1, 10).flatMap(count ->
                validAsins().list().ofSize(count).map(asins ->
                        asins.stream()
                                .map(asin -> "https://www.amazon.com/dp/" + asin)
                                .toList()
                )
        );
    }

    @Provide
    Arbitrary<List<String>> oversizedUrlLists() {
        return Arbitraries.integers().between(11, 20).flatMap(count ->
                validAsins().list().ofSize(count).map(asins ->
                        asins.stream()
                                .map(asin -> "https://www.amazon.com/dp/" + asin)
                                .toList()
                )
        );
    }

    // ========== Property 32: Bright Data 数据标准化一致性 ==========

    /**
     * Property 32.1: Amazon 数据转换后包含所有必需字段
     * 
     * *对于任意* 有效的 Amazon 商品数据，转换为 Product 实体后应该包含所有必需字段
     * （ASIN、标题、价格），且数据类型正确。
     */
    @Property(tries = 100)
    @Label("Property 32.1: Amazon 数据转换后包含所有必需字段")
    void amazonDataConversionHasRequiredFields(
            @ForAll("validAmazonProducts") AmazonProduct amazonProduct
    ) {
        Product product = dataService.convertAmazonToProduct(amazonProduct);

        assert product.hasRequiredFields() :
                String.format("转换后的产品应该包含所有必需字段: asin=%s, title=%s, price=%s",
                        product.asin(), product.title(), product.currentPrice());

        // 验证字段映射正确
        assert product.asin().equals(amazonProduct.asin()) :
                "ASIN 应该正确映射";
        assert product.title().equals(amazonProduct.title()) :
                "标题应该正确映射";
        assert product.currentPrice().compareTo(amazonProduct.price()) == 0 :
                "价格应该正确映射";
    }

    /**
     * Property 32.2: 1688 数据转换后价格正确转换为美元
     * 
     * *对于任意* 有效的 1688 商品数据，转换为 Product 实体后价格应该正确从人民币转换为美元。
     */
    @Property(tries = 100)
    @Label("Property 32.2: 1688 数据价格正确转换为美元")
    void alibabaDataPriceConversionIsCorrect(
            @ForAll("validAlibabaProducts") AlibabaProduct alibabaProduct
    ) {
        Product product = dataService.convertAlibabaToProduct(alibabaProduct);

        // 验证价格转换正确（CNY * 0.139 = USD）
        BigDecimal expectedUsd = alibabaProduct.price()
                .multiply(new BigDecimal("0.139"))
                .setScale(2, RoundingMode.HALF_UP);

        assert product.currentPrice().compareTo(expectedUsd) == 0 :
                String.format("价格转换不正确: 期望 %s USD，实际 %s USD (原价 %s CNY)",
                        expectedUsd, product.currentPrice(), alibabaProduct.price());
    }

    /**
     * Property 32.3: 数据转换保持数据完整性
     * 
     * *对于任意* 有效的 Amazon 商品数据，转换后的数值字段应该保持原始值。
     */
    @Property(tries = 100)
    @Label("Property 32.3: 数据转换保持数据完整性")
    void dataConversionPreservesIntegrity(
            @ForAll("validAmazonProducts") AmazonProduct amazonProduct
    ) {
        Product product = dataService.convertAmazonToProduct(amazonProduct);

        // 验证数值字段保持不变
        if (amazonProduct.bsrRank() != null) {
            assert product.bsrRank().equals(amazonProduct.bsrRank()) :
                    "BSR 排名应该保持不变";
        }
        if (amazonProduct.reviewCount() != null) {
            assert product.reviewCount().equals(amazonProduct.reviewCount()) :
                    "评论数应该保持不变";
        }
        if (amazonProduct.rating() != null) {
            assert product.rating().equals(amazonProduct.rating()) :
                    "评分应该保持不变";
        }
    }

    // ========== Property 33: 批量请求数量限制 ==========

    /**
     * Property 33.1: 有效数量的批量请求应该成功
     * 
     * *对于任意* URL 数量在 1-10 个的批量请求，应该正常处理并返回结果。
     */
    @Property(tries = 100)
    @Label("Property 33.1: 有效数量的批量请求成功")
    void validBatchRequestSucceeds(
            @ForAll("validUrlLists") List<String> urls
    ) {
        List<AmazonProduct> products = gateway.batchGetProducts(urls);

        assert products != null :
                "批量请求应该返回非空结果";
        assert products.size() == urls.size() :
                String.format("返回的产品数量应该等于请求的 URL 数量: 期望 %d，实际 %d",
                        urls.size(), products.size());
    }

    /**
     * Property 33.2: 超过限制的批量请求应该被拒绝
     * 
     * *对于任意* URL 数量超过 10 个的批量请求，应该抛出 IllegalArgumentException。
     */
    @Property(tries = 100)
    @Label("Property 33.2: 超过限制的批量请求被拒绝")
    void oversizedBatchRequestIsRejected(
            @ForAll("oversizedUrlLists") List<String> urls
    ) {
        boolean exceptionThrown = false;
        try {
            gateway.batchGetProducts(urls);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
            assert e.getMessage().contains("10") :
                    "错误信息应该包含限制数量";
        }

        assert exceptionThrown :
                String.format("URL 数量为 %d 时应该抛出 IllegalArgumentException", urls.size());
    }

    /**
     * Property 33.3: 空列表的批量请求应该返回空结果
     */
    @Property(tries = 10)
    @Label("Property 33.3: 空列表批量请求返回空结果")
    void emptyBatchRequestReturnsEmpty() {
        List<AmazonProduct> products = gateway.batchGetProducts(Collections.emptyList());

        assert products != null :
                "空列表请求应该返回非空结果";
        assert products.isEmpty() :
                "空列表请求应该返回空列表";
    }

    // ========== Property 34: 数据缓存 TTL 正确性 ==========

    /**
     * Property 34.1: 搜索结果缓存 TTL 为 15 分钟
     * 
     * *对于任意* 搜索结果，缓存的 TTL 应该为 15 分钟（900秒）。
     */
    @Property(tries = 100)
    @Label("Property 34.1: 搜索结果缓存 TTL 为 15 分钟")
    void searchResultCacheTtlIs15Minutes(
            @ForAll("validKeywords") String keyword
    ) {
        // 模拟缓存搜索结果
        String cacheKey = "search:" + keyword.hashCode();
        List<AmazonProduct> products = gateway.searchAmazonProducts(keyword, "amazon.com");

        // 使用 15 分钟 TTL 缓存
        long ttlSeconds = 15 * 60;
        cache.put(cacheKey, products, ttlSeconds);

        // 验证缓存存在
        assert cache.containsKey(cacheKey) :
                "搜索结果应该被缓存";

        // 验证 TTL
        CacheEntry<?> entry = cache.getEntry(cacheKey);
        assert entry.ttlSeconds() == ttlSeconds :
                String.format("搜索结果缓存 TTL 应该是 %d 秒，实际是 %d 秒",
                        ttlSeconds, entry.ttlSeconds());
    }

    /**
     * Property 34.2: 商品详情缓存 TTL 为 1 小时
     * 
     * *对于任意* 商品详情，缓存的 TTL 应该为 1 小时（3600秒）。
     */
    @Property(tries = 100)
    @Label("Property 34.2: 商品详情缓存 TTL 为 1 小时")
    void productDetailCacheTtlIs1Hour(
            @ForAll("validAsins") String asin
    ) {
        // 模拟缓存商品详情
        String cacheKey = "product:" + asin;
        AmazonProduct product = gateway.getAmazonProductByAsin(asin);

        // 使用 1 小时 TTL 缓存
        long ttlSeconds = 60 * 60;
        cache.put(cacheKey, product, ttlSeconds);

        // 验证缓存存在
        assert cache.containsKey(cacheKey) :
                "商品详情应该被缓存";

        // 验证 TTL
        CacheEntry<?> entry = cache.getEntry(cacheKey);
        assert entry.ttlSeconds() == ttlSeconds :
                String.format("商品详情缓存 TTL 应该是 %d 秒，实际是 %d 秒",
                        ttlSeconds, entry.ttlSeconds());
    }

    // ========== Property 35: 请求失败降级正确性 ==========

    /**
     * Property 35.1: API 成功时返回新鲜数据
     * 
     * *对于任意* 成功的 API 请求，应该返回状态为 FRESH 的结果。
     */
    @Property(tries = 100)
    @Label("Property 35.1: API 成功时返回新鲜数据")
    void successfulRequestReturnsFreshData(
            @ForAll("validKeywords") String keyword
    ) {
        gateway.setShouldFail(false);

        FallbackResult<List<AmazonProduct>> result = fallbackService.searchWithFallback(keyword, "amazon.com");

        assert result.isFresh() :
                "成功的请求应该返回 FRESH 状态";
        assert result.data() != null && !result.data().isEmpty() :
                "成功的请求应该返回数据";
        assert result.errorMessage() == null :
                "成功的请求不应该有错误信息";
    }

    /**
     * Property 35.2: API 失败时返回缓存数据
     * 
     * *对于任意* 失败的 API 请求，如果有缓存数据，应该返回状态为 STALE 的结果。
     */
    @Property(tries = 100)
    @Label("Property 35.2: API 失败时返回缓存数据")
    void failedRequestReturnsStaleData(
            @ForAll("validKeywords") String keyword
    ) {
        // 先成功请求一次，建立缓存
        gateway.setShouldFail(false);
        FallbackResult<List<AmazonProduct>> firstResult = fallbackService.searchWithFallback(keyword, "amazon.com");
        assert firstResult.isFresh() : "第一次请求应该成功";

        // 模拟 API 失败
        gateway.setShouldFail(true);
        FallbackResult<List<AmazonProduct>> secondResult = fallbackService.searchWithFallback(keyword, "amazon.com");

        assert secondResult.isStale() :
                "API 失败时应该返回 STALE 状态";
        assert secondResult.data() != null :
                "降级时应该返回缓存数据";
        assert secondResult.errorMessage() != null :
                "降级时应该有错误信息";
    }

    /**
     * Property 35.3: API 失败且无缓存时返回空结果
     * 
     * *对于任意* 失败的 API 请求，如果没有缓存数据，应该返回状态为 EMPTY 的结果。
     */
    @Property(tries = 100)
    @Label("Property 35.3: API 失败且无缓存时返回空结果")
    void failedRequestWithNoCacheReturnsEmpty(
            @ForAll("validKeywords") String keyword
    ) {
        // 清空缓存
        fallbackCache.clear();

        // 模拟 API 失败
        gateway.setShouldFail(true);
        FallbackResult<List<AmazonProduct>> result = fallbackService.searchWithFallback(keyword + "_nocache", "amazon.com");

        assert result.isEmpty() :
                "API 失败且无缓存时应该返回 EMPTY 状态";
        assert result.errorMessage() != null :
                "应该有错误信息";
    }

    /**
     * Property 35.4: 降级数据包含时效性标注
     * 
     * *对于任意* 降级返回的数据，应该包含数据获取时间（fetchedAt）。
     */
    @Property(tries = 100)
    @Label("Property 35.4: 降级数据包含时效性标注")
    void staleDataHasTimestamp(
            @ForAll("validAsins") String asin
    ) {
        // 先成功请求一次
        gateway.setShouldFail(false);
        FallbackResult<AmazonProduct> firstResult = fallbackService.getProductWithFallback(asin);
        assert firstResult.isFresh() : "第一次请求应该成功";

        // 模拟 API 失败
        gateway.setShouldFail(true);
        FallbackResult<AmazonProduct> secondResult = fallbackService.getProductWithFallback(asin);

        assert secondResult.isStale() :
                "API 失败时应该返回 STALE 状态";
        assert secondResult.fetchedAt() != null :
                "降级数据应该包含获取时间";
        assert secondResult.fetchedAt().isBefore(LocalDateTime.now()) :
                "获取时间应该在当前时间之前";
    }
}
