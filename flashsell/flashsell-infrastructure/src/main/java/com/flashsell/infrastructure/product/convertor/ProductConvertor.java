package com.flashsell.infrastructure.product.convertor;

import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.infrastructure.product.dataobject.ProductDO;
import com.flashsell.infrastructure.product.dataobject.ProductPriceHistoryDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品转换器
 * 负责 ProductDO/ProductPriceHistoryDO 和领域实体之间的转换
 */
@Component
public class ProductConvertor {

    /**
     * 将产品数据对象转换为领域实体
     *
     * @param productDO 产品数据对象
     * @return 产品领域实体
     */
    public Product toEntity(ProductDO productDO) {
        if (productDO == null) {
            return null;
        }

        return Product.builder()
                .id(productDO.getId())
                .asin(productDO.getAsin())
                .title(productDO.getTitle())
                .imageUrl(productDO.getImageUrl())
                .currentPrice(productDO.getCurrentPrice())
                .bsrRank(productDO.getBsrRank())
                .reviewCount(productDO.getReviewCount())
                .rating(productDO.getRating())
                .categoryId(productDO.getCategoryId())
                .competitionScore(productDO.getCompetitionScore())
                .aiRecommendation(productDO.getAiRecommendation())
                .lastUpdated(productDO.getLastUpdated())
                .createdAt(productDO.getCreatedAt())
                .build();
    }

    /**
     * 将产品领域实体转换为数据对象
     *
     * @param product 产品领域实体
     * @return 产品数据对象
     */
    public ProductDO toDataObject(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDO.builder()
                .id(product.getId())
                .asin(product.getAsin())
                .title(product.getTitle())
                .imageUrl(product.getImageUrl())
                .currentPrice(product.getCurrentPrice())
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .categoryId(product.getCategoryId())
                .competitionScore(product.getCompetitionScore())
                .aiRecommendation(product.getAiRecommendation())
                .lastUpdated(product.getLastUpdated())
                .createdAt(product.getCreatedAt())
                .build();
    }

    /**
     * 将产品数据对象列表转换为领域实体列表
     *
     * @param productDOList 产品数据对象列表
     * @return 产品领域实体列表
     */
    public List<Product> toEntityList(List<ProductDO> productDOList) {
        if (productDOList == null) {
            return List.of();
        }
        return productDOList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将价格历史数据对象转换为领域实体
     *
     * @param priceHistoryDO 价格历史数据对象
     * @return 价格点领域实体
     */
    public PricePoint toPricePointEntity(ProductPriceHistoryDO priceHistoryDO) {
        if (priceHistoryDO == null) {
            return null;
        }

        return PricePoint.builder()
                .id(priceHistoryDO.getId())
                .productId(priceHistoryDO.getProductId())
                .price(priceHistoryDO.getPrice())
                .recordedDate(priceHistoryDO.getRecordedDate())
                .createdAt(priceHistoryDO.getCreatedAt())
                .build();
    }

    /**
     * 将价格点领域实体转换为数据对象
     *
     * @param pricePoint 价格点领域实体
     * @return 价格历史数据对象
     */
    public ProductPriceHistoryDO toPriceHistoryDataObject(PricePoint pricePoint) {
        if (pricePoint == null) {
            return null;
        }

        return ProductPriceHistoryDO.builder()
                .id(pricePoint.getId())
                .productId(pricePoint.getProductId())
                .price(pricePoint.getPrice())
                .recordedDate(pricePoint.getRecordedDate())
                .createdAt(pricePoint.getCreatedAt())
                .build();
    }

    /**
     * 将价格历史数据对象列表转换为领域实体列表
     *
     * @param priceHistoryDOList 价格历史数据对象列表
     * @return 价格点领域实体列表
     */
    public List<PricePoint> toPricePointEntityList(List<ProductPriceHistoryDO> priceHistoryDOList) {
        if (priceHistoryDOList == null) {
            return List.of();
        }
        return priceHistoryDOList.stream()
                .map(this::toPricePointEntity)
                .collect(Collectors.toList());
    }
}
