package com.flashsell.adapter.scheduler;

import com.flashsell.app.service.HotProductAnalysisService;
import com.flashsell.app.service.HotProductAppService;
import com.flashsell.app.service.ProductDataService;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 爆品推荐定时任务
 * 每日凌晨 2:00 执行，分析所有品类的爆品
 * 
 * Requirements: 11.1, 11.2, 11.6, 11.7, 15.1
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HotProductScheduler {

    private final CategoryGateway categoryGateway;
    private final ProductDataService productDataService;
    private final HotProductAnalysisService hotProductAnalysisService;
    private final HotProductAppService hotProductAppService;

    /**
     * 每日凌晨 2:00 执行爆品分析任务
     * 遍历所有 45 个品类，通过 Bright Data 获取热销商品，AI 分析并保存 Top 20 爆品
     */
    @Scheduled(cron = "${flashsell.scheduler.hot-product-cron:0 0 2 * * ?}")
    public void analyzeHotProducts() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("========== 开始执行爆品分析定时任务 ==========");
        log.info("任务开始时间: {}", startTime);

        try {
            // 获取所有 45 个品类
            List<Category> categories = categoryGateway.findAll();
            log.info("获取到品类数量: {}", categories.size());

            if (categories.isEmpty()) {
                log.warn("品类列表为空，任务终止");
                return;
            }

            LocalDate today = LocalDate.now();
            int successCount = 0;
            int failureCount = 0;

            // 遍历每个品类进行分析
            for (Category category : categories) {
                try {
                    log.info("开始分析品类: id={}, name={}", category.getId(), category.getName());
                    analyzeCategoryHotProducts(category, today);
                    successCount++;
                    log.info("品类分析完成: id={}, name={}", category.getId(), category.getName());
                } catch (Exception e) {
                    failureCount++;
                    log.error("品类分析失败: id={}, name={}, error={}", 
                            category.getId(), category.getName(), e.getMessage(), e);
                    // 继续处理下一个品类，不中断整个任务
                }
            }

            // 清理过期数据（超过 7 天）
            try {
                cleanupExpiredData();
            } catch (Exception e) {
                log.error("清理过期数据失败: {}", e.getMessage(), e);
            }

            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();

            log.info("========== 爆品分析定时任务完成 ==========");
            log.info("任务结束时间: {}", endTime);
            log.info("任务耗时: {} 秒", durationSeconds);
            log.info("成功品类数: {}, 失败品类数: {}", successCount, failureCount);

            // 如果失败数量过多，发送告警
            if (failureCount > categories.size() / 2) {
                sendAlert(String.format("爆品分析任务失败率过高: 成功=%d, 失败=%d", successCount, failureCount));
            }

        } catch (Exception e) {
            log.error("爆品分析定时任务执行失败", e);
            sendAlert("爆品分析定时任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 分析单个品类的爆品
     *
     * @param category 品类
     * @param recommendDate 推荐日期
     */
    private void analyzeCategoryHotProducts(Category category, LocalDate recommendDate) {
        log.info("开始获取品类热销商品: categoryId={}, categoryName={}", 
                category.getId(), category.getName());

        // 1. 通过 ProductDataService 获取品类下的热销商品（集成 Bright Data）
        // 这里使用品类名称作为关键词搜索
        List<Product> products = productDataService.searchProducts(category.getName(), category.getId());
        
        if (products.isEmpty()) {
            log.warn("品类下没有找到商品: categoryId={}, categoryName={}", 
                    category.getId(), category.getName());
            return;
        }

        log.info("获取到商品数量: categoryId={}, count={}", category.getId(), products.size());

        // 2. 过滤符合爆品条件的产品
        List<Product> qualifiedProducts = hotProductAnalysisService.filterQualifiedProducts(products);
        log.info("符合爆品条件的商品数量: categoryId={}, count={}", 
                category.getId(), qualifiedProducts.size());

        if (qualifiedProducts.isEmpty()) {
            log.warn("品类下没有符合条件的商品: categoryId={}, categoryName={}", 
                    category.getId(), category.getName());
            return;
        }

        // 3. AI 分析并排名，获取 Top 20
        List<HotProductScore> topScores = hotProductAnalysisService.analyzeAndRankTopN(
                qualifiedProducts, 
                category.getId(), 
                recommendDate, 
                20  // Top 20
        );

        if (topScores.isEmpty()) {
            log.warn("品类分析未产生有效结果: categoryId={}, categoryName={}", 
                    category.getId(), category.getName());
            return;
        }

        log.info("分析完成，获得 Top 爆品: categoryId={}, count={}", 
                category.getId(), topScores.size());

        // 4. 保存爆品推荐结果
        hotProductAppService.saveHotProducts(topScores, category.getId(), recommendDate);

        // 5. 输出统计信息
        String statistics = hotProductAnalysisService.calculateStatistics(topScores);
        log.info("品类爆品统计: categoryId={}, {}", category.getId(), statistics);
    }

    /**
     * 清理过期的爆品数据（超过 7 天）
     */
    private void cleanupExpiredData() {
        log.info("开始清理过期爆品数据");
        
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        int deletedCount = hotProductAppService.deleteExpiredHotProducts(sevenDaysAgo);
        
        log.info("清理过期爆品数据完成: 删除记录数={}", deletedCount);
    }

    /**
     * 发送告警通知
     *
     * @param message 告警信息
     */
    private void sendAlert(String message) {
        // TODO: 实现告警通知（邮件、短信、钉钉等）
        log.error("【告警】{}", message);
        
        // 这里可以集成告警服务，例如：
        // - 发送邮件
        // - 发送钉钉/企业微信通知
        // - 记录到告警日志表
        // - 触发监控系统告警
    }

    /**
     * 手动触发爆品分析任务（用于测试）
     * 可以通过管理接口调用
     */
    public void triggerManually() {
        log.info("手动触发爆品分析任务");
        analyzeHotProducts();
    }

    /**
     * 分析指定品类的爆品（用于测试或补偿）
     *
     * @param categoryId 品类ID
     */
    public void analyzeSingleCategory(Long categoryId) {
        log.info("手动分析单个品类: categoryId={}", categoryId);
        
        try {
            Category category = categoryGateway.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + categoryId));
            
            LocalDate today = LocalDate.now();
            analyzeCategoryHotProducts(category, today);
            
            log.info("单个品类分析完成: categoryId={}", categoryId);
        } catch (Exception e) {
            log.error("单个品类分析失败: categoryId={}, error={}", categoryId, e.getMessage(), e);
            throw e;
        }
    }
}
