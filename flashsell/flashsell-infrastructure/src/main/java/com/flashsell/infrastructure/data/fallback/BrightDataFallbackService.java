package com.flashsell.infrastructure.data.fallback;

import com.flashsell.domain.data.entity.AlibabaProduct;
import com.flashsell.domain.data.entity.AmazonProduct;
import com.flashsell.domain.data.entity.AmazonReview;
import com.flashsell.domain.data.gateway.BrightDataGateway;
import com.flashsell.domain.data.gateway.DataFallbackGateway;
import com.flashsell.infrastructure.data.exception.BrightDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bright Data 降级服务
 * 当 API 请求失败时，返回缓存数据并标注数据时效性
 * 
 * Requirements: 15.5, 15.7
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BrightDataFallbackService implements DataFallbackGateway {

    private final BrightDataGateway brightDataGateway;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 降级缓存前缀
     */
    private static final String FALLBACK_PREFIX = "brightdata:fallback:";

    /**
     * 降级缓存 TTL（24小时）
     */
    private static final long FALLBACK_TTL = 24 * 60 * 60;

    /**
     * 搜索 Amazon 商品（带降级）
     *
     * @param keyword 搜索关键词
     * @param domain  Amazon 域名
     * @return 带时效性标注的结果
     */
    @Override
    public FallbackResult<List<AmazonProduct>> searchAmazonProductsWithFallback(String keyword, String domain) {
        String cacheKey = FALLBACK_PREFIX + "amazon:search:" + keyword.hashCode();

        try {
            List<AmazonProduct> products = brightDataGateway.searchAmazonProducts(keyword, domain);

            // 保存到降级缓存
            saveFallbackCache(cacheKey, products);

            return FallbackResult.fresh(products);
        } catch (BrightDataException e) {
            log.warn("Amazon 搜索失败，尝试降级: keyword={}, error={}", keyword, e.getMessage());
            return getFallbackData(cacheKey);
        }
    }

    /**
     * 获取 Amazon 商品详情（带降级）
     *
     * @param asin Amazon 标准识别号
     * @return 带时效性标注的结果
     */
    @Override
    public FallbackResult<AmazonProduct> getAmazonProductWithFallback(String asin) {
        String cacheKey = FALLBACK_PREFIX + "amazon:product:" + asin;

        try {
            AmazonProduct product = brightDataGateway.getAmazonProductByAsin(asin);

            // 保存到降级缓存
            saveFallbackCache(cacheKey, product);

            return FallbackResult.fresh(product);
        } catch (BrightDataException e) {
            log.warn("获取 Amazon 商品详情失败，尝试降级: asin={}, error={}", asin, e.getMessage());
            return getFallbackData(cacheKey);
        }
    }

    /**
     * 获取 Amazon 商品评论（带降级）
     *
     * @param asin Amazon 标准识别号
     * @return 带时效性标注的结果
     */
    @Override
    public FallbackResult<List<AmazonReview>> getAmazonReviewsWithFallback(String asin) {
        String cacheKey = FALLBACK_PREFIX + "amazon:reviews:" + asin;

        try {
            List<AmazonReview> reviews = brightDataGateway.getAmazonProductReviewsByAsin(asin);

            // 保存到降级缓存
            saveFallbackCache(cacheKey, reviews);

            return FallbackResult.fresh(reviews);
        } catch (BrightDataException e) {
            log.warn("获取 Amazon 商品评论失败，尝试降级: asin={}, error={}", asin, e.getMessage());
            return getFallbackData(cacheKey);
        }
    }

    /**
     * 爬取 1688 商品（带降级）
     *
     * @param keyword 搜索关键词
     * @return 带时效性标注的结果
     */
    @Override
    public FallbackResult<List<AlibabaProduct>> scrape1688ProductsWithFallback(String keyword) {
        String cacheKey = FALLBACK_PREFIX + "1688:search:" + keyword.hashCode();

        try {
            List<AlibabaProduct> products = brightDataGateway.scrape1688Products(keyword);

            // 保存到降级缓存
            saveFallbackCache(cacheKey, products);

            return FallbackResult.fresh(products);
        } catch (BrightDataException e) {
            log.warn("爬取 1688 商品失败，尝试降级: keyword={}, error={}", keyword, e.getMessage());
            return getFallbackData(cacheKey);
        }
    }

    /**
     * 批量获取商品（带降级）
     *
     * @param urls URL 列表
     * @return 带时效性标注的结果
     */
    @Override
    public FallbackResult<List<AmazonProduct>> batchGetProductsWithFallback(List<String> urls) {
        String cacheKey = FALLBACK_PREFIX + "batch:" + urls.hashCode();

        try {
            List<AmazonProduct> products = brightDataGateway.batchGetProducts(urls);

            // 保存到降级缓存
            saveFallbackCache(cacheKey, products);

            return FallbackResult.fresh(products);
        } catch (BrightDataException e) {
            log.warn("批量获取商品失败，尝试降级: count={}, error={}", urls.size(), e.getMessage());
            return getFallbackData(cacheKey);
        } catch (IllegalArgumentException e) {
            // 参数错误不降级，直接抛出
            throw e;
        }
    }

    /**
     * 保存降级缓存
     */
    private void saveFallbackCache(String cacheKey, Object data) {
        if (data == null) {
            return;
        }

        FallbackCacheEntry entry = new FallbackCacheEntry(data, LocalDateTime.now());
        redisTemplate.opsForValue().set(cacheKey, entry, FALLBACK_TTL, TimeUnit.SECONDS);
    }

    /**
     * 获取降级数据
     */
    @SuppressWarnings("unchecked")
    private <T> FallbackResult<T> getFallbackData(String cacheKey) {
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached instanceof FallbackCacheEntry entry) {
            log.info("使用降级缓存数据: key={}, cachedAt={}", cacheKey, entry.getCachedAt());
            return FallbackResult.stale((T) entry.getData(), entry.getCachedAt());
        }

        log.warn("无可用的降级缓存数据: key={}", cacheKey);
        return FallbackResult.empty();
    }

    /**
     * 降级缓存条目
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FallbackCacheEntry implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Object data;
        private LocalDateTime cachedAt;
    }
}
