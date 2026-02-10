package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.*;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.entity.CategoryGroup;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 爆品推荐转换器
 * 负责 HotProductScore 和 DTO 之间的转换
 */
@Component
@RequiredArgsConstructor
public class HotProductAssembler {

    private final ProductAssembler productAssembler;
    private final CategoryAssembler categoryAssembler;

    /**
     * 转换为爆品DTO
     *
     * @param hotProductScore 爆品评分
     * @param product 产品实体
     * @param previousRank 前一天的排名（用于计算排名变化）
     * @return 爆品DTO
     */
    public HotProductDTO toDTO(HotProductScore hotProductScore, Product product, Integer previousRank) {
        if (hotProductScore == null) {
            return null;
        }

        // 计算排名变化
        Integer rankChange = null;
        if (previousRank != null && hotProductScore.getRankInCategory() != null) {
            rankChange = previousRank - hotProductScore.getRankInCategory();
        }

        return HotProductDTO.builder()
                .product(productAssembler.toProductItemRes(product))
                .hotScore(hotProductScore.getHotScore())
                .rankInCategory(hotProductScore.getRankInCategory())
                .daysOnList(hotProductScore.getDaysOnList())
                .rankChange(rankChange)
                .recommendation(hotProductScore.getRecommendation())
                .recommendDate(hotProductScore.getRecommendDate())
                .build();
    }

    /**
     * 转换为爆品DTO（不计算排名变化）
     *
     * @param hotProductScore 爆品评分
     * @param product 产品实体
     * @return 爆品DTO
     */
    public HotProductDTO toDTO(HotProductScore hotProductScore, Product product) {
        return toDTO(hotProductScore, product, null);
    }

    /**
     * 转换为爆品DTO列表
     *
     * @param hotProductScores 爆品评分列表
     * @param products 产品实体列表
     * @return 爆品DTO列表
     */
    public List<HotProductDTO> toDTOList(List<HotProductScore> hotProductScores, List<Product> products) {
        if (hotProductScores == null || hotProductScores.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建产品ID到产品的映射
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return hotProductScores.stream()
                .map(score -> {
                    Product product = productMap.get(score.getProductId());
                    return toDTO(score, product);
                })
                .filter(dto -> dto.getProduct() != null) // 过滤掉产品不存在的
                .collect(Collectors.toList());
    }

    /**
     * 转换为爆品分组
     *
     * @param categoryGroup 品类组
     * @param hotProductDTOs 爆品DTO列表
     * @return 爆品分组
     */
    public HotProductGroup toGroup(CategoryGroup categoryGroup, List<HotProductDTO> hotProductDTOs) {
        return HotProductGroup.builder()
                .categoryGroup(categoryAssembler.toCategoryGroupRes(categoryGroup))
                .products(hotProductDTOs)
                .build();
    }

    /**
     * 转换为爆品推荐列表响应
     *
     * @param date 推荐日期
     * @param groups 爆品分组列表
     * @return 爆品推荐列表响应
     */
    public HotProductsRes toHotProductsRes(java.time.LocalDate date, List<HotProductGroup> groups) {
        int total = groups.stream()
                .mapToInt(group -> group.getProducts().size())
                .sum();

        return HotProductsRes.builder()
                .date(date)
                .groups(groups)
                .total(total)
                .build();
    }

    /**
     * 转换为爆品历史趋势点
     *
     * @param hotProductScore 爆品评分
     * @return 爆品历史趋势点
     */
    public HotProductHistoryPoint toHistoryPoint(HotProductScore hotProductScore) {
        if (hotProductScore == null) {
            return null;
        }

        return HotProductHistoryPoint.builder()
                .date(hotProductScore.getRecommendDate())
                .rank(hotProductScore.getRankInCategory())
                .hotScore(hotProductScore.getHotScore())
                .build();
    }

    /**
     * 转换为爆品历史趋势响应
     *
     * @param productId 产品ID
     * @param hotProductScores 爆品评分列表
     * @return 爆品历史趋势响应
     */
    public HotProductHistoryRes toHistoryRes(Long productId, List<HotProductScore> hotProductScores) {
        List<HotProductHistoryPoint> history = hotProductScores.stream()
                .map(this::toHistoryPoint)
                .collect(Collectors.toList());

        return HotProductHistoryRes.builder()
                .productId(productId)
                .history(history)
                .build();
    }
}
