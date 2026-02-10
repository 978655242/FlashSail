package com.flashsell.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
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
 * 品类属性测试
 * 
 * Property 8: 品类限制有效性
 * *对于任意* 搜索结果，返回的所有产品都应该属于预定义的 45 个支持品类之一。
 * 
 * Validates: Requirements 2.8
 * 
 * Feature: flashsell-technical-solution, Property 8: 品类限制有效性
 */
class CategoryAppServicePropertyTest {

    // ========== Test Infrastructure ==========

    /**
     * 品类组实体（简化版）
     */
    record CategoryGroup(Long id, String name, Integer sortOrder, List<Category> categories) {}

    /**
     * 品类实体（简化版）
     */
    record Category(Long id, Long groupId, String name, String amazonCategoryId, Integer productCount) {}

    /**
     * 产品实体（简化版）
     */
    record Product(Long id, String title, Long categoryId) {}

    /**
     * 品类网关 - 内存实现用于测试
     */
    static class TestCategoryGateway {
        private final Map<Long, CategoryGroup> groups = new HashMap<>();
        private final Map<Long, Category> categories = new HashMap<>();
        private final Set<Long> validCategoryIds;

        public TestCategoryGateway(List<CategoryGroup> initialGroups) {
            for (CategoryGroup group : initialGroups) {
                groups.put(group.id(), group);
                for (Category category : group.categories()) {
                    categories.put(category.id(), category);
                }
            }
            this.validCategoryIds = categories.keySet();
        }

        public List<CategoryGroup> findAllGroups() {
            return new ArrayList<>(groups.values());
        }

        public Optional<CategoryGroup> findGroupById(Long id) {
            return Optional.ofNullable(groups.get(id));
        }

        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(categories.get(id));
        }

        public List<Category> findByGroupId(Long groupId) {
            return categories.values().stream()
                    .filter(c -> c.groupId().equals(groupId))
                    .collect(Collectors.toList());
        }

        public List<Category> findAll() {
            return new ArrayList<>(categories.values());
        }

        public int countAll() {
            return categories.size();
        }

        public int countAllGroups() {
            return groups.size();
        }

        public boolean isValidCategoryId(Long categoryId) {
            return categoryId != null && validCategoryIds.contains(categoryId);
        }

