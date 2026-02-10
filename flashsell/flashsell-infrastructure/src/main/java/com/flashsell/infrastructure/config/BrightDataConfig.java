package com.flashsell.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Bright Data MCP 配置类
 * 配置 API Token、超时、重试策略等
 * 
 * Requirements: 15.1, 15.10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "brightdata.mcp")  // 改用不同的前缀
@EnableConfigurationProperties
public class BrightDataConfig {

    /**
     * Bright Data API Token
     */
    private String apiToken;

    /**
     * Bright Data MCP 基础 URL
     */
    private String baseUrl = "https://mcp.brightdata.com";

    /**
     * 运行模式：rapid(免费) 或 pro(付费)
     */
    private String mode = "pro";

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeout = 10000;

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 速率限制配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 成本监控配置
     */
    private CostMonitorConfig costMonitor = new CostMonitorConfig();

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 重试延迟（毫秒）
         */
        private long delay = 1000;

        /**
         * 重试延迟倍数（用于指数退避）
         */
        private double multiplier = 2.0;

        /**
         * 最大重试延迟（毫秒）
         */
        private long maxDelay = 10000;
    }

    /**
     * 速率限制配置
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 每分钟最大请求数
         */
        private int requestsPerMinute = 100;

        /**
         * 是否启用速率限制
         */
        private boolean enabled = true;
    }

    /**
     * 成本监控配置
     */
    @Data
    public static class CostMonitorConfig {
        /**
         * 每日请求警告阈值
         */
        private int dailyWarningThreshold = 5000;

        /**
         * 每月请求警告阈值
         */
        private int monthlyWarningThreshold = 100000;

        /**
         * 是否启用成本监控
         */
        private boolean enabled = true;
    }

    /**
     * 创建 WebClient Bean 用于调用 Bright Data API（异步/响应式）
     * 配置了连接超时、读取超时、写入超时
     */
    @Bean
    public WebClient brightDataWebClient() {
        // 配置 Netty HttpClient 超时
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB 最大响应体大小
                .build();
    }

    /**
     * 创建 RestTemplate Bean 用于调用 Bright Data API（同步）
     * 作为 WebClient 的备选方案，用于需要同步调用的场景
     */
    @Bean
    public RestTemplate brightDataRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(baseUrl)
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .build();
    }

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        return apiToken != null && !apiToken.isEmpty() 
                && !apiToken.equals("your-api-token")
                && baseUrl != null && !baseUrl.isEmpty();
    }

    /**
     * 检查是否为 Pro 模式
     */
    public boolean isProMode() {
        return "pro".equalsIgnoreCase(mode);
    }

    /**
     * 检查是否为 Rapid 模式（免费模式）
     */
    public boolean isRapidMode() {
        return "rapid".equalsIgnoreCase(mode);
    }
}
