package com.flashsell.app.service;

import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 缓存预热服务
 * 在应用启动时预热关键数据到缓存
 *
 * Requirements: 10.4, 10.5
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.warmup.enabled", havingValue = "true", matchIfMissing = true)
public class CacheWarmupService {

    private final CategoryGateway categoryGateway;
    private final ProductGateway productGateway;
    private final HotProductAppService hotProductAppService;
    private final DashboardAppService dashboardAppService;

    /**
     * 应用启动完成后执行缓存预热
     * 使用异步方式避免阻塞应用启动
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheOnStartup() {
        log.info("开始缓存预热...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 预热品类列表
            warmupCategories();

            // 2. 预热今日爆品数据
            warmupTodayHotProducts();

            // 3. 预热仪表盘概览数据
            warmupDashboardData();

            long duration = System.currentTimeMillis() - startTime;
            log.info("缓存预热完成，耗时: {}ms", duration);
        } catch (Exception e) {
            log.error("缓存预热过程中发生错误", e);
            // 预热失败不影响应用启动
        }
    }

    /**
     * 预热品类列表
     * 品类数据在应用中非常常用，预热可以提升首次访问性能
     */
    private void warmupCategories() {
        log.info("预热品类列表...");
        try {
            // 调用 findAllGroups 会触发缓存逻辑
            categoryGateway.findAllGroups();
            log.info("品类列表预热完成");
        } catch (Exception e) {
            log.warn("品类列表预热失败: {}", e.getMessage());
        }
    }

    /**
     * 预热今日爆品数据
     * 爆品数据在首页展示，预热可以提升首页加载速度
     */
    private void warmupTodayHotProducts() {
        log.info("预热今日爆品数据...");
        try {
            // 获取今日爆品数据，触发缓存
            hotProductAppService.getTodayTop4HotProducts();
            log.info("今日爆品数据预热完成");
        } catch (Exception e) {
            log.warn("今日爆品数据预热失败: {}", e.getMessage());
        }
    }

    /**
     * 预热仪表盘数据
     * 仪表盘概览数据预热可以提升首页加载速度
     */
    private void warmupDashboardData() {
        log.info("预热仪表盘数据...");
        try {
            // 获取仪表盘概览数据，触发缓存
            dashboardAppService.getOverview();
            log.info("仪表盘数据预热完成");
        } catch (Exception e) {
            log.warn("仪表盘数据预热失败: {}", e.getMessage());
        }
    }

    /**
     * 手动触发缓存预热（供管理接口调用）
     */
    public void manualWarmup() {
        log.info("手动触发缓存预热...");
        warmupCacheOnStartup();
    }
}
