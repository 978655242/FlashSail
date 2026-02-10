package com.flashsell.infrastructure.data.monitor;

import com.flashsell.infrastructure.common.CacheConstants;
import com.flashsell.infrastructure.config.BrightDataConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Bright Data 成本监控组件
 * 监控 API 请求数量，在达到阈值时发出告警
 * 
 * 功能：
 * 1. 每日/每月请求计数
 * 2. 请求阈值告警
 * 3. 定时重置计数器（通过 Redis TTL 自动过期）
 * 
 * Requirements: 15.9
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BrightDataCostMonitor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BrightDataConfig config;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 每日计数器 TTL：2天（确保跨天统计时仍可访问昨日数据）
     */
    private static final long DAILY_COUNTER_TTL_DAYS = 2;

    /**
     * 每月计数器 TTL：35天（确保跨月统计时仍可访问上月数据）
     */
    private static final long MONTHLY_COUNTER_TTL_DAYS = 35;

    /**
     * 记录 API 请求
     * 增加每日和每月计数，并检查是否达到阈值
     *
     * @param tool 工具名称
     */
    public void recordRequest(String tool) {
        if (!config.getCostMonitor().isEnabled()) {
            return;
        }

        String today = LocalDate.now().format(DATE_FORMATTER);
        String month = LocalDate.now().format(MONTH_FORMATTER);

        String dailyKey = CacheConstants.brightDataDailyRequestCountKey(today);
        String monthlyKey = CacheConstants.brightDataMonthlyRequestCountKey(month);

        // 增加计数
        Long dailyCount = redisTemplate.opsForValue().increment(dailyKey);
        Long monthlyCount = redisTemplate.opsForValue().increment(monthlyKey);

        // 设置 TTL（仅在首次创建时设置）
        setTtlIfNotExists(dailyKey, DAILY_COUNTER_TTL_DAYS, TimeUnit.DAYS);
        setTtlIfNotExists(monthlyKey, MONTHLY_COUNTER_TTL_DAYS, TimeUnit.DAYS);

        // 检查是否达到警告阈值
        checkThresholds(dailyCount, monthlyCount, tool);
    }

    /**
     * 设置 TTL（仅当 key 没有 TTL 时设置）
     */
    private void setTtlIfNotExists(String key, long timeout, TimeUnit unit) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            redisTemplate.expire(key, timeout, unit);
        }
    }

    /**
     * 获取今日请求数
     *
     * @return 今日请求数
     */
    public long getDailyRequestCount() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String dailyKey = CacheConstants.brightDataDailyRequestCountKey(today);
        Object count = redisTemplate.opsForValue().get(dailyKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 获取本月请求数
     *
     * @return 本月请求数
     */
    public long getMonthlyRequestCount() {
        String month = LocalDate.now().format(MONTH_FORMATTER);
        String monthlyKey = CacheConstants.brightDataMonthlyRequestCountKey(month);
        Object count = redisTemplate.opsForValue().get(monthlyKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 获取指定日期的请求数
     *
     * @param date 日期
     * @return 请求数
     */
    public long getRequestCountByDate(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        String dailyKey = CacheConstants.brightDataDailyRequestCountKey(dateStr);
        Object count = redisTemplate.opsForValue().get(dailyKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 获取指定月份的请求数
     *
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 请求数
     */
    public long getRequestCountByMonth(String yearMonth) {
        String monthlyKey = CacheConstants.brightDataMonthlyRequestCountKey(yearMonth);
        Object count = redisTemplate.opsForValue().get(monthlyKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 检查是否超过每日警告阈值
     *
     * @return 是否超过
     */
    public boolean isDailyThresholdExceeded() {
        return getDailyRequestCount() >= config.getCostMonitor().getDailyWarningThreshold();
    }

    /**
     * 检查是否超过每月警告阈值
     *
     * @return 是否超过
     */
    public boolean isMonthlyThresholdExceeded() {
        return getMonthlyRequestCount() >= config.getCostMonitor().getMonthlyWarningThreshold();
    }

    /**
     * 获取每日警告阈值
     *
     * @return 每日警告阈值
     */
    public int getDailyWarningThreshold() {
        return config.getCostMonitor().getDailyWarningThreshold();
    }

    /**
     * 获取每月警告阈值
     *
     * @return 每月警告阈值
     */
    public int getMonthlyWarningThreshold() {
        return config.getCostMonitor().getMonthlyWarningThreshold();
    }

    /**
     * 获取每日使用率（百分比）
     *
     * @return 使用率
     */
    public double getDailyUsagePercentage() {
        long count = getDailyRequestCount();
        int threshold = getDailyWarningThreshold();
        return threshold > 0 ? (count * 100.0 / threshold) : 0;
    }

    /**
     * 获取每月使用率（百分比）
     *
     * @return 使用率
     */
    public double getMonthlyUsagePercentage() {
        long count = getMonthlyRequestCount();
        int threshold = getMonthlyWarningThreshold();
        return threshold > 0 ? (count * 100.0 / threshold) : 0;
    }

    /**
     * 手动重置今日计数器
     * 注意：正常情况下计数器通过 Redis TTL 自动过期，此方法仅用于特殊情况
     */
    public void resetDailyCounter() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String dailyKey = CacheConstants.brightDataDailyRequestCountKey(today);
        redisTemplate.delete(dailyKey);
        log.info("【Bright Data 成本监控】手动重置今日计数器: date={}", today);
    }

    /**
     * 手动重置本月计数器
     * 注意：正常情况下计数器通过 Redis TTL 自动过期，此方法仅用于特殊情况
     */
    public void resetMonthlyCounter() {
        String month = LocalDate.now().format(MONTH_FORMATTER);
        String monthlyKey = CacheConstants.brightDataMonthlyRequestCountKey(month);
        redisTemplate.delete(monthlyKey);
        log.info("【Bright Data 成本监控】手动重置本月计数器: month={}", month);
    }

    /**
     * 获取成本监控统计摘要
     *
     * @return 统计摘要字符串
     */
    public String getStatsSummary() {
        long dailyCount = getDailyRequestCount();
        long monthlyCount = getMonthlyRequestCount();
        int dailyThreshold = getDailyWarningThreshold();
        int monthlyThreshold = getMonthlyWarningThreshold();

        return String.format(
                "Bright Data 成本统计 - 今日: %d/%d (%.1f%%), 本月: %d/%d (%.1f%%)",
                dailyCount, dailyThreshold, getDailyUsagePercentage(),
                monthlyCount, monthlyThreshold, getMonthlyUsagePercentage()
        );
    }

    /**
     * 检查阈值并发出告警
     */
    private void checkThresholds(Long dailyCount, Long monthlyCount, String tool) {
        int dailyThreshold = config.getCostMonitor().getDailyWarningThreshold();
        int monthlyThreshold = config.getCostMonitor().getMonthlyWarningThreshold();

        // 检查每日阈值 - 达到阈值时告警
        if (dailyCount != null && dailyCount == dailyThreshold) {
            log.warn("【Bright Data 成本告警】每日请求数达到警告阈值: {} (阈值: {}), 最后请求工具: {}",
                    dailyCount, dailyThreshold, tool);
            triggerAlert("DAILY_THRESHOLD_REACHED", dailyCount, dailyThreshold, tool);
        }

        // 检查每月阈值 - 达到阈值时告警
        if (monthlyCount != null && monthlyCount == monthlyThreshold) {
            log.warn("【Bright Data 成本告警】每月请求数达到警告阈值: {} (阈值: {}), 最后请求工具: {}",
                    monthlyCount, monthlyThreshold, tool);
            triggerAlert("MONTHLY_THRESHOLD_REACHED", monthlyCount, monthlyThreshold, tool);
        }

        // 超过阈值 10% 时再次告警
        if (dailyCount != null && dailyCount == (long) (dailyThreshold * 1.1)) {
            log.error("【Bright Data 成本告警】每日请求数超过警告阈值 10%: {} (阈值: {})", dailyCount, dailyThreshold);
            triggerAlert("DAILY_THRESHOLD_EXCEEDED_10PCT", dailyCount, dailyThreshold, tool);
        }

        if (monthlyCount != null && monthlyCount == (long) (monthlyThreshold * 1.1)) {
            log.error("【Bright Data 成本告警】每月请求数超过警告阈值 10%: {} (阈值: {})", monthlyCount, monthlyThreshold);
            triggerAlert("MONTHLY_THRESHOLD_EXCEEDED_10PCT", monthlyCount, monthlyThreshold, tool);
        }

        // 超过阈值 50% 时发出严重告警
        if (dailyCount != null && dailyCount == (long) (dailyThreshold * 1.5)) {
            log.error("【Bright Data 成本严重告警】每日请求数超过警告阈值 50%: {} (阈值: {})", dailyCount, dailyThreshold);
            triggerAlert("DAILY_THRESHOLD_EXCEEDED_50PCT", dailyCount, dailyThreshold, tool);
        }

        if (monthlyCount != null && monthlyCount == (long) (monthlyThreshold * 1.5)) {
            log.error("【Bright Data 成本严重告警】每月请求数超过警告阈值 50%: {} (阈值: {})", monthlyCount, monthlyThreshold);
            triggerAlert("MONTHLY_THRESHOLD_EXCEEDED_50PCT", monthlyCount, monthlyThreshold, tool);
        }
    }

    /**
     * 触发告警通知
     * 可扩展为发送邮件、短信、钉钉等通知
     *
     * @param alertType 告警类型
     * @param currentCount 当前计数
     * @param threshold 阈值
     * @param tool 触发告警的工具
     */
    private void triggerAlert(String alertType, long currentCount, int threshold, String tool) {
        // 记录告警到 Redis（用于后续查询告警历史）
        String alertKey = "brightdata:alert:" + LocalDate.now().format(DATE_FORMATTER) + ":" + alertType;
        String alertMessage = String.format(
                "告警类型: %s, 当前计数: %d, 阈值: %d, 触发工具: %s, 时间: %s",
                alertType, currentCount, threshold, tool, java.time.LocalDateTime.now()
        );
        redisTemplate.opsForList().rightPush(alertKey, alertMessage);
        redisTemplate.expire(alertKey, 7, TimeUnit.DAYS);

        // TODO: 集成告警通知服务（邮件、短信、钉钉等）
        // notificationService.sendAlert(alertType, alertMessage);
    }

    /**
     * 每日凌晨记录昨日统计
     * 计数器通过 Redis TTL 自动过期，无需手动重置
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void logDailyStats() {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        long count = getRequestCountByDate(LocalDate.now().minusDays(1));
        int threshold = getDailyWarningThreshold();
        double usageRate = threshold > 0 ? (count * 100.0 / threshold) : 0;

        log.info("【Bright Data 统计】昨日({})请求总数: {}, 阈值: {}, 使用率: {:.1f}%",
                yesterday, count, threshold, usageRate);

        // 如果昨日使用率超过 80%，记录警告
        if (usageRate >= 80) {
            log.warn("【Bright Data 统计警告】昨日使用率较高: {:.1f}%，请关注成本控制", usageRate);
        }
    }

    /**
     * 每月1日凌晨记录上月统计
     * 计数器通过 Redis TTL 自动过期，无需手动重置
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void logMonthlyStats() {
        String lastMonth = LocalDate.now().minusMonths(1).format(MONTH_FORMATTER);
        long count = getRequestCountByMonth(lastMonth);
        int threshold = getMonthlyWarningThreshold();
        double usageRate = threshold > 0 ? (count * 100.0 / threshold) : 0;

        log.info("【Bright Data 统计】上月({})请求总数: {}, 阈值: {}, 使用率: {:.1f}%",
                lastMonth, count, threshold, usageRate);

        // 如果上月使用率超过 80%，记录警告
        if (usageRate >= 80) {
            log.warn("【Bright Data 统计警告】上月使用率较高: {:.1f}%，请关注成本控制", usageRate);
        }
    }

    /**
     * 每小时检查当前使用情况
     * 用于及时发现异常流量
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyUsageCheck() {
        if (!config.getCostMonitor().isEnabled()) {
            return;
        }

        double dailyUsage = getDailyUsagePercentage();
        double monthlyUsage = getMonthlyUsagePercentage();

        // 如果当前使用率超过 90%，发出警告
        if (dailyUsage >= 90) {
            log.warn("【Bright Data 每小时检查】今日使用率已达 {:.1f}%，接近阈值", dailyUsage);
        }

        if (monthlyUsage >= 90) {
            log.warn("【Bright Data 每小时检查】本月使用率已达 {:.1f}%，接近阈值", monthlyUsage);
        }

        // 记录当前状态（DEBUG 级别）
        log.debug("【Bright Data 每小时检查】{}", getStatsSummary());
    }
}
