package com.flashsell.domain.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 搜索分析结果实体
 * 包含 AI 对用户查询的分析结果
 * 
 * Requirements: 2.1, 2.6, 2.7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSearchResult {

    /**
     * 提取的搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 推荐的品类ID列表
     */
    private List<Long> categoryIds;

    /**
     * 推荐的品类名称列表
     */
    private List<String> categoryNames;

    /**
     * 建议的最低价格
     */
    private BigDecimal priceMin;

    /**
     * 建议的最高价格
     */
    private BigDecimal priceMax;

    /**
     * 建议的最低评分
     */
    private Double minRating;

    /**
     * AI 搜索摘要
     */
    private String summary;

    /**
     * 原始查询
     */
    private String originalQuery;

    /**
     * 是否解析成功
     */
    private boolean success;

    /**
     * 错误信息（如果解析失败）
     */
    private String errorMessage;

    /**
     * 创建一个成功的结果
     */
    public static AiSearchResult success(List<String> keywords, String summary) {
        return AiSearchResult.builder()
                .keywords(keywords)
                .summary(summary)
                .success(true)
                .build();
    }

    /**
     * 创建一个失败的结果
     */
    public static AiSearchResult failure(String errorMessage) {
        return AiSearchResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 检查是否有价格范围限制
     */
    public boolean hasPriceRange() {
        return priceMin != null || priceMax != null;
    }

    /**
     * 检查是否有品类限制
     */
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }

    /**
     * 检查是否有评分限制
     */
    public boolean hasRatingFilter() {
        return minRating != null && minRating > 0;
    }

    /**
     * 获取主要搜索关键词（第一个）
     */
    public String getPrimaryKeyword() {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        return keywords.get(0);
    }
}
