package com.flashsell.infrastructure.common;

/**
 * 缓存 Key 常量和 TTL 配置
 */
public final class CacheConstants {

    private CacheConstants() {
        // 私有构造函数，防止实例化
    }

    // =====================================================
    // 缓存 Key 前缀
    // =====================================================

    /**
     * 用户会话缓存前缀
     */
    public static final String SESSION_PREFIX = "session:";

    /**
     * AI 搜索结果缓存前缀
     */
    public static final String SEARCH_PREFIX = "search:";

    /**
     * 产品详情缓存前缀
     */
    public static final String PRODUCT_PREFIX = "product:";

    /**
     * 爆品推荐缓存前缀
     */
    public static final String HOT_PRODUCTS_PREFIX = "hot_products:";

    /**
     * 品类列表缓存 Key
     */
    public static final String CATEGORIES_ALL = "categories:all";

    /**
     * 用户订阅状态缓存前缀
     */
    public static final String SUBSCRIPTION_PREFIX = "subscription:";

    /**
     * 仪表盘数据概览缓存 Key
     */
    public static final String DASHBOARD_OVERVIEW = "dashboard:overview";

    /**
     * 热门关键词缓存 Key
     */
    public static final String DASHBOARD_HOT_KEYWORDS = "dashboard:hot_keywords";

    /**
     * 热门品类趋势缓存 Key
     */
    public static final String DASHBOARD_TRENDING_CATEGORIES = "dashboard:trending_categories";

    /**
     * 用户最近活动缓存前缀
     */
    public static final String USER_RECENT_ACTIVITY_PREFIX = "user:recent_activity:";

    /**
     * 验证码缓存前缀
     */
    public static final String VERIFY_CODE_PREFIX = "verify_code:";

    // =====================================================
    // Bright Data 缓存 Key 前缀
    // =====================================================

    /**
     * Bright Data Amazon 搜索结果缓存前缀
     */
    public static final String BRIGHTDATA_AMAZON_SEARCH_PREFIX = "brightdata:amazon:search:";

    /**
     * Bright Data Amazon 商品详情缓存前缀
     */
    public static final String BRIGHTDATA_AMAZON_PRODUCT_PREFIX = "brightdata:amazon:product:";

    /**
     * Bright Data Amazon 商品评论缓存前缀
     */
    public static final String BRIGHTDATA_AMAZON_REVIEWS_PREFIX = "brightdata:amazon:reviews:";

    /**
     * Bright Data 1688 搜索结果缓存前缀
     */
    public static final String BRIGHTDATA_1688_SEARCH_PREFIX = "brightdata:1688:search:";

    /**
     * Bright Data 请求计数缓存前缀
     */
    public static final String BRIGHTDATA_REQUEST_COUNT_PREFIX = "brightdata:request_count:";

    // =====================================================
    // 缓存 TTL（单位：秒）
    // =====================================================

    /**
     * 用户会话 TTL：7天
     */
    public static final long SESSION_TTL = 7 * 24 * 60 * 60;

    /**
     * AI 搜索结果 TTL：15分钟
     */
    public static final long SEARCH_TTL = 15 * 60;

    /**
     * 产品详情 TTL：1小时
     */
    public static final long PRODUCT_TTL = 60 * 60;

    /**
     * 爆品推荐 TTL：24小时
     */
    public static final long HOT_PRODUCTS_TTL = 24 * 60 * 60;

    /**
     * 品类列表 TTL：1小时
     */
    public static final long CATEGORIES_TTL = 60 * 60;

    /**
     * 用户订阅状态 TTL：1小时
     */
    public static final long SUBSCRIPTION_TTL = 60 * 60;

    /**
     * 仪表盘数据概览 TTL：5分钟
     */
    public static final long DASHBOARD_OVERVIEW_TTL = 5 * 60;

    /**
     * 热门关键词 TTL：30分钟
     */
    public static final long DASHBOARD_HOT_KEYWORDS_TTL = 30 * 60;

