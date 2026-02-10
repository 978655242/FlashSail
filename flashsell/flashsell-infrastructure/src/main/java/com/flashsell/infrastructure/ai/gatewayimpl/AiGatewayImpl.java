package com.flashsell.infrastructure.ai.gatewayimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsell.domain.ai.entity.AiSearchResult;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.gateway.AiGateway;
import com.flashsell.domain.product.entity.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 网关实现
 * 使用 Spring AI + 智谱 GLM-4 实现 AI 功能
 * 
 * Requirements: 2.1, 2.6, 2.7, 8.7
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class AiGatewayImpl implements AiGateway {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * 搜索分析 Prompt 模板
     */
    private static final String SEARCH_ANALYSIS_PROMPT = """
        你是一个跨境电商选品专家。根据用户的搜索需求，分析并提取关键信息。
        
        用户搜索: %s
        可选品类: %s
        
        请分析用户需求，返回以下 JSON 格式（不要包含任何其他文字，只返回 JSON）：
        {
            "keywords": ["关键词1", "关键词2"],
            "categoryNames": ["匹配的品类名称"],
            "priceMin": 最低价格数字或null,
            "priceMax": 最高价格数字或null,
            "minRating": 最低评分数字或null,
            "summary": "搜索摘要，简要描述用户想要找什么产品"
        }
        
        注意：
        1. keywords 应该是英文关键词，用于在 Amazon 上搜索
        2. categoryNames 只能从可选品类中选择，如果没有匹配的品类则返回空数组
        3. 价格单位是美元
        4. 评分范围是 1.0-5.0
        5. summary 用中文描述
        """;

    /**
     * 爆品分析 Prompt 模板
     */
    private static final String HOT_PRODUCT_ANALYSIS_PROMPT = """
        分析以下产品数据，评估其爆品潜力（0-100分）：
        
        产品: %s
        价格: $%s
        BSR排名: %s
        评论数: %s
        评分: %s
        品类: %s
        
        请返回以下 JSON 格式（不要包含任何其他文字，只返回 JSON）：
        {
            "hotScore": 爆品评分数字(0-100),
            "reasons": ["原因1", "原因2", "原因3"],
            "recommendation": "推荐理由，用中文描述为什么这个产品有/没有爆品潜力"
        }
        
        评分标准：
        - BSR排名越低（越靠前）得分越高
        - 评论数越多得分越高
        - 评分越高得分越高
        - 价格在合理区间（$10-$50）得分较高
        """;

    /**
     * 产品推荐 Prompt 模板
     */
    private static final String PRODUCT_RECOMMENDATION_PROMPT = """
        为以下产品生成一段简短的推荐理由（50字以内，中文）：
        
        产品: %s
        价格: $%s
        BSR排名: %s
        评论数: %s
        评分: %s
        
        只返回推荐理由文本，不要包含任何其他内容。
        """;

    /**
     * Markdown 解析 Prompt 模板
     */
    private static final String MARKDOWN_PARSE_PROMPT = """
        从以下 Markdown 内容中提取商品信息，返回 JSON 数组格式：
        
        %s
        
        请返回以下 JSON 格式（不要包含任何其他文字，只返回 JSON 数组）：
        [
            {
                "offerId": "商品ID",
                "title": "商品标题",
                "price": 价格数字,
                "soldCount": 销量数字或null,
                "supplierName": "供应商名称或null"
            }
        ]
        
        如果无法提取任何商品信息，返回空数组 []
        """;

    @Override
    public AiSearchResult analyzeSearchQuery(String query, List<String> availableCategories) {
        log.info("AI 分析搜索查询: query={}", query);

        String categoriesStr = String.join(", ", availableCategories);
        String prompt = String.format(SEARCH_ANALYSIS_PROMPT, query, categoriesStr);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI 响应: {}", response);
            return parseSearchResult(response, query);
        } catch (Exception e) {
            log.error("AI 分析搜索查询失败: query={}, error={}", query, e.getMessage());
            // 返回基于原始查询的默认结果
            return createDefaultSearchResult(query);
        }
    }

    @Override
    public HotProductScore analyzeHotProductPotential(Product product, String categoryName) {
        log.info("AI 分析爆品潜力: productId={}", product.getId());

        String prompt = String.format(HOT_PRODUCT_ANALYSIS_PROMPT,
                product.getTitle(),
                product.getCurrentPrice(),
                product.getBsrRank() != null ? product.getBsrRank() : "未知",
                product.getReviewCount() != null ? product.getReviewCount() : 0,
                product.getRating() != null ? product.getRating() : "未知",
                categoryName);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI 响应: {}", response);
            return parseHotProductScore(response, product.getId());
        } catch (Exception e) {
            log.error("AI 分析爆品潜力失败: productId={}, error={}", product.getId(), e.getMessage());
            // 返回基于规则的默认评分
            return calculateDefaultHotScore(product);
        }
    }

    @Override
    public String generateProductRecommendation(Product product) {
        log.info("AI 生成产品推荐: productId={}", product.getId());

        String prompt = String.format(PRODUCT_RECOMMENDATION_PROMPT,
                product.getTitle(),
                product.getCurrentPrice(),
                product.getBsrRank() != null ? product.getBsrRank() : "未知",
                product.getReviewCount() != null ? product.getReviewCount() : 0,
                product.getRating() != null ? product.getRating() : "未知");

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return response.trim();
        } catch (Exception e) {
            log.error("AI 生成产品推荐失败: productId={}, error={}", product.getId(), e.getMessage());
            return generateDefaultRecommendation(product);
        }
    }

    @Override
    public String parseProductsFromMarkdown(String markdown) {
        log.info("AI 解析 Markdown 商品数据");

        // 限制 Markdown 长度，避免超出 token 限制
        String truncatedMarkdown = markdown.length() > 10000 
                ? markdown.substring(0, 10000) + "..." 
                : markdown;

        String prompt = String.format(MARKDOWN_PARSE_PROMPT, truncatedMarkdown);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return response.trim();
        } catch (Exception e) {
            log.error("AI 解析 Markdown 失败: error={}", e.getMessage());
            return "[]";
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user("ping")
                    .call()
                    .content();
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.warn("AI 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 解析 AI 搜索分析结果
     */
    private AiSearchResult parseSearchResult(String response, String originalQuery) {
        try {
            // 清理响应，提取 JSON
            String jsonStr = extractJson(response);
            JsonNode json = objectMapper.readTree(jsonStr);

            List<String> keywords = new ArrayList<>();
            if (json.has("keywords") && json.get("keywords").isArray()) {
                json.get("keywords").forEach(node -> keywords.add(node.asText()));
            }

            List<String> categoryNames = new ArrayList<>();
            if (json.has("categoryNames") && json.get("categoryNames").isArray()) {
                json.get("categoryNames").forEach(node -> categoryNames.add(node.asText()));
            }

            BigDecimal priceMin = null;
            if (json.has("priceMin") && !json.get("priceMin").isNull()) {
                priceMin = new BigDecimal(json.get("priceMin").asText());
            }

            BigDecimal priceMax = null;
            if (json.has("priceMax") && !json.get("priceMax").isNull()) {
                priceMax = new BigDecimal(json.get("priceMax").asText());
            }

            Double minRating = null;
            if (json.has("minRating") && !json.get("minRating").isNull()) {
                minRating = json.get("minRating").asDouble();
            }

            String summary = json.has("summary") ? json.get("summary").asText() : "";

            // 如果没有提取到关键词，使用原始查询
            if (keywords.isEmpty()) {
                keywords.add(originalQuery);
            }

            return AiSearchResult.builder()
                    .keywords(keywords)
                    .categoryNames(categoryNames)
                    .priceMin(priceMin)
                    .priceMax(priceMax)
                    .minRating(minRating)
                    .summary(summary)
                    .originalQuery(originalQuery)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("解析 AI 搜索结果失败: {}", e.getMessage());
            return createDefaultSearchResult(originalQuery);
        }
    }

    /**
     * 解析爆品评分结果
     */
    private HotProductScore parseHotProductScore(String response, Long productId) {
        try {
            String jsonStr = extractJson(response);
            JsonNode json = objectMapper.readTree(jsonStr);

            BigDecimal hotScore = json.has("hotScore") 
                    ? new BigDecimal(json.get("hotScore").asText()) 
                    : new BigDecimal("50.0");

            List<String> reasons = new ArrayList<>();
            if (json.has("reasons") && json.get("reasons").isArray()) {
                json.get("reasons").forEach(node -> reasons.add(node.asText()));
            }

            String recommendation = json.has("recommendation") 
                    ? json.get("recommendation").asText() 
                    : "";

            return HotProductScore.builder()
                    .productId(productId)
                    .hotScore(hotScore)
                    .reasons(reasons)
                    .recommendation(recommendation)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("解析爆品评分结果失败: {}", e.getMessage());
            return HotProductScore.failure(productId, "解析失败");
        }
    }

    /**
     * 从响应中提取 JSON 字符串
     */
    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        // 尝试找到 JSON 的开始和结束位置
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start == -1) {
            // 尝试数组格式
            start = response.indexOf('[');
            end = response.lastIndexOf(']');
        }

        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }

        return response.trim();
    }

    /**
     * 创建默认的搜索结果（当 AI 分析失败时）
     */
    private AiSearchResult createDefaultSearchResult(String query) {
        List<String> keywords = new ArrayList<>();
        keywords.add(query);

        return AiSearchResult.builder()
                .keywords(keywords)
                .categoryNames(new ArrayList<>())
                .summary("搜索: " + query)
                .originalQuery(query)
                .success(true)
                .build();
    }

    /**
     * 计算默认的爆品评分（基于规则）
     */
    private HotProductScore calculateDefaultHotScore(Product product) {
        double score = 50.0; // 基础分
        List<String> reasons = new ArrayList<>();

        // BSR 排名评分
        if (product.getBsrRank() != null) {
            if (product.getBsrRank() <= 100) {
                score += 30;
                reasons.add("BSR排名极高（Top 100）");
            } else if (product.getBsrRank() <= 1000) {
                score += 20;
                reasons.add("BSR排名较高（Top 1000）");
            } else if (product.getBsrRank() <= 10000) {
                score += 10;
                reasons.add("BSR排名良好（Top 10000）");
            }
        }

        // 评论数评分
        if (product.getReviewCount() != null) {
            if (product.getReviewCount() >= 1000) {
                score += 15;
                reasons.add("评论数量丰富");
            } else if (product.getReviewCount() >= 100) {
                score += 10;
                reasons.add("评论数量适中");
            }
        }

        // 评分评分
        if (product.getRating() != null) {
            if (product.getRating() >= 4.5) {
                score += 10;
                reasons.add("用户评分优秀");
            } else if (product.getRating() >= 4.0) {
                score += 5;
                reasons.add("用户评分良好");
            }
        }

        // 限制最高分为 100
        score = Math.min(score, 100.0);

        return HotProductScore.builder()
                .productId(product.getId())
                .hotScore(BigDecimal.valueOf(score))
                .reasons(reasons)
                .recommendation("基于产品数据的自动评估")
                .success(true)
                .build();
    }

    /**
     * 生成默认的产品推荐（当 AI 生成失败时）
     */
    private String generateDefaultRecommendation(Product product) {
        StringBuilder sb = new StringBuilder();

        if (product.getBsrRank() != null && product.getBsrRank() <= 1000) {
            sb.append("热销产品，");
        }

        if (product.getRating() != null && product.getRating() >= 4.5) {
            sb.append("用户好评，");
        }

        if (product.getReviewCount() != null && product.getReviewCount() >= 100) {
            sb.append("口碑验证，");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 移除最后的逗号
            sb.append("。");
        } else {
            sb.append("值得关注的产品。");
        }

        return sb.toString();
    }
}
