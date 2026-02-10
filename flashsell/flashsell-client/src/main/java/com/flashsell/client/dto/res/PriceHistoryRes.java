package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 价格历史响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryRes {

    /**
     * 价格历史列表
     */
    private List<PricePointRes> prices;
}
