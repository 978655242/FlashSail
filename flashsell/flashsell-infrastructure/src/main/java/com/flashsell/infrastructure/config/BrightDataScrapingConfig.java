package com.flashsell.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Bright Data Scraping Browser API 配置
 * 配置 Playwright 连接到 Bright Data 代理
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "brightdata")
public class BrightDataScrapingConfig {

    /**
     * 运行模式: playwright 或 mcp
     */
    private String mode = "playwright";

    /**
     * 代理配置
     */
    private ProxyConfig proxy = new ProxyConfig();

    /**
     * 浏览器配置
     */
    private BrowserConfig browser = new BrowserConfig();

    /**
     * 超时配置（毫秒）
     */
    private int timeout = 60000;

    /**
     * 连接超时（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时（毫秒）
     */
    private int readTimeout = 60000;

    /**
     * 写入超时（毫秒）
     */
    private int writeTimeout = 10000;

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 成本监控
     */
    private CostMonitorConfig costMonitor = new CostMonitorConfig();

    /**
     * 速率限制
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Data
    public static class ProxyConfig {
        /**
         * 是否启用代理
         */
        private boolean enabled = true;

        /**
         * 代理主机
         * Bright Data Zone 代理地址（通常是 gw-brightdata.net）
         */
        private String host = "gw-brightdata.net";

        /**
         * 代理端口
         * 根据Zone类型不同：Super Proxy(24000), Residential(24001), Datacenter(24002)
         */
        private int port = 24000;

        /**
         * Zone 用户名
         * 在 Bright Data 控制面板的 Zone 页面中获取
         */
        private String username = "your-zone-username";

        /**
         * Zone 密码
         * 在 Bright Data 控制面板的 Zone 页面中获取
         */
        private String password = "your-zone-password";
    }

    @Data
    public static class BrowserConfig {
        /**
         * 是否使用无头模式
         */
        private boolean headless = true;

        /**
         * 浏览器超时（毫秒）
         */
        private int timeout = 60000;

        /**
         * 重试配置
         */
        private RetryConfig retry = new RetryConfig();
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long delay = 2000;
        private double multiplier = 2.0;
        private long maxDelay = 10000;
    }

    @Data
    public static class CostMonitorConfig {
        private int dailyWarningThreshold = 5000;
        private int monthlyWarningThreshold = 100000;
        private boolean enabled = true;
    }

    @Data
    public static class RateLimitConfig {
        private int requestsPerMinute = 60;
        private boolean enabled = true;
    }
}
