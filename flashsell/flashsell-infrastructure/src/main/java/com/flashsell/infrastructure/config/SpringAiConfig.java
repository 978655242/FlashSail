package com.flashsell.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置类
 * 配置智谱 GLM-4 API 相关的 Bean
 * 
 * Requirements: 8.7
 * 
 * Note: 
 * - ObjectMapper is configured in JacksonConfig with Java 8 date/time support
 * - ChatClient.Builder is auto-configured by Spring AI
 */
@Configuration
public class SpringAiConfig {
    // All beans are auto-configured by Spring AI starter
}
