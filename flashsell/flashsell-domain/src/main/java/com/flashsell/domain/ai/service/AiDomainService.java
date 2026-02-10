package com.flashsell.domain.ai.service;

import com.flashsell.domain.ai.entity.AiSearchResult;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.gateway.AiGateway;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI 领域服务
 * 处理 AI 相关的核心业务逻辑
 * 
 * Requirements: 2.1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiDomainService {

    private final AiGateway aiGateway;
    private final CategoryGateway categoryGateway;

    /**
     * 分析用户搜索查询
     * 使用 AI 解析用户的自然语言查询
     *
     * @param query 用户的自然语言查询
     * @return AI 分析结果
     */
    public AiSearchResult analyzeSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return AiSearchResult.failure("搜索查询不能为空");
        }

        // 获取所有可用品类名称
        List<String> categoryNames = getCategoryNames();

        try {
            AiSearchResult result = aiGateway.analyzeSearchQuery(query.trim(), categoryNames);
            
            // 如果 AI 返回了品类名称，映射到品类ID
            if (result.isSuccess() && result.getCategoryNames() != null) {
                List<Long> categoryIds = mapCategoryNamesToIds(result.getCategoryNames());
                result.setCategoryIds(categoryIds);
            }
            
            result.setOriginalQuery(query);
            return result;
        } catch (Exception e) {
            log.error("AI 分析搜索查询失败: query={}, error={}", query, e.getMessage());
            return AiSearchResult.failure("AI 分析失败: " + e.getMessage());
        }
    }

    /**
     * 分析产品的爆品潜力
     *
     * @param product 产品实体
     * @return 爆品评分结果
     */
    public HotProductScore analyzeHotProductPotential(Product product) {
        if (product == null) {
            return HotProductScore.failure(null, "产品不能为空");
        }

        // 获取品类名称
        String categoryName = getCategoryName(product.getCategoryId());

        try {
            return aiGateway.analyzeHotProductPotential(product, categoryName);
        } catch (Exception e) {
            log.error("AI 分析爆品潜力失败: productId={}, error={}", product.getId(), e.getMessage());
            return HotProductScore.failure(product.getId(), "AI 分析失败: " + e.getMessage());
        }
    }

    /**
     * 生成产品推荐理由
     *
     * @param product 产品实体
     * @return 推荐理由文本
     */
    public String generateProductRecommendation(Product product) {
        if (product == null) {
            return null;
        }

        try {
            return aiGateway.generateProductRecommendation(product);
        } catch (Exception e) {
            log.error("AI 生成推荐理由失败: productId={}, error={}", product.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 检查 AI 服务是否可用
     *
     * @return 是否可用
     */
    public boolean isAiServiceAvailable() {
        return aiGateway.isAvailable();
    }

    /**
     * 获取所有品类名称
     */
    private List<String> getCategoryNames() {
        List<Category> categories = categoryGateway.findAll();
        return categories.stream()
                .map(Category::getName)
                .toList();
    }

    /**
     * 获取品类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "未知品类";
        }
        return categoryGateway.findById(categoryId)
                .map(Category::getName)
                .orElse("未知品类");
    }

    /**
     * 将品类名称映射到品类ID
     */
    private List<Long> mapCategoryNamesToIds(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> allCategories = categoryGateway.findAll();
        List<Long> categoryIds = new ArrayList<>();

        for (String name : categoryNames) {
            String normalizedName = name.toLowerCase().trim();
            for (Category category : allCategories) {
                String catName = category.getName().toLowerCase();
                // 支持模糊匹配
                if (catName.contains(normalizedName) || normalizedName.contains(catName)) {
                    categoryIds.add(category.getId());
                    break;
                }
            }
        }

        return categoryIds;
    }
}
