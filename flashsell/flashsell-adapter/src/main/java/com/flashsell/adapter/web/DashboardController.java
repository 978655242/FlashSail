package com.flashsell.adapter.web;

import com.flashsell.app.service.DashboardAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.res.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 * 
 * Requirements: 13.1, 13.2, 13.3, 13.4, 13.5
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardAppService dashboardAppService;

    /**
     * 获取仪表盘数据概览
     * 
     * GET /api/dashboard/overview
     * 
     * Requirements: 13.1, 13.7
     */
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewRes> getOverview(Authentication authentication) {
        log.info("获取仪表盘数据概览");

        try {
            DashboardOverviewRes overview;
            
            if (authentication != null && authentication.isAuthenticated()) {
                // 已登录用户，返回包含用户数据的概览
                Long userId = Long.parseLong(authentication.getName());
                overview = dashboardAppService.getOverviewForUser(userId);
            } else {
                // 未登录用户，返回基础概览
                overview = dashboardAppService.getOverview();
            }

            return ApiResponse.success(overview);
        } catch (Exception e) {
            log.error("获取仪表盘数据概览失败", e);
            return ApiResponse.error("获取仪表盘数据失败");
        }
    }

    /**
     * 获取 AI 爆品推荐 Top 4
     * 
     * GET /api/dashboard/hot-recommendations
     * 
     * Requirements: 13.2
     */
    @GetMapping("/hot-recommendations")
    public ApiResponse<HotRecommendationsRes> getHotRecommendations() {
        log.info("获取 AI 爆品推荐 Top 4");

        try {
            HotRecommendationsRes recommendations = dashboardAppService.getHotRecommendations();
            return ApiResponse.success(recommendations);
        } catch (Exception e) {
            log.error("获取爆品推荐失败", e);
            return ApiResponse.error("获取爆品推荐失败");
        }
    }

    /**
     * 获取用户最近活动
     * 
     * GET /api/dashboard/recent-activity
     * 
     * Requirements: 13.3, 14.3, 14.4
     */
    @GetMapping("/recent-activity")
    public ApiResponse<RecentActivityRes> getRecentActivity(Authentication authentication) {
        log.info("获取用户最近活动");

        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error(401, "请先登录");
        }

        try {
            Long userId = Long.parseLong(authentication.getName());
            RecentActivityRes activity = dashboardAppService.getRecentActivity(userId);
            return ApiResponse.success(activity);
        } catch (Exception e) {
            log.error("获取最近活动失败", e);
            return ApiResponse.error("获取最近活动失败");
        }
    }

    /**
     * 获取热门品类趋势
     * 
     * GET /api/dashboard/trending-categories
     * 
     * Requirements: 13.4
     */
    @GetMapping("/trending-categories")
    public ApiResponse<TrendingCategoriesRes> getTrendingCategories() {
        log.info("获取热门品类趋势");

        try {
            TrendingCategoriesRes categories = dashboardAppService.getTrendingCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("获取热门品类趋势失败", e);
            return ApiResponse.error("获取热门品类趋势失败");
        }
    }

    /**
     * 获取热门搜索关键词
     * 
     * GET /api/dashboard/hot-keywords
     * 
     * Requirements: 13.5
     */
    @GetMapping("/hot-keywords")
    public ApiResponse<HotKeywordsRes> getHotKeywords() {
        log.info("获取热门搜索关键词");

        try {
            HotKeywordsRes keywords = dashboardAppService.getHotKeywords();
            return ApiResponse.success(keywords);
        } catch (Exception e) {
            log.error("获取热门关键词失败", e);
            return ApiResponse.error("获取热门关键词失败");
        }
    }
}
