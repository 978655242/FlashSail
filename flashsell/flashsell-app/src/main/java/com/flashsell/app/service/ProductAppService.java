package com.flashsell.app.service;

import com.flashsell.app.assembler.ProductAssembler;
import com.flashsell.client.dto.res.PriceHistoryRes;
import com.flashsell.client.dto.res.ProductDetailRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.data.entity.DataFreshness;
import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductCacheGateway;
import com.flashsell.domain.product.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品应用服务
 * 提供产品相关的业务编排
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAppService {

    private final ProductDomainService productDomainService;
    private final CategoryGateway categoryGateway;
    private final ProductAssembler productAssembler;
    private final ProductCacheGateway productCacheGateway;

    /**
     * 获取产品详情
     *
     * @param productId 产品ID
     * @return 产品详情响应（可能为空）
     */
    public Optional<ProductDetailRes> getProductDetail(Long productId) {
        log.debug("获取产品详情: productId={}", productId);

        // 1. 先从缓存获取
        Optional<ProductDetailRes> cachedRes = productCacheGateway.getProductDetail(productId);
        if (cachedRes.isPresent()) {
            log.debug("从缓存获取产品详情: productId={}", productId);
            return cachedRes;
        }

        // 2. 从数据库获取
        Optional<Product> productOpt = productDomainService.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("产品不存在: productId={}", productId);
            return Optional.empty();
        }

        Product product = productOpt.get();

        // 3. 获取价格历史
        List<PricePoint> priceHistory = productDomainService.getPriceHistory(productId);

        // 4. 获取品类信息
        Category category = null;
        if (product.getCategoryId() != null) {
            category = categoryGateway.findById(product.getCategoryId()).orElse(null);
        }

        // 5. 组装响应
        ProductDetailRes res = productAssembler.toProductDetailRes(product, priceHistory, category);

        // 6. 缓存结果
        productCacheGateway.cacheProductDetail(productId, res);
        log.debug("缓存产品详情: productId={}", productId);

        return Optional.of(res);
    }

    /**
     * 获取产品价格历史
     *
     * @param productId 产品ID
     * @return 价格历史响应（可能为空）
     */
    public Optional<PriceHistoryRes> getPriceHistory(Long productId) {
        log.debug("获取产品价格历史: productId={}", productId);

        // 检查产品是否存在
        Optional<Product> productOpt = productDomainService.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("产品不存在: productId={}", productId);
            return Optional.empty();
        }

        // 获取价格历史
        List<PricePoint> priceHistory = productDomainService.getPriceHistory(productId);
        PriceHistoryRes res = productAssembler.toPriceHistoryRes(priceHistory);

        return Optional.of(res);
    }

    /**
     * 获取指定日期范围内的产品价格历史
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格历史响应（可能为空）
     */
    public Optional<PriceHistoryRes> getPriceHistoryByDateRange(Long productId, LocalDate startDate, LocalDate endDate) {
        log.debug("获取产品价格历史: productId={}, startDate={}, endDate={}", productId, startDate, endDate);

        // 检查产品是否存在
        Optional<Product> productOpt = productDomainService.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("产品不存在: productId={}", productId);
            return Optional.empty();
        }

        // 获取价格历史
        List<PricePoint> priceHistory = productDomainService.getPriceHistoryByDateRange(productId, startDate, endDate);
        PriceHistoryRes res = productAssembler.toPriceHistoryRes(priceHistory);

        return Optional.of(res);
    }

    /**
     * 检查产品是否存在
     *
     * @param productId 产品ID
     * @return 是否存在
     */
    public boolean existsById(Long productId) {
        return productDomainService.findById(productId).isPresent();
    }

    /**
     * 使产品缓存失效
     *
     * @param productId 产品ID
     */
    public void invalidateProductCache(Long productId) {
        productCacheGateway.invalidateProductDetail(productId);
        log.debug("产品缓存已失效: productId={}", productId);
    }

    /**
     * 获取产品详情（带数据时效性标注）
     * 当数据来自缓存或降级时，会标注数据的新鲜度状态
     *
     * @param productId 产品ID
     * @return 产品详情响应（含数据时效性标注，可能为空）
     * @see Requirements 15.5, 15.7
     */
    public Optional<ProductDetailRes> getProductDetailWithFreshness(Long productId) {
        log.debug("获取产品详情（带时效性标注）: productId={}", productId);

        // 1. 先从缓存获取
        Optional<ProductDetailRes> cachedRes = productCacheGateway.getProductDetail(productId);
        if (cachedRes.isPresent()) {
            log.debug("从缓存获取产品详情: productId={}", productId);
            ProductDetailRes res = cachedRes.get();
            // 标注数据来自缓存
            res.setDataFreshness(DataFreshness.DataStatus.STALE.name());
            res.setFreshnessMessage("数据来自缓存");
            return Optional.of(res);
        }

        // 2. 从数据库获取
        Optional<Product> productOpt = productDomainService.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("产品不存在: productId={}", productId);
            return Optional.empty();
        }

        Product product = productOpt.get();

        // 3. 获取价格历史
        List<PricePoint> priceHistory = productDomainService.getPriceHistory(productId);

        // 4. 获取品类信息
        Category category = null;
        if (product.getCategoryId() != null) {
            category = categoryGateway.findById(product.getCategoryId()).orElse(null);
        }

        // 5. 确定数据新鲜度
        DataFreshness freshness;
        if (product.isDataStale()) {
            freshness = DataFreshness.stale(product.getLastUpdated(), "数据可能已过期，建议刷新");
        } else {
            freshness = DataFreshness.fresh();
        }

        // 6. 组装响应（带数据时效性标注）
        ProductDetailRes res = productAssembler.toProductDetailResWithFreshness(
                product, priceHistory, category, freshness);

        // 7. 缓存结果
        productCacheGateway.cacheProductDetail(productId, res);
        log.debug("缓存产品详情: productId={}", productId);

        return Optional.of(res);
    }
}
