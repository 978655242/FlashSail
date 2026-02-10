package com.flashsell.domain.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 爆品评分领域实体
 * 包含AI分析的爆品潜力评分和推荐理由
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductScore {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 爆品评分（0-100）
     */
    private BigDecimal hotScore;

    /**
     * 品类内排名
     */
    private Integer rankInCategory;

    /**
     * 推荐日期
     */
    private LocalDate recommendDate;

    /**
     * 推荐理由列表
     */
    private List<String> reasons;

    /**
     * AI推荐详情
     */
    private String recommendation;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 领域行为 ====================

    /**
     * 检查是否为Top 20爆品（排名<=20）
     *
     * @return 是否为Top 20
     */
    public boolean isTop20() {
        return rankInCategory != null && rankInCategory <= 20;
    }

    /**
     * 检查是否为高潜力爆品（评分>=80）
     *
     * @return 是否为高潜力爆品
     */
    public boolean isHighPotential() {
        return hotScore != null && hotScore.compareTo(new BigDecimal("80")) >= 0;
    }

    /**
     * 检查评分是否有效（0-100之间）
     *
     * @return 是否有效
     */
    public boolean isScoreValid() {
        return hotScore != null 
                && hotScore.compareTo(BigDecimal.ZERO) >= 0 
                && hotScore.compareTo(new BigDecimal("100")) <= 0;
    }

    /**
     * 检查是否为今日推荐
     *
     * @return 是否为今日推荐
     */
    public boolean isTodayRecommendation() {
        return recommendDate != null && recommendDate.equals(LocalDate.now());
    }

    /**
     * 检查推荐是否过期（超过7天）
     *
     * @return 是否过期
     */
    public boolean isExpired() {
        if (recommendDate == null) {
            return true;
        }
        return recommendDate.isBefore(LocalDate.now().minusDays(7));
    }

    /**
     * 获取上榜天数（从推荐日期到今天）
     *
     * @return 上榜天数
     */
    public long getDaysOnList() {
        if (recommendDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(recommendDate, LocalDate.now());
    }

    /**
     * 创建失败的爆品评分结果
     *
     * @param productId 产品ID
     * @param errorMessage 错误信息
     * @return 失败的爆品评分
     */
    public static HotProductScore failure(Long productId, String errorMessage) {
        return HotProductScore.builder()
                .productId(productId)
                .hotScore(BigDecimal.ZERO)
                .reasons(new ArrayList<>())
                .recommendation(errorMessage)
                .success(false)
                .build();
    }

    /**
     * 检查是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }
}
