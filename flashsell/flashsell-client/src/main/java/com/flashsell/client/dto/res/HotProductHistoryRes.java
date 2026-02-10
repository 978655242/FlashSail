package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爆品历史趋势响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductHistoryRes {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 历史趋势点列表
     */
    private List<HotProductHistoryPoint> history;
}
