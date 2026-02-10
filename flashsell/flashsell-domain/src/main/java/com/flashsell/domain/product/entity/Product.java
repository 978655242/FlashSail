package com.flashsell.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品领域实体
 * 包含产品的核心属性和领域行为
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * 产品ID
     */
    private Long id;

    /**
     * Amazon标准识别号
     */
    private String asin;

    /**
     * 产品标题
     */
    private String title;

    /**
     * 产品图片URL
     */
    private String imageUrl;

    /**
     * 当前价格（美元）
     */
    private BigDecimal currentPrice;

    /**
     * BSR排名（Best Sellers Rank）
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
     * 竞争评分（0.00-1.00）
     */
    private BigDecimal competitionScore;

    /**
     * AI推荐理由
     */
    private String aiRecommendation;

    /**
     * 数据最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 领域行为 ====================

    /**
     * 检查产品数据是否过期（超过1小时）
     *
     * @return 是否过期
     */
    public boolean isDataStale() {
        if (lastUpdated == null) {
            return true;
        }
        return lastUpdated.isBefore(LocalDateTime.now().minusHours(1));
    }

    /**
     * 检查产品是否有有效的BSR排名
     *
     * @return 是否有BSR排名
     */
    public boolean hasBsrRank() {
        return bsrRank != null && bsrRank > 0;
    }

    /**
     * 检查产品是否有评论
     *
     * @return 是否有评论
     */
    public boolean hasReviews() {
        return reviewCount != null && reviewCount > 0;
    }

    /**
     * 检查产品是否有评分
     *
     * @return 是否有评分
     */
    public boolean hasRating() {
        return rating != null && rating > 0;
    }

    /**
     * 检查产品是否为高评分产品（评分>=4.0）
     *
     * @return 是否为高评分产品
     */
    public boolean isHighRated() {
        return rating != null && rating >= 4.0;
    }

    /**
     * 检查产品是否为热销产品（BSR排名<=1000）
     *
     * @return 是否为热销产品
     */
    public boolean isHotSelling() {
        return bsrRank != null && bsrRank <= 1000;
    }

    /**
     * 检查产品是否为高竞争产品（竞争评分>=0.7）
     *
     * @return 是否为高竞争产品
     */
    public boolean isHighCompetition() {
        return competitionScore != null && competitionScore.compareTo(new BigDecimal("0.7")) >= 0;
    }

    /**
     * 更新产品数据
     *
     * @param price 新价格
     * @param bsrRank 新BSR排名
     * @param reviewCount 新评论数
     * @param rating 新评分
     */
    public void updateData(BigDecimal price, Integer bsrRank, Integer reviewCount, Double rating) {
        this.currentPrice = price;
        this.bsrRank = bsrRank;
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 设置AI推荐信息
     *
     * @param recommendation AI推荐理由
     * @param competitionScore 竞争评分
     */
    public void setAiAnalysis(String recommendation, BigDecimal competitionScore) {
        this.aiRecommendation = recommendation;
        this.competitionScore = competitionScore;
    }

    /**
     * 检查产品详情是否完整
     * 产品详情必须包含：标题、图片、当前价格、BSR排名、评论统计和竞争评分
     *
     * @return 是否完整
     */
    public boolean isDetailComplete() {
        return title != null && !title.isEmpty()
                && imageUrl != null && !imageUrl.isEmpty()
                && currentPrice != null
                && bsrRank != null
                && reviewCount != null
                && competitionScore != null;
    }
}
