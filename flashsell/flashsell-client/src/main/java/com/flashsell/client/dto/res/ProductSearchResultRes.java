package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品搜索结果响应 DTO（带数据时效性标注）
 * 用于返回搜索结果及其数据新鲜度状态
 * 
 * Requirements: 15.5, 15.7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResultRes {

    /**
     * 搜索结果产品列表
     */
    private List<ProductDetailRes> products;

    /**
     * 结果总数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * AI 搜索摘要
     */
    private String aiSummary;

    /**
     * 数据新鲜度状态：FRESH（新鲜）、STALE（过期/来自缓存）、EMPTY（无数据）
     */
    private String dataFreshness;

    /**
     * 数据获取时间
     */
    private LocalDateTime fetchedAt;

    /**
     * 数据新鲜度提示信息
     */
    private String freshnessMessage;
}
