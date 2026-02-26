package com.flashsell.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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
     * 配置 ByteArrayHttpMessageConverter 支持所有媒体类型
     * 配置 MappingJackson2HttpMessageConverter 支持 JSON
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 秒连接超时
        factory.setReadTimeout(60000);    // 60 秒读取超时

        RestTemplate restTemplate = new RestTemplate(factory);

        // 配置 ByteArrayHttpMessageConverter 支持所有媒体类型
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(Arrays.asList(
            MediaType.ALL
        ));

        // 配置 JSON 转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        // 设置消息转换器列表
        restTemplate.setMessageConverters(List.of(
            byteArrayConverter,
            jsonConverter
        ));

        return restTemplate;
    }
}
