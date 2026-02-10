package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销量数据点响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDataPointRes {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 销量（估算值）
     */
    private Long salesVolume;

    /**
     * 平均价格
     */
    private BigDecimal averagePrice;

    /**
     * 产品数量
     */
    private Integer productCount;

    /**
     * 环比增长率（百分比）
     */
    private BigDecimal growthRate;
}
