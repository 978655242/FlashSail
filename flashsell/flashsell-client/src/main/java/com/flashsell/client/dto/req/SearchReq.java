package com.flashsell.client.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI 搜索请求 DTO
 * 
 * Requirements: 2.1, 2.2, 2.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchReq {

    /**
     * 自然语言查询
     */
    @NotBlank(message = "搜索查询不能为空")
    @Size(max = 500, message = "搜索查询不能超过500个字符")
    private String query;

    /**
     * 品类ID（可选）
     */
    private Long categoryId;

    /**
     * 最低价格（可选）
     */
    @Min(value = 0, message = "最低价格不能为负数")
    private BigDecimal priceMin;

    /**
     * 最高价格（可选）
     */
    private BigDecimal priceMax;

    /**
     * 最低评分（可选，1.0-5.0）
     */
    @Min(value = 1, message = "最低评分不能小于1")
    @Max(value = 5, message = "最低评分不能大于5")
    private Double minRating;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码不能小于1")
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Builder.Default
    private Integer pageSize = 20;
}
