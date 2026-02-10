package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 热门品类趋势响应 DTO
 * 
 * Requirements: 13.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingCategoriesRes {

    /**
     * 热门品类列表
     */
    private List<TrendingCategoryDTO> categories;
}
