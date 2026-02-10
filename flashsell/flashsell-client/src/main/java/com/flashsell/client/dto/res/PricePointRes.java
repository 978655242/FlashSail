package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 价格点响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePointRes {

    /**
     * 记录日期
     */
    private LocalDate date;

    /**
     * 价格（美元）
     */
    private BigDecimal price;
}
