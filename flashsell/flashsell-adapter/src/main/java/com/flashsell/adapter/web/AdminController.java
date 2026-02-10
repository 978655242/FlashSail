package com.flashsell.adapter.web;

import com.flashsell.adapter.scheduler.HotProductScheduler;
import com.flashsell.client.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 * 用于测试和管理功能
 * 
 * 注意: 生产环境应该添加权限控制
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final HotProductScheduler hotProductScheduler;

    /**
     * 手动触发爆品分析定时任务
     * POST /api/admin/hot-products/trigger
     *
     * @return 执行结果
     */
    @PostMapping("/hot-products/trigger")
    public ApiResponse<String> triggerHotProductAnalysis() {
        log.info("收到手动触发爆品分析任务请求");
        
        try {
            // 异步执行，避免请求超时
            new Thread(() -> {
                try {
                    hotProductScheduler.triggerManually();
                } catch (Exception e) {
                    log.error("手动触发爆品分析任务失败", e);
                }
            }).start();
            
            return ApiResponse.success("爆品分析任务已触发，请查看日志了解执行进度");
        } catch (Exception e) {
            log.error("触发爆品分析任务失败", e);
            return ApiResponse.error(500, "触发任务失败: " + e.getMessage());
        }
    }

    /**
     * 分析指定品类的爆品
     * POST /api/admin/hot-products/analyze-category
     *
     * @param categoryId 品类ID
     * @return 执行结果
     */
    @PostMapping("/hot-products/analyze-category")
    public ApiResponse<String> analyzeSingleCategory(@RequestParam Long categoryId) {
        log.info("收到分析单个品类请求: categoryId={}", categoryId);
        
        try {
            // 异步执行
            new Thread(() -> {
                try {
                    hotProductScheduler.analyzeSingleCategory(categoryId);
                } catch (Exception e) {
                    log.error("分析单个品类失败: categoryId={}", categoryId, e);
                }
            }).start();
            
            return ApiResponse.success("品类分析任务已触发: categoryId=" + categoryId);
        } catch (Exception e) {
            log.error("触发品类分析任务失败: categoryId={}", categoryId, e);
            return ApiResponse.error(500, "触发任务失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * GET /api/admin/health
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }
}
