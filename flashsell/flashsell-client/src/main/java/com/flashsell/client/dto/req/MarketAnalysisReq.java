package com.flashsell.client.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 市场分析请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisReq {

    /**
     * 品类ID
     */
    @NotNull(message = "品类ID不能为空")
    @Positive(message = "品类ID必须为正数")
    private Long categoryId;

    /**
     * 时间范围（天数）
     * 可选值：30、90、365
     * 默认：30
     */
    private Integer timeRangeDays = 30;
}
