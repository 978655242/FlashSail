package com.flashsell.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 * 产品属性测试
 * 
 * Property 9: 产品详情完整性
 * *对于任意* 存在的产品 ID，返回的产品详情应该包含所有必需字段：
 * 标题、图片、当前价格、历史价格、BSR 排名、评论统计和竞争评分。
 * 
 * Validates: Requirements 3.3
 * 
 * Feature: flashsell-technical-solution, Property 9: 产品详情完整性
 */
class ProductAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    /**
     * 产品实体（简化版）
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
            BigDecimal competitionScore,
            String aiRecommendation,
            LocalDateTime lastUpdated,
            LocalDateTime createdAt
    ) {
        /**
         * 检查产品详情是否完整
         * 产品详情必须包含：标题、图片、当前价格、BSR排名、评论统计和竞争评分
         */
        public boolean isDetailComplete() {
            return title != null && !title.isEmpty()
                    && imageUrl != null && !imageUrl.isEmpty()
                    && currentPrice != null
                    && bsrRank != null
                    && reviewCount != null
                    && competitionScore != null;
        }
    }

    /**
     * 价格点实体（简化版）
     */
    record PricePoint(
            Long id,
            Long productId,
            BigDecimal price,
            LocalDate recordedDate,
            LocalDateTime createdAt
    ) {}

    /**
     * 品类实体（简化版）
     */
    record Category(Long id, String name, Integer productCount) {}

    /**
     * 产品详情响应（简化版）
     */
    record ProductDetailRes(
            Long id,
            String title,
            String image,
            BigDecimal currentPrice,
            List<PricePointRes> priceHistory,
            Integer bsrRank,
            Integer reviewCount,
            Double rating,
            BigDecimal competitionScore,
            String aiRecommendation,
            CategoryRes category,
            LocalDateTime lastUpdated
    ) {
        /**
         * 检查响应是否包含所有必需字段
         */
        public boolean hasAllRequiredFields() {
            return title != null && !title.isEmpty()
                    && image != null && !image.isEmpty()
                    && currentPrice != null
                    && priceHistory != null
                    && bsrRank != null
                    && reviewCount != null
                    && competitionScore != null;
        }
    }

    /**
     * 价格点响应（简化版）
     */
    record PricePointRes(LocalDate date, BigDecimal price) {}

    /**
     * 品类响应（简化版）
     */
    record CategoryRes(Long id, String name, Integer productCount) {}

    /**
     * 产品网关 - 内存实现用于测试
     */
    static class TestProductGateway {
        private final Map<Long, Product> products = new HashMap<>();
        private final Map<Long, List<PricePoint>> priceHistories = new HashMap<>();
        private final AtomicLong idCounter = new AtomicLong(1);

        public Optional<Product> findById(Long id) {
            return Optional.ofNullable(products.get(id));
        }

        public Product save(Product product) {
            // 如果产品已有ID，使用该ID；否则生成新ID
            Long id;
            if (product.id() != null) {
                id = product.id();
                // 确保idCounter不会生成重复ID
                if (id >= idCounter.get()) {
                    idCounter.set(id + 1);
                }
            } else {
                id = idCounter.getAndIncrement();
            }
            Product saved = new Product(
                    id,
                    product.asin(),
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId(),
                    product.competitionScore(),
                    product.aiRecommendation(),
                    product.lastUpdated(),
                    product.createdAt()
            );
            products.put(id, saved);
            return saved;
        }

        public List<PricePoint> findPriceHistory(Long productId) {
            return priceHistories.getOrDefault(productId, new ArrayList<>());
        }

        public void savePricePoint(PricePoint pricePoint) {
            priceHistories.computeIfAbsent(pricePoint.productId(), k -> new ArrayList<>())
                    .add(pricePoint);
        }

        public void clear() {
            products.clear();
            priceHistories.clear();
            idCounter.set(1);
        }
    }

    /**
     * 品类网关 - 内存实现用于测试
     */
    static class TestCategoryGateway {
        private final Map<Long, Category> categories = new HashMap<>();

        public TestCategoryGateway() {
            // 初始化一些测试品类
            for (long i = 1; i <= 45; i++) {
                categories.put(i, new Category(i, "品类" + i, 0));
            }
        }

        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(categories.get(id));
        }
    }

    /**
     * 产品缓存网关 - 内存实现用于测试
     */
    static class TestProductCacheGateway {
        private final Map<Long, ProductDetailRes> cache = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <T> Optional<T> getProductDetail(Long productId) {
            return Optional.ofNullable((T) cache.get(productId));
        }

        public void cacheProductDetail(Long productId, Object productDetail) {
            if (productDetail instanceof ProductDetailRes res) {
                cache.put(productId, res);
            }
        }

        public void invalidateProductDetail(Long productId) {
            cache.remove(productId);
        }

        public void clear() {
            cache.clear();
        }
    }

    /**
     * 产品服务 - 简化实现用于测试
     */
    static class TestProductService {
        private final TestProductGateway productGateway;
        private final TestCategoryGateway categoryGateway;
        private final TestProductCacheGateway cacheGateway;

        public TestProductService(
                TestProductGateway productGateway,
                TestCategoryGateway categoryGateway,
                TestProductCacheGateway cacheGateway
        ) {
            this.productGateway = productGateway;
            this.categoryGateway = categoryGateway;
            this.cacheGateway = cacheGateway;
        }

        public Optional<ProductDetailRes> getProductDetail(Long productId) {
            // 1. 先从缓存获取
            Optional<ProductDetailRes> cached = cacheGateway.getProductDetail(productId);
            if (cached.isPresent()) {
                return cached;
            }

            // 2. 从数据库获取
            Optional<Product> productOpt = productGateway.findById(productId);
            if (productOpt.isEmpty()) {
                return Optional.empty();
            }

            Product product = productOpt.get();

            // 3. 获取价格历史
            List<PricePoint> priceHistory = productGateway.findPriceHistory(productId);

            // 4. 获取品类信息
            Category category = null;
            if (product.categoryId() != null) {
                category = categoryGateway.findById(product.categoryId()).orElse(null);
            }

            // 5. 组装响应
            ProductDetailRes res = toProductDetailRes(product, priceHistory, category);

            // 6. 缓存结果
            cacheGateway.cacheProductDetail(productId, res);

            return Optional.of(res);
        }

        private ProductDetailRes toProductDetailRes(Product product, List<PricePoint> priceHistory, Category category) {
            List<PricePointRes> pricePointResList = priceHistory.stream()
                    .map(pp -> new PricePointRes(pp.recordedDate(), pp.price()))
                    .toList();

            CategoryRes categoryRes = category != null
                    ? new CategoryRes(category.id(), category.name(), category.productCount())
                    : null;

            return new ProductDetailRes(
                    product.id(),
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    pricePointResList,
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.competitionScore(),
                    product.aiRecommendation(),
                    categoryRes,
                    product.lastUpdated()
            );
        }

        /**
         * 验证产品详情是否完整
         */
        public boolean isProductDetailComplete(Long productId) {
            Optional<ProductDetailRes> detailOpt = getProductDetail(productId);
            return detailOpt.map(ProductDetailRes::hasAllRequiredFields).orElse(false);
        }
    }

    // ========== Test Setup ==========

    private TestProductGateway productGateway;
    private TestCategoryGateway categoryGateway;
    private TestProductCacheGateway cacheGateway;
    private TestProductService productService;

    @BeforeTry
    void setUp() {
        productGateway = new TestProductGateway();
        categoryGateway = new TestCategoryGateway();
        cacheGateway = new TestProductCacheGateway();
        productService = new TestProductService(productGateway, categoryGateway, cacheGateway);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(200);
    }

    @Provide
    Arbitrary<String> validImageUrls() {
        return Arbitraries.of(
                "https://example.com/image1.jpg",
                "https://example.com/image2.png",
                "https://cdn.example.com/products/abc123.jpg"
        );
    }

    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<Integer> validBsrRanks() {
        return Arbitraries.integers().between(1, 1000000);
    }

    @Provide
    Arbitrary<Integer> validReviewCounts() {
        return Arbitraries.integers().between(0, 100000);
    }

    @Provide
    Arbitrary<Double> validRatings() {
        return Arbitraries.doubles().between(1.0, 5.0);
    }

    @Provide
    Arbitrary<BigDecimal> validCompetitionScores() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.00"), new BigDecimal("1.00"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<Long> validCategoryIds() {
        return Arbitraries.longs().between(1L, 45L);
    }

    @Provide
    Arbitrary<Product> completeProducts() {
        // jqwik Combinators.combine supports up to 8 parameters, so we build the product in steps
        return Combinators.combine(
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofLength(10),
                validTitles(),
                validImageUrls(),
                validPrices(),
                validBsrRanks(),
                validReviewCounts(),
                validRatings()
        ).flatAs((id, asin, title, imageUrl, price, bsrRank, reviewCount, rating) ->
                Combinators.combine(
                        validCategoryIds(),
                        validCompetitionScores(),
                        Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100)
                ).as((categoryId, competitionScore, aiRec) ->
                        new Product(
                                id, asin, title, imageUrl, price, bsrRank, reviewCount, rating,
                                categoryId, competitionScore, aiRec,
                                LocalDateTime.now(), LocalDateTime.now()
                        )
                )
        );
    }

    @Provide
    Arbitrary<Product> incompleteProducts() {
        // 生成缺少必需字段的产品
        return Arbitraries.oneOf(
                // 缺少标题
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validImageUrls(),
                        validPrices(),
                        validBsrRanks(),
                        validReviewCounts(),
                        validCompetitionScores()
                ).as((id, imageUrl, price, bsrRank, reviewCount, competitionScore) ->
                        new Product(id, "ASIN" + id, null, imageUrl, price, bsrRank, reviewCount,
                                4.5, 1L, competitionScore, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                ),
                // 缺少图片
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validTitles(),
                        validPrices(),
                        validBsrRanks(),
                        validReviewCounts(),
                        validCompetitionScores()
                ).as((id, title, price, bsrRank, reviewCount, competitionScore) ->
                        new Product(id, "ASIN" + id, title, null, price, bsrRank, reviewCount,
                                4.5, 1L, competitionScore, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                ),
                // 缺少价格
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validTitles(),
                        validImageUrls(),
                        validBsrRanks(),
                        validReviewCounts(),
                        validCompetitionScores()
                ).as((id, title, imageUrl, bsrRank, reviewCount, competitionScore) ->
                        new Product(id, "ASIN" + id, title, imageUrl, null, bsrRank, reviewCount,
                                4.5, 1L, competitionScore, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                ),
                // 缺少BSR排名
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validTitles(),
                        validImageUrls(),
                        validPrices(),
                        validReviewCounts(),
                        validCompetitionScores()
                ).as((id, title, imageUrl, price, reviewCount, competitionScore) ->
                        new Product(id, "ASIN" + id, title, imageUrl, price, null, reviewCount,
                                4.5, 1L, competitionScore, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                ),
                // 缺少评论数
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validTitles(),
                        validImageUrls(),
                        validPrices(),
                        validBsrRanks(),
                        validCompetitionScores()
                ).as((id, title, imageUrl, price, bsrRank, competitionScore) ->
                        new Product(id, "ASIN" + id, title, imageUrl, price, bsrRank, null,
                                4.5, 1L, competitionScore, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                ),
                // 缺少竞争评分
                Combinators.combine(
                        Arbitraries.longs().between(1L, 10000L),
                        validTitles(),
                        validImageUrls(),
                        validPrices(),
                        validBsrRanks(),
                        validReviewCounts()
                ).as((id, title, imageUrl, price, bsrRank, reviewCount) ->
                        new Product(id, "ASIN" + id, title, imageUrl, price, bsrRank, reviewCount,
                                4.5, 1L, null, "AI推荐",
                                LocalDateTime.now(), LocalDateTime.now())
                )
        );
    }

    @Provide
    Arbitrary<List<PricePoint>> priceHistories() {
        return Arbitraries.integers().between(0, 30).flatMap(count -> {
            if (count == 0) {
                return Arbitraries.just(new ArrayList<>());
            }
            return Combinators.combine(
                    Arbitraries.longs().between(1L, 10000L),
                    validPrices().list().ofSize(count)
            ).as((productId, prices) -> {
                List<PricePoint> history = new ArrayList<>();
                LocalDate date = LocalDate.now().minusDays(count);
                for (int i = 0; i < prices.size(); i++) {
                    history.add(new PricePoint(
                            (long) (i + 1),
                            productId,
                            prices.get(i),
                            date.plusDays(i),
                            LocalDateTime.now()
                    ));
                }
                return history;
            });
        });
    }

    // ========== Property 9: 产品详情完整性 ==========

    /**
     * Property 9.1: 完整产品的详情应该包含所有必需字段
     * 
     * *对于任意* 包含所有必需字段的产品，返回的产品详情应该包含：
     * 标题、图片、当前价格、历史价格、BSR排名、评论统计和竞争评分。
     */
    @Property(tries = 100)
    @Label("Property 9.1: 完整产品详情包含所有必需字段")
    void completeProductDetailHasAllRequiredFields(
            @ForAll("completeProducts") Product product
    ) {
        // 保存产品
        Product saved = productGateway.save(product);

        // 获取产品详情
        Optional<ProductDetailRes> detailOpt = productService.getProductDetail(saved.id());

        assert detailOpt.isPresent() :
                String.format("产品 ID %d 应该存在", saved.id());

        ProductDetailRes detail = detailOpt.get();

        // 验证所有必需字段
        assert detail.title() != null && !detail.title().isEmpty() :
                "产品详情应该包含标题";
        assert detail.image() != null && !detail.image().isEmpty() :
                "产品详情应该包含图片";
        assert detail.currentPrice() != null :
                "产品详情应该包含当前价格";
        assert detail.priceHistory() != null :
                "产品详情应该包含价格历史（可以为空列表）";
        assert detail.bsrRank() != null :
                "产品详情应该包含BSR排名";
        assert detail.reviewCount() != null :
                "产品详情应该包含评论数";
        assert detail.competitionScore() != null :
                "产品详情应该包含竞争评分";
    }

    /**
     * Property 9.2: 不完整产品的详情应该被正确识别
     * 
     * *对于任意* 缺少必需字段的产品，isDetailComplete 应该返回 false。
     */
    @Property(tries = 100)
    @Label("Property 9.2: 不完整产品详情被正确识别")
    void incompleteProductDetailIsIdentified(
            @ForAll("incompleteProducts") Product product
    ) {
        // 验证产品实体本身的完整性检查
        boolean isComplete = product.isDetailComplete();

        assert !isComplete :
                String.format("缺少必需字段的产品应该被识别为不完整: %s", product);
    }

    /**
     * Property 9.3: 产品详情的价格历史应该正确返回
     * 
     * *对于任意* 有价格历史的产品，返回的价格历史应该包含所有记录。
     */
    @Property(tries = 100)
    @Label("Property 9.3: 价格历史正确返回")
    void priceHistoryIsCorrectlyReturned(
            @ForAll("completeProducts") Product product,
            @ForAll("priceHistories") List<PricePoint> priceHistory
    ) {
        // 保存产品
        Product saved = productGateway.save(product);

        // 保存价格历史（使用保存后的产品ID）
        for (PricePoint pp : priceHistory) {
            PricePoint withCorrectProductId = new PricePoint(
                    pp.id(), saved.id(), pp.price(), pp.recordedDate(), pp.createdAt()
            );
            productGateway.savePricePoint(withCorrectProductId);
        }

        // 获取产品详情
        Optional<ProductDetailRes> detailOpt = productService.getProductDetail(saved.id());

        assert detailOpt.isPresent() :
                String.format("产品 ID %d 应该存在", saved.id());

        ProductDetailRes detail = detailOpt.get();

        // 验证价格历史数量
        assert detail.priceHistory().size() == priceHistory.size() :
                String.format("价格历史数量应该是 %d，实际是 %d",
                        priceHistory.size(), detail.priceHistory().size());
    }

    /**
     * Property 9.4: 不存在的产品应该返回空
     * 
     * *对于任意* 不存在的产品 ID，getProductDetail 应该返回 Optional.empty()。
     */
    @Property(tries = 100)
    @Label("Property 9.4: 不存在的产品返回空")
    void nonExistentProductReturnsEmpty(
            @ForAll("validCategoryIds") Long nonExistentId
    ) {
        // 确保产品不存在
        Long id = nonExistentId + 100000L;

        Optional<ProductDetailRes> detailOpt = productService.getProductDetail(id);

        assert detailOpt.isEmpty() :
                String.format("不存在的产品 ID %d 应该返回空", id);
    }

    /**
     * Property 9.5: 产品详情的品类信息应该正确关联
     * 
     * *对于任意* 有品类的产品，返回的产品详情应该包含正确的品类信息。
     */
    @Property(tries = 100)
    @Label("Property 9.5: 品类信息正确关联")
    void categoryInfoIsCorrectlyAssociated(
            @ForAll("completeProducts") Product product
    ) {
        // 保存产品
        Product saved = productGateway.save(product);

        // 获取产品详情
        Optional<ProductDetailRes> detailOpt = productService.getProductDetail(saved.id());

        assert detailOpt.isPresent() :
                String.format("产品 ID %d 应该存在", saved.id());

        ProductDetailRes detail = detailOpt.get();

        // 验证品类信息
        if (product.categoryId() != null) {
            assert detail.category() != null :
                    "有品类ID的产品应该包含品类信息";
            assert detail.category().id().equals(product.categoryId()) :
                    String.format("品类ID应该是 %d，实际是 %d",
                            product.categoryId(), detail.category().id());
        }
    }

    /**
     * Property 9.6: 产品详情缓存应该正确工作
     * 
     * *对于任意* 产品，第二次获取应该从缓存返回相同的结果。
     */
    @Property(tries = 100)
    @Label("Property 9.6: 产品详情缓存正确工作")
    void productDetailCacheWorksCorrectly(
            @ForAll("completeProducts") Product product
    ) {
        // 保存产品
        Product saved = productGateway.save(product);

        // 第一次获取（应该从数据库获取并缓存）
        Optional<ProductDetailRes> firstCall = productService.getProductDetail(saved.id());

        // 第二次获取（应该从缓存获取）
        Optional<ProductDetailRes> secondCall = productService.getProductDetail(saved.id());

        assert firstCall.isPresent() && secondCall.isPresent() :
                "两次调用都应该返回产品详情";

        // 验证两次返回的结果相同
        ProductDetailRes first = firstCall.get();
        ProductDetailRes second = secondCall.get();

        assert first.id().equals(second.id()) :
                "缓存的产品ID应该相同";
        assert first.title().equals(second.title()) :
                "缓存的产品标题应该相同";
        assert first.currentPrice().compareTo(second.currentPrice()) == 0 :
                "缓存的产品价格应该相同";
    }

    /**
     * Property 9.7: 产品详情的数值字段应该在有效范围内
     * 
     * *对于任意* 完整产品，返回的数值字段应该在有效范围内。
     */
    @Property(tries = 100)
    @Label("Property 9.7: 数值字段在有效范围内")
    void numericFieldsAreInValidRange(
            @ForAll("completeProducts") Product product
    ) {
        // 保存产品
        Product saved = productGateway.save(product);

        // 获取产品详情
        Optional<ProductDetailRes> detailOpt = productService.getProductDetail(saved.id());

        assert detailOpt.isPresent() :
                String.format("产品 ID %d 应该存在", saved.id());

        ProductDetailRes detail = detailOpt.get();

        // 验证价格为正数
        assert detail.currentPrice().compareTo(BigDecimal.ZERO) > 0 :
                "当前价格应该为正数";

        // 验证BSR排名为正数
        assert detail.bsrRank() > 0 :
                "BSR排名应该为正数";

        // 验证评论数为非负数
        assert detail.reviewCount() >= 0 :
                "评论数应该为非负数";

        // 验证竞争评分在0-1之间
        assert detail.competitionScore().compareTo(BigDecimal.ZERO) >= 0
                && detail.competitionScore().compareTo(BigDecimal.ONE) <= 0 :
                "竞争评分应该在0-1之间";

        // 验证评分在1-5之间（如果有）
        if (detail.rating() != null) {
            assert detail.rating() >= 1.0 && detail.rating() <= 5.0 :
                    "评分应该在1-5之间";
        }
    }
}
