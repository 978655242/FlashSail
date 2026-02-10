package com.flashsell.domain.product.gateway;

import java.util.Optional;

/**
 * 产品缓存网关接口
 * 定义产品缓存访问的抽象接口，由 infrastructure 层实现
 * 
 * 注意：此接口使用 Object 类型来避免 domain 层依赖 client 层的 DTO
 * 实现类需要处理具体的类型转换
 */
public interface ProductCacheGateway {

    /**
     * 从缓存获取产品详情
     *
     * @param productId 产品ID
     * @return 产品详情（可能为空）
     */
    <T> Optional<T> getProductDetail(Long productId);

    /**
     * 缓存产品详情
     *
     * @param productId 产品ID
     * @param productDetail 产品详情
     */
    void cacheProductDetail(Long productId, Object productDetail);

    /**
     * 使产品详情缓存失效
     *
     * @param productId 产品ID
     */
    void invalidateProductDetail(Long productId);

    /**
     * 检查产品详情缓存是否存在
     *
     * @param productId 产品ID
     * @return 是否存在
     */
    boolean hasProductDetailCache(Long productId);
}
