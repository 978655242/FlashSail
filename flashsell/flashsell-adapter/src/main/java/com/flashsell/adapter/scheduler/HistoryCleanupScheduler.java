package com.flashsell.adapter.scheduler;

import com.flashsell.app.service.HistoryAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 历史记录清理定时任务
 * 每日凌晨 3:00 执行，清理 30 天前的历史记录
 * 
 * Requirements: 14.5
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HistoryCleanupScheduler {
    
    private final HistoryAppService historyAppService;
    
    /**
     * 每日凌晨 3:00 执行历史记录清理任务
     * 清理 30 天前的搜索历史和浏览历史
     */
    @Scheduled(cron = "${flashsell.scheduler.history-cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredHistory() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("========== 开始执行历史记录清理定时任务 ==========");
        log.info("任务开始时间: {}", startTime);
        
        try {
            // 清理 30 天前的历史记录
            int deletedCount = historyAppService.cleanExpiredHistory();
            
            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            
            log.info("========== 历史记录清理定时任务完成 ==========");
            log.info("任务结束时间: {}", endTime);
            log.info("任务耗时: {} 秒", durationSeconds);
            log.info("清理记录数: {}", deletedCount);
            
        } catch (Exception e) {
            log.error("历史记录清理定时任务执行失败", e);
            sendAlert("历史记录清理定时任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发历史记录清理任务（用于测试）
     * 可以通过管理接口调用
     */
    public void triggerManually() {
        log.info("手动触发历史记录清理任务");
        cleanupExpiredHistory();
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
}
