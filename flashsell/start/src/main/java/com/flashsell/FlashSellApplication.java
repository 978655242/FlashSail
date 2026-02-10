package com.flashsell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FlashSell 应用启动类
 *
 * AI驱动的跨境电商爆品选品工具
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FlashSellApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashSellApplication.class, args);
    }
}
