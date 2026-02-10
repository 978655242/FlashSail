package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 爆品推荐响应 DTO（Top 4）
 * 
 * Requirements: 13.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotRecommendationsRes {

    /**
     * Top 4 爆品推荐
     */
    private List<HotProductDTO> products;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
