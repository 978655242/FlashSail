package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 最近活动响应 DTO
 * 
 * Requirements: 13.3, 14.3, 14.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityRes {

    /**
     * 最近搜索历史（最多10条）
     */
    private List<SearchHistoryDTO> recentSearches;

    /**
     * 最近浏览产品（最多8个）
     */
    private List<ProductItemRes> recentBrowsed;
}
