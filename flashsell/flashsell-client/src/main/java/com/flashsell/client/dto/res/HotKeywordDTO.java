package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门关键词 DTO
 * 
 * Requirements: 13.5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotKeywordDTO {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 趋势：UP-上升, DOWN-下降, STABLE-稳定
     */
    private String trend;
}
