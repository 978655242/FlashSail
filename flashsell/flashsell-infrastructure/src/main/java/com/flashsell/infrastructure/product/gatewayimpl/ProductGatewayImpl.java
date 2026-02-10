package com.flashsell.infrastructure.product.gatewayimpl;

import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductCacheGateway;
import com.flashsell.domain.product.gateway.ProductGateway;
import com.flashsell.infrastructure.product.convertor.ProductConvertor;
import com.flashsell.infrastructure.product.dataobject.ProductDO;
import com.flashsell.infrastructure.product.dataobject.ProductPriceHistoryDO;
import com.flashsell.infrastructure.product.mapper.ProductMapper;
import com.flashsell.infrastructure.product.mapper.ProductPriceHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 产品网关实现
 * 实现 ProductGateway 接口，提供产品数据访问的具体实现
 * 包含缓存失效机制
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {

    private final ProductMapper productMapper;
    private final ProductPriceHistoryMapper priceHistoryMapper;
    private final ProductConvertor productConvertor;
    private final ProductCacheGateway productCacheGateway;

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        ProductDO productDO = productMapper.selectById(id);
        return Optional.ofNullable(productConvertor.toEntity(productDO));
    }

    @Override
    public Product findByIdDirect(Long id) {
        if (id == null) {
            return null;
        }
        ProductDO productDO = productMapper.selectById(id);
        return productConvertor.toEntity(productDO);
    }

    @Override
    public int countCreatedAfter(LocalDateTime afterTime) {
        if (afterTime == null) {
            return 0;
        }
        Long count = productMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductDO>()
                        .ge(ProductDO::getCreatedAt, afterTime)
        );
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Optional<Product> findByAsin(String asin) {
        if (asin == null || asin.isEmpty()) {
            return Optional.empty();
        }
        ProductDO productDO = productMapper.selectByAsin(asin);
        return Optional.ofNullable(productConvertor.toEntity(productDO));
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        List<ProductDO> productDOList = productMapper.selectByCategoryId(categoryId);
        return productConvertor.toEntityList(productDOList);
    }

    @Override
    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        ProductDO productDO = productConvertor.toDataObject(product);
        LocalDateTime now = LocalDateTime.now();

        if (productDO.getId() == null) {
            // 新增产品
            productDO.setCreatedAt(now);
            productDO.setLastUpdated(now);
            productMapper.insert(productDO);
        } else {
            // 更新产品
            productDO.setLastUpdated(now);
            productMapper.updateById(productDO);
        }

        // 返回包含生成ID的产品实体
        return productConvertor.toEntity(productDO);
    }

    @Override
    public void update(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product and product ID cannot be null");
        }

        ProductDO productDO = productConvertor.toDataObject(product);
        productDO.setLastUpdated(LocalDateTime.now());
        productMapper.updateById(productDO);

        // 使缓存失效
        productCacheGateway.invalidateProductDetail(product.getId());
        log.debug("产品更新后缓存已失效: productId={}", product.getId());
    }

    @Override
    public Product saveOrUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // 如果有ASIN，先检查是否存在
        if (product.getAsin() != null && !product.getAsin().isEmpty()) {
            ProductDO existingProduct = productMapper.selectByAsin(product.getAsin());
            if (existingProduct != null) {
                // 更新现有产品
                product.setId(existingProduct.getId());
                product.setCreatedAt(existingProduct.getCreatedAt());
                update(product);
                return findById(existingProduct.getId()).orElse(product);
            }
        }

        // 新增产品
        Product savedProduct = save(product);

        // 新增后不需要缓存失效（因为是新数据）
        return savedProduct;
    }

    @Override
    public boolean existsByAsin(String asin) {
        if (asin == null || asin.isEmpty()) {
            return false;
        }
        return productMapper.countByAsin(asin) > 0;
    }

    @Override
    public List<PricePoint> findPriceHistory(Long productId) {
        if (productId == null) {
            return List.of();
        }
        List<ProductPriceHistoryDO> priceHistoryDOList = priceHistoryMapper.selectByProductId(productId);
        return productConvertor.toPricePointEntityList(priceHistoryDOList);
    }

    @Override
    public List<PricePoint> findPriceHistoryByDateRange(Long productId, LocalDate startDate, LocalDate endDate) {
        if (productId == null || startDate == null || endDate == null) {
            return List.of();
        }
        List<ProductPriceHistoryDO> priceHistoryDOList = priceHistoryMapper.selectByProductIdAndDateRange(
                productId, startDate, endDate);
        return productConvertor.toPricePointEntityList(priceHistoryDOList);
    }

    @Override
    public PricePoint savePricePoint(PricePoint pricePoint) {
        if (pricePoint == null) {
            throw new IllegalArgumentException("PricePoint cannot be null");
        }

        ProductPriceHistoryDO priceHistoryDO = productConvertor.toPriceHistoryDataObject(pricePoint);
        LocalDateTime now = LocalDateTime.now();

        if (priceHistoryDO.getId() == null) {
            priceHistoryDO.setCreatedAt(now);
            priceHistoryMapper.insert(priceHistoryDO);
        } else {
            priceHistoryMapper.updateById(priceHistoryDO);
        }

        return productConvertor.toPricePointEntity(priceHistoryDO);
    }

    @Override
    public void savePricePoints(List<PricePoint> pricePoints) {
        if (pricePoints == null || pricePoints.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (PricePoint pricePoint : pricePoints) {
            ProductPriceHistoryDO priceHistoryDO = productConvertor.toPriceHistoryDataObject(pricePoint);
            priceHistoryDO.setCreatedAt(now);
            priceHistoryMapper.insert(priceHistoryDO);
        }
    }

    @Override
    public Optional<PricePoint> findLatestPricePoint(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        ProductPriceHistoryDO priceHistoryDO = priceHistoryMapper.selectLatestByProductId(productId);
        return Optional.ofNullable(productConvertor.toPricePointEntity(priceHistoryDO));
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ProductDO> productDOList = productMapper.selectBatchIds(ids);
        return productConvertor.toEntityList(productDOList);
    }
}
