package com.flashsell.app.service;

import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.service.AiDomainService;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爆品分析应用服务
 * 负责分析产品的爆品潜力
 * 
 * Requirements: 11.2
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HotProductAnalysisService {

    private final AiDomainService aiDomainService;

    /**
     * 分析单个产品的爆品潜力
     *
     * @param product 产品实体
     * @return 爆品评分结果
     */
    public HotProductScore analyzeProduct(Product product) {
        if (product == null) {
            log.warn("产品为空，无法分析爆品潜力");
            return HotProductScore.failure(null, "产品不能为空");
        }

        log.info("开始分析产品爆品潜力: productId={}, title={}", product.getId(), product.getTitle());

        try {
            // 调用 AI 领域服务分析爆品潜力
            HotProductScore score = aiDomainService.analyzeHotProductPotential(product);
            
            if (score.isSuccess()) {
                log.info("产品爆品潜力分析完成: productId={}, score={}", 
                        product.getId(), score.getHotScore());
            } else {
                log.warn("产品爆品潜力分析失败: productId={}, reason={}", 
                        product.getId(), score.getRecommendation());
            }
            
            return score;
        } catch (Exception e) {
            log.error("分析产品爆品潜力异常: productId={}, error={}", product.getId(), e.getMessage(), e);
            return HotProductScore.failure(product.getId(), "分析异常: " + e.getMessage());
        }
    }

    /**
     * 批量分析产品的爆品潜力
     *
     * @param products 产品列表
     * @return 爆品评分列表
     */
    public List<HotProductScore> analyzeProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            log.warn("产品列表为空，无法批量分析");
            return new ArrayList<>();
        }

        log.info("开始批量分析产品爆品潜力: count={}", products.size());

        List<HotProductScore> scores = new ArrayList<>();
        for (Product product : products) {
            HotProductScore score = analyzeProduct(product);
            if (score.isSuccess()) {
                scores.add(score);
            }
        }

        log.info("批量分析完成: total={}, success={}", products.size(), scores.size());
        return scores;
    }

    /**
     * 分析并排序产品，返回 Top N 爆品
     *
     * @param products 产品列表
     * @param categoryId 品类ID
     * @param recommendDate 推荐日期
     * @param topN 返回数量
     * @return Top N 爆品评分列表（已排序和设置排名）
     */
    public List<HotProductScore> analyzeAndRankTopN(List<Product> products, 
                                                      Long categoryId, 
                                                      LocalDate recommendDate, 
                                                      int topN) {
        if (products == null || products.isEmpty()) {
            log.warn("产品列表为空，无法分析排名");
            return new ArrayList<>();
        }

        log.info("开始分析并排名产品: categoryId={}, count={}, topN={}", 
                categoryId, products.size(), topN);

        // 批量分析产品
        List<HotProductScore> scores = analyzeProducts(products);

        // 按评分降序排序
        List<HotProductScore> sortedScores = scores.stream()
                .sorted(Comparator.comparing(HotProductScore::getHotScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        // 设置排名、品类ID和推荐日期
        for (int i = 0; i < sortedScores.size(); i++) {
            HotProductScore score = sortedScores.get(i);
            score.setRankInCategory(i + 1);
            score.setCategoryId(categoryId);
            score.setRecommendDate(recommendDate);
        }

        log.info("分析并排名完成: categoryId={}, topN={}, actual={}", 
                categoryId, topN, sortedScores.size());

        return sortedScores;
    }

    /**
     * 检查产品是否符合爆品基本条件
     * 基本条件：有BSR排名、有评论、有评分
     *
     * @param product 产品实体
     * @return 是否符合条件
     */
    public boolean meetsHotProductCriteria(Product product) {
        if (product == null) {
            return false;
        }

        // 必须有 BSR 排名
        if (product.getBsrRank() == null || product.getBsrRank() <= 0) {
            return false;
        }

        // 必须有评论
        if (product.getReviewCount() == null || product.getReviewCount() <= 0) {
            return false;
        }

        // 必须有评分
        if (product.getRating() == null || product.getRating() <= 0) {
            return false;
        }

        // 评分不能太低（至少3.0）
        if (product.getRating() < 3.0) {
            return false;
        }

        return true;
    }

    /**
     * 过滤符合爆品条件的产品
     *
     * @param products 产品列表
     * @return 符合条件的产品列表
     */
    public List<Product> filterQualifiedProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        return products.stream()
                .filter(this::meetsHotProductCriteria)
                .collect(Collectors.toList());
    }

    /**
     * 计算爆品评分的统计信息
     *
     * @param scores 爆品评分列表
     * @return 统计信息字符串
     */
    public String calculateStatistics(List<HotProductScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return "无数据";
        }

        double avgScore = scores.stream()
                .map(HotProductScore::getHotScore)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        BigDecimal maxScore = scores.stream()
                .map(HotProductScore::getHotScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minScore = scores.stream()
                .map(HotProductScore::getHotScore)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        long highPotentialCount = scores.stream()
                .filter(HotProductScore::isHighPotential)
                .count();

        return String.format("总数: %d, 平均分: %.2f, 最高分: %s, 最低分: %s, 高潜力: %d",
                scores.size(), avgScore, maxScore, minScore, highPotentialCount);
    }
}
