package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 搜索响应 DTO
 * 
 * Requirements: 2.1, 2.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRes {

    /**
     * 产品列表
     */
    private List<ProductItemRes> products;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * AI 搜索摘要
     */
    private String aiSummary;

    /**
     * 数据新鲜度状态：FRESH（新鲜）、STALE（过期/来自缓存）、UNKNOWN（未知）
     */
    private String dataFreshness;

    /**
     * 数据新鲜度提示信息
     */
    private String freshnessMessage;
}