    /**
     * 热门品类趋势 TTL：1小时
     */
    public static final long DASHBOARD_TRENDING_CATEGORIES_TTL = 60 * 60;

    /**
     * 用户最近活动 TTL：5分钟
     */
    public static final long USER_RECENT_ACTIVITY_TTL = 5 * 60;

    /**
     * 验证码 TTL：5分钟
     */
    public static final long VERIFY_CODE_TTL = 5 * 60;

    /**
     * Bright Data Amazon 搜索结果 TTL：15分钟
     */
    public static final long BRIGHTDATA_AMAZON_SEARCH_TTL = 15 * 60;

    /**
     * Bright Data Amazon 商品详情 TTL：1小时
     */
    public static final long BRIGHTDATA_AMAZON_PRODUCT_TTL = 60 * 60;

    /**
     * Bright Data Amazon 商品评论 TTL：1小时
     */
    public static final long BRIGHTDATA_AMAZON_REVIEWS_TTL = 60 * 60;

    /**
     * Bright Data 1688 搜索结果 TTL：15分钟
     */
    public static final long BRIGHTDATA_1688_SEARCH_TTL = 15 * 60;

    // =====================================================
    // 工具方法
    // =====================================================

    /**
     * 生成用户会话缓存 Key
     */
    public static String sessionKey(Long userId) {
        return SESSION_PREFIX + userId;
    }

    /**
     * 生成搜索结果缓存 Key
     */
    public static String searchKey(String queryHash) {
        return SEARCH_PREFIX + queryHash;
    }

    /**
     * 生成产品详情缓存 Key
     */
    public static String productKey(Long productId) {
        return PRODUCT_PREFIX + productId;
    }

    /**
     * 生成爆品推荐缓存 Key
     */
    public static String hotProductsKey(String date, Long categoryGroupId) {
        return HOT_PRODUCTS_PREFIX + date + ":" + categoryGroupId;
    }

    /**
     * 生成用户订阅状态缓存 Key
     */
    public static String subscriptionKey(Long userId) {
        return SUBSCRIPTION_PREFIX + userId;
    }

    /**
     * 生成用户最近活动缓存 Key
     */
    public static String userRecentActivityKey(Long userId) {
        return USER_RECENT_ACTIVITY_PREFIX + userId;
    }

    /**
     * 生成验证码缓存 Key
     */
    public static String verifyCodeKey(String phone) {
        return VERIFY_CODE_PREFIX + phone;
    }

    /**
     * 生成 Bright Data Amazon 搜索结果缓存 Key
     */
    public static String brightDataAmazonSearchKey(String keyword) {
        return BRIGHTDATA_AMAZON_SEARCH_PREFIX + keyword.hashCode();
    }

    /**
     * 生成 Bright Data Amazon 商品详情缓存 Key
     */
    public static String brightDataAmazonProductKey(String asin) {
        return BRIGHTDATA_AMAZON_PRODUCT_PREFIX + asin;
    }

    /**
     * 生成 Bright Data Amazon 商品评论缓存 Key
     */
    public static String brightDataAmazonReviewsKey(String asin) {
        return BRIGHTDATA_AMAZON_REVIEWS_PREFIX + asin;
    }

    /**
     * 生成 Bright Data 1688 搜索结果缓存 Key
     */
    public static String brightData1688SearchKey(String keyword) {
        return BRIGHTDATA_1688_SEARCH_PREFIX + keyword.hashCode();
    }

    /**
     * 生成 Bright Data 每日请求计数缓存 Key
     */
    public static String brightDataDailyRequestCountKey(String date) {
        return BRIGHTDATA_REQUEST_COUNT_PREFIX + "daily:" + date;
    }

    /**
     * 生成 Bright Data 每月请求计数缓存 Key
     */
    public static String brightDataMonthlyRequestCountKey(String month) {
        return BRIGHTDATA_REQUEST_COUNT_PREFIX + "monthly:" + month;
    }
}
