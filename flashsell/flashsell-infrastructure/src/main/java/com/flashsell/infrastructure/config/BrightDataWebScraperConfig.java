package com.flashsell.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Bright Data Web Scraper API 配置
 * 配置 Web Scraper API 的基本参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "brightdata.webscraper")
public class BrightDataWebScraperConfig {

    /**
     * API Base URL
     * Bright Data Web Scraper API 端点
     */
    private String baseUrl = "https://api.brightdata.com";

    /**
     * API Key
     * 从 Bright Data 控制面板获取
     */
    private String apiKey = "";

    /**
     * Zone 名称
     * 在 Bright Data 控制面板中创建的 Zone 名称
     */
    private String zoneName = "";

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        return enabled
                && apiKey != null
                && !apiKey.isEmpty()
                && zoneName != null
                && !zoneName.isEmpty()
                && baseUrl != null
                && !baseUrl.isEmpty();
    }
}
