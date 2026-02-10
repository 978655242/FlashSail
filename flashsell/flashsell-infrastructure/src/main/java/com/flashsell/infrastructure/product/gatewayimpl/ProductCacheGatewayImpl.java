package com.flashsell.infrastructure.product.gatewayimpl;

import com.flashsell.domain.product.gateway.ProductCacheGateway;
import com.flashsell.infrastructure.common.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 产品缓存网关实现
 * 实现 ProductCacheGateway 接口，提供产品缓存访问的具体实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductCacheGatewayImpl implements ProductCacheGateway {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProductDetail(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }

        String cacheKey = CacheConstants.productKey(productId);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取产品详情: productId={}", productId);
                return Optional.of((T) cached);
            }
        } catch (Exception e) {
            log.warn("获取产品缓存失败: productId={}, error={}", productId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void cacheProductDetail(Long productId, Object productDetail) {
        if (productId == null || productDetail == null) {
            return;
        }

        String cacheKey = CacheConstants.productKey(productId);
        try {
            redisTemplate.opsForValue().set(cacheKey, productDetail, CacheConstants.PRODUCT_TTL, TimeUnit.SECONDS);
            log.debug("缓存产品详情: productId={}", productId);
        } catch (Exception e) {
            log.warn("缓存产品详情失败: productId={}, error={}", productId, e.getMessage());
        }
    }

    @Override
    public void invalidateProductDetail(Long productId) {
        if (productId == null) {
            return;
        }

        String cacheKey = CacheConstants.productKey(productId);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("产品缓存已失效: productId={}", productId);
        } catch (Exception e) {
            log.warn("使产品缓存失效失败: productId={}, error={}", productId, e.getMessage());
        }
    }

    @Override
    public boolean hasProductDetailCache(Long productId) {
        if (productId == null) {
            return false;
        }

        String cacheKey = CacheConstants.productKey(productId);
        try {
            Boolean hasKey = redisTemplate.hasKey(cacheKey);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.warn("检查产品缓存失败: productId={}, error={}", productId, e.getMessage());
            return false;
        }
    }
}
