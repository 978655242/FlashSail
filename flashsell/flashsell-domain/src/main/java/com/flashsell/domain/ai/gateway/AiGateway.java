package com.flashsell.domain.ai.gateway;

import com.flashsell.domain.ai.entity.AiSearchResult;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.product.entity.Product;

import java.util.List;

/**
 * AI 网关接口
 * 定义 AI 服务的抽象接口，由 infrastructure 层实现
 * 
 * Requirements: 2.1, 2.6, 2.7
 */
public interface AiGateway {

    /**
     * 分析用户搜索查询
     * 使用 AI 解析用户的自然语言查询，提取关键词、品类和价格范围
     *
     * @param query 用户的自然语言查询
     * @param availableCategories 可用的品类名称列表
     * @return AI 分析结果
     */
    AiSearchResult analyzeSearchQuery(String query, List<String> availableCategories);

    /**
     * 分析产品的爆品潜力
     * 使用 AI 评估产品的爆品潜力并给出评分
     *
     * @param product 产品实体
     * @param categoryName 品类名称
     * @return 爆品评分结果
     */
    HotProductScore analyzeHotProductPotential(Product product, String categoryName);

    /**
     * 生成产品推荐理由
     * 使用 AI 为产品生成推荐理由
     *
     * @param product 产品实体
     * @return 推荐理由文本
     */
    String generateProductRecommendation(Product product);

    /**
     * 从 Markdown 内容解析商品信息
     * 用于解析 1688 爬取的 Markdown 数据
     *
     * @param markdown Markdown 内容
     * @return 解析后的商品信息 JSON 字符串
     */
    String parseProductsFromMarkdown(String markdown);

    /**
     * 检查 AI 服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
