package com.flashsell.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * RestTemplate 配置类
 * 用于 HTTP 请求
 * 优先处理字节数组响应，支持 gzip 压缩数据
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate Bean
     * 配置 ByteArrayHttpMessageConverter 在第一位
     * 这样可以正确处理二进制数据（包括 gzip 压缩数据）
     * 避免 StringHttpMessageConverter 拦截二进制响应
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 秒连接超时
        factory.setReadTimeout(60000);    // 60 秒读取超时

        RestTemplate restTemplate = new RestTemplate(factory);

        // 重要：ByteArrayHttpMessageConverter 必须在第一位
        // 这样才能正确处理所有响应（包括 gzip 压缩和纯文本）
        restTemplate.setMessageConverters(List.of(
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter(),
            new AllEncompassingFormHttpMessageConverter()
        ));

        return restTemplate;
    }
}
