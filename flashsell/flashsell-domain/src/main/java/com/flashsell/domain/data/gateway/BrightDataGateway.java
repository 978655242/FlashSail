package com.flashsell.domain.data.gateway;

import com.flashsell.domain.data.entity.AlibabaProduct;
import com.flashsell.domain.data.entity.AmazonProduct;
import com.flashsell.domain.data.entity.AmazonReview;

import java.util.List;

/**
 * Bright Data MCP 网关接口
 * 定义实时电商数据获取的抽象接口，由 infrastructure 层实现
 */
public interface BrightDataGateway {

    /**
     * 搜索 Amazon 商品
     *
     * @param keyword 搜索关键词
     * @param domain  Amazon 域名（如 amazon.com）
     * @return 商品列表
     */
    List<AmazonProduct> searchAmazonProducts(String keyword, String domain);

    /**
     * 获取 Amazon 商品详情
     *
     * @param productUrl 商品 URL（包含 /dp/）
     * @return 商品详情
     */
    AmazonProduct getAmazonProductDetail(String productUrl);

    /**
     * 根据 ASIN 获取 Amazon 商品详情
     *
     * @param asin Amazon 标准识别号
     * @return 商品详情
     */
    AmazonProduct getAmazonProductByAsin(String asin);

    /**
     * 获取 Amazon 商品评论
     *
     * @param productUrl 商品 URL
     * @return 评论列表
     */
    List<AmazonReview> getAmazonProductReviews(String productUrl);

    /**
     * 根据 ASIN 获取 Amazon 商品评论
     *
     * @param asin Amazon 标准识别号
     * @return 评论列表
     */
    List<AmazonReview> getAmazonProductReviewsByAsin(String asin);

    /**
     * 爬取 1688 商品数据
     *
     * @param keyword 搜索关键词
     * @return 商品列表（需 AI 解析）
     */
    List<AlibabaProduct> scrape1688Products(String keyword);

    /**
     * 批量获取商品数据
     *
     * @param urls 商品 URL 列表（最多 10 个）
     * @return 商品详情列表
     * @throws IllegalArgumentException 如果 URL 数量超过 10 个
     */
    List<AmazonProduct> batchGetProducts(List<String> urls);

    /**
     * 检查 API 是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
