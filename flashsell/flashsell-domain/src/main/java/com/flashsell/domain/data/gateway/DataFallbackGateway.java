package com.flashsell.domain.data.gateway;

import com.flashsell.domain.data.entity.AlibabaProduct;
import com.flashsell.domain.data.entity.AmazonProduct;
import com.flashsell.domain.data.entity.AmazonReview;
import com.flashsell.domain.data.entity.DataFreshness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 数据降级网关接口
 * 提供带降级支持的数据获取能力
 * 当实时数据获取失败时，返回缓存数据并标注数据时效性
 * 
 * Requirements: 15.5, 15.7
 */
public interface DataFallbackGateway {

    /**
     * 搜索 Amazon 商品（带降级）
     *
     * @param keyword 搜索关键词
     * @param domain  Amazon 域名
     * @return 带时效性标注的结果
     */
    FallbackResult<List<AmazonProduct>> searchAmazonProductsWithFallback(String keyword, String domain);

    /**
     * 获取 Amazon 商品详情（带降级）
     *
     * @param asin Amazon 标准识别号
     * @return 带时效性标注的结果
     */
    FallbackResult<AmazonProduct> getAmazonProductWithFallback(String asin);

    /**
     * 获取 Amazon 商品评论（带降级）
     *
     * @param asin Amazon 标准识别号
     * @return 带时效性标注的结果
     */
    FallbackResult<List<AmazonReview>> getAmazonReviewsWithFallback(String asin);

    /**
     * 爬取 1688 商品（带降级）
     *
     * @param keyword 搜索关键词
     * @return 带时效性标注的结果
     */
    FallbackResult<List<AlibabaProduct>> scrape1688ProductsWithFallback(String keyword);

    /**
     * 批量获取商品（带降级）
     *
     * @param urls URL 列表
     * @return 带时效性标注的结果
     */
    FallbackResult<List<AmazonProduct>> batchGetProductsWithFallback(List<String> urls);

    /**
     * 降级结果包装类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FallbackResult<T> {
        /**
         * 数据
         */
        private T data;

        /**
         * 数据新鲜度信息
         */
        private DataFreshness freshness;

        /**
         * 创建新鲜数据结果
         */
        public static <T> FallbackResult<T> fresh(T data) {
            return FallbackResult.<T>builder()
                    .data(data)
                    .freshness(DataFreshness.fresh())
                    .build();
        }

        /**
         * 创建过期数据结果
         */
        public static <T> FallbackResult<T> stale(T data, LocalDateTime cachedAt) {
            return FallbackResult.<T>builder()
                    .data(data)
                    .freshness(DataFreshness.stale(cachedAt, "数据来自缓存，可能已过期"))
                    .build();
        }

        /**
         * 创建空数据结果
         */
        @SuppressWarnings("unchecked")
        public static <T> FallbackResult<T> empty() {
            return FallbackResult.<T>builder()
                    .data((T) Collections.emptyList())
                    .freshness(DataFreshness.empty())
                    .build();
        }

        /**
         * 是否为新鲜数据
         */
        public boolean isFresh() {
            return freshness != null && freshness.isFresh();
        }

        /**
         * 是否为过期数据
         */
        public boolean isStale() {
            return freshness != null && freshness.isStale();
        }

        /**
         * 是否为空数据
         */
        public boolean isEmpty() {
            return freshness != null && freshness.isEmpty();
        }

        /**
         * 是否有数据
         */
        public boolean hasData() {
            return data != null && (freshness == null || !freshness.isEmpty());
        }

        /**
         * 获取数据获取时间
         */
        public LocalDateTime getFetchedAt() {
            return freshness != null ? freshness.getFetchedAt() : null;
        }

        /**
         * 获取提示信息
         */
        public String getMessage() {
            return freshness != null ? freshness.getMessage() : null;
        }

        /**
         * 获取状态字符串
         */
        public String getStatusString() {
            return freshness != null ? freshness.getStatusString() : "UNKNOWN";
        }
    }
}
