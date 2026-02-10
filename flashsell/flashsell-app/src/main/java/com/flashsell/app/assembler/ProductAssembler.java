package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.CategoryRes;
import com.flashsell.client.dto.res.PriceHistoryRes;
import com.flashsell.client.dto.res.PricePointRes;
import com.flashsell.client.dto.res.ProductDetailRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.data.entity.DataFreshness;
import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品转换器
 * 负责领域实体和 DTO 之间的转换
 */
@Component
public class ProductAssembler {

    /**
     * 将产品实体转换为产品详情响应 DTO
     *
     * @param product 产品实体
     * @param priceHistory 价格历史列表
     * @param category 品类实体
     * @return 产品详情响应 DTO
     */
    public ProductDetailRes toProductDetailRes(Product product, List<PricePoint> priceHistory, Category category) {
        if (product == null) {
            return null;
        }

        List<PricePointRes> pricePointResList = priceHistory != null
                ? priceHistory.stream()
                        .map(this::toPricePointRes)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        CategoryRes categoryRes = category != null
                ? CategoryRes.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .productCount(category.getProductCount())
                        .build()
                : null;

        return ProductDetailRes.builder()
                .id(product.getId())
                .title(product.getTitle())
                .image(product.getImageUrl())
                .currentPrice(product.getCurrentPrice())
                .priceHistory(pricePointResList)
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .competitionScore(product.getCompetitionScore())
                .aiRecommendation(product.getAiRecommendation())
                .category(categoryRes)
                .lastUpdated(product.getLastUpdated())
                .build();
    }

    /**
     * 将产品实体转换为产品详情响应 DTO（不含价格历史和品类）
     *
     * @param product 产品实体
     * @return 产品详情响应 DTO
     */
    public ProductDetailRes toProductDetailRes(Product product) {
        return toProductDetailRes(product, null, null);
    }

    /**
     * 将价格点实体转换为价格点响应 DTO
     *
     * @param pricePoint 价格点实体
     * @return 价格点响应 DTO
     */
    public PricePointRes toPricePointRes(PricePoint pricePoint) {
        if (pricePoint == null) {
            return null;
        }

        return PricePointRes.builder()
                .date(pricePoint.getRecordedDate())
                .price(pricePoint.getPrice())
                .build();
    }

    /**
     * 将价格点列表转换为价格历史响应 DTO
     *
     * @param pricePoints 价格点列表
     * @return 价格历史响应 DTO
     */
    public PriceHistoryRes toPriceHistoryRes(List<PricePoint> pricePoints) {
        if (pricePoints == null || pricePoints.isEmpty()) {
            return PriceHistoryRes.builder()
                    .prices(Collections.emptyList())
                    .build();
        }

        List<PricePointRes> pricePointResList = pricePoints.stream()
                .map(this::toPricePointRes)
                .collect(Collectors.toList());

        return PriceHistoryRes.builder()
                .prices(pricePointResList)
                .build();
    }

    /**
     * 批量将价格点转换为响应 DTO
     *
     * @param pricePoints 价格点列表
     * @return 价格点响应 DTO 列表
     */
    public List<PricePointRes> toPricePointResList(List<PricePoint> pricePoints) {
        if (pricePoints == null || pricePoints.isEmpty()) {
            return Collections.emptyList();
        }
        return pricePoints.stream()
                .map(this::toPricePointRes)
                .collect(Collectors.toList());
    }

    /**
     * 将产品实体转换为产品详情响应 DTO（带数据时效性标注）
     * 用于返回带有数据新鲜度信息的产品详情
     *
     * @param product 产品实体
     * @param priceHistory 价格历史列表
     * @param category 品类实体
     * @param dataFreshness 数据新鲜度信息
     * @return 产品详情响应 DTO（含数据时效性标注）
     * @see Requirements 15.5, 15.7
     */
    public ProductDetailRes toProductDetailResWithFreshness(
            Product product, 
            List<PricePoint> priceHistory, 
            Category category,
            DataFreshness dataFreshness) {
        
        ProductDetailRes res = toProductDetailRes(product, priceHistory, category);
        
        if (res != null && dataFreshness != null) {
            res.setDataFreshness(dataFreshness.getStatusString());
            res.setFreshnessMessage(dataFreshness.getMessage());
        }
        
        return res;
    }

    /**
     * 将产品实体转换为产品详情响应 DTO（带数据时效性标注，不含价格历史和品类）
     *
     * @param product 产品实体
     * @param dataFreshness 数据新鲜度信息
     * @return 产品详情响应 DTO（含数据时效性标注）
     * @see Requirements 15.5, 15.7
     */
    public ProductDetailRes toProductDetailResWithFreshness(Product product, DataFreshness dataFreshness) {
        return toProductDetailResWithFreshness(product, null, null, dataFreshness);
    }

    /**
     * 将产品实体转换为产品列表项响应 DTO
     * 用于搜索结果列表、仪表盘等场景
     *
     * @param product 产品实体
     * @return 产品列表项响应 DTO
     */
    public com.flashsell.client.dto.res.ProductItemRes toProductItemRes(Product product) {
        if (product == null) {
            return null;
        }

        return com.flashsell.client.dto.res.ProductItemRes.builder()
                .id(product.getId())
                .title(product.getTitle())
                .image(product.getImageUrl())
                .price(product.getCurrentPrice())
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .categoryId(product.getCategoryId())
                .categoryName(null) // 需要调用方单独设置
                .build();
    }
}
