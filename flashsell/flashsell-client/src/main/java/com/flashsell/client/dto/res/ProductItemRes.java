package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 产品列表项响应 DTO
 * 用于搜索结果列表展示
 * 
 * Requirements: 2.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemRes {

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
    private BigDecimal price;

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
     * 所属品类ID
     */
    private Long categoryId;

    /**
     * 所属品类名称
     */
    private String categoryName;
}
