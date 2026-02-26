package com.flashsell.app.service;

import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.data.entity.AlibabaProduct;
import com.flashsell.domain.data.entity.AmazonProduct;
import com.flashsell.domain.data.entity.DataFreshness;
import com.flashsell.domain.data.gateway.BrightDataGateway;
import com.flashsell.domain.data.gateway.DataFallbackGateway;
import com.flashsell.domain.data.gateway.DataFallbackGateway.FallbackResult;
import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 产品数据服务
 * 负责从 Bright Data 获取实时数据并标准化为 Product 实体
 * 
 * 主要功能：
 * 1. Amazon 数据到 Product 实体的转换
 * 2. 1688 数据到 Product 实体的转换
 * 3. 价格转换（USD/CNY）
 * 4. 品类映射
 * 5. 数据新鲜度检查和刷新逻辑
 * 
 * @see Requirements 15.6
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductDataService {

    private final BrightDataGateway brightDataGateway;
    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;
    private final DataFallbackGateway dataFallbackGateway;

    /**
     * USD 到 CNY 的汇率（固定汇率，实际应用中可从配置或API获取）
     */
    private static final BigDecimal USD_TO_CNY_RATE = new BigDecimal("7.2");

    /**
     * CNY 到 USD 的汇率
     */
    private static final BigDecimal CNY_TO_USD_RATE = new BigDecimal("0.139");

    /**
     * 数据新鲜度阈值（小时）
     * Amazon 商品详情缓存 TTL 为 1 小时
     */
    private static final int DATA_FRESHNESS_HOURS = 1;

    /**
     * 品类关键词映射缓存
     * 用于加速品类匹配
     */
    private Map<String, Long> categoryKeywordCache = null;

    /**
     * 搜索并标准化商品数据
     *
     * @param keyword    搜索关键词
     * @param categoryId 品类ID（可选）
     * @return 标准化后的产品列表
     */
    @Transactional
    public List<Product> searchProducts(String keyword, Long categoryId) {
        log.info("搜索商品: keyword={}, categoryId={}", keyword, categoryId);

        // 使用带降级的搜索方法
        ProductSearchResult result = searchProductsWithFallback(keyword, categoryId);
        return result.getProducts();
    }

    /**
     * 搜索商品（带降级支持）
     * 当 Bright Data API 失败时，返回缓存数据并标注数据时效性
     *
     * @param keyword    搜索关键词
     * @param categoryId 品类ID（可选）
     * @return 带数据时效性标注的搜索结果
     * @see Requirements 15.5, 15.7
     */
    @Transactional
    public ProductSearchResult searchProductsWithFallback(String keyword, Long categoryId) {
        log.info("搜索商品（带降级）: keyword={}, categoryId={}", keyword, categoryId);

        // 1. 使用降级服务获取 Amazon 数据
        FallbackResult<List<AmazonProduct>> fallbackResult = 
                dataFallbackGateway.searchAmazonProductsWithFallback(keyword, "amazon.com");

        // 2. 标准化数据
        List<Product> products = new ArrayList<>();
        log.info("开始处理搜索结果: hasData={}, dataSize={}",
                fallbackResult.hasData(),
                fallbackResult.getData() != null ? fallbackResult.getData().size() : "null");

        if (fallbackResult.hasData()) {
            for (AmazonProduct amazonProduct : fallbackResult.getData()) {
                try {
                    log.debug("处理产品: asin={}, title={}", amazonProduct.getAsin(), amazonProduct.getTitle());
                    Product product = convertAmazonToProduct(amazonProduct);

                    if (product == null) {
                        log.warn("转换产品失败，结果为null: asin={}", amazonProduct.getAsin());
                        continue;
                    }

                    // 3. 如果指定了品类，过滤不匹配的产品
                    if (categoryId != null && !categoryId.equals(product.getCategoryId())) {
                        log.debug("品类不匹配，跳过: asin={}, productCategoryId={}, filterCategoryId={}",
                                amazonProduct.getAsin(), product.getCategoryId(), categoryId);
                        continue;
                    }

                    // 4. 只有新鲜数据才保存到数据库
                    if (fallbackResult.isFresh()) {
                        product = productGateway.saveOrUpdate(product);
                        savePriceHistory(product);
                    }

                    products.add(product);
                    log.debug("产品添加成功: asin={}", amazonProduct.getAsin());
                } catch (Exception e) {
                    log.warn("转换商品数据失败: asin={}, error={}", amazonProduct.getAsin(), e.getMessage(), e);
                }
            }
        }

        log.info("搜索完成: keyword={}, 结果数量={}, 数据状态={}", 
                keyword, products.size(), fallbackResult.getStatusString());

        return ProductSearchResult.builder()
                .products(products)
                .dataFreshness(fallbackResult.isFresh() ? DataFreshness.fresh() : 
                        (fallbackResult.isStale() ? DataFreshness.stale(fallbackResult.getFetchedAt(), fallbackResult.getMessage()) : 
                                DataFreshness.empty()))
                .build();
    }

    /**
     * 获取商品详情（实时 + 缓存）
     *
     * @param asin Amazon 标准识别号
     * @return 产品实体
     */
    @Transactional
    public Product getProductDetail(String asin) {
        log.debug("获取商品详情: asin={}", asin);

        // 使用带降级的方法
        ProductDetailResult result = getProductDetailWithFallback(asin);
        return result.getProduct();
    }

    /**
     * 获取商品详情（带降级支持）
     * 当 Bright Data API 失败时，返回缓存数据并标注数据时效性
     *
     * @param asin Amazon 标准识别号
     * @return 带数据时效性标注的产品详情
     * @see Requirements 15.5, 15.7
     */
    @Transactional
    public ProductDetailResult getProductDetailWithFallback(String asin) {
        log.debug("获取商品详情（带降级）: asin={}", asin);

        // 1. 先查本地数据库
        Optional<Product> localProduct = productGateway.findByAsin(asin);

        // 2. 如果数据不存在或已过期，从 Bright Data 刷新（带降级）
        if (localProduct.isEmpty() || isDataStale(localProduct.get())) {
            log.info("从 Bright Data 刷新商品数据（带降级）: asin={}", asin);

            FallbackResult<AmazonProduct> fallbackResult = 
                    dataFallbackGateway.getAmazonProductWithFallback(asin);

            if (fallbackResult.hasData() && fallbackResult.getData() != null) {
                Product product = convertAmazonToProduct(fallbackResult.getData());
                
                // 只有新鲜数据才保存到数据库
                if (fallbackResult.isFresh()) {
                    product = productGateway.saveOrUpdate(product);
                    savePriceHistory(product);
                }

                return ProductDetailResult.builder()
                        .product(product)
                        .dataFreshness(fallbackResult.isFresh() ? DataFreshness.fresh() : 
                                DataFreshness.stale(fallbackResult.getFetchedAt(), fallbackResult.getMessage()))
                        .build();
            }

            // 如果降级也没有数据，返回本地数据（如果有）
            if (localProduct.isPresent()) {
                return ProductDetailResult.builder()
                        .product(localProduct.get())
                        .dataFreshness(DataFreshness.stale(localProduct.get().getLastUpdated(), "数据来自本地缓存，可能已过期"))
                        .build();
            }

            // 完全没有数据
            return ProductDetailResult.builder()
                    .product(null)
                    .dataFreshness(DataFreshness.empty())
                    .build();
        }

        // 本地数据新鲜，直接返回
        return ProductDetailResult.builder()
                .product(localProduct.get())
                .dataFreshness(DataFreshness.fresh())
                .build();
    }

    /**
     * 根据产品ID获取商品详情（实时 + 缓存）
     *
     * @param productId 产品ID
     * @return 产品实体
     */
    @Transactional
    public Product getProductDetailById(Long productId) {
        Optional<Product> product = productGateway.findById(productId);
        if (product.isEmpty()) {
            return null;
        }

        // 如果数据过期，尝试刷新
        if (isDataStale(product.get()) && product.get().getAsin() != null) {
            return getProductDetail(product.get().getAsin());
        }

        return product.get();
    }

    /**
     * 批量获取商品详情
     *
     * @param asins ASIN 列表（最多10个）
     * @return 产品列表
     */
    @Transactional
    public List<Product> batchGetProducts(List<String> asins) {
        if (asins == null || asins.isEmpty()) {
            return new ArrayList<>();
        }

        // 限制批量请求数量
        if (asins.size() > 10) {
            asins = asins.subList(0, 10);
        }

        // 构建 URL 列表
        List<String> urls = asins.stream()
                .map(asin -> "https://www.amazon.com/dp/" + asin)
                .toList();

        // 批量获取
        List<AmazonProduct> amazonProducts = brightDataGateway.batchGetProducts(urls);

        // 转换并保存
        List<Product> products = new ArrayList<>();
        for (AmazonProduct amazonProduct : amazonProducts) {
            try {
                Product product = convertAmazonToProduct(amazonProduct);
                product = productGateway.saveOrUpdate(product);
                savePriceHistory(product);
                products.add(product);
            } catch (Exception e) {
                log.warn("转换商品数据失败: asin={}, error={}", amazonProduct.getAsin(), e.getMessage());
            }
        }

        return products;
    }

    /**
     * 将 Amazon 商品数据转换为 Product 实体
     * 保持价格为美元（Amazon 原始货币）
     *
     * @param amazon Amazon 商品数据
     * @return Product 实体
     */
    public Product convertAmazonToProduct(AmazonProduct amazon) {
        if (amazon == null) {
            return null;
        }
        
        Product product = Product.builder()
                .asin(amazon.getAsin())
                .title(amazon.getTitle())
                .imageUrl(amazon.getImageUrl())
                .currentPrice(amazon.getPrice())
                .bsrRank(amazon.getBsrRank())
                .reviewCount(amazon.getReviewCount())
                .rating(amazon.getRating())
                .lastUpdated(LocalDateTime.now())
                .build();

        // 映射品类
        if (amazon.getCategory() != null) {
            Long categoryId = mapCategory(amazon.getCategory());
            product.setCategoryId(categoryId);
        }

        return product;
    }

    /**
     * 将 Amazon 商品数据转换为 Product 实体（带品类ID覆盖）
     *
     * @param amazon     Amazon 商品数据
     * @param categoryId 指定的品类ID（覆盖自动映射）
     * @return Product 实体
     */
    public Product convertAmazonToProduct(AmazonProduct amazon, Long categoryId) {
        Product product = convertAmazonToProduct(amazon);
        if (product != null && categoryId != null) {
            product.setCategoryId(categoryId);
        }
        return product;
    }

    /**
     * 将 1688 商品数据转换为 Product 实体
     * 价格从人民币转换为美元
     *
     * @param alibaba 1688 商品数据
     * @return Product 实体
     */
    public Product convertAlibabaToProduct(AlibabaProduct alibaba) {
        if (alibaba == null) {
            return null;
        }
        
        // 将人民币价格转换为美元
        BigDecimal priceInUsd = convertCnyToUsd(alibaba.getPrice());

        Product product = Product.builder()
                .asin(alibaba.getOfferId()) // 使用 offerId 作为标识
                .title(alibaba.getTitle())
                .imageUrl(alibaba.getImageUrl())
                .currentPrice(priceInUsd)
                .rating(alibaba.getRating())
                .lastUpdated(LocalDateTime.now())
                .build();
        
        // 尝试映射品类（基于标题关键词）
        if (alibaba.getTitle() != null) {
            Long categoryId = mapCategoryByKeywords(alibaba.getTitle());
            product.setCategoryId(categoryId);
        }
        
        return product;
    }

    /**
     * 根据标题关键词映射品类
     *
     * @param title 商品标题
     * @return 品类ID，如果无法映射则返回 null
     */
    public Long mapCategoryByKeywords(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        
        // 初始化品类关键词缓存（懒加载）
        if (categoryKeywordCache == null) {
            initCategoryKeywordCache();
        }
        
        String normalizedTitle = title.toLowerCase();
        List<Category> allCategories = categoryGateway.findAll();
        
        // 尝试匹配品类名称
        for (Category cat : allCategories) {
            String catName = cat.getName().toLowerCase();
            if (normalizedTitle.contains(catName)) {
                return cat.getId();
            }
        }
        
        return null;
    }

    /**
     * 将人民币转换为美元
     *
     * @param cny 人民币金额
     * @return 美元金额
     */
    public BigDecimal convertCnyToUsd(BigDecimal cny) {
        if (cny == null) {
            return null;
        }
        return cny.multiply(CNY_TO_USD_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 将美元转换为人民币
     *
     * @param usd 美元金额
     * @return 人民币金额
     */
    public BigDecimal convertUsdToCny(BigDecimal usd) {
        if (usd == null) {
            return null;
        }
        return usd.multiply(USD_TO_CNY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 映射品类
     * 根据 Amazon 品类名称或 ID 映射到系统内部品类
     *
     * @param amazonCategory Amazon 品类名称或 ID
     * @return 品类ID，如果无法映射则返回 null
     */
    public Long mapCategory(String amazonCategory) {
        if (amazonCategory == null || amazonCategory.isEmpty()) {
            return null;
        }

        // 1. 尝试通过 Amazon 品类 ID 精确查找
        Optional<Category> category = categoryGateway.findByAmazonCategoryId(amazonCategory);
        if (category.isPresent()) {
            return category.get().getId();
        }

        // 2. 初始化品类关键词缓存（懒加载）
        if (categoryKeywordCache == null) {
            initCategoryKeywordCache();
        }

        // 3. 尝试通过关键词缓存快速匹配
        String normalizedCategory = amazonCategory.toLowerCase().trim();
        if (categoryKeywordCache.containsKey(normalizedCategory)) {
            return categoryKeywordCache.get(normalizedCategory);
        }

        // 4. 尝试通过品类名称模糊匹配
        List<Category> allCategories = categoryGateway.findAll();
        for (Category cat : allCategories) {
            String catName = cat.getName().toLowerCase();
            // 双向包含匹配
            if (normalizedCategory.contains(catName) || catName.contains(normalizedCategory)) {
                // 缓存匹配结果
                categoryKeywordCache.put(normalizedCategory, cat.getId());
                return cat.getId();
            }
        }

        // 5. 尝试通过关键词分词匹配
        String[] keywords = normalizedCategory.split("[\\s,&/]+");
        for (Category cat : allCategories) {
            String catName = cat.getName().toLowerCase();
            for (String keyword : keywords) {
                if (keyword.length() >= 3 && catName.contains(keyword)) {
                    categoryKeywordCache.put(normalizedCategory, cat.getId());
                    return cat.getId();
                }
            }
        }

        log.debug("无法映射品类: {}", amazonCategory);
        return null;
    }

    /**
     * 初始化品类关键词缓存
     * 预加载常见的品类关键词映射
     */
    private void initCategoryKeywordCache() {
        categoryKeywordCache = new HashMap<>();
        List<Category> allCategories = categoryGateway.findAll();
        
        for (Category cat : allCategories) {
            // 添加品类名称的小写形式
            categoryKeywordCache.put(cat.getName().toLowerCase(), cat.getId());
            
            // 如果有 Amazon 品类 ID，也添加到缓存
            if (cat.getAmazonCategoryId() != null && !cat.getAmazonCategoryId().isEmpty()) {
                categoryKeywordCache.put(cat.getAmazonCategoryId().toLowerCase(), cat.getId());
            }
        }
        
        log.info("品类关键词缓存初始化完成，共 {} 个条目", categoryKeywordCache.size());
    }

    /**
     * 清除品类关键词缓存
     * 当品类数据更新时调用
     */
    public void clearCategoryCache() {
        categoryKeywordCache = null;
        log.info("品类关键词缓存已清除");
    }

    /**
     * 检查数据是否过期
     *
     * @param product 产品实体
     * @return 是否过期
     */
    public boolean isDataStale(Product product) {
        if (product == null || product.getLastUpdated() == null) {
            return true;
        }
        return product.getLastUpdated().isBefore(
                LocalDateTime.now().minusHours(DATA_FRESHNESS_HOURS));
    }

    /**
     * 检查数据新鲜度并返回状态
     *
     * @param product 产品实体
     * @return 数据新鲜度状态（FRESH, STALE, UNKNOWN）
     */
    public String getDataFreshnessStatus(Product product) {
        if (product == null || product.getLastUpdated() == null) {
            return "UNKNOWN";
        }
        return isDataStale(product) ? "STALE" : "FRESH";
    }

    /**
     * 获取数据的剩余有效时间（秒）
     *
     * @param product 产品实体
     * @return 剩余有效时间（秒），如果已过期返回 0
     */
    public long getRemainingFreshnessSeconds(Product product) {
        if (product == null || product.getLastUpdated() == null) {
            return 0;
        }
        LocalDateTime expiryTime = product.getLastUpdated().plusHours(DATA_FRESHNESS_HOURS);
        if (expiryTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
    }

    /**
     * 强制刷新产品数据
     * 无论数据是否过期，都从 Bright Data 获取最新数据
     *
     * @param asin Amazon 标准识别号
     * @return 刷新后的产品实体
     */
    @Transactional
    public Product forceRefreshProduct(String asin) {
        log.info("强制刷新商品数据: asin={}", asin);
        
        AmazonProduct amazonProduct = brightDataGateway.getAmazonProductByAsin(asin);
        if (amazonProduct != null) {
            Product product = convertAmazonToProduct(amazonProduct);
            product = productGateway.saveOrUpdate(product);
            savePriceHistory(product);
            return product;
        }
        
        return null;
    }

    /**
     * 搜索 1688 商品并标准化
     *
     * @param keyword 搜索关键词
     * @return 标准化后的产品列表
     */
    @Transactional
    public List<Product> search1688Products(String keyword) {
        log.info("搜索 1688 商品: keyword={}", keyword);

        // 1. 从 1688 获取实时数据
        List<AlibabaProduct> alibabaProducts = brightDataGateway.scrape1688Products(keyword);

        // 2. 标准化数据
        List<Product> products = new ArrayList<>();
        for (AlibabaProduct alibabaProduct : alibabaProducts) {
            try {
                if (!alibabaProduct.isValid()) {
                    log.warn("1688 商品数据无效: offerId={}", alibabaProduct.getOfferId());
                    continue;
                }
                
                Product product = convertAlibabaToProduct(alibabaProduct);
                
                // 3. 保存到数据库（更新或插入）
                product = productGateway.saveOrUpdate(product);
                
                // 4. 记录价格历史
                savePriceHistory(product);
                
                products.add(product);
            } catch (Exception e) {
                log.warn("转换 1688 商品数据失败: offerId={}, error={}", 
                        alibabaProduct.getOfferId(), e.getMessage());
            }
        }

        log.info("1688 搜索完成: keyword={}, 结果数量={}", keyword, products.size());
        return products;
    }

    /**
     * 合并搜索 Amazon 和 1688 商品
     *
     * @param keyword    搜索关键词
     * @param categoryId 品类ID（可选）
     * @param includeAlibaba 是否包含 1688 数据
     * @return 标准化后的产品列表
     */
    @Transactional
    public List<Product> searchAllPlatforms(String keyword, Long categoryId, boolean includeAlibaba) {
        List<Product> allProducts = new ArrayList<>();
        
        // 搜索 Amazon
        allProducts.addAll(searchProducts(keyword, categoryId));
        
        // 搜索 1688（如果需要）
        if (includeAlibaba) {
            allProducts.addAll(search1688Products(keyword));
        }
        
        return allProducts;
    }

    /**
     * 保存价格历史记录
     *
     * @param product 产品实体
     */
    private void savePriceHistory(Product product) {
        if (product.getId() == null || product.getCurrentPrice() == null) {
            return;
        }

        // 检查今天是否已有记录
        Optional<PricePoint> latestPrice = productGateway.findLatestPricePoint(product.getId());
        if (latestPrice.isPresent()
                && latestPrice.get().getRecordedDate().equals(LocalDate.now())) {
            // 今天已有记录，更新价格
            PricePoint pricePoint = latestPrice.get();
            pricePoint.setPrice(product.getCurrentPrice());
            productGateway.savePricePoint(pricePoint);
        } else {
            // 创建新记录
            PricePoint pricePoint = PricePoint.builder()
                    .productId(product.getId())
                    .price(product.getCurrentPrice())
                    .recordedDate(LocalDate.now())
                    .build();
            productGateway.savePricePoint(pricePoint);
        }
    }

    // ==================== 结果包装类 ====================

    /**
     * 产品搜索结果（带数据时效性标注）
     * Requirements: 15.5, 15.7
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchResult {
        /**
         * 产品列表
         */
        private List<Product> products;

        /**
         * 数据新鲜度信息
         */
        private DataFreshness dataFreshness;

        /**
         * 是否为新鲜数据
         */
        public boolean isFresh() {
            return dataFreshness != null && dataFreshness.isFresh();
        }

        /**
         * 是否为过期数据
         */
        public boolean isStale() {
            return dataFreshness != null && dataFreshness.isStale();
        }

        /**
         * 获取数据状态字符串
         */
        public String getDataStatusString() {
            return dataFreshness != null ? dataFreshness.getStatusString() : "UNKNOWN";
        }

        /**
         * 获取提示信息
         */
        public String getFreshnessMessage() {
            return dataFreshness != null ? dataFreshness.getMessage() : null;
        }

        /**
         * 获取数据获取时间
         */
        public LocalDateTime getFetchedAt() {
            return dataFreshness != null ? dataFreshness.getFetchedAt() : null;
        }
    }

    /**
     * 产品详情结果（带数据时效性标注）
     * Requirements: 15.5, 15.7
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetailResult {
        /**
         * 产品实体
         */
        private Product product;

        /**
         * 数据新鲜度信息
         */
        private DataFreshness dataFreshness;

        /**
         * 是否有数据
         */
        public boolean hasData() {
            return product != null;
        }

        /**
         * 是否为新鲜数据
         */
        public boolean isFresh() {
            return dataFreshness != null && dataFreshness.isFresh();
        }

        /**
         * 是否为过期数据
         */
        public boolean isStale() {
            return dataFreshness != null && dataFreshness.isStale();
        }

        /**
         * 获取数据状态字符串
         */
        public String getDataStatusString() {
            return dataFreshness != null ? dataFreshness.getStatusString() : "UNKNOWN";
        }

        /**
         * 获取提示信息
         */
        public String getFreshnessMessage() {
            return dataFreshness != null ? dataFreshness.getMessage() : null;
        }

        /**
         * 获取数据获取时间
         */
        public LocalDateTime getFetchedAt() {
            return dataFreshness != null ? dataFreshness.getFetchedAt() : null;
        }
    }
}