        public Set<Long> getValidCategoryIds() {
            return validCategoryIds;
        }
    }

    /**
     * 品类服务 - 简化实现用于测试
     */
    static class TestCategoryService {
        private final TestCategoryGateway categoryGateway;

        public TestCategoryService(TestCategoryGateway categoryGateway) {
            this.categoryGateway = categoryGateway;
        }

        public List<CategoryGroup> getAllCategories() {
            return categoryGateway.findAllGroups();
        }

        public boolean isValidCategoryId(Long categoryId) {
            return categoryGateway.isValidCategoryId(categoryId);
        }

        public int getCategoryCount() {
            return categoryGateway.countAll();
        }

        public int getCategoryGroupCount() {
            return categoryGateway.countAllGroups();
        }

        /**
         * 验证产品是否属于有效品类
         */
        public boolean validateProductCategory(Product product) {
            if (product == null || product.categoryId() == null) {
                return false;
            }
            return isValidCategoryId(product.categoryId());
        }

        /**
         * 验证所有产品是否都属于有效品类
         */
        public boolean validateAllProductCategories(List<Product> products) {
            if (products == null || products.isEmpty()) {
                return true; // 空列表视为有效
            }
            return products.stream().allMatch(this::validateProductCategory);
        }

        /**
         * 过滤出属于有效品类的产品
         */
        public List<Product> filterValidCategoryProducts(List<Product> products) {
            if (products == null) {
                return List.of();
            }
            return products.stream()
                    .filter(this::validateProductCategory)
                    .collect(Collectors.toList());
        }
    }

    // ========== Test Setup ==========

    private TestCategoryGateway categoryGateway;
    private TestCategoryService categoryService;
    private static final int EXPECTED_CATEGORY_COUNT = 45;
    private static final int EXPECTED_GROUP_COUNT = 4;

    @BeforeProperty
    void setUp() {
        // 初始化 45 个固定品类数据（模拟真实数据）
        List<CategoryGroup> groups = createFixedCategories();
        categoryGateway = new TestCategoryGateway(groups);
        categoryService = new TestCategoryService(categoryGateway);
    }

    /**
     * 创建 45 个固定品类数据
     */
    private List<CategoryGroup> createFixedCategories() {
        List<CategoryGroup> groups = new ArrayList<>();
        AtomicLong categoryIdCounter = new AtomicLong(1);

        // 工业用品类目（12个品类）
        List<Category> industrialCategories = List.of(
                new Category(categoryIdCounter.getAndIncrement(), 1L, "五金工具", "industrial-hardware", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "安全防护", "safety-security", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "电气设备", "electrical-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "测量仪器", "measuring-instruments", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "焊接设备", "welding-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "气动工具", "pneumatic-tools", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "液压设备", "hydraulic-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "工业照明", "industrial-lighting", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "包装材料", "packaging-materials", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "清洁设备", "cleaning-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "搬运设备", "material-handling", 0),
                new Category(categoryIdCounter.getAndIncrement(), 1L, "工业胶带", "industrial-tape", 0)
        );
        groups.add(new CategoryGroup(1L, "工业用品", 1, industrialCategories));

        // 节日装饰类目（10个品类）
        List<Category> holidayCategories = List.of(
                new Category(categoryIdCounter.getAndIncrement(), 2L, "圣诞装饰", "christmas-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "万圣节装饰", "halloween-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "复活节装饰", "easter-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "情人节装饰", "valentines-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "派对用品", "party-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "婚庆用品", "wedding-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "生日装饰", "birthday-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "节日灯饰", "holiday-lights", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "气球装饰", "balloon-decorations", 0),
                new Category(categoryIdCounter.getAndIncrement(), 2L, "节日礼品包装", "gift-wrapping", 0)
        );
        groups.add(new CategoryGroup(2L, "节日装饰", 2, holidayCategories));

        // 家居生活与百货类目（13个品类）
        List<Category> homeCategories = List.of(
                new Category(categoryIdCounter.getAndIncrement(), 3L, "厨房用品", "kitchen-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "浴室用品", "bathroom-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "收纳整理", "storage-organization", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "家纺布艺", "home-textiles", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "家居装饰", "home-decor", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "清洁用品", "cleaning-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "宠物用品", "pet-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "园艺用品", "garden-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "户外用品", "outdoor-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "汽车用品", "automotive-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "办公用品", "office-supplies", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "运动健身", "sports-fitness", 0),
                new Category(categoryIdCounter.getAndIncrement(), 3L, "母婴用品", "baby-supplies", 0)
        );
        groups.add(new CategoryGroup(3L, "家居生活与百货", 3, homeCategories));

        // 数码配件与小家电类目（10个品类）
        List<Category> digitalCategories = List.of(
                new Category(categoryIdCounter.getAndIncrement(), 4L, "手机配件", "phone-accessories", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "电脑配件", "computer-accessories", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "音频设备", "audio-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "智能穿戴", "wearable-devices", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "充电设备", "charging-equipment", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "数据线材", "cables-connectors", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "小型家电", "small-appliances", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "个护电器", "personal-care-appliances", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "厨房电器", "kitchen-appliances", 0),
                new Category(categoryIdCounter.getAndIncrement(), 4L, "智能家居", "smart-home", 0)
        );
        groups.add(new CategoryGroup(4L, "数码配件与小家电", 4, digitalCategories));

        return groups;
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<Long> validCategoryIds() {
        // 生成 1-45 范围内的有效品类 ID
        return Arbitraries.longs().between(1L, 45L);
    }

    @Provide
    Arbitrary<Long> invalidCategoryIds() {
        // 生成超出范围的无效品类 ID
        return Arbitraries.oneOf(
                Arbitraries.longs().between(46L, 1000L),
                Arbitraries.longs().between(-1000L, 0L)
        );
    }

    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
                validCategoryIds()
        ).as(Product::new);
    }

    @Provide
    Arbitrary<Product> invalidProducts() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 10000L),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100),
                invalidCategoryIds()
        ).as(Product::new);
    }

    @Provide
    Arbitrary<List<Product>> validProductLists() {
        return validProducts().list().ofMinSize(0).ofMaxSize(20);
    }

    @Provide
    Arbitrary<List<Product>> mixedProductLists() {
        return Arbitraries.oneOf(validProducts(), invalidProducts())
                .list().ofMinSize(1).ofMaxSize(20);
    }

    // ========== Property 8: 品类限制有效性 ==========

    /**
     * Property 8.1: 系统应该有且仅有 45 个预定义品类
     * 
     * 验证品类总数为 45 个，分布在 4 个品类组中。
     */
    @Property(tries = 100)
    @Label("Property 8.1: 系统有且仅有 45 个预定义品类")
    void systemHasExactly45Categories() {
        int categoryCount = categoryService.getCategoryCount();
        int groupCount = categoryService.getCategoryGroupCount();

        assert categoryCount == EXPECTED_CATEGORY_COUNT :
                String.format("品类总数应该是 %d，实际是 %d", EXPECTED_CATEGORY_COUNT, categoryCount);
        assert groupCount == EXPECTED_GROUP_COUNT :
                String.format("品类组总数应该是 %d，实际是 %d", EXPECTED_GROUP_COUNT, groupCount);
    }

    /**
     * Property 8.2: 有效品类 ID 应该被正确识别
     * 
     * *对于任意* 1-45 范围内的品类 ID，应该被识别为有效品类。
     */
    @Property(tries = 100)
    @Label("Property 8.2: 有效品类 ID 被正确识别")
    void validCategoryIdIsRecognized(
            @ForAll("validCategoryIds") Long categoryId
    ) {
        boolean isValid = categoryService.isValidCategoryId(categoryId);

        assert isValid :
                String.format("品类 ID %d 应该是有效的", categoryId);
    }

    /**
     * Property 8.3: 无效品类 ID 应该被正确拒绝
     * 
     * *对于任意* 超出 1-45 范围的品类 ID，应该被识别为无效品类。
     */
    @Property(tries = 100)
    @Label("Property 8.3: 无效品类 ID 被正确拒绝")
    void invalidCategoryIdIsRejected(
            @ForAll("invalidCategoryIds") Long categoryId
    ) {
        boolean isValid = categoryService.isValidCategoryId(categoryId);

        assert !isValid :
                String.format("品类 ID %d 应该是无效的", categoryId);
    }

    /**
     * Property 8.4: 属于有效品类的产品应该通过验证
     * 
     * *对于任意* 品类 ID 在 1-45 范围内的产品，应该通过品类验证。
     */
    @Property(tries = 100)
    @Label("Property 8.4: 有效品类产品通过验证")
    void productWithValidCategoryPassesValidation(
            @ForAll("validProducts") Product product
    ) {
        boolean isValid = categoryService.validateProductCategory(product);

        assert isValid :
                String.format("产品 %s（品类 ID: %d）应该通过品类验证",
                        product.title(), product.categoryId());
    }

    /**
     * Property 8.5: 属于无效品类的产品应该被拒绝
     * 
     * *对于任意* 品类 ID 超出 1-45 范围的产品，应该被品类验证拒绝。
     */
    @Property(tries = 100)
    @Label("Property 8.5: 无效品类产品被拒绝")
    void productWithInvalidCategoryFailsValidation(
            @ForAll("invalidProducts") Product product
    ) {
        boolean isValid = categoryService.validateProductCategory(product);

        assert !isValid :
                String.format("产品 %s（品类 ID: %d）应该被品类验证拒绝",
                        product.title(), product.categoryId());
    }

    /**
     * Property 8.6: 所有有效品类产品列表应该全部通过验证
     * 
     * *对于任意* 产品列表，如果所有产品的品类 ID 都在 1-45 范围内，
     * 则整个列表应该通过验证。
     */
    @Property(tries = 100)
    @Label("Property 8.6: 有效品类产品列表全部通过验证")
    void allValidCategoryProductsPassValidation(
            @ForAll("validProductLists") List<Product> products
    ) {
        boolean allValid = categoryService.validateAllProductCategories(products);

        assert allValid :
                "所有有效品类的产品列表应该通过验证";
    }

    /**
     * Property 8.7: 过滤后的产品列表只包含有效品类产品
     * 
     * *对于任意* 混合产品列表，过滤后的结果应该只包含有效品类的产品。
     */
    @Property(tries = 100)
    @Label("Property 8.7: 过滤后只包含有效品类产品")
    void filteredProductsOnlyContainValidCategories(
            @ForAll("mixedProductLists") List<Product> products
    ) {
        List<Product> filtered = categoryService.filterValidCategoryProducts(products);

        // 验证过滤后的所有产品都属于有效品类
        boolean allValid = filtered.stream()
                .allMatch(p -> categoryService.isValidCategoryId(p.categoryId()));

        assert allValid :
                "过滤后的产品列表应该只包含有效品类的产品";

        // 验证过滤后的数量不超过原始数量
        assert filtered.size() <= products.size() :
                "过滤后的产品数量不应超过原始数量";
    }

    /**
     * Property 8.8: 每个品类组都包含正确数量的品类
     * 
     * 验证四大品类组的品类数量分布正确：
     * - 工业用品: 12 个
     * - 节日装饰: 10 个
     * - 家居生活与百货: 13 个
     * - 数码配件与小家电: 10 个
     */
    @Property(tries = 100)
    @Label("Property 8.8: 品类组包含正确数量的品类")
    void categoryGroupsHaveCorrectCounts() {
        List<CategoryGroup> groups = categoryService.getAllCategories();

        // 验证品类组数量
        assert groups.size() == 4 :
                String.format("应该有 4 个品类组，实际有 %d 个", groups.size());

        // 验证各组品类数量
        Map<String, Integer> expectedCounts = Map.of(
                "工业用品", 12,
                "节日装饰", 10,
                "家居生活与百货", 13,
                "数码配件与小家电", 10
        );

        for (CategoryGroup group : groups) {
            Integer expectedCount = expectedCounts.get(group.name());
            assert expectedCount != null :
                    String.format("未知的品类组: %s", group.name());
            assert group.categories().size() == expectedCount :
                    String.format("品类组 '%s' 应该有 %d 个品类，实际有 %d 个",
                            group.name(), expectedCount, group.categories().size());
        }
    }

    /**
     * Property 8.9: null 品类 ID 应该被拒绝
     * 
     * 验证 null 品类 ID 被正确处理为无效。
     */
    @Property(tries = 100)
    @Label("Property 8.9: null 品类 ID 被拒绝")
    void nullCategoryIdIsRejected() {
        boolean isValid = categoryService.isValidCategoryId(null);

        assert !isValid :
                "null 品类 ID 应该被拒绝";
    }

    /**
     * Property 8.10: 品类 ID 验证是确定性的
     * 
     * *对于任意* 品类 ID，多次验证应该返回相同的结果。
     */
    @Property(tries = 100)
    @Label("Property 8.10: 品类 ID 验证是确定性的")
    void categoryIdValidationIsDeterministic(
            @ForAll("validCategoryIds") Long categoryId
    ) {
        boolean result1 = categoryService.isValidCategoryId(categoryId);
        boolean result2 = categoryService.isValidCategoryId(categoryId);
        boolean result3 = categoryService.isValidCategoryId(categoryId);

        assert result1 == result2 && result2 == result3 :
                "品类 ID 验证应该是确定性的，多次调用应返回相同结果";
    }
}
