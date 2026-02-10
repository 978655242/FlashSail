package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门品类趋势 DTO
 * 
 * Requirements: 13.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingCategoryDTO {

    /**
     * 品类信息
     */
    private CategoryRes category;

    /**
     * 趋势评分（0.0-100.0）
     */
    private Double trendScore;

    /**
     * 周环比增长率（-1.0 到 1.0）
     */
    private Double weekOverWeek;

    /**
     * 热门产品数量
     */
    private Integer hotProductCount;
}
