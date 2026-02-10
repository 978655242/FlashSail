package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 爆品历史趋势点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductHistoryPoint {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 爆品评分
     */
    private BigDecimal hotScore;
}
