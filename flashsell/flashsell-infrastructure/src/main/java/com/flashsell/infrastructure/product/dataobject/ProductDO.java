package com.flashsell.infrastructure.product.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品数据对象
 * 对应数据库 products 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("products")
public class ProductDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Amazon标准识别号，唯一
     */
    private String asin;

    /**
     * 产品标题
     */
    private String title;

    /**
     * 产品图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 当前价格（美元）
     */
    @TableField("current_price")
    private BigDecimal currentPrice;

    /**
     * BSR排名（Best Sellers Rank）
     */
    @TableField("bsr_rank")
    private Integer bsrRank;

    /**
     * 评论数量
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 评分（1.0-5.0）
     */
    private Double rating;

    /**
     * 所属品类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 竞争评分（0.00-1.00）
     */
    @TableField("competition_score")
    private BigDecimal competitionScore;

    /**
     * AI推荐理由
     */
    @TableField("ai_recommendation")
    private String aiRecommendation;

    /**
     * 数据最后更新时间
     */
    @TableField("last_updated")
    private LocalDateTime lastUpdated;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
