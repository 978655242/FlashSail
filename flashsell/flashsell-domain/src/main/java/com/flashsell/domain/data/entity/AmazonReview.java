package com.flashsell.domain.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Amazon 商品评论数据实体
 * 从 Bright Data MCP 获取的 Amazon 商品评论数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmazonReview {

    /**
     * 评论 ID
     */
    private String reviewId;

    /**
     * 商品 ASIN
     */
    private String asin;

    /**
     * 评论者名称
     */
    private String reviewerName;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 评论标题
     */
    private String title;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论日期
     */
    private LocalDate reviewDate;

    /**
     * 是否为已验证购买
     */
    private Boolean verifiedPurchase;

    /**
     * 有帮助的投票数
     */
    private Integer helpfulVotes;

    /**
     * 数据获取时间
     */
    private LocalDateTime fetchedAt;

    // ==================== 领域行为 ====================

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return reviewId != null && !reviewId.isEmpty()
                && asin != null && !asin.isEmpty()
                && rating != null && rating >= 1 && rating <= 5;
    }

    /**
     * 检查是否为正面评价（评分 >= 4）
     */
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    /**
     * 检查是否为负面评价（评分 <= 2）
     */
    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    /**
     * 检查是否为中性评价（评分 = 3）
     */
    public boolean isNeutral() {
        return rating != null && rating == 3;
    }

    /**
     * 检查是否为有帮助的评论（有帮助投票数 > 0）
     */
    public boolean isHelpful() {
        return helpfulVotes != null && helpfulVotes > 0;
    }
}
