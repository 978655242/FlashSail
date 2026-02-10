package com.flashsell.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * 收藏属性测试
 * 
 * Property 10: 收藏操作幂等性
 * *对于任意* 用户和产品，重复添加收藏应该返回相同的结果，不会创建重复记录。
 * 
 * Property 12: 分页一致性
 * *对于任意* 用户的收藏列表，分页查询的总数应该等于所有页面项目数之和。
 * 
 * Validates: Requirements 4.1, 4.4
 * 
 * Feature: flashsell-technical-solution, Property 10: 收藏操作幂等性, Property 12: 分页一致性
 */
class FavoriteAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    /**
     * 收藏实体（简化版）
     */
    record Favorite(
            Long id,
            Long userId,
            Long productId,
            LocalDateTime createdAt
    ) {
        public static Favorite create(Long userId, Long productId) {
            return new Favorite(null, userId, productId, LocalDateTime.now());
        }
    }

    /**
     * 产品实体（简化版）
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
     * 品类实体（简化版）
     */
    record Category(Long id, String name) {}

    /**
     * 收藏项响应（简化版）
     */
    record FavoriteItemRes(
            Long favoriteId,
            Long productId,
            String title,
            LocalDateTime favoritedAt
    ) {}

    /**
     * 收藏列表响应（简化版）
     */
    record FavoritesRes(
            List<FavoriteItemRes> products,
            Long total,
            Integer page,
            Integer pageSize,
            Boolean hasMore
    ) {}

    /**
     * 收藏网关 - 内存实现用于测试
     */
    static class TestFavoriteGateway {
        private final Map<Long, Favorite> favorites = new HashMap<>();
        private final Map<String, Favorite> userProductIndex = new HashMap<>();
        private final AtomicLong idCounter = new AtomicLong(1);

        public Optional<Favorite> findById(Long id) {
            return Optional.ofNullable(favorites.get(id));
        }

        public Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId) {
            String key = userId + "_" + productId;
            return Optional.ofNullable(userProductIndex.get(key));
        }

        public List<Favorite> findByUserId(Long userId, int page, int pageSize) {
            int offset = (page - 1) * pageSize;
            return favorites.values().stream()
                    .filter(f -> f.userId().equals(userId))
                    .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                    .skip(offset)
                    .limit(pageSize)
                    .toList();
        }

        public List<Favorite> findAllByUserId(Long userId) {
            return favorites.values().stream()
                    .filter(f -> f.userId().equals(userId))
                    .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                    .toList();
        }

        public long countByUserId(Long userId) {
            return favorites.values().stream()
                    .filter(f -> f.userId().equals(userId))
                    .count();
        }

        public boolean existsByUserIdAndProductId(Long userId, Long productId) {
            String key = userId + "_" + productId;
            return userProductIndex.containsKey(key);
        }

        public Favorite save(Favorite favorite) {
            Long id = favorite.id() != null ? favorite.id() : idCounter.getAndIncrement();
            Favorite saved = new Favorite(
                    id,
                    favorite.userId(),
                    favorite.productId(),
                    favorite.createdAt() != null ? favorite.createdAt() : LocalDateTime.now()
            );
            favorites.put(id, saved);
            String key = saved.userId() + "_" + saved.productId();
            userProductIndex.put(key, saved);
            return saved;
        }

        public boolean deleteByUserIdAndProductId(Long userId, Long productId) {
            String key = userId + "_" + productId;
            Favorite removed = userProductIndex.remove(key);
            if (removed != null) {
                favorites.remove(removed.id());
                return true;
            }
            return false;
        }

        public void clear() {
            favorites.clear();
            userProductIndex.clear();
            idCounter.set(1);
        }
    }

    /**
     * 产品网关 - 内存实现用于测试
     */
    static class TestProductGateway {
        private final Map<Long, Product> products = new HashMap<>();
        private final AtomicLong idCounter = new AtomicLong(1);

        public Optional<Product> findById(Long id) {
            return Optional.ofNullable(products.get(id));
        }

        public Product save(Product product) {
            Long id = product.id() != null ? product.id() : idCounter.getAndIncrement();
            Product saved = new Product(
                    id,
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId()
            );
            products.put(id, saved);
            return saved;
        }

        public void clear() {
            products.clear();
            idCounter.set(1);
        }
    }

    /**
     * 品类网关 - 内存实现用于测试
     */
    static class TestCategoryGateway {
        private final Map<Long, Category> categories = new HashMap<>();

        public TestCategoryGateway() {
            for (long i = 1; i <= 45; i++) {
                categories.put(i, new Category(i, "品类" + i));
            }
        }

        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(categories.get(id));
        }
    }

    /**
     * 收藏服务 - 简化实现用于测试
     */
    static class TestFavoriteService {
        private final TestFavoriteGateway favoriteGateway;
        private final TestProductGateway productGateway;

        public TestFavoriteService(
                TestFavoriteGateway favoriteGateway,
                TestProductGateway productGateway,
                TestCategoryGateway categoryGateway
        ) {
            this.favoriteGateway = favoriteGateway;
            this.productGateway = productGateway;
            // categoryGateway is available for future use if needed
        }

        /**
         * 添加收藏（幂等操作）
         */
        public FavoriteItemRes addFavorite(Long userId, Long productId) {
            // 检查产品是否存在
            Optional<Product> productOpt = productGateway.findById(productId);
            if (productOpt.isEmpty()) {
                throw new IllegalArgumentException("产品不存在");
            }
            Product product = productOpt.get();

            // 检查是否已收藏（幂等性）
            Optional<Favorite> existingFavorite = favoriteGateway.findByUserIdAndProductId(userId, productId);
            if (existingFavorite.isPresent()) {
                return toFavoriteItemRes(existingFavorite.get(), product);
            }

            // 创建新收藏
            Favorite favorite = Favorite.create(userId, productId);
            Favorite savedFavorite = favoriteGateway.save(favorite);

            return toFavoriteItemRes(savedFavorite, product);
        }

        /**
         * 取消收藏
         */
        public boolean removeFavorite(Long userId, Long productId) {
            return favoriteGateway.deleteByUserIdAndProductId(userId, productId);
        }

        /**
         * 获取收藏列表（分页）
         */
        public FavoritesRes getFavorites(Long userId, int page, int pageSize) {
            long total = favoriteGateway.countByUserId(userId);
            List<Favorite> favorites = favoriteGateway.findByUserId(userId, page, pageSize);

            List<FavoriteItemRes> items = favorites.stream()
                    .map(f -> {
                        Product product = productGateway.findById(f.productId()).orElse(null);
                        if (product == null) return null;
                        return toFavoriteItemRes(f, product);
                    })
                    .filter(item -> item != null)
                    .toList();

            boolean hasMore = (long) page * pageSize < total;

            return new FavoritesRes(items, total, page, pageSize, hasMore);
        }

        /**
         * 检查是否已收藏
         */
        public boolean isFavorited(Long userId, Long productId) {
            return favoriteGateway.existsByUserIdAndProductId(userId, productId);
        }

        /**
         * 获取收藏数量
         */
        public long getFavoriteCount(Long userId) {
            return favoriteGateway.countByUserId(userId);
        }

        private FavoriteItemRes toFavoriteItemRes(Favorite favorite, Product product) {
            return new FavoriteItemRes(
                    favorite.id(),
                    product.id(),
                    product.title(),
                    favorite.createdAt()
            );
        }
    }

    // ========== Test Setup ==========

    private TestFavoriteGateway favoriteGateway;
    private TestProductGateway productGateway;
    private TestCategoryGateway categoryGateway;
    private TestFavoriteService favoriteService;

    @BeforeTry
    void setUp() {
        favoriteGateway = new TestFavoriteGateway();
        productGateway = new TestProductGateway();
        categoryGateway = new TestCategoryGateway();
        favoriteService = new TestFavoriteService(favoriteGateway, productGateway, categoryGateway);
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 10000L);
    }

    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
                Arbitraries.of("https://example.com/img1.jpg", "https://example.com/img2.jpg"),
                Arbitraries.bigDecimals().between(new BigDecimal("0.01"), new BigDecimal("999.99")).ofScale(2),
                Arbitraries.integers().between(1, 100000),
                Arbitraries.integers().between(0, 10000),
                Arbitraries.doubles().between(1.0, 5.0),
                Arbitraries.longs().between(1L, 45L)
        ).as(Product::new);
    }

    @Provide
    Arbitrary<List<Product>> productLists() {
        return validProducts().list().ofMinSize(1).ofMaxSize(50);
    }

    @Provide
    Arbitrary<Integer> validPageSizes() {
        return Arbitraries.integers().between(1, 50);
    }

    // ========== Property 10: 收藏操作幂等性 ==========

    /**
     * Property 10.1: 重复添加收藏应该返回相同的收藏ID
     * 
     * *对于任意* 用户和产品，多次添加收藏应该返回相同的收藏记录。
     */
    @Property(tries = 100)
    @Label("Property 10.1: 重复添加收藏返回相同ID")
    void addFavoriteIsIdempotent(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProducts") Product product
    ) {
        // 保存产品
        Product savedProduct = productGateway.save(product);

        // 第一次添加收藏
        FavoriteItemRes firstResult = favoriteService.addFavorite(userId, savedProduct.id());

        // 第二次添加收藏（应该返回相同的结果）
        FavoriteItemRes secondResult = favoriteService.addFavorite(userId, savedProduct.id());

        // 验证两次返回相同的收藏ID
        assert firstResult.favoriteId().equals(secondResult.favoriteId()) :
                String.format("重复添加收藏应该返回相同的ID: first=%d, second=%d",
                        firstResult.favoriteId(), secondResult.favoriteId());

        // 验证收藏数量只有1
        long count = favoriteService.getFavoriteCount(userId);
        assert count == 1 :
                String.format("重复添加收藏后数量应该是1，实际是 %d", count);
    }

    /**
     * Property 10.2: 添加收藏后应该能查询到
     * 
     * *对于任意* 用户和产品，添加收藏后 isFavorited 应该返回 true。
     */
    @Property(tries = 100)
    @Label("Property 10.2: 添加收藏后可查询")
    void addedFavoriteCanBeQueried(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProducts") Product product
    ) {
        // 保存产品
        Product savedProduct = productGateway.save(product);

        // 添加前应该未收藏
        boolean beforeAdd = favoriteService.isFavorited(userId, savedProduct.id());
        assert !beforeAdd :
                "添加前不应该已收藏";

        // 添加收藏
        favoriteService.addFavorite(userId, savedProduct.id());

        // 添加后应该已收藏
        boolean afterAdd = favoriteService.isFavorited(userId, savedProduct.id());
        assert afterAdd :
                "添加后应该已收藏";
    }

    /**
     * Property 10.3: 取消收藏后应该查询不到
     * 
     * *对于任意* 用户和产品，取消收藏后 isFavorited 应该返回 false。
     */
    @Property(tries = 100)
    @Label("Property 10.3: 取消收藏后查询不到")
    void removedFavoriteCannotBeQueried(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProducts") Product product
    ) {
        // 保存产品
        Product savedProduct = productGateway.save(product);

        // 添加收藏
        favoriteService.addFavorite(userId, savedProduct.id());

        // 取消收藏
        boolean removed = favoriteService.removeFavorite(userId, savedProduct.id());
        assert removed :
                "取消收藏应该成功";

        // 取消后应该未收藏
        boolean afterRemove = favoriteService.isFavorited(userId, savedProduct.id());
        assert !afterRemove :
                "取消后不应该已收藏";
    }

    /**
     * Property 10.4: 重复取消收藏应该返回 false
     * 
     * *对于任意* 用户和产品，重复取消收藏应该返回 false（幂等性）。
     */
    @Property(tries = 100)
    @Label("Property 10.4: 重复取消收藏返回false")
    void removeFavoriteIsIdempotent(
            @ForAll("validUserIds") Long userId,
            @ForAll("validProducts") Product product
    ) {
        // 保存产品
        Product savedProduct = productGateway.save(product);

        // 添加收藏
        favoriteService.addFavorite(userId, savedProduct.id());

        // 第一次取消
        boolean firstRemove = favoriteService.removeFavorite(userId, savedProduct.id());
        assert firstRemove :
                "第一次取消应该成功";

        // 第二次取消（应该返回 false）
        boolean secondRemove = favoriteService.removeFavorite(userId, savedProduct.id());
        assert !secondRemove :
                "第二次取消应该返回 false";
    }

    // ========== Property 12: 分页一致性 ==========

    /**
     * Property 12.1: 分页查询的总数应该等于实际收藏数
     * 
     * *对于任意* 用户的收藏列表，分页响应中的 total 应该等于实际收藏数量。
     */
    @Property(tries = 100)
    @Label("Property 12.1: 分页总数等于实际收藏数")
    void paginationTotalMatchesActualCount(
            @ForAll("validUserIds") Long userId,
            @ForAll("productLists") List<Product> products,
            @ForAll("validPageSizes") int pageSize
    ) {
        // 保存产品并添加收藏，使用唯一ID避免覆盖
        int expectedCount = 0;
        long productIdOffset = 0;
        for (Product product : products) {
            // 创建具有唯一ID的产品
            Product uniqueProduct = new Product(
                    productIdOffset++,
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId()
            );
            Product savedProduct = productGateway.save(uniqueProduct);
            favoriteService.addFavorite(userId, savedProduct.id());
            expectedCount++;
        }

        // 获取分页结果
        FavoritesRes result = favoriteService.getFavorites(userId, 1, pageSize);

        // 验证总数
        assert result.total() == expectedCount :
                String.format("分页总数应该是 %d，实际是 %d", expectedCount, result.total());
    }

    /**
     * Property 12.2: 所有页面的项目数之和应该等于总数
     * 
     * *对于任意* 用户的收藏列表，遍历所有页面的项目数之和应该等于 total。
     */
    @Property(tries = 100)
    @Label("Property 12.2: 所有页面项目数之和等于总数")
    void allPagesItemCountEqualsTotal(
            @ForAll("validUserIds") Long userId,
            @ForAll("productLists") List<Product> products,
            @ForAll("validPageSizes") int pageSize
    ) {
        // 保存产品并添加收藏，使用唯一ID避免覆盖
        long productIdOffset = 0;
        for (Product product : products) {
            Product uniqueProduct = new Product(
                    productIdOffset++,
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId()
            );
            Product savedProduct = productGateway.save(uniqueProduct);
            favoriteService.addFavorite(userId, savedProduct.id());
        }

        // 获取第一页以获取总数
        FavoritesRes firstPage = favoriteService.getFavorites(userId, 1, pageSize);
        long total = firstPage.total();

        // 遍历所有页面，统计项目数
        long itemCount = 0;
        int page = 1;
        int maxPages = (int) Math.ceil((double) total / pageSize) + 1; // 防止无限循环

        while (page <= maxPages) {
            FavoritesRes pageResult = favoriteService.getFavorites(userId, page, pageSize);
            itemCount += pageResult.products().size();

            if (!pageResult.hasMore() || pageResult.products().isEmpty()) {
                break;
            }
            page++;
        }

        // 验证项目数之和等于总数
        assert itemCount == total :
                String.format("所有页面项目数之和应该是 %d，实际是 %d", total, itemCount);
    }

    /**
     * Property 12.3: hasMore 标志应该正确反映是否有更多数据
     * 
     * *对于任意* 分页查询，hasMore 应该正确指示是否还有更多页面。
     */
    @Property(tries = 100)
    @Label("Property 12.3: hasMore标志正确")
    void hasMoreFlagIsCorrect(
            @ForAll("validUserIds") Long userId,
            @ForAll("productLists") List<Product> products,
            @ForAll("validPageSizes") int pageSize
    ) {
        // 保存产品并添加收藏，使用唯一ID避免覆盖
        long productIdOffset = 0;
        for (Product product : products) {
            Product uniqueProduct = new Product(
                    productIdOffset++,
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId()
            );
            Product savedProduct = productGateway.save(uniqueProduct);
            favoriteService.addFavorite(userId, savedProduct.id());
        }

        // 获取第一页
        FavoritesRes result = favoriteService.getFavorites(userId, 1, pageSize);

        // 计算预期的 hasMore
        boolean expectedHasMore = result.total() > pageSize;

        // 验证 hasMore
        assert result.hasMore() == expectedHasMore :
                String.format("hasMore 应该是 %b，实际是 %b (total=%d, pageSize=%d)",
                        expectedHasMore, result.hasMore(), result.total(), pageSize);
    }

    /**
     * Property 12.4: 每页返回的项目数不应超过 pageSize
     * 
     * *对于任意* 分页查询，返回的项目数应该 <= pageSize。
     */
    @Property(tries = 100)
    @Label("Property 12.4: 每页项目数不超过pageSize")
    void pageItemCountDoesNotExceedPageSize(
            @ForAll("validUserIds") Long userId,
            @ForAll("productLists") List<Product> products,
            @ForAll("validPageSizes") int pageSize
    ) {
        // 保存产品并添加收藏，使用唯一ID避免覆盖
        long productIdOffset = 0;
        for (Product product : products) {
            Product uniqueProduct = new Product(
                    productIdOffset++,
                    product.title(),
                    product.imageUrl(),
                    product.currentPrice(),
                    product.bsrRank(),
                    product.reviewCount(),
                    product.rating(),
                    product.categoryId()
            );
            Product savedProduct = productGateway.save(uniqueProduct);
            favoriteService.addFavorite(userId, savedProduct.id());
        }

        // 获取多个页面并验证
        for (int page = 1; page <= 3; page++) {
            FavoritesRes result = favoriteService.getFavorites(userId, page, pageSize);

            assert result.products().size() <= pageSize :
                    String.format("第 %d 页的项目数 %d 超过了 pageSize %d",
                            page, result.products().size(), pageSize);
        }
    }

    /**
     * Property 12.5: 空收藏列表应该返回正确的分页结果
     * 
     * *对于任意* 没有收藏的用户，分页查询应该返回空列表和 total=0。
     */
    @Property(tries = 100)
    @Label("Property 12.5: 空收藏列表返回正确结果")
    void emptyFavoriteListReturnsCorrectResult(
            @ForAll("validUserIds") Long userId,
            @ForAll("validPageSizes") int pageSize
    ) {
        // 不添加任何收藏

        // 获取分页结果
        FavoritesRes result = favoriteService.getFavorites(userId, 1, pageSize);

        // 验证结果
        assert result.total() == 0 :
                String.format("空收藏列表的 total 应该是 0，实际是 %d", result.total());
        assert result.products().isEmpty() :
                "空收藏列表的 products 应该为空";
        assert !result.hasMore() :
                "空收藏列表的 hasMore 应该是 false";
    }

    /**
     * Property 12.6: 不同用户的收藏应该相互隔离
     * 
     * *对于任意* 两个不同用户，他们的收藏列表应该相互独立。
     */
    @Property(tries = 100)
    @Label("Property 12.6: 不同用户收藏相互隔离")
    void differentUsersHaveIsolatedFavorites(
            @ForAll("validUserIds") Long userId1,
            @ForAll("validProducts") Product product1,
            @ForAll("validProducts") Product product2
    ) {
        // 确保两个用户ID不同
        Long userId2 = userId1 + 10000L;

        // 保存产品
        Product savedProduct1 = productGateway.save(product1);
        Product savedProduct2 = productGateway.save(product2);

        // 用户1收藏产品1
        favoriteService.addFavorite(userId1, savedProduct1.id());

        // 用户2收藏产品2
        favoriteService.addFavorite(userId2, savedProduct2.id());

        // 验证用户1只能看到自己的收藏
        FavoritesRes user1Favorites = favoriteService.getFavorites(userId1, 1, 10);
        assert user1Favorites.total() == 1 :
                String.format("用户1的收藏数应该是1，实际是 %d", user1Favorites.total());
        assert user1Favorites.products().get(0).productId().equals(savedProduct1.id()) :
                "用户1的收藏应该是产品1";

        // 验证用户2只能看到自己的收藏
        FavoritesRes user2Favorites = favoriteService.getFavorites(userId2, 1, 10);
        assert user2Favorites.total() == 1 :
                String.format("用户2的收藏数应该是1，实际是 %d", user2Favorites.total());
        assert user2Favorites.products().get(0).productId().equals(savedProduct2.id()) :
                "用户2的收藏应该是产品2";
    }
}
