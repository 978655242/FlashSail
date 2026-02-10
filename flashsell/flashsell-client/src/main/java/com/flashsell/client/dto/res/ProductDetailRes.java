package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品详情响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRes {

    /**
     * 产品ID
     */
    private Long id;

    /**
     * 产品标题
     */
    private String title;

    /**
     * 产品图片URL
     */
    private String image;

    /**
     * 当前价格（美元）
     */
    private BigDecimal currentPrice;

    /**
     * 价格历史
     */
    private List<PricePointRes> priceHistory;

    /**
     * BSR排名
     */
    private Integer bsrRank;

    /**
     * 评论数量
     */
    private Integer reviewCount;

    /**
     * 评分（1.0-5.0）
     */
    private Double rating;

    /**
     * 竞争评分（0.00-1.00）
     */
    private BigDecimal competitionScore;

    /**
     * AI推荐理由
     */
    private String aiRecommendation;

    /**
     * 所属品类
     */
    private CategoryRes category;

    /**
     * 数据最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 数据新鲜度状态：FRESH（新鲜）、STALE（过期/来自缓存）、UNKNOWN（未知）
     * 用于标注数据时效性，帮助用户了解数据的可靠程度
     * Requirements: 15.5, 15.7
     */
    private String dataFreshness;

    /**
     * 数据新鲜度提示信息
     * 当数据来自缓存时，提供友好的提示信息
     */
    private String freshnessMessage;
}
